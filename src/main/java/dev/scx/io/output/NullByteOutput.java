package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// NullByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteOutput extends AbstractByteOutput {

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
    public void close() throws OutputAlreadyClosedException {
        ensureOpen();

        closed = true;
    }

}
