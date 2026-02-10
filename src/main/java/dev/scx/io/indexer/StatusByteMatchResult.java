package dev.scx.io.indexer;

import static dev.scx.io.indexer.StatusByteMatchResult.Status.*;

/// StatusByteMatchResult
///
/// 表示 ByteIndexer 的状态机输出, 一般内部使用.
///
/// @author scx567888
/// @version 0.0.1
public final class StatusByteMatchResult {

    public static final StatusByteMatchResult NO_MATCH_RESULT = new StatusByteMatchResult(NO_MATCH, -1, -1);

    public static final StatusByteMatchResult PARTIAL_MATCH_RESULT = new StatusByteMatchResult(PARTIAL_MATCH, -1, -1);

    /// 匹配状态
    public final Status status;

    /// - 若完全未匹配或部分匹配, 该数据无意义.
    /// - 若完全匹配, 表示 相对于 当前 chunk 的索引值, 因为允许跨 chunk, 索引值可能位于之前的 chunk, 这时可以是 负数索引.
    public final int index;

    /// - 若完全未匹配或部分匹配, 该数据无意义.
    /// - 若完全匹配, 表示本次实际匹配的长度
    public final int matchedLength;

    private StatusByteMatchResult(Status status, int index, int matchedLength) {
        this.status = status;
        this.index = index;
        this.matchedLength = matchedLength;
    }

    public static StatusByteMatchResult fullMatch(int index, int matchedLength) {
        return new StatusByteMatchResult(FULL_MATCH, index, matchedLength);
    }

    @Override
    public String toString() {
        return "StatusByteMatchResult[status=" + status + ", index=" + index + ", matchedLength=" + matchedLength + ']';
    }

    public enum Status {

        /// 完全未匹配
        NO_MATCH,

        /// 部分匹配
        PARTIAL_MATCH,

        /// 完全匹配
        FULL_MATCH

    }

}
