package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;

/// ByteSupplier
///
/// - 它负责从某个上游来源 (如 InputStream, Decoder, File, 甚至另一个 ByteSupplier) 中,
///   执行 "提取 / 缓冲 / 解码 / 转换 / 分帧 / 拆包" 等行为, 并以统一格式输出 ByteChunk.
///
/// - 本质是 数据的生产者 (也可视为转换器) + 资源持有者.
///
/// - ByteSupplier 返回的所有 ByteChunk **均为只读视图**.
///   调用者 **严禁** 修改 ByteChunk 所引用的底层 byte[] 内容.
///   违反该约束属于未定义行为, 可能导致缓存回放, 视图共享或流水线处理中的数据损坏.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteSupplier extends AutoCloseable {

    /// 获取下一个数据块
    ///
    /// get() 提供 owned 语义, 用于缓存/异步/跨线程/保存到集合等场景.
    /// 其返回值一直有效. 但仍为只读视图 (owned != 可写所有权转移)
    /// 实现不得在后续调用中修改返回区间内容.
    ///
    /// - 必须是推进式的阻塞拉取, 每次调用必须消耗上游输入或推进 I/O 状态, 不允许无意义空转.
    /// - 返回 ByteChunk 允许 length == 0.
    /// - 如果 EOF -> 返回 null.
    /// - 为了防止 异常冲突 get 不允许抛出任何易混淆异常 如 [InputAlreadyClosedException].
    ByteChunk get() throws ScxInputException;

    /// 借用下一个数据块
    ///
    /// borrow() 提供 borrowed 快路, 用于立即消费/流水线处理.
    /// 其返回值仅保证在下一次 get/borrow 之前有效. 且仍为只读视图 (borrowed != 可写所有权转移)
    /// 实现可复用底层缓冲以避免重复分配.
    ///
    /// - 必须是推进式的阻塞拉取, 每次调用必须消耗上游输入或推进 I/O 状态, 不允许无意义空转.
    /// - 返回 ByteChunk 允许 length == 0.
    /// - 如果 EOF -> 返回 null.
    /// - 为了防止 异常冲突 borrow 不允许抛出任何易混淆异常 如 [InputAlreadyClosedException].
    default ByteChunk borrow() throws ScxInputException {
        // 默认退化为 get();
        return get();
    }

    /// - 如果持有底层资源, 在此方法释放资源.
    /// - 此方法为幂等.
    /// - 为了防止 异常冲突 close 不允许抛出任何易混淆异常 如 [InputAlreadyClosedException].
    /// - close 之后继续调用 get/borrow 是未定义行为.
    @Override
    default void close() throws ScxInputException {

    }

}
