package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;

/// SkipByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class SkipByteConsumer implements ByteConsumer<RuntimeException> {

    private long bytesSkipped;

    public SkipByteConsumer() {
        this.bytesSkipped = 0;
    }

    @Override
    public boolean accept(ByteChunk chunk) {
        bytesSkipped += chunk.length;
        return true; // 一直跳过
    }

    public long bytesSkipped() {
        return bytesSkipped;
    }

}
