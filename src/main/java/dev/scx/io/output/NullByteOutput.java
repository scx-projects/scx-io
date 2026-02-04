package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// NullByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteOutput implements ByteOutput {

    private boolean closed;

    public NullByteOutput() {
        this.closed = false;
    }

    /// 确保现在是打开状态.
    private void ensureOpen() throws OutputAlreadyClosedException {
        if (closed) {
            throw new OutputAlreadyClosedException();
        }
    }

    @Override
    public void write(byte b) throws OutputAlreadyClosedException {
        ensureOpen();
    }

    @Override
    public void write(ByteChunk b) throws OutputAlreadyClosedException {
        ensureOpen();
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

}
