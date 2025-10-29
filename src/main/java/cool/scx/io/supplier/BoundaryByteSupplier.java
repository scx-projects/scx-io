package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteInput;
import cool.scx.io.consumer.ByteChunkByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.BitMaskByteIndexer;
import cool.scx.io.indexer.ByteIndexer;

import java.util.LinkedList;

import static cool.scx.io.ByteChunk.EMPTY_CHUNK;
import static cool.scx.io.indexer.ByteIndexer.NO_MATCH;

/// BoundaryByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class BoundaryByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;
    private final ByteIndexer byteIndexer;
    private final boolean autoClose;
    private final ByteChunkByteConsumer consumer;
    private final LinkedList<ByteChunk> cache;
    private boolean useCache;
    private boolean isFinish;

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes) {
        this(byteInput, boundaryBytes, false);
    }

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes, boolean autoClose) {
        this.byteInput = byteInput;
        this.byteIndexer = new BitMaskByteIndexer(boundaryBytes);
        this.autoClose = autoClose;
        this.consumer = new ByteChunkByteConsumer();
        this.cache = new LinkedList<>();
        this.useCache = false;
        this.isFinish = this.byteIndexer.patternLength() == 0;
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException, NoMoreDataException {
        // 1, 如果允许使用缓存, 优先使用缓存
        if (useCache) {
            var cacheChunk = cache.pollFirst();
            if (cacheChunk != null) {
                return cacheChunk;
            }
            // 缓存中已经没有数据, 切换状态
            useCache = false;
        }

        // 2, 完成了就永远返回 null
        if (isFinish) {
            return null;
        }

        // 3, 尝试 peek 一个分块
        try {
            byteInput.peek(consumer, Long.MAX_VALUE);
        } catch (NoMoreDataException e) {
            // 遇到 EOF
            isFinish = true;
            // 缓存中也没有数据, 返回 null
            if (cache.isEmpty()) {
                return null;
            } else {
                // 处于半匹配状态, 将缓存数据作为有效数据返回
                useCache = true;
                return cache.pollFirst();
            }
        }

        // 4, 读取当前块
        var byteChunk = consumer.byteChunk();

        // 5, 计算 索引
        var i = byteIndexer.indexOf(byteChunk);

        // 6, 匹配到了 应该终结
        if (i != NO_MATCH) {
            // 计算针对当前块来说的 安全索引.
            var safeLength = i + byteIndexer.patternLength();
            // 按照常规流程, 这里的 skipFully 只可能读取缓冲区中的数据, 也就是说理论上不可能出现 NoMoreDataException.
            // 但如果真的出现了 NoMoreDataException, 则说明是其他情况导致的, 比如外部在 另一线程中 读取了 byteInput 等.
            // 针对这种预计之外的异常, 这里直接抛出即可
            byteInput.skipFully(safeLength);

            // 标识不需要在继续读了
            isFinish = true;

            // 缓存中没数据, 直接返回分块
            if (cache.isEmpty()) {
                // 根据方法行为设定, 返回的块不应包含 分隔符.
                // 这里既然没有缓存 就说明 当前块中包含了完整的 boundary, 所以直接使用 i 进行 截断是安全的.
                return byteChunk.subChunk(0, i);
            } else {
                // 这里既然有缓存, 就说明当前分块只是包含了部分的 boundary.
                // 所以不能直接使用 i 截断, 而是应该使用 safeLength.
                // 这里虽然本可以 判断当前 分块是否存在 有效数据(不包含 boundary 的数据) 然后 选择性 addLast.
                // 但是为了保证 trimTailBytes 的执行逻辑简单. 这里无论当前分块是否包含 有效数据 都进行添加.
                cache.addLast(byteChunk.subChunk(0, safeLength));
                // 这里需要 移除尾部的 boundary
                trimTailBytes(byteIndexer.patternLength());
                // 允许使用 缓存块
                useCache = true;
                // 返回缓存中的数据
                return cache.pollFirst();
            }
        }

        // 7, 未匹配到, 需要判断是 完全未匹配 还是 部分匹配
        if (byteIndexer.matchedLength() == 0) { // 完全未匹配 表示当前块可以 安全使用
            // 异常相关说明, 参考上面的 byteInput.skipFully(safeLength);
            byteInput.skipFully(byteChunk.length);
            // 如果当前缓存 没有数据直接返回 否则添加到缓存中等待下次 读取
            if (cache.isEmpty()) {
                return byteChunk;
            } else {
                // 有缓存 添加到缓存中
                cache.addLast(byteChunk);
                // 允许使用 缓存块
                useCache = true;
                // 返回缓存中的数据
                return cache.pollFirst();
            }
        } else { // 部分匹配 我们不能确定这个块是安全的 所以先缓存起来 等待下次读取
            // 异常相关说明, 参考上面的 byteInput.skipFully(safeLength);
            byteInput.skipFully(byteChunk.length);
            // 添加到缓存中
            cache.addLast(byteChunk);
            // 因为 不能确定这个块是安全的, 我们这里不允许使用 缓存块
            useCache = false;
            // 返回 EMPTY_CHUNK, 表示暂时无法提供数据
            return EMPTY_CHUNK;
        }

    }

    private void trimTailBytes(int length) {
        while (length > 0) {
            var last = cache.pollLast();
            // 这里 last 理论上不可能是 null, 因为cache 中存储的数据必然 大于 boundary 的长度, 但是还是防御性处理一下
            if (last == null) {
                break;
            }
            if (last.length > length) {
                // 当前块比要移除的多，截取前部分, 再重新放回到最后
                cache.addLast(last.subChunk(0, last.length - length));
                length = 0;
            } else {
                // 整个块都被 boundary 吃掉了
                length -= last.length;
            }
        }
    }

    @Override
    public void close() throws ScxIOException {
        if (autoClose) {
            byteInput.close();
        }
    }

}
