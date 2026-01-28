package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class GZIPByteOutput implements ByteOutput {

    private static final int DEFAULT_BUF_SIZE = 512;
    private static final int SYNC_FLUSH_MIN_BUF_SIZE = 7;
    private static final int GZIP_MAGIC = 0x8b1f;
    private static final int TRAILER_SIZE = 8;
    private static final byte OS_UNKNOWN = (byte) 255;

    private final ByteOutput out;
    private final Deflater def;
    private final byte[] buf;
    private final boolean syncFlush;
    private final CRC32 crc;

    private boolean closed;

    public GZIPByteOutput(ByteOutput out) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, DEFAULT_BUF_SIZE, false);
    }

    public GZIPByteOutput(ByteOutput out, int size) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, size, false);
    }

    public GZIPByteOutput(ByteOutput out, boolean syncFlush) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, DEFAULT_BUF_SIZE, syncFlush);
    }

    public GZIPByteOutput(ByteOutput out, int size, boolean syncFlush) throws ScxOutputException, OutputAlreadyClosedException {
        this.out = out;
        this.def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.buf = new byte[size];
        this.syncFlush = syncFlush;
        writeHeader();
        this.crc = new CRC32();
        this.crc.reset();
        this.closed = false;
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        write(ByteChunk.of(new byte[]{b}));
    }

    @Override
    public void write(ByteChunk byteChunk) throws ScxOutputException, OutputAlreadyClosedException {
        var b = byteChunk.bytes;
        var off = byteChunk.start;
        var len = byteChunk.length;

        if (def.finished()) {
            throw new ScxOutputException("write beyond end of stream");
        }

        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        if (!def.finished()) {
            def.setInput(b, off, len);
            while (!def.needsInput()) {
                int alen = def.deflate(buf, 0, buf.length);
                if (alen > 0) {
                    out.write(ByteChunk.of(buf, 0, alen));
                }
            }
        }

        // crc 校验
        crc.update(byteChunk.bytes, byteChunk.start, byteChunk.length);
    }

    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        if (syncFlush && !def.finished()) {
            int len = 0;
            // For SYNC_FLUSH, the Deflater.deflate() expects the callers
            // to use a buffer whose length is greater than 6 to avoid
            // flush marker (5 bytes) being repeatedly output to the output buffer
            // every time it is invoked.
            final byte[] flushBuf = buf.length < SYNC_FLUSH_MIN_BUF_SIZE
                ? new byte[DEFAULT_BUF_SIZE]
                : buf;
            while ((len = def.deflate(flushBuf, 0, flushBuf.length, Deflater.SYNC_FLUSH)) > 0) {
                out.write(ByteChunk.of(flushBuf, 0, len));
                if (len < flushBuf.length) {
                    break;
                }
            }
        }
        out.flush();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        if (!closed) {
            closed = true;
            ScxOutputException finishException = null;
            try {
                finish();
            } catch (ScxOutputException ioe) {
                finishException = ioe;
                throw ioe;
            } finally {
                def.end();
                if (finishException == null) {
                    out.close();
                } else {
                    try {
                        out.close();
                    } catch (ScxOutputException ioe) {
                        if (finishException != ioe) {
                            ioe.addSuppressed(finishException);
                        }
                        throw ioe;
                    }
                }
            }
        }
    }

    public void finish() throws ScxOutputException, OutputAlreadyClosedException {
        if (def.finished()) {
            return;
        }

        try {
            def.finish();

            while (!def.finished()) {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                    // last deflater buffer. Fit trailer at the end
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(ByteChunk.of(buf, 0, len));
                    return;
                }
                if (len > 0) {
                    out.write(ByteChunk.of(buf, 0, len));
                }
            }

            // if we can't fit the trailer at the end of the last
            // deflater buffer, we write it separately
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer);
        } catch (ScxOutputException | OutputAlreadyClosedException e) {
            def.end();
            throw e;
        }
    }

    /// Writes GZIP member header.
    private void writeHeader() throws ScxOutputException, OutputAlreadyClosedException {
        out.write(new byte[]{
            (byte) GZIP_MAGIC,        // Magic number (short)
            (byte) (GZIP_MAGIC >> 8),  // Magic number (short)
            Deflater.DEFLATED,        // Compression method (CM)
            0,                        // Flags (FLG)
            0,                        // Modification time MTIME (int)
            0,                        // Modification time MTIME (int)
            0,                        // Modification time MTIME (int)
            0,                        // Modification time MTIME (int)
            0,                        // Extra flags (XFLG)
            OS_UNKNOWN                // Operating system (OS)
        });
    }

    /// Writes GZIP member trailer to a byte array, starting at a given offset.
    private void writeTrailer(byte[] buf, int offset) {
        // CRC-32 of uncompr. data
        int crc32 = (int) crc.getValue();
        buf[offset] = (byte) (crc32);
        buf[offset + 1] = (byte) (crc32 >>> 8);
        buf[offset + 2] = (byte) (crc32 >>> 16);
        buf[offset + 3] = (byte) (crc32 >>> 24);
        // RFC 1952: Size of the original (uncompressed) input data modulo 2^32
        int iSize = (int) def.getBytesRead();
        buf[offset + 4] = (byte) (iSize);
        buf[offset + 5] = (byte) (iSize >>> 8);
        buf[offset + 6] = (byte) (iSize >>> 16);
        buf[offset + 7] = (byte) (iSize >>> 24);
    }

}
