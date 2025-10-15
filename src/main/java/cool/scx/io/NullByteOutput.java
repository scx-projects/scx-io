package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;

/// NullByteOutput
///
/// @author scx567888
/// @version 0.0.1
public class NullByteOutput implements ByteOutput {

    private volatile boolean closed;

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
    public void write(byte[] b) throws AlreadyClosedException {
        ensureOpen();
    }

    @Override
    public void write(byte[] b, int off, int len) throws AlreadyClosedException {
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
    public void close() {
        closed = true;
    }

}
