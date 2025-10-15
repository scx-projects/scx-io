package cool.scx.io.consumer;

import cool.scx.io.ByteChunk;

/// ByteConsumer
///
/// @author scx567888
/// @version 0.0.1
public interface ByteConsumer<X extends Throwable> {

    /// @return needMore 是否需要更多数据
    boolean accept(ByteChunk chunk) throws X;

}
