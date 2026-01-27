package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.consumer.ByteArrayByteConsumer;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// ByteArrayByteOutput
///
/// 这里直接借用 [ByteArrayByteConsumer] 来实现
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteOutput extends AbstractByteOutput {

    private final ByteArrayByteConsumer byteConsumer;

    public ByteArrayByteOutput() {
        this.byteConsumer = new ByteArrayByteConsumer();
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

    public byte[] bytes() {
        return byteConsumer.bytes();
    }

}
