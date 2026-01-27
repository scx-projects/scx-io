package dev.scx.io;

import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// ByteOutput
///
/// - 注意 : ByteOutput 的 close() 为非幂等操作, 重复 close 将抛 [OutputAlreadyClosedException].
///
/// @author scx567888
/// @version 0.0.1
public interface ByteOutput extends AutoCloseable {

    void write(byte b) throws ScxOutputException, OutputAlreadyClosedException;

    void write(ByteChunk b) throws ScxOutputException, OutputAlreadyClosedException;

    void flush() throws ScxOutputException, OutputAlreadyClosedException;

    boolean isClosed();

    /// 关闭此流, 并执行与关闭相关的所有必要行为.
    ///
    /// close() 不只是单纯的关闭操作:
    /// 在某些实现中, close() 可能触发额外的有意义动作, 例如:
    /// - 刷新缓冲区或输出最终数据
    /// - 发送或接收协议结束标志
    /// - 写入校验、尾块或元数据
    /// - 提交事务性状态
    /// - 释放底层系统资源
    ///
    /// 正因为 close() 可能包含这些只能发生一次的行为, 所以此处 close() 设计为一次性操作，而非幂等操作.
    /// 若流已关闭, 重复调用 close() 属非法调用, 将抛出 [OutputAlreadyClosedException].
    /// 若流关闭时发生异常, 将抛出 [ScxOutputException]. 但此时不应该改变 isClosed 标识状态.
    void close() throws ScxOutputException, OutputAlreadyClosedException;

    default void write(byte[] b) throws ScxOutputException, OutputAlreadyClosedException {
        write(ByteChunk.of(b));
    }

    default void write(byte[] b, int off, int len) throws ScxOutputException, OutputAlreadyClosedException {
        write(ByteChunk.of(b, off, off + len));
    }

}
