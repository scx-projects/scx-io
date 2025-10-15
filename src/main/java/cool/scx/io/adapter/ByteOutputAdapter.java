package cool.scx.io.adapter;

import cool.scx.io.ByteOutput;
import cool.scx.io.OutputStreamByteOutput;

import java.io.OutputStream;

/// ByteOutputAdapter
///
/// @author scx567888
/// @version 0.0.1
public interface ByteOutputAdapter {

    static OutputStream byteOutputToOutputStream(ByteOutput byteOutput) {
        if (byteOutput instanceof OutputStreamByteOutput outputStreamByteOutput) {
            return outputStreamByteOutput.outputStream();
        }
        return new ByteOutputOutputStream(byteOutput);
    }

    static ByteOutput outputStreamToByteOutput(OutputStream outputStream) {
        if (outputStream instanceof ByteOutputAdapter byteOutputAdapter) {
            return byteOutputAdapter.byteOutput();
        }
        return new OutputStreamByteOutput(outputStream);
    }

    ByteOutput byteOutput();

}
