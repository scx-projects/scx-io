package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// ByteConsumer
///
/// 表示一个用于消费由 ByteInput 产生的 ByteChunk 的读侧回调接口.
///
/// 语义说明:
///
/// 1. ByteConsumer 仅用于 ByteInput 读取链路,
///    它不是 ByteOutput，也不代表 "写入目的地".
///
/// 2. 实现方 **允许** 在 accept 返回后继续使用或保存传入的 ByteChunk
///    (例如延迟处理, 聚合, 索引, 缓存等).
///    因此, ByteConsumer 具有 retaining 语义.
///
/// 3. 调用 accept 的一方必须保证:
///    在 consumer 可能使用期间, 传入的 ByteChunk 所引用的底层 byte[]
///    不会被覆写或复用;
///    若无法保证该稳定性, 调用方必须在调用 accept 之前自行拷贝数据.
///
/// 4. accept 方法返回值表示是否需要继续提供更多数据:
///    - 返回 true  表示 consumer 仍需要更多数据;
///    - 返回 false 表示 consumer 已满足需求, 读取过程可提前终止.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteConsumer {

    /// 接收一个 ByteChunk 以供消费.
    ///
    /// @param chunk 由 ByteInput 提供的稳定数据块视图.
    /// @return needMore 是否需要继续提供更多数据.
    /// @throws Throwable consumer 内部抛出的异常将由调用方包装并重新抛出.
    boolean accept(ByteChunk chunk) throws Throwable;

}
