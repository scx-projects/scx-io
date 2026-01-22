package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

/// ByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public interface ByteConsumer {

    /// @return needMore 是否需要更多数据
    boolean accept(ByteChunk chunk) throws Throwable;

}
