package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteOutput;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

/// ByteOutputByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public class ByteOutputByteConsumer implements ByteConsumer<RuntimeException> {

    private final ByteOutput out;
    private long bytesWritten;

    public ByteOutputByteConsumer(ByteOutput out) {
        this.out = out;
        this.bytesWritten = 0;
    }

    @Override
    public boolean accept(ByteChunk chunk) throws ScxIOException, AlreadyClosedException {
        out.write(chunk.bytes, chunk.start, chunk.length);
        bytesWritten += chunk.length;
        return true;
    }

    /// 写入的总长度
    public long bytesWritten() {
        return bytesWritten;
    }

}
