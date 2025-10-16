package cool.scx.io;

import cool.scx.io.consumer.ByteArrayByteConsumer;
import cool.scx.io.consumer.ByteConsumer;
import cool.scx.io.consumer.SkipByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.ByteIndexer;
import cool.scx.io.indexer.KMPByteIndexer;
import cool.scx.io.indexer.SingleByteIndexer;

/// ByteInput
///
/// 方法分为两大类, 动作方法/请求方法.
///
/// - 动作方法 read / readUpTo / readFully 以及相关的 peek, skip 镜像方法.
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
/// - 请求方法 readAll / peekAll / skipAll.
///   - 和动作方法唯一的不同在于, 请求方法的调用者一般只关心结果 而不是流的结束状态, 所以即使处于 EOF 状态 也会宽松的返回空数组.
///
/// @author scx567888
/// @version 0.0.1
public interface ByteInput extends AutoCloseable {

    /// 读取单个字节
    /// - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    byte read() throws ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 最多读取 maxLength 个字节, 可能少于 maxLength (即使尚未遇到 EOF, 如底层缓冲区不足)
    /// - 如果 maxLength = 0, 立即返回 (不抛出异常, 即使在 EOF 状态)
    /// - 如果 maxLength > 0, 至少读取 1 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则停止读取
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    <X extends Throwable> void read(ByteConsumer<X> byteConsumer, long maxLength) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 尽量读取 length 个字节, 可能少于 length (仅在 EOF 时发生)
    /// - 如果 length = 0, 立即返回 (不抛出异常, 即使在 EOF 状态)
    /// - 如果 length > 0, 至少读取 1 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则停止读取
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    <X extends Throwable> void readUpTo(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 恰好读取 length 个字节
    /// - 如果 length = 0, 立即返回且不抛出异常 (即使在 EOF 状态)
    /// - 如果 length > 0. 一定读取 length 个字节.
    ///   - 如果 数据不足 (读取过程中遇到 EOF), 则会抛出 NoMoreDataException
    ///   - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException
    <X extends Throwable> void readFully(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 查看字节, 行为参考 [ByteInput#read()]
    byte peek() throws ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 查看字节, 行为参考 [ByteInput#read(ByteConsumer, long)]
    <X extends Throwable> void peek(ByteConsumer<X> byteConsumer, long maxLength) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 查看字节, 行为参考 [ByteInput#readUpTo(ByteConsumer, long)]
    <X extends Throwable> void peekUpTo(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 查看字节, 行为参考 [ByteInput#readFully(ByteConsumer, long)]
    <X extends Throwable> void peekFully(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 在最多 maxLength 个字节中查找匹配 (或直到 EOF).
    /// - 如果 indexer 是 空匹配模式. (意味着没有消费数据的能力和意义)
    ///   - 在任意情况下都返回 0. (恒为 0)
    /// - 如果 indexer 不是 空匹配模式.
    ///   - 如果 maxLength = 0, 抛 NoMatchFoundException (恒 NoMatch, 因为这是不可能完成的任务).
    ///   - 如果 maxLength > 0. (正常逻辑)
    ///     - 如果在边界达成条件内仍未匹配到 (如达到 maxLength限制 或 读取过程中遇到 EOF) 抛出 NoMatchFoundException.
    ///     - 如果 当前没有数据可读 (立即遇到 EOF), 则会抛出 NoMoreDataException.
    long indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException;

    /// 标记当前读取位置.
    /// - 每次调用 mark() 会覆盖上一次的标记 (即不支持嵌套 mark)
    void mark() throws AlreadyClosedException;

    /// 将读取位置恢复到上一次 mark() 时的状态.
    /// - 若尚未调用 mark(), 则无效果.
    /// - 调用 reset() 不会清除标记, 可重复 reset().
    void reset() throws AlreadyClosedException;

    boolean isClosed();

    void close() throws ScxIOException;

    default byte[] read(int maxLength) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        read(consumer, maxLength);
        return consumer.bytes();
    }

    default byte[] readUpTo(int length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        readUpTo(consumer, length);
        return consumer.bytes();
    }

    default byte[] readFully(int length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        readFully(consumer, length);
        return consumer.bytes();
    }

    default byte[] readAll() throws ScxIOException, AlreadyClosedException {
        var consumer = new ByteArrayByteConsumer();
        readAll(consumer);
        return consumer.bytes();
    }

    default <X extends Throwable> void readAll(ByteConsumer<X> byteConsumer) throws X, ScxIOException, AlreadyClosedException {
        try {
            this.readUpTo(byteConsumer, Long.MAX_VALUE);
        } catch (NoMoreDataException _) {

        }
    }

    default byte[] peek(int maxLength) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        peek(consumer, maxLength);
        return consumer.bytes();
    }

    default byte[] peekUpTo(int length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        peekUpTo(consumer, length);
        return consumer.bytes();
    }

    default byte[] peekFully(int length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new ByteArrayByteConsumer();
        peekFully(consumer, length);
        return consumer.bytes();
    }

    default byte[] peekAll() throws ScxIOException, AlreadyClosedException {
        var consumer = new ByteArrayByteConsumer();
        peekAll(consumer);
        return consumer.bytes();
    }

    default <X extends Throwable> void peekAll(ByteConsumer<X> byteConsumer) throws X, ScxIOException, AlreadyClosedException {
        try {
            peekUpTo(byteConsumer, Long.MAX_VALUE);
        } catch (NoMoreDataException _) {

        }
    }

    default long skip(long length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new SkipByteConsumer();
        read(consumer, length);
        return consumer.bytesSkipped();
    }

    default long skipUpTo(long length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new SkipByteConsumer();
        this.readUpTo(consumer, length);
        return consumer.bytesSkipped();
    }

    default long skipFully(long length) throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        var consumer = new SkipByteConsumer();
        readFully(consumer, length);
        return consumer.bytesSkipped();
    }

    default long skipAll() throws ScxIOException, AlreadyClosedException {
        var consumer = new SkipByteConsumer();
        readAll(consumer);
        return consumer.bytesSkipped();
    }

    default long indexOf(byte b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return indexOf(b, Long.MAX_VALUE);
    }

    default long indexOf(byte b, long maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return indexOf(new SingleByteIndexer(b), maxLength);
    }

    default long indexOf(byte[] b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return indexOf(b, Long.MAX_VALUE);
    }

    default long indexOf(byte[] b, long maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return indexOf(new KMPByteIndexer(b), maxLength);
    }

    default byte[] readUntil(byte b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return readUntil(b, Integer.MAX_VALUE);
    }

    default byte[] readUntil(byte b, int maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        var index = indexOf(b, maxLength);
        var bytes = readFully((int) index);
        skipFully(1);
        return bytes;
    }

    default byte[] readUntil(byte[] b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return readUntil(b, Integer.MAX_VALUE);
    }

    default byte[] readUntil(byte[] b, int maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        var index = indexOf(b, maxLength);
        var bytes = readFully((int) index);
        skipFully(b.length);
        return bytes;
    }

    default byte[] peekUntil(byte b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return peekUntil(b, Integer.MAX_VALUE);
    }

    default byte[] peekUntil(byte b, int maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        var index = indexOf(b, maxLength);
        return peekFully((int) index);
    }

    default byte[] peekUntil(byte[] b) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        return peekUntil(b, Integer.MAX_VALUE);
    }

    default byte[] peekUntil(byte[] b, int maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        var index = indexOf(b, maxLength);
        return peekFully((int) index);
    }

}
