package cool.scx.io.aaa;

import cool.scx.io.ByteChunk;

/// ByteChunkNode
///
/// @author scx567888
/// @version 0.0.1
public class ByteChunkNode {

    public final ByteChunk chunk;
    public ByteChunkNode next;

    public ByteChunkNode(ByteChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public String toString() {
        return chunk.toString();
    }

}
