package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

import java.io.IOException;
import java.io.InputStream;

/// InputStreamByteSupplier
///
/// 1, 当大部分时候读取的数据长度等于 bufferLength 的时候, 性能会高一点 因为只会进行数组创建这一步
///
/// 2, 当大部分时候读取的数据长度小于 bufferLength 的时候, 性能会差一点 因为每次都会创建一个 bufferLength 大小的数组
///
/// 这时建议使用  [BufferedInputStreamByteSupplier]
///
/// @author scx567888
/// @version 0.0.1
public final class InputStreamByteSupplier implements ByteSupplier {

    private final InputStream inputStream;
    private final int bufferLength;
    private byte[] buffer;

    public InputStreamByteSupplier(InputStream inputStream) {
        this(inputStream, 8192);
    }

    public InputStreamByteSupplier(InputStream inputStream, int bufferLength) {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength must be greater than 0");
        }
        this.inputStream = inputStream;
        this.bufferLength = bufferLength;
    }

    public ByteChunk get0() throws IOException {
        // 这里每次都创建一个 byte 数组是因为我们后续需要直接使用 这个数组
        // 即使使用成员变量 来作为缓冲 buffer
        // 也是需要重新分配 一个新的数组 来将数据复制过去 所以本质上并没有区别
        // 甚至这种情况再同时持有多个 InputStreamByteSupplier 的时候 内存占用会更少 因为没有成员变量
        var bytes = new byte[bufferLength];
        int i = inputStream.read(bytes);
        if (i == -1) {
            return null; // 数据结束
        }
        return ByteChunk.of(bytes, 0, i);
    }

    public ByteChunk borrow0() throws IOException {
        if (buffer == null) {
            buffer = new byte[bufferLength];
        }
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
