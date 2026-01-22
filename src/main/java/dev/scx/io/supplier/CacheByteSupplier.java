package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.consumer.ByteChunkByteConsumer;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;

import java.util.ArrayList;
import java.util.List;

/// CacheByteSupplier
///
/// 可以缓存 byteInput, 调用 reset 可以从头继续使用
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
    public ByteChunk get() throws ScxInputException {
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
        } catch (InputAlreadyClosedException e) {
            // 降级为 ScxInputException 因为在 CacheByteSupplier 的视角来看 就是 输入异常.
            throw new ScxInputException("byteInput already closed", e);
        }

    }

    @Override
    public void close() throws ScxInputException {
        try {
            byteInput.close();
        } catch (InputAlreadyClosedException _) {

        }
    }

    public void reset() {
        chunkIndex = 0;
    }

    public ByteInput byteInput() {
        return byteInput;
    }

}
