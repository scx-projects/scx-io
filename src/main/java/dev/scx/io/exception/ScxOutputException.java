package dev.scx.io.exception;

/// 这里表示宏观的 输出端 IO 异常, 如 包裹底层的 [java.io.IOException], 编码过程中的编码异常 等.
/// 输出端 IO 异常 必须明确原因或来源, 所以此处不提供无参构造.
///
/// @author scx567888
/// @version 0.0.1
public class ScxOutputException extends RuntimeException {

    public ScxOutputException(String message) {
        super(message);
    }

    public ScxOutputException(Throwable cause) {
        super(cause);
    }

    public ScxOutputException(String message, Throwable cause) {
        super(message, cause);
    }

}
