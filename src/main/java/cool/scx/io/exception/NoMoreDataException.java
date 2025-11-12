package cool.scx.io.exception;

/// 没有更多可用的数据, 本质上是一种正常的流结束信号.
///
/// - 作为控制流异常, 设计为 受检异常 (Checked Exception)
///
/// @author scx567888
/// @version 0.0.1
public final class NoMoreDataException extends Exception {

    public NoMoreDataException() {

    }

}
