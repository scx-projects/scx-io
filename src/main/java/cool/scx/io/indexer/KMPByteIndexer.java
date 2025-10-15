package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// KMPByteIndexer
///
/// @author scx567888
/// @version 0.0.1
public final class KMPByteIndexer implements ByteIndexer {

    private final byte[] pattern;
    private final int[] lps;
    private int matchedLength;

    public KMPByteIndexer(byte[] pattern) {
        this.pattern = pattern;
        this.lps = buildLPS(pattern);// 创建部分匹配表
        this.matchedLength = 0; // 模式串索引
    }

    public static int[] buildLPS(byte[] pattern) {
        int[] lps = new int[pattern.length];
        int length = 0;
        int i = 1;

        while (i < pattern.length) {
            if (pattern[i] == pattern[length]) {
                length = length + 1;
                lps[i] = length;
                i = i + 1;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i = i + 1;
                }
            }
        }

        return lps;
    }

    @Override
    public int indexOf(ByteChunk chunk) {
        // 匹配模式为空
        if (isEmptyPattern()) {
            return 0;
        }

        //KMP 查找
        for (int i = 0; i < chunk.length; i = i + 1) {

            var currentByte = chunk.getByte(i);

            while (matchedLength > 0 && currentByte != pattern[matchedLength]) {
                matchedLength = lps[matchedLength - 1];
            }

            if (currentByte == pattern[matchedLength]) {
                matchedLength = matchedLength + 1;
            }

            if (matchedLength == pattern.length) {
                var result = i - matchedLength + 1;
                // 重置 patternIndex 为 0, 保证下次匹配
                matchedLength = 0;
                return result;
            }
        }
        return NO_MATCH;
    }

    @Override
    public boolean isEmptyPattern() {
        return pattern.length == 0;
    }

    public byte[] pattern() {
        return pattern;
    }

    /// 当前已匹配的模式串长度
    public int matchedLength() {
        return matchedLength;
    }

    public void reset() {
        matchedLength = 0;
    }

}
