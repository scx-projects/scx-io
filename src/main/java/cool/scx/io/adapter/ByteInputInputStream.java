package cool.scx.io.adapter;

import cool.scx.io.ByteInput;
import cool.scx.io.consumer.FillByteArrayByteConsumer;
import cool.scx.io.consumer.OutputStreamByteConsumer;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/// ByteInputInputStream
///
/// @author scx567888
/// @version 0.0.1
public class ByteInputInputStream extends InputStream implements ByteInputAdapter {

    private final ByteInput byteInput;

    public ByteInputInputStream(ByteInput byteInput) {
        this.byteInput = byteInput;
    }

    @Override
    public int read() throws IOException {
        try {
            return byteInput.read() & 0xFF;
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        var consumer = new FillByteArrayByteConsumer(b, off, len);
        try {
            byteInput.read(consumer, len);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            return -1;
        }
        return consumer.bytesFilled();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        var consumer = new FillByteArrayByteConsumer(b, off, len);
        try {
            byteInput.readUpTo(consumer, len);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            return 0;
        }
        return consumer.bytesFilled();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        try {
            return byteInput.readUpTo(len);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            return new byte[]{};
        }
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        try {
            return byteInput.readAll();
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return byteInput.skip(n);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            return 0;
        }
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        // 注意 InputStream 规定 skipNBytes 需要强制跳过 这里和 readNBytes 的行为是不一致的 (这也是为什么 InputStream 用起来这么混乱)
        try {
            byteInput.skipFully(n);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        } catch (NoMoreDataException e) {
            throw new EOFException();
        }
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        var consumer = new OutputStreamByteConsumer(out);
        try {
            byteInput.readAll(consumer);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
        return consumer.bytesWritten();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        byteInput.mark();
    }

    @Override
    public void reset() throws IOException {
        byteInput.reset();
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        try {
            this.byteInput.close();
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public ByteInput byteInput() {
        return byteInput;
    }

}
