package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// LazyByteArrayByteConsumer
///
/// lazy 模式: accept 仅记录 ByteChunk 引用 (每次 accept 分配节点), 在 bytes() 时一次性分配并合并拷贝.
/// 适合: accept 次数较少但单次 chunk 较大, 或仅在最后调用一次 bytes() 的场景.
/// 代价: chunk 很小且 accept 次数很高时, 节点分配与遍历开销会主导, 可能明显慢于 [EagerByteArrayByteConsumer], 且内存波动更大.
///
/// 注意: 本实现会持有 ByteChunk 的 backing byte[] 引用; 调用方需保证其在 bytes() 前保持有效且不被修改.
///
/// @author scx567888
/// @version 0.0.1
public final class LazyByteArrayByteConsumer implements ByteConsumer {

    private ByteChunkNode head;
    private ByteChunkNode tail;
    private int total;

    public LazyByteArrayByteConsumer() {
        this.head = null;
        this.tail = null;
        this.total = 0;
    }

    @Override
    public boolean accept(ByteChunk byteChunk) {
        var dataNode = new ByteChunkNode(byteChunk);
        if (head == null) {
            head = dataNode;
            tail = head;
        } else {
            tail.next = dataNode;
            tail = tail.next;
        }
        total += byteChunk.length;
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

    public int size() {
        return total;
    }

    private static class ByteChunkNode {

        public final ByteChunk chunk;
        public ByteChunkNode next;

        public ByteChunkNode(ByteChunk chunk) {
            this.chunk = chunk;
        }

    }

}
