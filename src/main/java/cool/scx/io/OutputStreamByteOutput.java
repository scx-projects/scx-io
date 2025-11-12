package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

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

    private void ensureOpen() throws AlreadyClosedException {
        if (closed) {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public void write(byte b) throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.write(b);
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    @Override
    public void write(ByteChunk b) throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.write(b.bytes, b.start, b.length);
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    @Override
    public void flush() throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.flush();
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        try {
            this.outputStream.close();
            closed = true; // 只有成功关闭才算作 关闭
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    public OutputStream outputStream() {
        return outputStream;
    }

}
