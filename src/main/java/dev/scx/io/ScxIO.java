package dev.scx.io;

import dev.scx.io.adapter.ByteInputAdapter;
import dev.scx.io.adapter.ByteInputInputStream;
import dev.scx.io.adapter.ByteOutputAdapter;
import dev.scx.io.adapter.ByteOutputOutputStream;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.output.OutputStreamByteOutput;
import dev.scx.io.supplier.*;

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

    public static ByteInput createByteInput(ByteSupplier byteSupplier) {
        return new DefaultByteInput(byteSupplier);
    }

    public static ByteInput createByteInput(byte[]... byteArrays) {
        return new DefaultByteInput(new ByteArrayByteSupplier(byteArrays));
    }

    public static ByteInput createByteInput(InputStream inputStream) {
        return new DefaultByteInput(new InputStreamByteSupplier(inputStream));
    }

    public static ByteInput createByteInput(File file) throws ScxInputException {
        return new DefaultByteInput(new FileByteSupplier(file));
    }

    public static ByteInput createByteInput(File file, long offset, long length) throws ScxInputException {
        return new DefaultByteInput(new FileByteSupplier(file, offset, length));
    }

    public static ByteOutput createByteOutput(OutputStream outputStream) {
        return new OutputStreamByteOutput(outputStream);
    }

    public static ByteInput gzipByteInput(ByteInput byteInput) throws ScxInputException {
        try {
            return ScxIO.inputStreamToByteInput(new GZIPInputStream(ScxIO.byteInputToInputStream(byteInput)));
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

    public static ByteOutput gzipByteOutput(ByteOutput byteOutput) throws ScxOutputException {
        try {
            return ScxIO.outputStreamToByteOutput(new GZIPOutputStream(ScxIO.byteOutputToOutputStream(byteOutput)));
        } catch (IOException e) {
            throw new ScxOutputException(e);
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

    /// 隔离底层 close.
    public static ByteSupplier noClose(ByteSupplier byteSupplier) {
        return new NoCloseByteSupplier(byteSupplier);
    }

    /// close 时排空, 同时会穿透底层 close.
    public static ByteSupplier drainOnClose(ByteSupplier byteSupplier) {
        return new DrainOnCloseByteSupplier(byteSupplier);
    }

    /// close 时排空, 但是隔离底层 close.
    public static ByteSupplier drainOnCloseNoClose(ByteSupplier byteSupplier) {
        return new DrainOnCloseByteSupplier(new NoCloseByteSupplier(byteSupplier));
    }

}
