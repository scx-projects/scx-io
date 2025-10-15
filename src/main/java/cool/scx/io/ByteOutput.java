package cool.scx.io;

import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;

/// ByteOutput
///
/// @author scx567888
/// @version 0.0.1
public interface ByteOutput extends AutoCloseable {

    void write(byte b) throws ScxIOException, AlreadyClosedException;

    void write(byte[] b) throws ScxIOException, AlreadyClosedException;

    void write(byte[] b, int off, int len) throws ScxIOException, AlreadyClosedException;

    void flush() throws ScxIOException, AlreadyClosedException;

    boolean isClosed();

    void close() throws ScxIOException;

}
