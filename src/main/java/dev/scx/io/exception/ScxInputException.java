package dev.scx.io.exception;

/// 这里表示宏观的 输入端 IO 异常, 如 包裹底层的 [java.io.IOException], 解码过程中的解码异常 等.
/// 输入端 IO 异常 必须明确原因或来源, 所以此处不提供无参构造.
///
/// @author scx567888
/// @version 0.0.1
public class ScxInputException extends RuntimeException {

    public ScxInputException(String message) {
        super(message);
    }

    public ScxInputException(Throwable cause) {
        super(cause);
    }

    public ScxInputException(String message, Throwable cause) {
        super(message, cause);
    }

}
