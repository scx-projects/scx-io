package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// ByteIndexer
///
/// @author scx567888
/// @version 0.0.1
public interface ByteIndexer {

    int NO_MATCH = Integer.MIN_VALUE;

    /// 支持跨 chunk 的回溯匹配, 因此返回值可能为负数
    /// 若未匹配到 请返回 NO_MATCH
    ///
    /// @param chunk chunk
    /// @return 匹配的索引位置
    int indexOf(ByteChunk chunk);

    /// 是否为空匹配模式
    boolean isEmptyPattern();

}
