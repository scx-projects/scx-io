package cool.scx.io.exception;

/// 没有找到匹配的字节序列, 本质上是一种控制流信号.
///
/// - 作为控制流异常, 设计为 受检异常 (Checked Exception)
///
/// @author scx567888
/// @version 0.0.1
public final class NoMatchFoundException extends Exception {

    public NoMatchFoundException() {

    }

}
