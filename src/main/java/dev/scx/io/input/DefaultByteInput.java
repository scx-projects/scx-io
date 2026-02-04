package dev.scx.io.input;

import dev.scx.exception.ScxWrappedException;
import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteInputMark;
import dev.scx.io.ByteMatchResult;
import dev.scx.io.consumer.ByteConsumer;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMatchFoundException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.indexer.ByteIndexer;
import dev.scx.io.supplier.ByteSupplier;

import static dev.scx.io.ByteChunk.EMPTY_BYTE_CHUNK;
import static dev.scx.io.indexer.StatusByteMatchResult.Status.FULL_MATCH;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.min;

/// DefaultByteInput
///
/// @author scx567888
/// @version 0.0.1
public final class DefaultByteInput implements ByteInput {

    private final ByteSupplier byteSupplier;

    private ByteChunkNode head;
    private ByteChunkNode tail;
    private boolean closed;

    public DefaultByteInput(ByteSupplier byteSupplier) {
        this.byteSupplier = byteSupplier;
        this.head = new ByteChunkNode(EMPTY_BYTE_CHUNK);
        this.tail = this.head;
        this.closed = false;
    }

    private void appendByteChunk(ByteChunk byteChunk) {
        tail.next = new ByteChunkNode(byteChunk);
        tail = tail.next;
    }

    /// 从 byteSupplier 中持续拉取直到得到有效数据块
    ///
    /// @return EOF
    private boolean pullByteChunk() throws ScxInputException {
        while (true) {
            var byteChunk = byteSupplier.get();
            if (byteChunk == null) {
                return true; // EOF
            }
            if (byteChunk.length == 0) {
                continue;// 空块 我们视为无动作 继续拉取
            }
            appendByteChunk(byteChunk);
            return false;
        }
    }

    /// 确保 有数据可用 (至少 1 字节)
    ///
    /// @return 调用了几次底层拉取
    private long ensureAvailable() throws NoMoreDataException, ScxInputException {
        var pullCount = 0L;
        // 保证 当前 head 中至少有 1个 字节
        if (head.hasAvailable()) {
            return pullCount;
        }
        if (head.next == null) {
            var eof = this.pullByteChunk();
            if (eof) {
                throw new NoMoreDataException();
            }
            pullCount = pullCount + 1;
        }
        head = head.next;
        return pullCount;
    }

    /// 确保现在是打开状态.
    private void ensureOpen() throws InputAlreadyClosedException {
        if (closed) {
            throw new InputAlreadyClosedException();
        }
    }

    /**
     * 底层 的 read
     *
     * @param consumer     字节消费者
     * @param maxLength    希望读取的字节数
     * @param movePointer  是否移动指针
     * @param maxPullCount 最大允许调用底层 ByteSupplier 的次数 (只对 非空块计数)
     * @param throwOnEOF   当遇到 EOF (底层数据源结束), 导致实际读取字节数不足时 是否抛异常
     */
    private void read0(ByteConsumer consumer, long maxLength, boolean movePointer, long maxPullCount, boolean throwOnEOF) throws NoMoreDataException, ScxInputException, ScxWrappedException {

        var remaining = maxLength; // 剩余需要读取的字节数

        var n = head; // 用于循环的节点
        var pullCount = 0L; // 拉取次数计数器

        // 循环中有 4 种情况
        // 1, 已经读取到足够的数据 我们无需循环
        // 2, 消费者返回 false 我们会至少循环一次
        // 3, 达到最大拉取次数 我们会至少循环一次
        // 4, 没有更多数据了 我们会至少循环一次

        // 初始只判断是否 已经读取到足够的数据
        while (remaining > 0) {

            // 计算当前节点中可读取的最大长度, (这里因为是将 int 和 long 值进行最小值比较 所以返回值一定是 int 所以类型转换不会丢失精度)
            var length = (int) min(n.available(), remaining);

            // 调用消费者 写入数据
            boolean needMore;
            try {
                needMore = consumer.accept(n.chunk.subChunk(n.position, n.position + length));
            } catch (Throwable e) {
                // consumer 中的异常需要包装成 ScxWrappedException
                throw new ScxWrappedException(e);
            }

            // 计算剩余字节数
            remaining -= length;

            if (movePointer) {
                // 移动当前节点的指针位置
                n.position += length;
            }

            // 数据已经读取够 或者 无需继续读取了 我们直接跳出循环
            if (remaining <= 0 || !needMore) {
                break;
            }

            // 当走到这里时 说明 remaining 一定大于 0,
            // 而从 remaining 和 length 的计算方式得出, 此时 n 一定已经被彻底消耗掉了
            // 所以我们可以放心的直接更新到下一节点 即可

            if (n.next == null) {
                //已经达到最大拉取次数 直接退出
                if (pullCount >= maxPullCount) {
                    break;
                }
                // 如果 当前节点没有下一个节点 并且拉取失败 则退出循环
                var eof = this.pullByteChunk();
                // 数据不足
                if (eof) {
                    // 不允许 中途 eof
                    if (throwOnEOF) {
                        throw new NoMoreDataException();
                    } else {
                        // 直接退出
                        break;
                    }
                }
                pullCount = pullCount + 1;
            }
            n = n.next;

            // 更新 头节点
            if (movePointer) {
                head = n;
            }

        }

    }

