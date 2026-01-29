package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

import java.util.zip.CRC32;
import java.util.zip.Deflater;

/// GZIPByteOutput
///
/// 注意: 本实现向下游写出的 ByteChunk 均基于同一个内部输出缓冲区(backing byte[]),
/// 每次写出后该缓冲区内容可能被后续写入覆盖.
/// 下游实现若延迟读取或保存 ByteChunk 引用(而非立即拷贝数据),
/// 将导致结果不正确.
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

    public GZIPByteOutput(ByteOutput out) {
        this(out, new GZIPByteOutputOptions());
    }

    public GZIPByteOutput(ByteOutput out, GZIPByteOutputOptions options) {
        this.out = out;
        this.def = new Deflater(options.compressionLevel, true);
        this.buffer = new byte[options.bufferLength];
        this.syncFlush = options.syncFlush;
        this.crc = new CRC32();
        this.headerWritten = false;
        this.closed = false;
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {

        // 注意: 此处直接 new.
        // 不要尝试复用单字节 ByteChunk; 在现代 JVM 上短生命周期对象通常可被优化消除,
        // 复用共享可变实例反而会限制 JIT 优化, 可能更慢.
        write(ByteChunk.of(new byte[]{b}));
    }

    /// 逻辑参考 [java.util.zip.GZIPOutputStream#write(byte\[\], int, int)]
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
                writeBuffer(len);
            }
        }

        // crc 校验
        crc.update(byteChunk.bytes, byteChunk.start, byteChunk.length);
    }

    /// 逻辑参考 [java.util.zip.DeflaterOutputStream#flush()]
    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        ensureHeader();

        if (syncFlush && !def.finished()) {
            while (true) {
                var len = def.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);
                if (len > 0) {
                    writeBuffer(len);
                } else {
                    break;
                }
            }
        }

        out.flush();
    }

    /// 逻辑参考 [java.util.zip.DeflaterOutputStream#close()]
    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try (def) {

            ensureHeader();

            def.finish();

            while (!def.finished()) {
                int len = def.deflate(buffer, 0, buffer.length, Deflater.NO_FLUSH);
                if (len > 0) {
                    writeBuffer(len);
                }
            }

            out.write(createTrailer());
            out.close();

        }

        closed = true;

    }

    /// 写入缓冲区
    private void writeBuffer(int len) {
        // 注意: 写出的 ByteChunk 视图复用同一 backing byte[] (buffer);
        // 后续写入会覆盖该缓冲区内容, 下游必须在 write 返回前立即消费, 不得保存引用.
        out.write(ByteChunk.of(buffer, 0, len));
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

    /// 因配置项过多, 此处拆成独立的 Options 类.
    public static final class GZIPByteOutputOptions {

        private int bufferLength;
        private boolean syncFlush;
        private int compressionLevel;

        public GZIPByteOutputOptions() {
            this.bufferLength = 1024;
            this.syncFlush = false;
            this.compressionLevel = Deflater.DEFAULT_COMPRESSION;
        }

        public int bufferLength() {
            return bufferLength;
        }

        public GZIPByteOutputOptions bufferLength(int bufferLength) {
            if (bufferLength <= 0) {
                throw new IllegalArgumentException("bufferLength must be greater than 0");
            }
            this.bufferLength = bufferLength;
            return this;
        }

        public boolean syncFlush() {
            return syncFlush;
        }

        public GZIPByteOutputOptions syncFlush(boolean syncFlush) {
            this.syncFlush = syncFlush;
            return this;
        }

        public int compressionLevel() {
            return compressionLevel;
        }

        public GZIPByteOutputOptions compressionLevel(int compressionLevel) {
            if (compressionLevel != Deflater.DEFAULT_COMPRESSION && (compressionLevel < 0 || compressionLevel > 9)) {
                throw new IllegalArgumentException("compressionLevel must be -1 or 0..9");
            }
            this.compressionLevel = compressionLevel;
            return this;
        }

    }

}
