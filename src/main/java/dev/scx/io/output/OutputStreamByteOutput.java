package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

import java.io.IOException;
import java.io.OutputStream;

/// OutputStreamByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class OutputStreamByteOutput implements ByteOutput {

    private final OutputStream outputStream;

    private boolean closed;

    public OutputStreamByteOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.closed = false;
    }

    private void ensureOpen() throws OutputAlreadyClosedException {
        if (closed) {
            throw new OutputAlreadyClosedException();
        }
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.write(b);
        } catch (IOException e) {
            throw new ScxOutputException(e);
        }
    }

    @Override
    public void write(ByteChunk b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.write(b.bytes, b.start, b.length);
        } catch (IOException e) {
            throw new ScxOutputException(e);
        }
    }

    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.flush();
        } catch (IOException e) {
            throw new ScxOutputException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.close();
        } catch (IOException e) {
            throw new ScxOutputException(e);
        }

        closed = true; // 只有成功关闭才算作 关闭
    }

    public OutputStream outputStream() {
        return outputStream;
    }

}
