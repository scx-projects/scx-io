package dev.scx.io.exception;

/// 匹配器异常, 没有找到匹配的字节序列, 本质上是一种控制流信号.
///
/// 注意: 此类设计为保留调用栈, 不要为了性能改为无栈:
/// 首先 触发频率并不是特别高, 通常用于边界/协议解析失败的终止路径.
/// 其次 保留栈便于定位 匹配发生的上下文.
///
/// @author scx567888
/// @version 0.0.1
public final class NoMatchFoundException extends RuntimeException {

    public NoMatchFoundException() {

    }

}
