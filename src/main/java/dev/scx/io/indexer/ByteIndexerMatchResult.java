package dev.scx.io.indexer;

import static dev.scx.io.indexer.ByteIndexerMatchResult.*;

/// ByteIndexerMatchResult
///
/// 表示 ByteIndexer 的状态机输出, 一般内部使用.
///
/// @author scx567888
public sealed interface ByteIndexerMatchResult permits NoMatch, PartialMatch, FullMatch {

    ByteIndexerMatchResult NO_MATCH = new NoMatch();

    static ByteIndexerMatchResult partialMatch(int unresolvedSuffixLength) {
        return new PartialMatch(unresolvedSuffixLength);
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

    /// 部分匹配 (存在尚不能安全释放的未决后缀)
    final class PartialMatch implements ByteIndexerMatchResult {

        /// 未决后缀长度, 单位为字节.
        ///
        /// 表示截至当前, 已消费输入的末尾有多少字节尚不能被证明可以安全释放.
        /// 该后缀之前的所有字节均已确定不会再影响后续匹配, 可以永久释放.
        ///
        /// 该值描述的是安全释放边界, 而非匹配进度, 因此不一定等于实际匹配的前缀长度,
        /// 并且可以跨越多个 ByteChunk.
        ///
        /// 实现应尽可能返回精确值；允许保守地高估（保留更多字节）, 但绝不允许低估.
        /// 已经被判定为安全的字节, 不得在后续结果中重新变为未决.
        ///
        /// 例如, 假设匹配模式为 `"abcde"`:
        ///
        /// ```
        /// Chunk 1: "xxa"
        /// Chunk 2: "bc"
        ///
        /// 当前已消费输入: "xxabc"
        ///
        /// PartialMatch(3)
        ///
        /// 安全前缀: "xx"
        /// 未决后缀: "abc"
        /// ```
        ///
        /// 此时 `"abc"` 仍有可能与后续输入 `"de"` 组成完整匹配, 因此必须保留; 而前面的 `"xx"` 已经可以安全释放.
        public final int unresolvedSuffixLength;

        private PartialMatch(int unresolvedSuffixLength) {
            this.unresolvedSuffixLength = unresolvedSuffixLength;
        }

        @Override
        public String toString() {
            return "PartialMatch[unresolvedSuffixLength=" + unresolvedSuffixLength + ']';
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
