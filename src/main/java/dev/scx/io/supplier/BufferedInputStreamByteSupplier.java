package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

import java.io.IOException;
import java.io.InputStream;

/// BufferedInputStreamByteSupplier
///
/// 和 [InputStreamByteSupplier] 性能表现相反
///
/// 1, 当大部分时候读取的数据长度等于 bufferLength 的时候, 性能会差一点 因为多了一次复制
///
/// 2, 当大部分时候读取的数据长度小于 bufferLength 的时候, 性能会好一点 因为只会创建一个较小的数组并复制数据
///
/// @author scx567888
/// @version 0.0.1
public final class BufferedInputStreamByteSupplier implements ByteSupplier {

    private final InputStream inputStream;
    private final byte[] buffer;

    public BufferedInputStreamByteSupplier(InputStream inputStream) {
        this(inputStream, 8192);
    }

    public BufferedInputStreamByteSupplier(InputStream inputStream, int bufferLength) {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength must be greater than 0");
        }
        this.inputStream = inputStream;
        this.buffer = new byte[bufferLength];
    }

    public ByteChunk get0() throws IOException {
        int i = inputStream.read(buffer);
        if (i == -1) {
            return null; // 数据结束
        }
        var bytes = new byte[i];
        System.arraycopy(buffer, 0, bytes, 0, i); // 复制数据到新的数组
        return ByteChunk.of(bytes);
    }

    public ByteChunk borrow0() throws IOException {
        int i = inputStream.read(buffer);
        if (i == -1) {
            return null; // 数据结束
        }
        return ByteChunk.of(buffer, 0, i);
    }

    public void close0() throws IOException {
        // 此处依赖 InputStream 的 close 幂等.
        inputStream.close();
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

    public InputStream inputStream() {
        return inputStream;
    }

}
