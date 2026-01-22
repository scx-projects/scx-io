package dev.scx.io.exception;

/// 读取端异常, 表示没有更多可用的数据, 本质上是一种正常的流结束信号.
///
/// @author scx567888
/// @version 0.0.1
public final class NoMoreDataException extends RuntimeException {

    public NoMoreDataException() {

    }

}
