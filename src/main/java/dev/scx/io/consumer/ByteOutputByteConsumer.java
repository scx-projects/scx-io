package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;

/// ByteOutputByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class ByteOutputByteConsumer implements ByteConsumer {

    private final ByteOutput out;
    private long bytesWritten;

    public ByteOutputByteConsumer(ByteOutput out) {
        this.out = out;
        this.bytesWritten = 0;
    }

    @Override
    public boolean accept(ByteChunk chunk) throws ScxOutputException, OutputAlreadyClosedException {
        out.write(chunk);
        bytesWritten += chunk.length;
        return true;
    }

    /// 写入的总长度
    public long bytesWritten() {
        return bytesWritten;
    }

}
