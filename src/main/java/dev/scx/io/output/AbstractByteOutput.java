package dev.scx.io.output;

import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;

/// AbstractByteOutput
///
/// @author scx567888
/// @version 0.0.1
public abstract class AbstractByteOutput implements ByteOutput {

    protected boolean closed;

    protected AbstractByteOutput() {
        this.closed = false;
    }

    protected void ensureOpen() throws OutputAlreadyClosedException {
        if (closed) {
            throw new OutputAlreadyClosedException();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

}
