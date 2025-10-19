package cool.scx.io;

/// ByteChunkQueue
///
/// @author scx567888
/// @version 0.0.1
public class ByteChunkQueue {

    private ByteNode head;
    private ByteNode tail;

    public void append(ByteChunk chunk) {
        var node = new ByteNode(chunk);
        if (head == null) {
            head = node;
            tail = head;
        } else {
            tail.next = node;
            tail = tail.next;
        }
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
        return chunk;
    }

    public boolean isEmpty() {
        return head == null;
    }

}
