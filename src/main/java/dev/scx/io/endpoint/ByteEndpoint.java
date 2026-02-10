package dev.scx.io.endpoint;

import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;

/// ByteEndpoint
///
/// @author scx567888
/// @version 0.0.1
public interface ByteEndpoint extends AutoCloseable {

    ByteInput in();

    ByteOutput out();

    /// close 是幂等的. 可重复调用.
    /// 只负责底层资源的释放, 不负责关闭 in/out.
    @Override
    void close() throws Exception;

}
