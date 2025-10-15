package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteNode;

/// ByteArrayByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteConsumer implements ByteConsumer<RuntimeException> {

    private ByteNode head;
    private ByteNode tail;
    private int total;

    public ByteArrayByteConsumer() {
        this.head = null;
        this.tail = null;
        this.total = 0;
    }

    @Override
    public boolean accept(ByteChunk byteChunk) {
        total += byteChunk.length;
        var dataNode = new ByteNode(byteChunk);
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
            return node.chunk.getBytes(node.position);
        }

        // 多个数据我们合并
        var bytes = new byte[total];
        int offset = 0;

        do {
            int length = node.available();
            int chunkOffset = node.chunk.start + node.position;
            System.arraycopy(node.chunk.bytes, chunkOffset, bytes, offset, length);
            offset += length;
            node = node.next;
        } while (node != null);

        return bytes;
    }

}
