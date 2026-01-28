package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

import java.util.zip.CRC32;
import java.util.zip.Deflater;

/// GZIPByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class GZIPByteOutput extends AbstractByteOutput {

    private static final int GZIP_MAGIC = 0x8b1f;

    private final ByteOutput out;
    private final Deflater def;
    private final byte[] buffer;
    private final boolean syncFlush;
    private final CRC32 crc;

    private boolean headerWritten;

    public GZIPByteOutput(ByteOutput out) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, 1024, false);
    }

    public GZIPByteOutput(ByteOutput out, int bufferLength) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, bufferLength, false);
    }

    public GZIPByteOutput(ByteOutput out, boolean syncFlush) throws ScxOutputException, OutputAlreadyClosedException {
        this(out, 1024, syncFlush);
    }

    public GZIPByteOutput(ByteOutput out, int bufferLength, boolean syncFlush) throws ScxOutputException, OutputAlreadyClosedException {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength must be greater than 0");
        }
        this.out = out;
        this.def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.buffer = new byte[bufferLength];
        this.syncFlush = syncFlush;
        this.crc = new CRC32();
        this.headerWritten = false;
        this.closed = false;
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        write(ByteChunk.of(new byte[]{b}));
    }

    @Override
    public void write(ByteChunk byteChunk) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        ensureHeader();

        ensureNotFinished();

        if (byteChunk.length == 0) {
            return;
        }

        // 设置输入
        def.setInput(byteChunk.bytes, byteChunk.start, byteChunk.length);

        // 如果能够写出就尝试写出.
        while (!def.needsInput()) {
            int len = def.deflate(buffer, 0, buffer.length, Deflater.NO_FLUSH);
            if (len > 0) {
                out.write(ByteChunk.of(buffer, 0, len));
            }
        }

        // crc 校验
        crc.update(byteChunk.bytes, byteChunk.start, byteChunk.length);
    }

    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        ensureHeader();

        if (syncFlush && !def.finished()) {
            while (true) {
                var len = def.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);
                if (len > 0) {
                    out.write(ByteChunk.of(buffer, 0, len));
                } else {
                    break;
                }
            }
        }

        out.flush();
    }

    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try (def) {

            ensureHeader();

            def.finish();

            while (!def.finished()) {
                int len = def.deflate(buffer, 0, buffer.length, Deflater.NO_FLUSH);
                if (len > 0) {
                    out.write(ByteChunk.of(buffer, 0, len));
                }
            }

            out.write(createTrailer());
            out.close();

        }

        closed = true;

    }

    /// Writes GZIP member header.
    private void ensureHeader() throws ScxOutputException, OutputAlreadyClosedException {
        if (headerWritten) {
            return;
        }
        out.write(createHeader());
        headerWritten = true;
    }

    private void ensureNotFinished() {
        if (def.finished()) {
            throw new ScxOutputException("write beyond end of stream");
        }
    }

    private byte[] createHeader() {
        return new byte[]{
            (byte) GZIP_MAGIC,         // Magic number (short)
            (byte) (GZIP_MAGIC >> 8),  // Magic number (short)
            Deflater.DEFLATED,         // Compression method (CM)
            0,                         // Flags (FLG)
            0,                         // Modification time MTIME (int)
            0,                         // Modification time MTIME (int)
            0,                         // Modification time MTIME (int)
            0,                         // Modification time MTIME (int)
            0,                         // Extra flags (XFLG)
            (byte) 255                 // Operating system (OS) "OS_UNKNOWN"
        };
    }

    private byte[] createTrailer() {
        // CRC-32 of uncompr. data
        int crc32 = (int) crc.getValue();
        // RFC 1952: Size of the original (uncompressed) input data modulo 2^32
        int iSize = (int) def.getBytesRead();
        return new byte[]{
            (byte) (crc32),
            (byte) (crc32 >>> 8),
            (byte) (crc32 >>> 16),
            (byte) (crc32 >>> 24),
            (byte) (iSize),
            (byte) (iSize >>> 8),
            (byte) (iSize >>> 16),
            (byte) (iSize >>> 24),
        };
    }

}
