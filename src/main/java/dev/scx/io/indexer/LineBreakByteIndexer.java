package dev.scx.io.indexer;

import dev.scx.io.ByteChunk;

import static dev.scx.io.indexer.StatusByteMatchResult.*;

/// LineBreakByteIndexer
///
/// 可同时匹配 `\r\n` 或 `\n` .
///
/// @author scx567888
/// @version 0.0.1
public final class LineBreakByteIndexer implements ByteIndexer {

    private int matchedLength;

    public LineBreakByteIndexer() {
        this.matchedLength = 0;
    }

    @Override
    public StatusByteMatchResult indexOf(ByteChunk chunk) {

        for (int i = 0; i < chunk.length; i = i + 1) {

            var currentByte = chunk.get(i);

            // 未曾匹配 或者 已经处于匹配成功状态
            if (matchedLength == 0) {
                if (currentByte == '\n') { // \n 匹配成功
                    matchedLength = 0; // 重置状态
                    return fullMatch(i, 1);
                } else if (currentByte == '\r') {
                    matchedLength = 1; // 暂存状态, 等待 \n
                } else {
                    matchedLength = 0; // 重置匹配
                }
            } else if (matchedLength == 1) {
                if (currentByte == '\n') { // \r\n 匹配成功
                    matchedLength = 0; // 重置状态
                    return fullMatch(i - 1, 2);
                } else if (currentByte == '\r') {
                    matchedLength = 1; // 当前字符又是 \r, 重启匹配
                } else {
                    matchedLength = 0; // 重置匹配
                }
            }
        }

        return matchedLength == 0 ? NO_MATCH_RESULT : PARTIAL_MATCH_RESULT;
    }

    @Override
    public boolean isEmptyPattern() {
        return false;
    }

    @Override
    public void reset() {
        matchedLength = 0;
    }

}
