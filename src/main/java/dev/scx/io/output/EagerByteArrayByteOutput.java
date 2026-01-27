package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.consumer.EagerByteArrayByteConsumer;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// EagerByteArrayByteOutput
///
/// 这里直接借用 [EagerByteArrayByteConsumer] 来实现
///
/// @author scx567888
/// @version 0.0.1
public final class EagerByteArrayByteOutput extends AbstractByteOutput {

    private final EagerByteArrayByteConsumer byteConsumer;

    public EagerByteArrayByteOutput() {
        this.byteConsumer = new EagerByteArrayByteConsumer();
    }

    public EagerByteArrayByteOutput(int size) {
        this.byteConsumer = new EagerByteArrayByteConsumer(size);
    }

    @Override
    public void write(byte b) throws OutputAlreadyClosedException {
        ensureOpen();

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

    public ByteChunk chunk() {
        return byteConsumer.chunk();
    }

    public byte[] bytes() {
        return byteConsumer.bytes();
    }

    public int size() {
        return byteConsumer.size();
    }

}
