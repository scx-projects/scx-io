package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteInput;
import cool.scx.io.consumer.ByteChunkByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;

/// ByteInputByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class ByteInputByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;
    private final ByteChunkByteConsumer consumer;
    private final boolean autoClose;

    public ByteInputByteSupplier(ByteInput byteInput, boolean autoClose) {
        this.byteInput = byteInput;
        this.consumer = new ByteChunkByteConsumer();
        this.autoClose = autoClose;
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException {
        try {
            // 这里我们直接引用 原始 byteInput 中的 ByteChunk, 避免了数组的多次拷贝
            byteInput.read(consumer, Long.MAX_VALUE);// 我们只尝试拉取一次
            return consumer.byteChunk();
        } catch (NoMoreDataException e) {
            // 如果底层 ByteInput 没数据了, 也返回 null
            return null;
        }
    }

    @Override
    public void close() throws ScxIOException {
        if (autoClose) {
            this.byteInput.close();
        }
    }

}
