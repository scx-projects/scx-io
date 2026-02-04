package dev.scx.io.exception;

/// 输入端 已经被关闭了, 本质上是一种状态异常.
///
/// 注意: 此类设计为保留调用栈, 不要为了性能改为无栈:
/// 此异常表示明确的状态违规(调用错误), 设计为保留调用栈以便定位问题.
///
/// @author scx567888
/// @version 0.0.1
public final class InputAlreadyClosedException extends RuntimeException {

    public InputAlreadyClosedException() {

    }

}
