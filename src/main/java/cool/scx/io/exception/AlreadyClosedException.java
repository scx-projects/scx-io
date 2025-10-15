package cool.scx.io.exception;

/// 输入/输出 已经被关闭了, 本质上是一种状态异常.
///
/// @author scx567888
/// @version 0.0.1
public class AlreadyClosedException extends IllegalStateException {

    public AlreadyClosedException() {

    }

}
