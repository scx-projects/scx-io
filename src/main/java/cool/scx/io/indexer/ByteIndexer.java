package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// ByteIndexer
///
/// 有状态的 索引匹配器
///
/// @author scx567888
/// @version 0.0.1
public interface ByteIndexer {

    int NO_MATCH = Integer.MIN_VALUE;

    /// 支持跨 chunk 的回溯匹配, 因此返回值可能为负数
    /// 若未匹配到 请返回 NO_MATCH
    /// 可以不处理 空匹配模式 的边界情况 (上层会根据 patternLength == 0 直接走快速路径), 但要保证 patternLength 实现正确
    ///
    /// @param chunk chunk
    /// @return 匹配的索引位置
    int indexOf(ByteChunk chunk);

    /// 模式串长度
    int patternLength();

    /// 当前已匹配的长度
    int matchedLength();

    /// 重置匹配状态
    void reset();

}
