package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;

/// ByteMark
///
/// @author scx567888
/// @version 0.0.1
public interface ByteMark {

    /// 将读取位置恢复到当前 mark.
    void reset() throws AlreadyClosedException;

}
