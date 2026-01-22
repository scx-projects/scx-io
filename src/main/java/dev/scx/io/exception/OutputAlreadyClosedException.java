package dev.scx.io.exception;

/// 输出端 已经被关闭了, 本质上是一种状态异常.
///
/// @author scx567888
/// @version 0.0.1
public final class OutputAlreadyClosedException extends RuntimeException {

    public OutputAlreadyClosedException() {

    }

}
