package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// ByteChunkByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public final class ByteChunkByteConsumer implements ByteConsumer {

    private ByteChunk byteChunk;

    @Override
    public boolean accept(ByteChunk byteChunk) {
        this.byteChunk = byteChunk;
        // 只接受一次
        return false;
    }

    public ByteChunk byteChunk() {
        return byteChunk;
    }

}
