package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;

import java.io.IOException;
import java.io.InputStream;

/// BufferedInputStreamByteSupplier
/// 和 [InputStreamByteSupplier] 性能表现相反
/// 1, 当大部分时候读取的数据长度等于 bufferLength 的时候, 性能会差一点 因为多了一次复制
/// 2, 当大部分时候读取的数据长度小于 bufferLength 的时候, 性能会好一点 因为只会创建一个较小的数组并复制数据
///
/// @author scx567888
/// @version 0.0.1
public final class BufferedInputStreamByteSupplier implements ByteSupplier {

    private final InputStream inputStream;
    private final byte[] buffer;

    public BufferedInputStreamByteSupplier(InputStream inputStream, int bufferLength) {
        this.inputStream = inputStream;
        this.buffer = new byte[bufferLength];
    }

    public BufferedInputStreamByteSupplier(InputStream inputStream) {
        this(inputStream, 8192);
    }

    @Override
    public ByteChunk get() throws IOException {
        int i = inputStream.read(buffer);
        if (i == -1) {
            return null; // 数据结束
        }
        var data = new byte[i];
        System.arraycopy(buffer, 0, data, 0, i); // 复制数据到新的数组
        return new ByteChunk(data);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}
