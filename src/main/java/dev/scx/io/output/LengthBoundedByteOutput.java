package dev.scx.io.output;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// LengthBoundedOutput
///
/// @author scx567888
public final class LengthBoundedByteOutput implements ByteOutput {

    private final ByteOutput byteOutput;
    private final long minLength;
    private final long maxLength;
    private long bytesWritten;

    // 仅用于保证 close() 的幂等性, 避免重复执行 ensureMin() 以及底层 close().
    // 该标志只在 close() 成功完成后才会被置为 true.
    // 注意：该标志不表示底层 ByteOutput 是否已关闭, 也不用于校验 write/flush 等操作.
    private boolean closed;

    public LengthBoundedByteOutput(ByteOutput byteOutput, long minLength, long maxLength) {
        this.byteOutput = byteOutput;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.bytesWritten = 0;
        this.closed = false;
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
        ensureMax(1);
        byteOutput.write(b);
        bytesWritten += 1;
    }

    @Override
    public void write(ByteChunk b) throws ScxOutputException, OutputAlreadyClosedException {
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
        // isClosed() 直接代理到底层 ByteOutput.
        //
        // 设计原因:
        // 1) LengthBoundedByteOutput 只是一个长度约束的包装器, 不拥有底层资源的生命周期.
        // 2) write/flush 等操作的可用性应以底层状态为准, 而不是以本类的 close 幂等门闩为准.
        // 3) 本类中的 closed 字段仅用于 close() 幂等控制, 并不代表资源是否真正关闭.
        // 4) 因此 isClosed() 返回底层状态, 作为对底层资源状态的观测接口, 而非控制流判断依据.
        return byteOutput.isClosed();
    }

    @Override
    public void close() throws ScxOutputException {
        if (closed) {
            return;
        }
        ensureMin();
        byteOutput.close();

        closed = true; // 只有成功 close 才置位
    }

    public ByteOutput byteOutput() {
        return byteOutput;
    }

}
