package cool.scx.io;

/// ByteChunkQueue
///
/// @author scx567888
/// @version 0.0.1
public class ByteChunkQueue {

    private ByteNode head;
    private ByteNode tail;
    private long totalLength;

    public ByteChunkQueue() {
        this.head = null;
        this.tail = null;
        this.totalLength = 0;
    }

    public void append(ByteChunk chunk) {
        var node = new ByteNode(chunk);
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

}
