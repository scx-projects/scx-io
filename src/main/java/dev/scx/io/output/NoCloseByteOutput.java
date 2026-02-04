package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// NoCloseByteOutput
///
/// 此处的 NoClose 指的是不关闭底层(改为刷新).
/// NoCloseByteOutput 本身还是有 closed 状态的.
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

    /// 确保现在是打开状态.
    private void ensureOpen() throws OutputAlreadyClosedException {
        if (closed) {
            throw new OutputAlreadyClosedException();
        }
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        this.byteOutput.write(b);
    }

    @Override
    public void write(ByteChunk b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        this.byteOutput.write(b);
    }

    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        this.byteOutput.flush();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();

        // 这里中断 close, 改为刷新
        this.byteOutput.flush();

        closed = true; // 只有成功关闭才算作 关闭
    }

    public ByteOutput byteOutput() {
        return byteOutput;
    }

}
