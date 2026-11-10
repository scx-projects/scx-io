package dev.scx.io.indexer;

import static dev.scx.io.indexer.ByteIndexerMatchResult.*;

/// ByteIndexerMatchResult
///
/// 表示 ByteIndexer 的状态机输出, 一般内部使用.
///
/// @author scx567888
public sealed interface ByteIndexerMatchResult permits NoMatch, PartialMatch, FullMatch {

    ByteIndexerMatchResult NO_MATCH = new NoMatch();

    static ByteIndexerMatchResult partialMatch(int matchedLength) {
        return new PartialMatch(matchedLength);
    }

    static ByteIndexerMatchResult fullMatch(int index, int matchedLength) {
        return new FullMatch(index, matchedLength);
    }

    /// 完全未匹配
    final class NoMatch implements ByteIndexerMatchResult {

        private NoMatch() {

        }

        @Override
        public String toString() {
            return "NoMatch[]";
        }

    }

    /// 部分匹配
    final class PartialMatch implements ByteIndexerMatchResult {

        /// 表示当前与 Pattern 前缀部分匹配的长度.
        public final int matchedLength;

        private PartialMatch(int matchedLength) {
            this.matchedLength = matchedLength;
        }

        @Override
        public String toString() {
            return "PartialMatch[matchedLength=" + matchedLength + ']';
        }

    }

    /// 完全匹配
    final class FullMatch implements ByteIndexerMatchResult {

        /// 相对于 当前 chunk 的索引值, 因为允许跨 chunk, 索引值可能位于之前的 chunk, 这时可以是 负数索引
        public final int index;

        /// 表示本次实际匹配的长度
        public final int matchedLength;

        private FullMatch(int index, int matchedLength) {
            this.index = index;
            this.matchedLength = matchedLength;
        }

        @Override
        public String toString() {
            return "FullMatch[index=" + index + ", matchedLength=" + matchedLength + ']';
        }

    }

}
