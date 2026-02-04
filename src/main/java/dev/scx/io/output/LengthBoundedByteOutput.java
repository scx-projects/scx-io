package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// LengthBoundedOutput
///
/// @author scx567888
/// @version 0.0.1
public final class LengthBoundedByteOutput implements ByteOutput {

    private final ByteOutput byteOutput;
    private final long minLength;
    private final long maxLength;
    private long bytesWritten;

    public LengthBoundedByteOutput(ByteOutput byteOutput, long minLength, long maxLength) {
        this.byteOutput = byteOutput;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.bytesWritten = 0;
    }

    private void ensureOpen() throws OutputAlreadyClosedException {
        if (byteOutput.isClosed()) {
            throw new OutputAlreadyClosedException();
        }
    }

    private void ensureMax(int length) throws ScxOutputException {
        if (bytesWritten + length > maxLength) {
            throw new ScxOutputException("写入超出最大长度: 已写入 " + bytesWritten + ", 本次写入 " + length + ", 最大允许 " + maxLength);
        }
    }

    private void ensureMin() throws ScxOutputException {
        if (bytesWritten < minLength) {
            throw new ScxOutputException("写入长度不足: 已写入 " + bytesWritten + ", 最小长度要求 " + minLength);
        }
    }

    @Override
    public void write(byte b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();
        ensureMax(1);
        byteOutput.write(b);
        bytesWritten += 1;
    }

    @Override
    public void write(ByteChunk b) throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();
        ensureMax(b.length);
        byteOutput.write(b);
        bytesWritten += b.length;
    }

    @Override
    public void flush() throws ScxOutputException, OutputAlreadyClosedException {
        byteOutput.flush();
    }

    @Override
    public boolean isClosed() {
        return byteOutput.isClosed();
    }

    @Override
    public void close() throws ScxOutputException, OutputAlreadyClosedException {
        ensureOpen();
        ensureMin();
        byteOutput.close();
    }

    public ByteOutput byteOutput() {
        return byteOutput;
    }

}
