package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;

import java.io.IOException;
import java.io.InputStream;

/// InputStreamByteSupplier
/// 1, 当大部分时候读取的数据长度等于 bufferLength 的时候, 性能会高一点 因为只会进行数组创建这一步
/// 2, 当大部分时候读取的数据长度小于 bufferLength 的时候, 性能会差一点 因为每次都会创建一个 bufferLength 大小的数组
/// 如果启用压缩则会产生第二次复制 增加时间
/// 如果未启用压缩则会造成内存上的一些浪费
/// 这时建议使用  [BufferedInputStreamByteSupplier]
///
/// @author scx567888
/// @version 0.0.1
public final class InputStreamByteSupplier implements ByteSupplier {

    private final InputStream inputStream;
    private final int bufferLength;
    private final boolean compress;

    public InputStreamByteSupplier(InputStream inputStream) {
        this(inputStream, 8192, false);
    }

    public InputStreamByteSupplier(InputStream inputStream, boolean compress) {
        this(inputStream, 8192, compress);
    }

    public InputStreamByteSupplier(InputStream inputStream, int bufferLength) {
        this(inputStream, bufferLength, false);
    }

    public InputStreamByteSupplier(InputStream inputStream, int bufferLength, boolean compress) {
        this.inputStream = inputStream;
        this.bufferLength = bufferLength;
        this.compress = compress;
    }

    @Override
    public ByteChunk get() throws IOException {
        // 这里每次都创建一个 byte 数组是因为我们后续需要直接使用 这个数组
        // 即使使用成员变量 来作为缓冲 buffer
        // 也是需要重新分配 一个新的数组 来将数据复制过去 所以本质上并没有区别
        // 甚至这种情况再同时持有多个 InputStreamDataSupplier 的时候 内存占用会更少 因为没有成员变量
        var bytes = new byte[bufferLength];
        int i = inputStream.read(bytes);
        if (i == -1) {
            return null; // end of data
        }
        // 如果读取的数据量与缓冲区大小一致, 直接返回内部数组
        if (i == bufferLength) {
            return new ByteChunk(bytes);
        } else if (compress) {// 否则判断是否开启压缩
            var data = new byte[i];
            System.arraycopy(bytes, 0, data, 0, i);
            return new ByteChunk(data);
        } else {// 不压缩 直接返回
            return new ByteChunk(bytes, 0, i);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}
