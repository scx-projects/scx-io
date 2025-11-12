package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;

/// NullByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteOutput implements ByteOutput {

    private boolean closed;

    public NullByteOutput() {
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
    }

    @Override
    public void write(ByteChunk b) throws AlreadyClosedException {
        ensureOpen();
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

}
