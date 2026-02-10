package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.consumer.EagerByteArrayByteConsumer;
import dev.scx.io.consumer.LazyByteArrayByteConsumer;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// ByteArrayByteOutput
///
/// 这里直接借用 [EagerByteArrayByteConsumer] 来实现,
/// 不采用 [LazyByteArrayByteConsumer] 是因为 在 ByteOutput 的 write 语义下(立即消费).
/// 传递给 LazyByteArrayByteConsumer 的 ByteChunk 每次都需要 copy,
/// 此时 LazyByteArrayByteConsumer 几乎不存在什么性能优势.
/// 而 EagerByteArrayByteConsumer 本身并不会保存 ByteChunk, 而是直接 copy,
/// 所以可以直接满足 ByteOutput 的 write 语义.
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteOutput implements ByteOutput {

    private final EagerByteArrayByteConsumer byteConsumer;

    private boolean closed;

    public ByteArrayByteOutput() {
        this.byteConsumer = new EagerByteArrayByteConsumer();
        this.closed = false;
    }

    public ByteArrayByteOutput(int size) {
        this.byteConsumer = new EagerByteArrayByteConsumer(size);
        this.closed = false;
    }

    private void ensureOpen() throws OutputAlreadyClosedException {
        if (closed) {
            throw new OutputAlreadyClosedException();
        }
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

        // EagerByteArrayByteConsumer 并不会保留 ByteChunk, 而是会直接复制到内部的数组中, 这里可以直接传递.
        byteConsumer.accept(b);
    }

    @Override
    public void flush() throws OutputAlreadyClosedException {
        ensureOpen();
    }

    @Override
    public boolean isClosed() {
        return closed;
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
