package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// ByteIndexer
///
/// 支持跨 chunk 匹配, 有状态的 索引匹配器, 可连续调用 indexOf.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteIndexer {

    int NO_MATCH = Integer.MIN_VALUE;

    ///
    /// - 若未匹配到, 请返回 NO_MATCH.
    /// - 若匹配到, 请返回 相对于 当前 chunk 的索引值,
    ///   因为跨 chunk 的原因, 索引值位于之前的 chunk, 这时允许 负数索引,
    /// - 可以不处理 空匹配模式 的边界情况 (上层会根据 patternLength == 0 直接走快速路径), 但要保证 patternLength 实现正确
    ///
    /// @param chunk chunk
    /// @return 匹配的索引位置
    int indexOf(ByteChunk chunk);

    /// 模式串长度
    int patternLength();

    /// 当前已经连续匹配的字节长度
    int matchedLength();

    /// 重置匹配状态
    void reset();

}
