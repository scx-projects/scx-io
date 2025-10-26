package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;

import java.io.IOException;
import java.io.OutputStream;

/// OutputStreamByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class OutputStreamByteConsumer implements ByteConsumer<IOException> {

    private final OutputStream out;
    private long bytesWritten;

    public OutputStreamByteConsumer(OutputStream out) {
        this.out = out;
        this.bytesWritten = 0;
    }

    @Override
    public boolean accept(ByteChunk chunk) throws IOException {
        out.write(chunk.bytes, chunk.start, chunk.length);
        bytesWritten += chunk.length;
        return true;
    }

    /// 写入的总长度
    public long bytesWritten() {
        return bytesWritten;
    }

}
