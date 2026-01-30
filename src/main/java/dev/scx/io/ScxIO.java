package dev.scx.io;

import dev.scx.exception.ScxWrappedException;
import dev.scx.io.adapter.ByteInputAdapter;
import dev.scx.io.adapter.ByteInputInputStream;
import dev.scx.io.adapter.ByteOutputAdapter;
import dev.scx.io.adapter.ByteOutputOutputStream;
import dev.scx.io.consumer.ByteOutputByteConsumer;
import dev.scx.io.exception.*;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.output.EagerByteArrayByteOutput;
import dev.scx.io.output.GZIPByteOutput;
import dev.scx.io.output.OutputStreamByteOutput;
import dev.scx.io.supplier.*;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static dev.scx.io.output.GZIPByteOutput.GZIPByteOutputOptions;

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

    public static ByteOutput gzipByteOutput(ByteOutput byteOutput) {
        return new GZIPByteOutput(byteOutput);
    }

    public static ByteOutput gzipByteOutput(ByteOutput byteOutput, GZIPByteOutputOptions options) {
        return new GZIPByteOutput(byteOutput, options);
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

    /// gzip 压缩 整个 byte[].
    /// 注意仅适用于小数据, 大数据请用 [GZIPByteOutput].
    public static byte[] gzip(byte[] data) throws ScxOutputException {
        // 此处不能使用 LazyByteArrayByteOutput, 因为 LazyByteArrayByteOutput 持有的是 ByteChunk 引用.
        // 而这个引用的 backing byte[], 在 GZIPByteOutput 中是可能被覆写的.
        var byteArrayByteOutput = new EagerByteArrayByteOutput();
        try (var gzipByteOutput = new GZIPByteOutput(byteArrayByteOutput)) {
            gzipByteOutput.write(data);
        }
        return byteArrayByteOutput.bytes();
    }

    /// gzip 解压 整个 byte[].
    /// 注意仅适用于小数据, 大数据请用 [GZIPInputStream].
    public static byte[] ungzip(byte[] data) throws ScxInputException {
        try (var gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return gzipInputStream.readAllBytes();
        } catch (IOException e) {
            throw new ScxInputException(e);
        }
    }

    /// [ByteOutput] 中的 异常 ([ScxOutputException], [OutputAlreadyClosedException]) 因为其本质是 RuntimeException, 这里强转是安全的.
    public static long transferTo(ByteInput byteInput, ByteOutput byteOutput, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxOutputException, OutputAlreadyClosedException {
        var consumer = new ByteOutputByteConsumer(byteOutput);
        try {
            byteInput.read(consumer, maxLength);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (RuntimeException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToUpTo(ByteInput byteInput, ByteOutput byteOutput, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxOutputException, OutputAlreadyClosedException {
        var consumer = new ByteOutputByteConsumer(byteOutput);
        try {
            byteInput.readUpTo(consumer, length);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (RuntimeException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToFully(ByteInput byteInput, ByteOutput byteOutput, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, ScxOutputException, OutputAlreadyClosedException {
        var consumer = new ByteOutputByteConsumer(byteOutput);
        try {
            byteInput.readFully(consumer, length);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (RuntimeException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToAll(ByteInput byteInput, ByteOutput byteOutput) throws ScxInputException, InputAlreadyClosedException, ScxOutputException, OutputAlreadyClosedException {
        var consumer = new ByteOutputByteConsumer(byteOutput);
        try {
            byteInput.readAll(consumer);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (RuntimeException) e.getCause();
        }
        return consumer.bytesWritten();
    }

}
