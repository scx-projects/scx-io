package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteChunkQueue;
import cool.scx.io.ByteInput;
import cool.scx.io.consumer.ByteChunkByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.KMPByteIndexer;

import static cool.scx.io.ByteChunk.EMPTY_CHUNK;
import static cool.scx.io.indexer.ByteIndexer.NO_MATCH;


/// BoundaryByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class BoundaryByteSupplier2 implements ByteSupplier {

    private final ByteInput byteInput;
    private final KMPByteIndexer byteIndexer;
    private final boolean autoClose;
    private final ByteChunkByteConsumer consumer;
    private final ByteChunkQueue cache;
    private boolean useCache;
    private boolean isFinish;

    public BoundaryByteSupplier2(ByteInput byteInput, byte[] boundaryBytes) {
        this(byteInput, boundaryBytes, false);
    }

    public BoundaryByteSupplier2(ByteInput byteInput, byte[] boundaryBytes, boolean autoClose) {
        this.byteInput = byteInput;
        this.byteIndexer = new KMPByteIndexer(boundaryBytes);
        this.autoClose = autoClose;
        this.consumer = new ByteChunkByteConsumer();
        this.cache = new ByteChunkQueue();
        this.useCache = false;
        this.isFinish = this.byteIndexer.isEmptyPattern();
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException, NoMoreDataException {
        // 如果需要使用缓存
        if (useCache) {
            var cacheChunk = cache.next();
            if (cacheChunk != null) {
                return cacheChunk;
            }
            // 缓存中已经没有数据
            useCache = false;
        }

        // 完成了就永远返回 null
        if (isFinish) {
            return null;
        }

        try {
            byteInput.peek(consumer, Long.MAX_VALUE);
        } catch (NoMoreDataException e) {
            isFinish = true;
            return null;
        }

        // 读取当前块
        var byteChunk = consumer.byteChunk();

        // 计算 索引
        var i = byteIndexer.indexOf(byteChunk);

        // 匹配到了 应该终结
        if (i != NO_MATCH) {
            // 计算针对当前块来说的 安全索引
            var safeLength = i + byteIndexer.pattern().length;
            // 这里按照常规流程, 这里的 skip 只可能读取缓冲区中的数据, 也就是说理论上不可能出现 NoMoreDataException.
            // 但如果真的出现了 NoMoreDataException, 则说明是其他情况导致的 比如外部在 另一线程中 读取了 byteInput.
            // 针对这种预计之外的异常, 这里直接抛出即可
            byteInput.skipFully(safeLength);

            isFinish = true;

            if (cache.isEmpty()) {
                // 返回的块不应包含 分隔符
                return byteChunk.subChunk(0, i);
            } else {
                //todo 这里需要 移除尾部的 boundaryBytes
                byteChunk = byteChunk.subChunk(0, safeLength);
                cache.append(byteChunk);
                useCache = true;
                return EMPTY_CHUNK;
            }
        }

        // 未匹配到, 需要判断是 完全未匹配 还是 部分匹配
        if (byteIndexer.matchedLength() == 0) { // 完全未匹配 表示当前块可以 安全使用
            // 异常相关说明, 参考上面分支的 byteInput.skipFully(safeLength);
            byteInput.skipFully(byteChunk.length);
            // 如果当前缓存 没有数据直接返回 否则添加到缓存中等待下次 读取
            if (cache.isEmpty()) {
                return byteChunk;
            } else {
                // 有缓存 添加到缓存中
                cache.append(byteChunk);
                // 允许使用 缓存块
                useCache = true;
                return EMPTY_CHUNK;
            }
        } else { // 部分匹配 我们不能确定这个块是安全的 所以先缓存起来 等待下次读取
            // 异常相关说明, 参考上面分支的 byteInput.skipFully(safeLength);
            byteInput.skipFully(byteChunk.length);
            cache.append(byteChunk);
            // 因为 不能确定这个块是安全的, 我们这里不允许使用 缓存块
            useCache = false;
            return EMPTY_CHUNK;
        }

    }

    @Override
    public void close() throws Exception {
        if (autoClose) {
            byteInput.close();
        }
    }

}
