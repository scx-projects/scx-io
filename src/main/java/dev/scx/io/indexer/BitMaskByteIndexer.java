package dev.scx.io.indexer;

import dev.scx.io.ByteChunk;

import static dev.scx.io.indexer.StatusByteMatchResult.*;

/// BitMaskByteIndexer
///
/// @author scx567888
/// @version 0.0.1
public final class BitMaskByteIndexer implements ByteIndexer {

    private final byte[] pattern;
    private final long[] mask;
    private long state;

    public BitMaskByteIndexer(byte[] pattern) {
        if (pattern.length > 64) {
            throw new IllegalArgumentException("BitMaskByteIndexer only supports pattern length <= 64");
        }
        this.pattern = pattern;
        this.mask = buildMask(pattern);
        this.state = 0;
    }

    private static long[] buildMask(byte[] pattern) {
        var mask = new long[256];
        // 构建 bitmask (仅 pattern 中的字节会有1)
        for (int i = 0; i < pattern.length; i = i + 1) {
            // 第 i 位代表模式第 i 字节
            var idx = pattern[i] & 0xFF;
            mask[idx] |= 1L << i;
        }
        return mask;
    }

    /// 前提条件:
    /// - pattern.length > 0
    /// - 空匹配模式不在本实现的处理范围内, 必须由调用者提前判断, 例如通过 isEmptyPattern().
    ///
    /// 若违反此前提, indexOf 的行为未定义.
    @Override
    public StatusByteMatchResult indexOf(ByteChunk chunk) {

        var endBit = 1L << (pattern.length - 1);

        // BitMask 查找
        for (var i = 0; i < chunk.length; i = i + 1) {

            var currentByte = chunk.get(i);

            var idx = currentByte & 0xFF;

            var m = mask[idx];

            // Shift-And 核心: 向前推进一位 (匹配了新字符),并加上初始状态 (|1)
            state = ((state << 1) | 1L) & m;

            if ((state & endBit) != 0) {
                // 重置 state 为 0, 保证下次匹配
                state = 0;
                // 当前索引 - 回退量 (模式串长度 - 1)
                return fullMatch(i - (pattern.length - 1), pattern.length);
            }

        }

        return state == 0 ? NO_MATCH_RESULT : PARTIAL_MATCH_RESULT;
    }

    @Override
    public boolean isEmptyPattern() {
        return pattern.length == 0;
    }

    @Override
    public void reset() {
        state = 0;
    }

}
