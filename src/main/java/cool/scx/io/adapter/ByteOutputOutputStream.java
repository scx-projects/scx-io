package cool.scx.io.adapter;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteOutput;
import cool.scx.io.exception.ScxIOException;

import java.io.IOException;
import java.io.OutputStream;

/// ByteOutputOutputStream
///
/// @author scx567888
/// @version 0.0.1
public class ByteOutputOutputStream extends OutputStream implements ByteOutputAdapter {

    private final ByteOutput byteOutput;

    public ByteOutputOutputStream(ByteOutput byteOutput) {
        this.byteOutput = byteOutput;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            this.byteOutput.write((byte) b);
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            this.byteOutput.write(ByteChunk.of(b, off, off + len));
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            this.byteOutput.flush();
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.byteOutput.close();
        } catch (ScxIOException e) {
            var cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    @Override
    public ByteOutput byteOutput() {
        return this.byteOutput;
    }

}
