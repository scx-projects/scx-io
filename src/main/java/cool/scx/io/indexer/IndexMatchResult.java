package cool.scx.io.indexer;

import static cool.scx.io.indexer.IndexMatchStatus.*;

/// IndexMatchStatus
///
/// @author scx567888
/// @version 0.0.1
public final class IndexMatchResult {

    public static final IndexMatchResult NO_MATCH_RESULT = new IndexMatchResult(NO_MATCH, -1, -1);

    public static final IndexMatchResult PARTIAL_MATCH_RESULT = new IndexMatchResult(PARTIAL_MATCH, -1, -1);

    /// 匹配状态
    public final IndexMatchStatus status;

    /// - 若完全未匹配或部分匹配, 该数据无意义.
    /// - 若完全匹配, 表示 相对于 当前 chunk 的索引值, 因为允许跨 chunk, 索引值可能位于之前的 chunk, 这时可以是 负数索引.
    public final long index;

    /// - 若完全未匹配或部分匹配, 该数据无意义.
    /// - 若完全匹配, 表示本次实际匹配的长度
    public final int matchedLength;

    private IndexMatchResult(IndexMatchStatus status, long index, int matchedLength) {
        this.status = status;
        this.index = index;
        this.matchedLength = matchedLength;
    }

    public static IndexMatchResult fullMatch(long index, int matchedLength) {
        return new IndexMatchResult(FULL_MATCH, index, matchedLength);
    }

}
