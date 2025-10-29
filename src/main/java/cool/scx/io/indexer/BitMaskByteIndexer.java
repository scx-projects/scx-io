package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

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

    @Override
    public int indexOf(ByteChunk chunk) {

        var endBit = 1L << (pattern.length - 1);

        // BitMask 查找
        for (var i = 0; i < chunk.length; i = i + 1) {

            var currentByte = chunk.getByte(i);

            var idx = currentByte & 0xFF;

            var m = mask[idx];

            // Shift-And 核心: 向前推进一位 (匹配了新字符),并加上初始状态 (|1)
            state = ((state << 1) | 1L) & m;

            if ((state & endBit) != 0) {
                // 重置 state 为 0, 保证下次匹配
                state = 0;
                // 当前索引 - 回退量 (模式串长度 - 1)
                return i - (pattern.length - 1);
            }

        }

        return NO_MATCH;
    }

    @Override
    public int patternLength() {
        return pattern.length;
    }

    @Override
    public int matchedLength() {
        return Long.SIZE - Long.numberOfLeadingZeros(state);
    }

    @Override
    public void reset() {
        state = 0;
    }

}
