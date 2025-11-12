package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.exception.ScxIOException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.Math.min;

/// FileByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class FileByteSupplier implements ByteSupplier {

    private final RandomAccessFile randomAccessFile;
    private final int bufferLength;
    private long remaining;

    public FileByteSupplier(File file) throws ScxIOException {
        this(file, 0, file.length(), 8192);
    }

    public FileByteSupplier(File file, int bufferLength) throws ScxIOException {
        this(file, 0, file.length(), bufferLength);
    }

    public FileByteSupplier(File file, long offset, long length) throws ScxIOException {
        this(file, offset, length, 8192);
    }

    public FileByteSupplier(File file, long offset, long length, int bufferLength) throws ScxIOException {
        // 检查边界情况
        if (offset < 0 || length < 0 || offset + length > file.length()) {
            throw new IllegalArgumentException("offset/length out of file bounds");
        }
        this.bufferLength = bufferLength;
        this.remaining = length;
        try {
            // 创建随机读写文件
            this.randomAccessFile = new RandomAccessFile(file, "r");
            // 先移动文件指针
            this.randomAccessFile.seek(offset);
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    public ByteChunk get0() throws IOException {
        if (remaining <= 0) {
            return null;
        }
        // 这里每次都创建一个 byte 数组的原因参考 InputStreamByteSupplier
        var bytes = new byte[bufferLength];
        // 读取
        int i = randomAccessFile.read(bytes, 0, (int) min(bufferLength, remaining));
        if (i == -1) {
            return null; // 处理文件结束情况
        }
        remaining -= i;
        return ByteChunk.of(bytes, 0, i);
    }

    public void close0() throws IOException {
        randomAccessFile.close();
    }

    @Override
    public ByteChunk get() throws ScxIOException {
        try {
            return get0();
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    @Override
    public void close() throws ScxIOException {
        try {
            close0();
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

}
