package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// LineBreakIndexer
///
/// 可同时匹配 `\r\n` 或 `\n` .
///
/// @author scx567888
/// @version 0.0.1
public final class LineBreakIndexer implements ByteIndexer {

    private int state = 0; // 0 nothing, 1 saw \r
    private int lastMatchedLength = 0; // 1 or 2

    @Override
    public int indexOf(ByteChunk chunk) {

        for (int i = 0; i < chunk.length; i++) {
            byte b = chunk.getByte(i);

            if (state == 0) {
                if (b == '\n') {
                    lastMatchedLength = 1;
                    state = 0;
                    return i;
                } else if (b == '\r') {
                    state = 1;
                    continue;
                }
            } else if (state == 1) {
                if (b == '\n') {
                    lastMatchedLength = 2;
                    state = 0;
                    return i - 1; // match "\r\n"
                } else {
                    // previous \r stands alone
                    lastMatchedLength = 1;
                    // 注意：匹配发生在上一字节
                    int pos = i - 1;
                    // 当前字节若是 '\r' 继续保持状态，否则清空
                    state = (b == '\r') ? 1 : 0;
                    return pos;
                }
            }
        }

        // 未匹配成功
        lastMatchedLength = 0;
        return NO_MATCH;
    }

    @Override
    public int patternLength() {
        // ✅ 匹配成功后返回真实长度（1 或 2）
        // ✅ 匹配前必须返回 > 0（避免误判为空模式）
        return lastMatchedLength == 0 ? 1 : lastMatchedLength;
    }

    @Override
    public int matchedLength() {
        // \r 已读 1 字节是部分匹配
        return state == 1 ? 1 : 0;
    }

    @Override
    public void reset() {
        state = 0;
        lastMatchedLength = 0;
    }

}
