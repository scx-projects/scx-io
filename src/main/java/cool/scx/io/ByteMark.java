package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;

/// ByteMark
///
/// @author scx567888
/// @version 0.0.1
public interface ByteMark {

    void reset() throws AlreadyClosedException;

}
