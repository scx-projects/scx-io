package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class GZIPByteOutput implements ByteOutput {

    static final int DEFAULT_BUF_SIZE = 512;
    private static final int SYNC_FLUSH_MIN_BUF_SIZE = 7;
    private static final int GZIP_MAGIC = 0x8b1f;
    private static final int TRAILER_SIZE = 8;
    private static final byte OS_UNKNOWN = (byte) 255;
    private final boolean syncFlush;
    protected ByteOutput out;
    protected Deflater def;
    protected byte[] buf;
    protected CRC32 crc = new CRC32();
    boolean usesDefaultDeflater = false;
    private boolean closed = false;

    public GZIPByteOutput(ByteOutput out, int size) throws IOException {
        this(out, size, false);
    }

    public GZIPByteOutput(ByteOutput out, int size, boolean syncFlush) throws IOException {
        this.out = out;
        this.def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.buf = new byte[size];
        this.syncFlush = syncFlush;
        usesDefaultDeflater = true;
        writeHeader();
        crc.reset();
    }

    public GZIPByteOutput(ByteOutput out) throws IOException {
        this(out, DEFAULT_BUF_SIZE, false);
    }

    public GZIPByteOutput(ByteOutput out, boolean syncFlush) throws IOException {
        this(out, DEFAULT_BUF_SIZE, syncFlush);
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        write(ByteChunk.of(new byte[]{b}));
    }

    public synchronized void write(ByteChunk byteChunk) throws ScxOutputException {
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
                deflate();
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
                if (usesDefaultDeflater) {
                    def.end();
                }
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

    protected void deflate() throws ScxOutputException {
        int len = def.deflate(buf, 0, buf.length);
        if (len > 0) {
            out.write(ByteChunk.of(buf, 0, len));
        }
    }

    public void finish() throws ScxOutputException {
        if (!def.finished()) {
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
            } catch (ScxOutputException e) {
                if (usesDefaultDeflater) {
                    def.end();
                }
                throw e;
            }
        }
    }

    /*
     * Writes GZIP member header.
     */
    private void writeHeader() throws IOException {
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

    /*
     * Writes GZIP member trailer to a byte array, starting at a given
     * offset.
     */
    private void writeTrailer(byte[] buf, int offset) {
        writeInt((int) crc.getValue(), buf, offset); // CRC-32 of uncompr. data
        // RFC 1952: Size of the original (uncompressed) input data modulo 2^32
        int iSize = (int) def.getBytesRead();
        writeInt(iSize, buf, offset + 4);
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) {
        buf[offset] = (byte) (s & 0xff);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);
    }

}
