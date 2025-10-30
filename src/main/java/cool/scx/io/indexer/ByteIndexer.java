package cool.scx.io.indexer;

import cool.scx.io.ByteChunk;

/// ByteIndexer
///
/// 支持跨 chunk 匹配, 有状态的 索引匹配器, 可连续调用 indexOf.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteIndexer {

    /// - 匹配成功需重置状态, 以便能够直接进行下次匹配.
    /// - 可以不处理 空匹配模式 的边界情况 (上层会根据 patternLength == 0 直接走快速路径), 但要保证 patternLength 实现正确.
    StatusIndexMatchResult indexOf(ByteChunk chunk);

    /// 是否为空匹配模式
    /// - 对于固定长度模式串: 返回 是否是空模式串
    /// - 对于无法确定的可变长度模式串 (如正则), 返回 false.
    boolean isEmptyPattern();

    /// 重置匹配状态
    void reset();

}
