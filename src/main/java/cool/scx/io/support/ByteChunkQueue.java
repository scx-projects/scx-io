package cool.scx.io.support;

import cool.scx.io.ByteChunk;

/// ByteChunkQueue
///
/// @author scx567888
/// @version 0.0.1
public class ByteChunkQueue {

    private ByteChunkNode head;
    private ByteChunkNode tail;
    private long totalLength;

    public ByteChunkQueue() {
        this.head = null;
        this.tail = null;
        this.totalLength = 0;
    }

    public void append(ByteChunk chunk) {
        var node = new ByteChunkNode(chunk);
        if (head == null) {
            head = node;
            tail = head;
        } else {
            tail.next = node;
            tail = tail.next;
        }
        this.totalLength += chunk.length;
    }

    public ByteChunk next() {
        if (head == null) {
            return null;
        }
        var chunk = head.chunk;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        this.totalLength -= chunk.length;
        return chunk;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public long totalLength() {
        return totalLength;
    }

    private static class ByteChunkNode {

        public final ByteChunk chunk;
        public ByteChunkNode next;

        public ByteChunkNode(ByteChunk chunk) {
            this.chunk = chunk;
        }

    }

}
