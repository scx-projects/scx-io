package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;

/// ByteSupplier
///
/// - 它负责从某个上游来源 (如 InputStream, Decoder, File, 甚至另一个 ByteSupplier) 中,
///   执行 "提取 / 缓冲 / 解码 / 转换 / 分帧 / 拆包" 等行为, 并以统一格式输出 ByteChunk.
///
/// - 本质是 数据的生产者 (也可称为转换器) + 资源持有者.
/// - 注意和 ByteInput 的语义区分, 这个类在语义上更类似于一个 InputStream.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteSupplier extends AutoCloseable {

    /// 获取下一个数据块
    ///
    /// - 返回的 ByteChunk 所覆盖的字节内容(区间)在后续不得被修改或覆写.
    /// - 必须是推进式的阻塞拉取
    /// - 如果当前已经读取到有效数据 -> 返回 ByteChunk(len > 0)
    /// - 如果暂时无法输出数据, 但 I/O 状态确实推进了 -> 返回 [ByteChunk#EMPTY_BYTE_CHUNK]
    /// - 如果 EOF 且不会再产生数据 -> 返回 null
    /// - 不允许无状态的空循环: 供应者不得无意义地连续返回 [ByteChunk#EMPTY_BYTE_CHUNK]
    /// - 连续多次调用 get() 必须使底层数据源或解码状态单调推进
    /// - 为了防止 异常冲突 get 不允许抛出任何易混淆异常 如 [InputAlreadyClosedException].
    ByteChunk get() throws ScxInputException;

    /// - 如果持有底层资源, 在此方法释放资源
    /// - 注意这里和 ByteInput 的 close 语义不同, 作为最底层的 数据提供器, 此处需要允许多次调用 (幂等)
    /// - 为了防止 异常冲突 close 不允许抛出任何易混淆异常 如 [InputAlreadyClosedException].
    @Override
    default void close() throws ScxInputException {

    }

}
