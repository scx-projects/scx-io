package cool.scx.io;

import cool.scx.io.consumer.ByteArrayByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;

/// ByteArrayByteOutput
///
/// 这里直接借用 ByteArrayByteConsumer 来实现
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteOutput implements ByteOutput {

    private final ByteArrayByteConsumer byteConsumer;
    private boolean closed;

    public ByteArrayByteOutput() {
        this.byteConsumer = new ByteArrayByteConsumer();
        this.closed = false;
    }

    private void ensureOpen() throws AlreadyClosedException {
        if (closed) {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public void write(byte b) throws AlreadyClosedException {
        ensureOpen();

        byteConsumer.accept(ByteChunk.of(b));
    }

    @Override
    public void write(ByteChunk b) throws AlreadyClosedException {
        ensureOpen();

        byteConsumer.accept(b);
    }

    @Override
    public void flush() throws AlreadyClosedException {
        ensureOpen();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws AlreadyClosedException {
        ensureOpen();

        closed = true;
    }

    public byte[] bytes() {
        return byteConsumer.bytes();
    }

}
