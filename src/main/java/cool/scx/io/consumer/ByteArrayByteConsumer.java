package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;

/// ByteArrayByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteConsumer implements ByteConsumer<RuntimeException> {

    private ByteChunkNode head;
    private ByteChunkNode tail;
    private int total;

    public ByteArrayByteConsumer() {
        this.head = null;
        this.tail = null;
        this.total = 0;
    }

    @Override
    public boolean accept(ByteChunk byteChunk) {
        total += byteChunk.length;
        var dataNode = new ByteChunkNode(byteChunk);
        if (head == null) {
            head = dataNode;
            tail = head;
        } else {
            tail.next = dataNode;
            tail = tail.next;
        }
        return true;
    }

    public byte[] bytes() {
        var node = head;

        // 从未调用 accept 会导致此情况
        if (node == null) {
            return new byte[0];
        }

        // 只调用了一次 accept, 我们直接返回当前数据
        if (node.next == null) {
            return node.chunk.getBytes();
        }

        // 多个数据我们合并
        var bytes = new byte[total];
        int offset = 0;

        do {
            int chunkLength = node.chunk.length;
            int chunkOffset = node.chunk.start;
            System.arraycopy(node.chunk.bytes, chunkOffset, bytes, offset, chunkLength);
            offset += chunkLength;
            node = node.next;
        } while (node != null);

        return bytes;
    }

    private static class ByteChunkNode {

        public final ByteChunk chunk;
        public ByteChunkNode next;

        public ByteChunkNode(ByteChunk chunk) {
            this.chunk = chunk;
        }

    }

}
