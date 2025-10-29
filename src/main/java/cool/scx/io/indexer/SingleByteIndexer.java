package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// SingleByteIndexer
///
/// @author scx567888
/// @version 0.0.1
public final class SingleByteIndexer implements ByteIndexer {

    private final byte b;

    public SingleByteIndexer(byte b) {
        this.b = b;
    }

    @Override
    public int indexOf(ByteChunk chunk) {
        //普通 查找
        for (var i = 0; i < chunk.length; i = i + 1) {
            if (chunk.getByte(i) == b) {
                return i;
            }
        }
        return NO_MATCH;
    }

    @Override
    public int patternLength() {
        return 1;
    }

    @Override
    public int matchedLength() {
        return 0;
    }

    @Override
    public void reset() {

    }

}
