package dev.scx.io;

import dev.scx.exception.ScxWrappedException;
import dev.scx.io.consumer.ByteConsumer;
import dev.scx.io.consumer.LazyByteArrayByteConsumer;
import dev.scx.io.consumer.SkipByteConsumer;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMatchFoundException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.indexer.ByteIndexer;
import dev.scx.io.indexer.KMPByteIndexer;
import dev.scx.io.indexer.SingleByteIndexer;

/// ByteInput
///
/// ByteInput 提供两类使用层级:
///
/// 1) 返回 byte[] 的方法 (read(int) / readFully(int) / readAll() 等)
///    - 语义为 "值" : 返回数组为调用者独占(owned)的结果数据.
///    - 调用者可自由修改返回数组; 修改不会影响 ByteInput 的内部状态与后续读取结果.
///
/// 2) 接收 [ByteConsumer] 的方法(read(ByteConsumer,...)/peek(ByteConsumer,...) 等)
///    - 语义为 "视图流式消费" : ByteInput 将内部数据块以 ByteChunk 视图形式交付给 consumer.
///    - 该类方法属于高级接口:适用于协议解析, 低分配/低拷贝处理, 索引/搜索, 以及 "提前中断读取" 等场景.
///    - 注意: consumer 接收到的 ByteChunk 本质上相当于 ByteInput 内部的数据视图, 使用限制 详见 [ByteConsumer].
///
/// 方法分为两大类, 动作方法/请求方法.
///
/// - 动作方法 read / readUpTo / readFully 以及相关的 peek, skip, transferTo 镜像方法.
///   - 当 maxLength / length = 0 时, 我们将其看作一种无动作, 均采用统一策略, 立即返回, 不抛异常, 无关流的状态 (即使流以及结束).
///   - 在 maxLength / length > 0 时, 读取操作必须产生有效数据 (至少读取一个字节), 这样保证动作不会成为空操作 并消除了 "0 字节读取歧义".
///   - EOF (流结束) 被设计为一种明确的信号, 当无法再读取任何字节时, 会抛出 [NoMoreDataException]. 这保证循环读取的安全性和逻辑自洽性. 同时消除了 EOF 表达性歧义.
///   - 严格契约保证用户可以安全循环读取, 同时明确何时流结束.
///   - 方法对读取的容忍度分成三个级别:
///     - [#read(ByteConsumer,long)] — 宽松 (可能少于指定长度)
///     - [#readUpTo(ByteConsumer,long)] — 中等 (尽量读取指定长度)
///     - [#readFully(ByteConsumer,long)] — 严格 (必须读取指定长度)
///
/// - 动作方法 indexOf.
///   - 空匹配模式的 indexOf 看作一种无动作 (因其事实上可以匹配任何数据), 恒返回 0.
///
/// - 请求方法 readAll / peekAll / skipAll / transferToAll .
///   - 和动作方法唯一的不同在于, 请求方法的调用者一般只关心结果 而不是流的结束状态, 所以即使处于 EOF 状态 也会宽松的返回结果 (空数组 或 0 等).
///
/// - 注意 1 : ByteInput 的 close() 为非幂等操作, 重复 close 将抛 [InputAlreadyClosedException].
/// - 注意 2 : 支持传入 byteConsumer 的方法 (如 read, peek) 中. 如果 byteConsumer 本身发生了异常 则会被包装为 [dev.scx.exception.ScxWrappedException] 并抛出.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteInput extends AutoCloseable {

    /// 读取单个字节
    /// - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    byte read() throws NoMoreDataException, ScxInputException, InputAlreadyClosedException;

    /// 最多读取 maxLength 个字节, 可能少于 maxLength (即使尚未遇到 EOF, 如底层缓冲区不足)
    /// - 如果 maxLength = 0, 立即返回 (不抛出异常, 即使在 EOF 状态)
    /// - 如果 maxLength > 0, 至少读取 1 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则停止读取
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    void read(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 尽量读取 length 个字节, 可能少于 length (仅在 EOF 时发生)
    /// - 如果 length = 0, 立即返回 (不抛出异常, 即使在 EOF 状态)
    /// - 如果 length > 0, 至少读取 1 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则停止读取
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    void readUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 恰好读取 length 个字节
    /// - 如果 length = 0, 立即返回且不抛出异常 (即使在 EOF 状态)
    /// - 如果 length > 0. 一定读取 length 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则会抛出 NoMoreDataException
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    void readFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 查看字节, 行为参考 [ByteInput#read()]
    byte peek() throws NoMoreDataException, ScxInputException, InputAlreadyClosedException;

    /// 查看字节, 行为参考 [ByteInput#read(ByteConsumer, long)]
    void peek(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 查看字节, 行为参考 [ByteInput#readUpTo(ByteConsumer, long)]
    void peekUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 查看字节, 行为参考 [ByteInput#readFully(ByteConsumer, long)]
    void peekFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException;

    /// 在最多 maxLength 个字节中查找匹配 (或直到 EOF).
    /// - 如果 indexer 是 空匹配模式. (意味着没有消费数据的能力和意义)
    ///   - 在任意情况下都返回 0. (恒为 0)
    /// - 如果 indexer 不是 空匹配模式.
    ///   - 如果 maxLength = 0, 抛 NoMatchFoundException (恒 NoMatch, 因为这是不可能完成的任务).
    ///   - 如果 maxLength > 0. (正常逻辑)
    ///     - 如果在边界达成条件内仍未匹配到 (如达到 maxLength限制 或 读取过程中遇到 EOF) 抛出 NoMatchFoundException.
    ///     - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException.
    ByteMatchResult indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException;

    /// 在当前读取位置创建一个标记对象.
    ByteInputMark mark() throws InputAlreadyClosedException;

    /// 检测当前流是否关闭
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
    /// 若流已关闭, 重复调用 close() 属非法调用, 将抛出 [InputAlreadyClosedException].
    /// 若流关闭时发生异常, 将抛出 [ScxInputException]. 但此时不应该改变 isClosed 标识状态.
    void close() throws ScxInputException, InputAlreadyClosedException;

    /// 返回值语义:
    /// - 本方法返回的 byte[] 为调用者独占 (owned) 的结果数组.
    /// - 调用者可自由修改该数组; 修改不会影响 ByteInput 的内部状态与后续读取结果.
    /// 注意 : 内置的 [LazyByteArrayByteConsumer] 不会抛出任何异常, 所以 此处方法签名 省略 ScxWrappedException. 其余方法同样参考此说明
    default byte[] read(int maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        read(consumer, maxLength);
        return consumer.bytes();
    }

    default byte[] readUpTo(int length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        readUpTo(consumer, length);
        return consumer.bytes();
    }

    default byte[] readFully(int length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        readFully(consumer, length);
        return consumer.bytes();
    }

    default byte[] readAll() throws ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        readAll(consumer);
        return consumer.bytes();
    }

    default void readAll(ByteConsumer byteConsumer) throws ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        try {
            readUpTo(byteConsumer, Long.MAX_VALUE);
        } catch (NoMoreDataException _) {
            // 忽略 EOF
        }
    }

    default byte[] peek(int maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        peek(consumer, maxLength);
        return consumer.bytes();
    }

    default byte[] peekUpTo(int length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        peekUpTo(consumer, length);
        return consumer.bytes();
    }

    default byte[] peekFully(int length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        peekFully(consumer, length);
        return consumer.bytes();
    }

    default byte[] peekAll() throws ScxInputException, InputAlreadyClosedException {
        var consumer = new LazyByteArrayByteConsumer();
        peekAll(consumer);
        return consumer.bytes();
    }

    default void peekAll(ByteConsumer byteConsumer) throws ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        try {
            peekUpTo(byteConsumer, Long.MAX_VALUE);
        } catch (NoMoreDataException _) {
            // 忽略 EOF
        }
    }

    default long skip(long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new SkipByteConsumer();
        read(consumer, maxLength);
        return consumer.bytesSkipped();
    }

    default long skipUpTo(long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new SkipByteConsumer();
        readUpTo(consumer, length);
        return consumer.bytesSkipped();
    }

    default long skipFully(long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var consumer = new SkipByteConsumer();
        readFully(consumer, length);
        return consumer.bytesSkipped();
    }

    default long skipAll() throws ScxInputException, InputAlreadyClosedException {
        var consumer = new SkipByteConsumer();
        readAll(consumer);
        return consumer.bytesSkipped();
    }

    default ByteMatchResult indexOf(ByteIndexer byteIndexer) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return indexOf(byteIndexer, Long.MAX_VALUE);
    }

    default ByteMatchResult indexOf(byte b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return indexOf(b, Long.MAX_VALUE);
    }

    default ByteMatchResult indexOf(byte b, long maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return indexOf(new SingleByteIndexer(b), maxLength);
    }

    default ByteMatchResult indexOf(byte[] b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return indexOf(b, Long.MAX_VALUE);
    }

    default ByteMatchResult indexOf(byte[] b, long maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return indexOf(new KMPByteIndexer(b), maxLength);
    }

    default byte[] readUntil(ByteIndexer byteIndexer) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return readUntil(byteIndexer, Integer.MAX_VALUE);
    }

    /// 从当前 ByteInput 中读取数据直到 ByteIndexer 成功匹配.
    ///
    /// - 返回的数据 不包含模式串.
    /// - 方法调用结束后, ByteInput 的读取指针会跳过模式串的长度, 即下一次读取从模式串之后开始.
    default byte[] readUntil(ByteIndexer byteIndexer, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var indexMatchResult = indexOf(byteIndexer, maxLength);
        var bytes = readFully((int) indexMatchResult.index);
        skipFully(indexMatchResult.matchedLength);
        return bytes;
    }

    default byte[] readUntil(byte b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return readUntil(b, Integer.MAX_VALUE);
    }

    default byte[] readUntil(byte b, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return readUntil(new SingleByteIndexer(b), maxLength);
    }

    default byte[] readUntil(byte[] b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return readUntil(b, Integer.MAX_VALUE);
    }

    default byte[] readUntil(byte[] b, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return readUntil(new KMPByteIndexer(b), maxLength);
    }

    default byte[] peekUntil(ByteIndexer byteIndexer) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return peekUntil(byteIndexer, Integer.MAX_VALUE);
    }

    default byte[] peekUntil(ByteIndexer byteIndexer, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var indexMatchResult = indexOf(byteIndexer, maxLength);
        return peekFully((int) indexMatchResult.index);
    }

    default byte[] peekUntil(byte b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return peekUntil(b, Integer.MAX_VALUE);
    }

    default byte[] peekUntil(byte b, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return peekUntil(new SingleByteIndexer(b), maxLength);
    }

    default byte[] peekUntil(byte[] b) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return peekUntil(b, Integer.MAX_VALUE);
    }

    default byte[] peekUntil(byte[] b, int maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        return peekUntil(new KMPByteIndexer(b), maxLength);
    }

}
