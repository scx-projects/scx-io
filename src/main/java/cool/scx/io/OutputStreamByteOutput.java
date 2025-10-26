package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

import java.io.IOException;
import java.io.OutputStream;

/// OutputStreamByteOutput
///
/// @author scx567888
/// @version 0.0.1
public class OutputStreamByteOutput implements ByteOutput {

    private final OutputStream outputStream;
    private volatile boolean closed;

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
    public void write(byte[] b) throws ScxIOException, AlreadyClosedException {
        ensureOpen();
        try {
            this.outputStream.write(b);
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws ScxIOException, AlreadyClosedException {
        ensureOpen();
        try {
            this.outputStream.write(b, off, len);
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
    public void close() throws ScxIOException {
        try {
            this.outputStream.close();
        } catch (IOException e) {
            throw new ScxIOException(e);
        } finally {
            closed = true;
        }
    }

    public OutputStream outputStream() {
        return outputStream;
    }

}
