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
    ///   因为跨 chunk 的原因, 索引值可能位于之前的 chunk, 这时允许 负数索引.
    /// - 可以不处理 空匹配模式 的边界情况 (上层会根据 patternLength == 0 直接走快速路径), 但要保证 patternLength 实现正确
    ///
    /// @param chunk chunk
    /// @return 匹配的索引位置
    int indexOf(ByteChunk chunk);

    /// 模式串长度
    /// - 对于固定长度模式串:
    ///   - 空模式串, 返回 0.
    ///   - 匹配成功前, 返回固定模式串长度.
    ///   - 匹配成功后, 返回固定模式串长度.
    /// - 对于可变长度模式串 (如正则):
    ///   - 空模式串, 返回 0; 如果无法静态推断为空模式串, 匹配前必须返回 > 0 (建议返回 1)
    ///   - 匹配成功前, 返回 > 0 即可, 不应由调用方作为逻辑依据.
    ///   - 匹配成功后, 返回本次实际匹配的长度.
    int patternLength();

    /// 当前已经连续匹配的字节长度
    int matchedLength();

    /// 重置匹配状态
    void reset();

}
