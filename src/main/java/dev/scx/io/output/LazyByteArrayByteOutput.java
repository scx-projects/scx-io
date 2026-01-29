package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.consumer.LazyByteArrayByteConsumer;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// LazyByteArrayByteOutput
///
/// 这里直接借用 [LazyByteArrayByteConsumer] 来实现
///
/// @author scx567888
/// @version 0.0.1
public final class LazyByteArrayByteOutput extends AbstractByteOutput {

    private final LazyByteArrayByteConsumer byteConsumer;

    public LazyByteArrayByteOutput() {
        this.byteConsumer = new LazyByteArrayByteConsumer();
    }

    @Override
    public void write(byte b) throws OutputAlreadyClosedException {
        ensureOpen();

        // 注意: 此处直接 new.
        // 不要尝试复用单字节 ByteChunk; 在现代 JVM 上短生命周期对象通常可被优化消除,
        // 复用共享可变实例反而会限制 JIT 优化, 可能更慢.
        byteConsumer.accept(ByteChunk.of(new byte[]{b}));
    }

    @Override
    public void write(ByteChunk b) throws OutputAlreadyClosedException {
        ensureOpen();

        byteConsumer.accept(b);
    }

    @Override
    public void flush() throws OutputAlreadyClosedException {
        ensureOpen();
    }

    @Override
    public void close() throws OutputAlreadyClosedException {
        ensureOpen();

        closed = true;
    }

    public byte[] bytes() {
        return byteConsumer.bytes();
    }

    public int size() {
        return byteConsumer.size();
    }

}
