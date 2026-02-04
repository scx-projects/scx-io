package dev.scx.io;

import dev.scx.exception.ScxWrappedException;
import dev.scx.io.adapter.ByteInputAdapter;
import dev.scx.io.adapter.ByteInputInputStream;
import dev.scx.io.adapter.ByteOutputAdapter;
import dev.scx.io.adapter.ByteOutputOutputStream;
import dev.scx.io.consumer.ByteOutputByteConsumer;
import dev.scx.io.consumer.OutputStreamByteConsumer;
import dev.scx.io.exception.*;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.output.OutputStreamByteOutput;
import dev.scx.io.supplier.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/// ScxIO
///
/// @author scx567888
/// @version 0.0.1
public final class ScxIO {

    //******************** createByteInput ***************************

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

    //******************** createByteOutput ***************************

    public static ByteOutput createByteOutput(OutputStream outputStream) {
        return new OutputStreamByteOutput(outputStream);
    }

    //******************** adapter ***************************

    public static InputStream byteInputToInputStream(ByteInput byteInput) {
        // 此处因为 byteInput 内部可能持有缓存.
        // 并且 并不存在一个叫做 InputStreamByteInput 的类.
        // 所以我们每次都重新包装.
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

    //******************** gzip ***************************

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

    /// gzip 压缩 整个 byte[].
    /// 注意仅适用于小数据, 大数据请用 [GZIPOutputStream].
    public static byte[] gzip(byte[] data) throws ScxOutputException {
        var byteArrayByteOutput = new ByteArrayOutputStream();
        try (var gzipByteOutput = new GZIPOutputStream(byteArrayByteOutput)) {
            gzipByteOutput.write(data);
        } catch (IOException e) {
            throw new ScxOutputException(e);
        }
        return byteArrayByteOutput.toByteArray();
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

    //******************** transferTo (byteInput -> byteOutput) ***************************

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

    //******************** transferTo (byteInput -> outputStream) ***************************

    public static long transferTo(ByteInput byteInput, OutputStream outputStream, long maxLength) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, IOException {
        var consumer = new OutputStreamByteConsumer(outputStream);
        try {
            byteInput.read(consumer, maxLength);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (IOException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToUpTo(ByteInput byteInput, OutputStream outputStream, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, IOException {
        var consumer = new OutputStreamByteConsumer(outputStream);
        try {
            byteInput.readUpTo(consumer, length);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (IOException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToFully(ByteInput byteInput, OutputStream outputStream, long length) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, IOException {
        var consumer = new OutputStreamByteConsumer(outputStream);
        try {
            byteInput.readFully(consumer, length);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (IOException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    public static long transferToAll(ByteInput byteInput, OutputStream outputStream) throws ScxInputException, InputAlreadyClosedException, IOException {
        var consumer = new OutputStreamByteConsumer(outputStream);
        try {
            byteInput.readAll(consumer);
        } catch (ScxWrappedException e) {
            // 这里强转是安全的.
            throw (IOException) e.getCause();
        }
        return consumer.bytesWritten();
    }

    //******************** transferTo (byteSupplier -> byteOutput) ***************************

    public static long transferToAll(ByteSupplier byteSupplier, ByteOutput byteOutput) throws ScxInputException, ScxOutputException, OutputAlreadyClosedException {
        var bytesWritten = 0L;
        while (true) {
            var byteChunk = byteSupplier.borrow();
            if (byteChunk == null) {
                break;
            }
            if (byteChunk.length == 0) {
                continue;// 空块 我们视为无动作 继续拉取
            }
            byteOutput.write(byteChunk);
            bytesWritten += byteChunk.length;
        }
        return bytesWritten;
    }

    //******************** transferTo (byteSupplier -> outputStream) ***************************

    public static long transferToAll(ByteSupplier byteSupplier, OutputStream outputStream) throws ScxInputException, IOException {
        var bytesWritten = 0L;
        while (true) {
            var byteChunk = byteSupplier.borrow();
            if (byteChunk == null) {
                break;
            }
            if (byteChunk.length == 0) {
                continue;// 空块 我们视为无动作 继续拉取
            }
            outputStream.write(byteChunk.bytes, byteChunk.start, byteChunk.length);
            bytesWritten += byteChunk.length;
        }
        return bytesWritten;
    }

    //******************** cache ***************************

    public static CacheByteSupplier cacheByteSupplier(ByteInput byteInput) {
        return new CacheByteSupplier(new ByteInputByteSupplier(byteInput));
    }

    public static CacheByteSupplier cacheByteSupplier(ByteSupplier byteSupplier) {
        return new CacheByteSupplier(byteSupplier);
    }

    //******************** other ***************************

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
