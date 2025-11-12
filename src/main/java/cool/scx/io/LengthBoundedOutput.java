package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

/// LengthBoundedOutput
///
/// @author scx567888
/// @version 0.0.1
public final class LengthBoundedOutput implements ByteOutput {

    private final ByteOutput byteOutput;
    private final long minLength;
    private final long maxLength;
    private long bytesWritten;

    public LengthBoundedOutput(ByteOutput byteOutput, long minLength, long maxLength) {
        this.byteOutput = byteOutput;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.bytesWritten = 0;
    }

    private void ensureMax(int length) throws ScxIOException {
        if (bytesWritten + length > maxLength) {
            throw new ScxIOException("写入超出最大长度: 已写入 " + bytesWritten + ", 本次写入 " + length + ", 最大允许 " + maxLength);
        }
    }

    private void ensureMin() throws ScxIOException {
        if (bytesWritten < minLength) {
            throw new ScxIOException("写入长度不足: 已写入 " + bytesWritten + ", 最小长度要求 " + minLength);
        }
    }

    @Override
    public void write(byte b) throws ScxIOException, AlreadyClosedException {
        ensureMax(1);
        byteOutput.write(b);
        bytesWritten += 1;
    }

    @Override
    public void write(ByteChunk b) throws ScxIOException, AlreadyClosedException {
        ensureMax(b.length);
        byteOutput.write(b);
        bytesWritten += b.length;
    }

    @Override
    public void flush() throws ScxIOException, AlreadyClosedException {
        byteOutput.flush();
    }

    @Override
    public boolean isClosed() {
        return byteOutput.isClosed();
    }

    @Override
    public void close() throws ScxIOException, AlreadyClosedException {
        ensureMin();
        byteOutput.close();
    }

    public ByteOutput byteOutput() {
        return byteOutput;
    }

}
