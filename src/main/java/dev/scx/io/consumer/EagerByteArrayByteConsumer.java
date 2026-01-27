package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

import java.util.Arrays;

/// EagerByteArrayByteConsumer
///
/// 逻辑来自 [java.io.ByteArrayOutputStream].
///
/// @author scx567888
/// @version 0.0.1
public final class EagerByteArrayByteConsumer implements ByteConsumer {

    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private byte[] bytes;
    private int total;

    public EagerByteArrayByteConsumer() {
        this(32);
    }

    public EagerByteArrayByteConsumer(int size) {
        this.bytes = new byte[size];
        this.total = 0;
    }

    private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        // preconditions not checked because of inlining
        // assert oldLength >= 0
        // assert minGrowth > 0

        int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            // put code cold in a separate method
            return hugeLength(oldLength, minGrowth);
        }
    }

    private static int hugeLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) { // overflow
            throw new OutOfMemoryError("Required array length " + oldLength + " + " + minGrowth + " is too large");
        } else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
            return SOFT_MAX_ARRAY_LENGTH;
        } else {
            return minLength;
        }
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = bytes.length;
        int minGrowth = minCapacity - oldCapacity;
        if (minGrowth > 0) {
            bytes = Arrays.copyOf(bytes, newLength(oldCapacity,
                minGrowth, oldCapacity /* preferred growth */));
        }
    }

    @Override
    public boolean accept(ByteChunk chunk) {
        ensureCapacity(total + chunk.length);
        System.arraycopy(chunk.bytes, chunk.start, bytes, total, chunk.length);
        total += chunk.length;
        return true;
    }

    public ByteChunk chunk() {
        return ByteChunk.of(bytes, 0, total);
    }

    public byte[] bytes() {
        return chunk().getBytes();
    }

    public int size() {
        return total;
    }

}
