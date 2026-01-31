package dev.scx.io.indexer;

import dev.scx.io.ByteChunk;

import static dev.scx.io.indexer.StatusByteMatchResult.NO_MATCH_RESULT;
import static dev.scx.io.indexer.StatusByteMatchResult.fullMatch;

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
    public StatusByteMatchResult indexOf(ByteChunk chunk) {
        // 普通 查找
        for (var i = 0; i < chunk.length; i = i + 1) {
            if (chunk.get(i) == b) {
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
