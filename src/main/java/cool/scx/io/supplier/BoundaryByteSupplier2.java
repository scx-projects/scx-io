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

        byteInput.peek(consumer, Long.MAX_VALUE);
        var byteChunk = consumer.byteChunk();
        var i = byteIndexer.indexOf(byteChunk);

        System.out.println(byteChunk);

        if (i != NO_MATCH) {
            var safeLength = i + byteIndexer.pattern().length;
            byteInput.skipFully(safeLength);
            byteChunk = byteChunk.subChunk(0, safeLength);
            isFinish = true;
            if (cache.isEmpty()) {
                return byteChunk;
            } else {
                cache.append(byteChunk);
                useCache = true;
                return EMPTY_CHUNK;
            }
        }

        // 未匹配到, 需要判断是 完全未匹配 还是 部分匹配
        if (byteIndexer.matchedLength() == 0) {
            // 完全未匹配 表示当前块可以 安全使用
            byteInput.skipFully(byteChunk.length);
            // 如果当前缓存 没有数据直接返回 否则添加到缓存中等待下次 读取
            if (cache.isEmpty()) {
                return byteChunk;
            } else {
                cache.append(byteChunk);
                useCache = true;
                return EMPTY_CHUNK;
            }
        } else {
            // 部分匹配 我们不能确定这个块是安全的 所以先缓存起来 等待下次读取
            byteInput.skipFully(byteChunk.length);
            cache.append(byteChunk);
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
