package dev.scx.io.input;

import dev.scx.io.ByteInput;
import dev.scx.io.ByteInputMark;
import dev.scx.io.ByteMatchResult;
import dev.scx.io.consumer.ByteConsumer;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMatchFoundException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.indexer.ByteIndexer;

/// NullByteInput
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteInput implements ByteInput {

    private boolean closed;

    public NullByteInput() {
        this.closed = false;
    }

    /// 确保现在是打开状态.
    private void ensureOpen() throws InputAlreadyClosedException {
        if (closed) {
            throw new InputAlreadyClosedException();
        }
    }

    @Override
    public byte read() throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        throw new NoMoreDataException();
    }

    @Override
    public void read(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public void readUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public void readFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public byte peek() throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        throw new NoMoreDataException();
    }

    @Override
    public void peek(ByteConsumer byteConsumer, long maxLength) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (maxLength > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public void peekUpTo(ByteConsumer byteConsumer, long length) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public void peekFully(ByteConsumer byteConsumer, long length) throws NoMoreDataException, InputAlreadyClosedException {
        ensureOpen();

        if (length > 0) {
            throw new NoMoreDataException();
        }
    }

    @Override
    public ByteMatchResult indexOf(ByteIndexer indexer, long maxLength) throws NoMatchFoundException, NoMoreDataException, InputAlreadyClosedException {
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
    public ByteInputMark mark() throws InputAlreadyClosedException {
        ensureOpen();

        return new NullByteInputMark(this);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws InputAlreadyClosedException {
        ensureOpen();

        closed = true;
    }

    private record NullByteInputMark(NullByteInput nullByteInput) implements ByteInputMark {

        @Override
        public void reset() throws InputAlreadyClosedException {
            nullByteInput.ensureOpen();
        }

    }

}
