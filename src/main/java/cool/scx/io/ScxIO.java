package cool.scx.io;

import cool.scx.io.adapter.ByteInputAdapter;
import cool.scx.io.adapter.ByteInputInputStream;
import cool.scx.io.adapter.ByteOutputAdapter;
import cool.scx.io.adapter.ByteOutputOutputStream;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.supplier.ByteArrayByteSupplier;
import cool.scx.io.supplier.FileByteSupplier;
import cool.scx.io.supplier.InputStreamByteSupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/// ScxIO
///
/// @author scx567888
/// @version 0.0.1
public final class ScxIO {

    public static ByteInput createByteInput(byte[]... byteArrays) {
        return new DefaultByteInput(new ByteArrayByteSupplier(byteArrays));
    }

    public static ByteInput createByteInput(InputStream inputStream) {
        return new DefaultByteInput(new InputStreamByteSupplier(inputStream));
    }

    public static ByteInput createByteInput(File file) throws ScxIOException {
        return new DefaultByteInput(new FileByteSupplier(file));
    }

    public static ByteInput createByteInput(File file, long offset, long length) throws ScxIOException {
        return new DefaultByteInput(new FileByteSupplier(file, offset, length));
    }

    public static ByteOutput createByteOutput(OutputStream outputStream) {
        return new OutputStreamByteOutput(outputStream);
    }

    public static ByteInput gzipByteInput(ByteInput byteInput) throws ScxIOException {
        try {
            return ScxIO.inputStreamToByteInput(new GZIPInputStream(ScxIO.byteInputToInputStream(byteInput)));
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    public static ByteOutput gzipByteOutput(ByteOutput byteOutput) throws ScxIOException {
        try {
            return ScxIO.outputStreamToByteOutput(new GZIPOutputStream(ScxIO.byteOutputToOutputStream(byteOutput)));
        } catch (IOException e) {
            throw new ScxIOException(e);
        }
    }

    public static InputStream byteInputToInputStream(ByteInput byteInput) {
        return new ByteInputInputStream(byteInput);
    }

    public static ByteInput inputStreamToByteInput(InputStream inputStream) {
        if (inputStream instanceof ByteInputAdapter byteInputAdapter) {
            return byteInputAdapter.byteInput();
        }
        return new DefaultByteInput(new InputStreamByteSupplier(inputStream));
    }

    public static OutputStream byteOutputToOutputStream(ByteOutput byteOutput) {
        if (byteOutput instanceof OutputStreamByteOutput outputStreamByteOutput) {
            return outputStreamByteOutput.outputStream();
        }
        return new ByteOutputOutputStream(byteOutput);
    }

    public static ByteOutput outputStreamToByteOutput(OutputStream outputStream) {
        if (outputStream instanceof ByteOutputAdapter byteOutputAdapter) {
            return byteOutputAdapter.byteOutput();
        }
        return new OutputStreamByteOutput(outputStream);
    }

}
