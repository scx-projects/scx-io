package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// LineBreakIndexer
///
/// 可同时匹配 `\r\n` 或 `\n` .
///
/// @author scx567888
/// @version 0.0.1
public final class LineBreakIndexer implements ByteIndexer {

    private int matchedLength;

    public LineBreakIndexer() {
        this.matchedLength = 0;
    }

    @Override
    public int indexOf(ByteChunk chunk) {

        for (int i = 0; i < chunk.length; i = i + 1) {

            var currentByte = chunk.getByte(i);

            if (matchedLength == 0 || matchedLength == 2) { // 未曾匹配
                if (currentByte == '\n') {
                    matchedLength = 1; // 重置匹配状态
                    return i; // 直接匹配到 \n
                } else if (currentByte == '\r') {
                    matchedLength = 1; // 暂存状态, 等待 \n
                }
            } else if (matchedLength == 1) {
                if (currentByte == '\n') {
                    matchedLength = 2; // 重置匹配状态
                    return i - 1; // \r 后匹配 \n
                } else if (currentByte == '\r') {
                    matchedLength = 1; // 当前字符又是 \r, 保留状态, 相当于重启匹配
                }else{
                    matchedLength = 0; // \r 后不是 \n, 重置匹配状态
                }
            }
        }

        return NO_MATCH;
    }

    @Override
    public boolean isEmptyPattern() {
        return false;
    }

    @Override
    public int matchedLength() {
        return matchedLength;
    }

    @Override
    public void reset() {
        matchedLength = 0;
    }

}
