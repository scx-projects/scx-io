package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// FillByteArrayByteConsumer
///
/// 严格填充 byte[] 的消费者.
///
/// 约定:
/// - 假定上层已明确限定总读取长度.
/// - 每次 accept 都完整写入传入的 ByteChunk.
/// - 若写入将超出容量, 直接抛异常, 不通过返回 false 终止.
///
/// 前提:
/// - position + length <= data.length
///
/// @author scx567888
/// @version 0.0.1
public final class FillByteArrayByteConsumer implements ByteConsumer {

    private final byte[] data;
    private final int position;
    private final int length;
    private int bytesFilled;

    public FillByteArrayByteConsumer(byte[] data) {
        this(data, 0, data.length);
    }

    public FillByteArrayByteConsumer(byte[] data, int position, int length) {
        this.data = data;
        this.position = position;
        this.length = length;
        this.bytesFilled = 0;
    }

    @Override
    public boolean accept(ByteChunk byteChunk) throws IndexOutOfBoundsException {
        if (bytesFilled + byteChunk.length > length) {
            throw new IndexOutOfBoundsException("Buffer overflow: not enough space to accept more data");
        }
        System.arraycopy(byteChunk.bytes, byteChunk.start, data, position + bytesFilled, byteChunk.length);
        bytesFilled += byteChunk.length;
        return true;
    }

    public int bytesFilled() {
        return bytesFilled;
    }

}
