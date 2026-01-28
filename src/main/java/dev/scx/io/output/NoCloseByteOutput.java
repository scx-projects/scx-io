package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// NoCloseByteOutput
///
/// @author scx567888
/// @version 0.0.1
public final class NoCloseByteOutput extends AbstractByteOutput {

    private final ByteOutput byteOutput;

    public NoCloseByteOutput(ByteOutput byteOutput) {
        this.byteOutput = byteOutput;
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
