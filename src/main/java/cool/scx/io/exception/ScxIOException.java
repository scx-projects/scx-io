package cool.scx.io.exception;

/// 这里表示宏观的 IO 异常, 如 包裹底层的 [java.io.IOException], 解码过程中的解码异常 等.
///
/// @author scx567888
/// @version 0.0.1
public class ScxIOException extends RuntimeException {

    public ScxIOException() {

    }

    public ScxIOException(String message) {
        super(message);
    }

    public ScxIOException(Throwable cause) {
        super(cause);
    }

    public ScxIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
