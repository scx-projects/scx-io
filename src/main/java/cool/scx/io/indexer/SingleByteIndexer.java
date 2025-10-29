package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

import static cool.scx.io.indexer.IndexMatchResult.NO_MATCH_RESULT;
import static cool.scx.io.indexer.IndexMatchResult.fullMatch;

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
    public IndexMatchResult indexOf(ByteChunk chunk) {
        //普通 查找
        for (var i = 0; i < chunk.length; i = i + 1) {
            if (chunk.getByte(i) == b) {
                return fullMatch(i, 1);
            }
        }
        return NO_MATCH_RESULT;
    }

    @Override
    public boolean isEmptyPattern() {
        return false;
    }

    @Override
    public void reset() {

    }

}
