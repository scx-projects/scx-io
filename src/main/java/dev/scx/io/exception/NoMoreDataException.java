package dev.scx.io.exception;

/// 读取端异常, 表示没有更多可用的数据, 本质上是一种正常的流结束信号.
///
/// 注意: 此类设计为保留调用栈, 不要为了性能改为无栈:
/// 首先 NoMoreDataException 通常在读取流程末尾触发且一般只出现一次, 所以一般不会被频繁创建.
/// 其次, 保留栈便于定位 "流何处结束/由哪次读取触发".
///
/// @author scx567888
/// @version 0.0.1
public final class NoMoreDataException extends RuntimeException {

    public NoMoreDataException() {

    }

}
