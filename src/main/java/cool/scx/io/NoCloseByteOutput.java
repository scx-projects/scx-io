package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

/// NoCloseByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class NoCloseByteOutput implements ByteOutput {

    private final ByteOutput byteOutput;

    private boolean closed;

    public NoCloseByteOutput(ByteOutput byteOutput) {
        this.byteOutput = byteOutput;
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

        this.byteOutput.write(b);
    }

    @Override
    public void write(ByteChunk b) throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        this.byteOutput.write(b);
    }

    @Override
    public void flush() throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        this.byteOutput.flush();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxIOException, AlreadyClosedException {
        ensureOpen();

        // 这里中断 close, 改为刷新
        this.byteOutput.flush();
        closed = true; // 只有成功关闭才算作 关闭
    }

    public ByteOutput byteOutput() {
        return byteOutput;
    }

}
