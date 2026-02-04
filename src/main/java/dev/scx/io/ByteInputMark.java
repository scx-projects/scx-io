package dev.scx.io;

import dev.scx.io.exception.InputAlreadyClosedException;

/// ByteInputMark
///
/// @author scx567888
/// @version 0.0.1
public interface ByteInputMark {

    /// 将读取位置恢复到当前 mark.
    void reset() throws InputAlreadyClosedException;

}
