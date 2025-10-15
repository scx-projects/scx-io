package cool.scx.io.adapter;

import cool.scx.io.ByteInput;
import cool.scx.io.DefaultByteInput;
import cool.scx.io.supplier.InputStreamByteSupplier;

import java.io.InputStream;

/// ByteInputAdapter
///
/// @author scx567888
/// @version 0.0.1
public interface ByteInputAdapter {

    static InputStream byteInputToInputStream(ByteInput byteInput) {
        return new ByteInputInputStream(byteInput);
    }

    static ByteInput inputStreamToByteInput(InputStream inputStream) {
        if (inputStream instanceof ByteInputAdapter byteInputAdapter) {
            return byteInputAdapter.byteInput();
        }
        return new DefaultByteInput(new InputStreamByteSupplier(inputStream));
    }

    ByteInput byteInput();

}
