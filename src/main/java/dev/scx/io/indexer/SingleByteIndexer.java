package dev.scx.io.indexer;

import dev.scx.io.ByteChunk;

import static dev.scx.io.indexer.ByteIndexerMatchResult.NO_MATCH_RESULT;
import static dev.scx.io.indexer.ByteIndexerMatchResult.fullMatch;

/// SingleByteIndexer
///
/// @author scx567888
public final class SingleByteIndexer implements ByteIndexer {

    private final byte b;

    public SingleByteIndexer(byte b) {
        this.b = b;
    }

    @Override
    public ByteIndexerMatchResult indexOf(ByteChunk chunk) {
        // 普通 查找
        for (var i = 0; i < chunk.length; i = i + 1) {
            if (chunk.get(i) == b) {
                return fullMatch(i, 1);
            }
        }
        return NO_MATCH_RESULT;
    }

    @Override
    public int patternLength() {
        return 1;
    }

    @Override
    public void reset() {

    }

}
