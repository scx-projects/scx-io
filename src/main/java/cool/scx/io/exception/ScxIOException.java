package cool.scx.io.exception;

/// 底层 IO 异常, 通常会包裹一个 {@link java.io.IOException}.
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
