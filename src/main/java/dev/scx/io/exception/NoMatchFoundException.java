package dev.scx.io.exception;

/// 匹配器异常, 没有找到匹配的字节序列, 本质上是一种控制流信号.
///
/// @author scx567888
/// @version 0.0.1
public final class NoMatchFoundException extends RuntimeException {

    public NoMatchFoundException() {

    }

}
