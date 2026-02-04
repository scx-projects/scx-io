package dev.scx.io.indexer;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;

/// ByteIndexer
///
/// 支持跨 chunk 匹配, 有状态的 索引匹配器, 可连续调用 indexOf.
///
/// 1. 实现方 **允许** 在 indexOf 返回后继续使用或保存传入的 ByteChunk (尽管大多数情况并不需要).
///    因此, ByteIndexer 具有 retaining 语义.
///
/// 2. 调用 indexOf 的一方必须保证:
///    传入的 ByteChunk 所引用的底层 byte[] 不会被覆写或复用;
///    若无法保证该稳定性, 调用方必须在调用 indexOf 之前自行拷贝数据.
///
/// 3. 只读契约:
///    传入的 ByteChunk 为只读视图.
///    ByteIndexer **严禁** 修改 ByteChunk 的底层内容.
///    违反该约束属于未定义行为, 可能导致数据损坏或不可预期结果.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteIndexer {

    /// - 匹配成功后, 实现需重置状态, 以便能够直接进行下次匹配.
    /// - 可以不处理 空匹配模式 的边界情况 (上层会根据 patternLength == 0 直接走快速路径), 但要保证 patternLength 实现正确.
    /// - 为了防止 异常冲突 indexOf 不允许抛出任何易混淆异常 如 [ScxInputException], [InputAlreadyClosedException].
    ///
    /// @param chunk 一个稳定只读数据块视图. 该 ByteChunk 可在 indexOf 返回后继续使用, 但严禁修改其底层内容.
    StatusByteMatchResult indexOf(ByteChunk chunk);

    /// 是否为空匹配模式
    /// - 对于固定长度模式串: 返回 是否是空模式串
    /// - 对于无法静态确定的可变长度模式串 (如正则), 返回 false.
    /// - 为了防止 异常冲突 isEmptyPattern 不允许抛出任何易混淆异常 如 [ScxInputException], [InputAlreadyClosedException].
    boolean isEmptyPattern();

    /// 重置匹配状态
    void reset();

}