    private ByteMatchResult indexOf0(ByteIndexer indexer, long maxLength, long maxPullCount) throws NoMatchFoundException, ScxInputException {

        var index = 0L; // 主串索引

        var n = head;
        var pullCount = 0L; // 拉取次数计数器

        // 初始只判断索引是否 达到最大长度
        while (index < maxLength) {
            // 计算当前节点中可读取的最大长度, 确保不超过 max (这里因为是将 int 和 long 值进行最小值比较 所以返回值一定是 int 所以类型转换不会丢失精度)
            var length = (int) min(n.available(), maxLength - index);
            var indexMatchResult = indexer.indexOf(n.chunk.subChunk(n.position, n.position + length));
            // 此处因为支持回溯匹配 所以可能是负数 NO_MATCH 表示真正未找到
            if (indexMatchResult.status == FULL_MATCH) {
                return new ByteMatchResult(index + indexMatchResult.index, indexMatchResult.matchedLength);
            }

            index += length;

            // 检查是否已达到最大长度
            if (index >= maxLength) {
                break;
            }

            // 如果 currentNode 没有下一个节点并且尝试拉取数据失败, 直接退出循环
            if (n.next == null) {
                if (pullCount >= maxPullCount) {
                    break;
                }
                var eof = this.pullByteChunk();
                // 数据不足
                if (eof) {
                    // 直接退出
                    break;
                }
                pullCount = pullCount + 1;
            }
            n = n.next;

        }

        throw new NoMatchFoundException();
    }

    @Override
    public byte read() throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        ensureOpen();// 确保 open

        ensureAvailable();// 确保 有可用数据

        var b = head.chunk.get(head.position);
        head.position = head.position + 1;
        return b;
    }

    @Override
    public void read(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        var pulledCount = 0L;
        if (maxLength > 0) {
            pulledCount = ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, maxLength, true, 1 - pulledCount, false);
    }

    @Override
    public void readUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        if (length > 0) {
            ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, length, true, MAX_VALUE, false);
    }

    @Override
    public void readFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        if (length > 0) {
            ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, length, true, MAX_VALUE, true);
    }

    @Override
    public byte peek() throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        ensureOpen();// 确保 open

        ensureAvailable();// 确保 有可用数据

        return head.chunk.get(head.position);
    }

    @Override
    public void peek(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        var pulledCount = 0L;
        if (maxLength > 0) {
            pulledCount = ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, maxLength, false, 1 - pulledCount, false);
    }

    @Override
    public void peekUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        if (length > 0) {
            ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, length, false, MAX_VALUE, false);
    }

    @Override
    public void peekFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxWrappedException {
        ensureOpen();// 确保 open

        if (length > 0) {
            ensureAvailable();// 确保 有可用数据
        }

        read0(byteConsumer, length, false, MAX_VALUE, true);
    }

    @Override
    public ByteMatchResult indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        ensureOpen();// 确保 open

        if (indexer.isEmptyPattern()) {
            return new ByteMatchResult(0, 0);
        }

        if (maxLength > 0) {
            ensureAvailable();// 确保 有可用数据
        }

        return indexOf0(indexer, maxLength, MAX_VALUE);
    }

    @Override
    public ByteInputMark mark() throws InputAlreadyClosedException {
        ensureOpen();// 确保 open

        return new DefaultByteInputMark(this, head, head.position);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxInputException, InputAlreadyClosedException {
        ensureOpen();

        byteSupplier.close();
        closed = true; // 只有成功关闭才算作 关闭
    }

    public ByteSupplier byteSupplier() {
        return byteSupplier;
    }

    private static class ByteChunkNode {

        public final ByteChunk chunk;
        /// 相对 索引 0 起始
        public int position;
        public ByteChunkNode next;

        public ByteChunkNode(ByteChunk chunk) {
            this.chunk = chunk;
            this.position = 0;
            this.next = null;
        }

        public int available() {
            return chunk.length - position;
        }

        public boolean hasAvailable() {
            return position < chunk.length;
        }

        public void reset() {
            position = 0;
        }

        /// 默认使用平台编码, 仅建议用于调试
        @Override
        public String toString() {
            return new String(chunk.bytes, chunk.start + position, chunk.length - position);
        }

    }

    private record DefaultByteInputMark(
        DefaultByteInput defaultByteInput,
        ByteChunkNode markNode,
        int markPosition
    ) implements ByteInputMark {

        @Override
        public void reset() throws InputAlreadyClosedException {
            defaultByteInput.ensureOpen();// 确保 open

            //重置当前 mark
            defaultByteInput.head = markNode;
            defaultByteInput.head.position = markPosition;
            //后续节点全部重置
            var n = defaultByteInput.head.next;
            while (n != null) {
                n.reset();
                n = n.next;
            }
        }

    }

}
