package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;

/// ByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public interface ByteSupplier extends AutoCloseable {

    /// 如果没有数据请返回 null
    ByteChunk get() throws Exception;

    @Override
    default void close() throws Exception {

    }

}
