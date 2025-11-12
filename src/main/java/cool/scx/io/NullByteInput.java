package cool.scx.io;

import cool.scx.io.consumer.ByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.indexer.ByteIndexer;

/// NullByteInput
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteInput implements ByteInput {

    private boolean closed;

    public NullByteInput() {
        this.closed = false;
    }

    private void ensureOpen() throws AlreadyClosedException {
        if (closed) {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public byte read() throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        throw new NoMoreDataException();
    }

    @Override
    public <X extends Throwable> void read(ByteConsumer<X> byteConsumer, long maxLength) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void readUpTo(ByteConsumer<X> byteConsumer, long length) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void readFully(ByteConsumer<X> byteConsumer, long length) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public byte peek() throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        throw new NoMoreDataException();
    }

    @Override
    public <X extends Throwable> void peek(ByteConsumer<X> byteConsumer, long maxLength) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void peekUpTo(ByteConsumer<X> byteConsumer, long length) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public <X extends Throwable> void peekFully(ByteConsumer<X> byteConsumer, long length) throws NoMoreDataException, AlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public ByteMatchResult indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, NoMoreDataException, AlreadyClosedException {
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
    public ByteInputMark mark() throws AlreadyClosedException {
        ensureOpen();

        return new NullByteInputMark(this);
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

    private record NullByteInputMark(NullByteInput nullByteInput) implements ByteInputMark {

        @Override
        public void reset() throws AlreadyClosedException {
            nullByteInput.ensureOpen();
        }

    }

}
