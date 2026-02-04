package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

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
    private byte[] buffer;

    public FileByteSupplier(File file) throws ScxInputException {
        this(file, 0, file.length(), 8192);
    }

    public FileByteSupplier(File file, int bufferLength) throws ScxInputException {
        this(file, 0, file.length(), bufferLength);
    }

    public FileByteSupplier(File file, long offset, long length) throws ScxInputException {
        this(file, offset, length, 8192);
    }

    public FileByteSupplier(File file, long offset, long length, int bufferLength) throws ScxInputException {
        // 检查边界情况
        if (offset < 0 || length < 0 || offset + length > file.length()) {
            throw new IllegalArgumentException("offset/length out of file bounds");
        }
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength must be greater than 0");
        }
        this.bufferLength = bufferLength;
        this.remaining = length;
        try {
            this.randomAccessFile = createRandomAccessFile(file, offset);
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

    /// 失败时释放资源
    private static RandomAccessFile createRandomAccessFile(File file, long offset) throws IOException {
        // 创建随机读写文件
        var randomAccessFile = new RandomAccessFile(file, "r");
        try {
            // 先移动文件指针
            randomAccessFile.seek(offset);
            return randomAccessFile;
        } catch (IOException e) {
            // 失败需要 close 资源
            try {
                randomAccessFile.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
    }

    public ByteChunk get0() throws IOException {
        // 读取够了
        if (remaining <= 0) {
            return null;
        }
        // 计算本次最多能读多少.
        var needLength = (int) min(bufferLength, remaining);
        // 这里每次都创建一个 byte 数组的原因参考 InputStreamByteSupplier
        var bytes = new byte[needLength];
        // 读取
        int i = randomAccessFile.read(bytes);
        if (i == -1) {
            return null; // 处理文件结束情况
        }
        remaining -= i;
        return ByteChunk.of(bytes, 0, i);
    }

    private ByteChunk borrow0() throws IOException {
        // 读取够了
        if (remaining <= 0) {
            return null;
        }
        // 计算本次最多能读多少.
        var needLength = (int) min(bufferLength, remaining);
        if (buffer == null) {
            buffer = new byte[needLength];
        }
        // 读取
        int i = randomAccessFile.read(buffer, 0, needLength);
        if (i == -1) {
            return null; // 处理文件结束情况
        }
        remaining -= i;
        return ByteChunk.of(buffer, 0, i);
    }

    public void close0() throws IOException {
        // 此处依赖 RandomAccessFile 的 close 幂等.
        randomAccessFile.close();
    }

    @Override
    public ByteChunk get() throws ScxInputException {
        try {
            return get0();
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

    @Override
    public ByteChunk borrow() throws ScxInputException {
        try {
            return borrow0();
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

    @Override
    public void close() throws ScxInputException {
        try {
            close0();
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

}
