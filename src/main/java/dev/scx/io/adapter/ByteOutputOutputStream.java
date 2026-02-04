package dev.scx.io.adapter;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.ScxOutputException;

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
        } catch (ScxOutputException e) {
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
        } catch (ScxOutputException e) {
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
        } catch (ScxOutputException e) {
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
        } catch (ScxOutputException e) {
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
