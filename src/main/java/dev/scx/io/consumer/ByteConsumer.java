package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// ByteConsumer
///
/// 一个支持中断的, ByteChunk 消费者接口.
///
/// 语义说明:
///
/// 1. 实现方 **允许** 在 accept 返回后继续使用或保存传入的 ByteChunk
///    (例如延迟处理, 聚合, 索引, 缓存等).
///    因此, ByteConsumer 具有 retaining 语义.
///
/// 2. 调用 accept 的一方必须保证:
///    传入的 ByteChunk 所引用的底层 byte[] 不会被覆写或复用;
///    若无法保证该稳定性, 调用方必须在调用 accept 之前自行拷贝数据.
///
/// 3. 只读契约:
///    传入的 ByteChunk 为只读视图.
///    ByteConsumer **严禁** 修改 ByteChunk 的底层内容.
///    违反该约束属于未定义行为, 可能导致数据损坏或不可预期结果.
///
/// 4. accept 方法返回值表示是否需要继续提供更多数据:
///    - 返回 true  表示 consumer 仍需要更多数据;
///    - 返回 false 表示 consumer 已满足需求, 读取过程可提前终止.
///
/// - 注意: retaining 只意味着引用可继续使用, 不等于拥有(ownership transfer). 即使可保存, 仍不得修改其底层内容.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteConsumer {

    /// 接收一个 ByteChunk 以供消费.
    ///
    /// @param chunk 一个稳定只读数据块视图. 该 ByteChunk 可在 accept 返回后继续使用, 但严禁修改其底层内容.
    /// @return needMore 是否需要继续提供更多数据.
    /// @throws Throwable consumer 内部抛出的异常将由调用方包装并重新抛出.
    boolean accept(ByteChunk chunk) throws Throwable;

}
