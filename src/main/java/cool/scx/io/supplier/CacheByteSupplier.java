package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteInput;
import cool.scx.io.consumer.ByteChunkByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;

import java.util.ArrayList;
import java.util.List;

/// CacheByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class CacheByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;
    private final ByteChunkByteConsumer consumer;
    private final List<ByteChunk> cache;
    private int chunkIndex;
    private boolean isFinish;

    public CacheByteSupplier(ByteInput byteInput) {
        this.byteInput = byteInput;
        this.consumer = new ByteChunkByteConsumer();
        this.cache = new ArrayList<>();
        this.chunkIndex = 0;
        this.isFinish = false;
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException {
        // 1, 如果允许使用缓存, 优先使用缓存
        if (chunkIndex < cache.size()) {
            var chunk = cache.get(chunkIndex);
            chunkIndex = chunkIndex + 1;
            return chunk;
        }

        // 2, 完成了就永远返回 null
        if (isFinish) {
            return null;
        }

        try {
            byteInput.read(consumer, Long.MAX_VALUE);
            var chunk = consumer.byteChunk();
            cache.add(chunk);
            chunkIndex = chunkIndex + 1;
            return chunk;
        } catch (NoMoreDataException e) {
            // 遇到 EOF
            isFinish = true;
            return null;
        }

    }

    @Override
    public void close() throws ScxIOException {
        byteInput.close();
    }

    public void reset() {
        chunkIndex = 0;
    }

}
