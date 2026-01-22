package dev.scx.io.input;

import dev.scx.io.ByteInput;
import dev.scx.io.exception.InputAlreadyClosedException;

/// AbstractByteInput
///
/// @author scx567888
/// @version 0.0.1
public abstract class AbstractByteInput implements ByteInput {

    protected boolean closed;

    protected AbstractByteInput() {
        this.closed = false;
    }

    protected void ensureOpen() throws InputAlreadyClosedException {
        if (closed) {
            throw new InputAlreadyClosedException();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

}
