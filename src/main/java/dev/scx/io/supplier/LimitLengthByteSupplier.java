package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.consumer.ByteChunkByteConsumer;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;

/// LimitLengthByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class LimitLengthByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;
    private final ByteChunkByteConsumer consumer;
    private long remaining;

    public LimitLengthByteSupplier(ByteInput byteInput, long maxLength) {
        this.byteInput = byteInput;
        this.consumer = new ByteChunkByteConsumer();
        this.remaining = maxLength;
    }

    @Override
    public ByteChunk get() throws ScxInputException {
        // 读取够了
        if (remaining <= 0) {
            return null;
        }
        try {
            // 这里我们直接引用 原始 byteInput 中的 ByteChunk, 避免了数组的多次拷贝
            byteInput.read(consumer, remaining);// 我们只尝试拉取一次
            var byteChunk = consumer.byteChunk();
            remaining -= byteChunk.length;
            return byteChunk;
        } catch (NoMoreDataException e) {
            // 如果底层 ByteInput 没数据了, 也返回 null
            return null;
        } catch (InputAlreadyClosedException e) {
            // 降级为 ScxInputException 因为在 LimitLengthByteSupplier 的视角来看 就是 输入异常.
            throw new ScxInputException("byteInput already closed", e);
        }
    }

    @Override
    public void close() throws ScxInputException {
        try {
            this.byteInput.close();
        } catch (InputAlreadyClosedException _) {
            // 忽略异常 保证幂等
        }
    }

    public ByteInput byteInput() {
        return byteInput;
    }

}
