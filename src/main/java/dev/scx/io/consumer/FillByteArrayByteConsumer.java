package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// FillByteArrayByteConsumer
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
