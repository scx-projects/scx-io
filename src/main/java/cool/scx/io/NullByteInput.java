package cool.scx.io;

import cool.scx.io.consumer.ByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.ByteIndexer;

/// NullByteInput
///
/// @author scx567888
/// @version 0.0.1
public class NullByteInput implements ByteInput {

    private volatile boolean closed;

    public NullByteInput() {
        this.closed = false;
    }

    private void ensureOpen() throws AlreadyClosedException {
        if (closed) {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public byte read() throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();
        throw new NoMoreDataException();
    }

    @Override
    public <X extends Throwable> void read(ByteConsumer<X> byteConsumer, long maxLength) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void readUpTo(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void readFully(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public byte peek() throws ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();
        throw new NoMoreDataException();
    }

    @Override
    public <X extends Throwable> void peek(ByteConsumer<X> byteConsumer, long maxLength) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void peekUpTo(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void peekFully(ByteConsumer<X> byteConsumer, long length) throws X, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public ByteMatchResult indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, ScxIOException, AlreadyClosedException, NoMoreDataException {
        ensureOpen();

        if (indexer.isEmptyPattern()) {
            return new ByteMatchResult(0, 0);
        }

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }

        throw new NoMatchFoundException();
    }

    @Override
    public void mark() throws AlreadyClosedException {
        ensureOpen();
    }

    @Override
    public void reset() throws AlreadyClosedException {
        ensureOpen();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxIOException {
        closed = true;
    }

}
