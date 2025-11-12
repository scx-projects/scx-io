package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.exception.ScxIOException;

/// ClosePolicyByteSupplier
///
/// 可配置 close 策略的 ByteSupplier 包装器.
///
/// @author scx567888
/// @version 0.0.1
public final class ClosePolicyByteSupplier implements ByteSupplier {

    private final ByteSupplier byteSupplier;
    /// 在 close 的时候 排空底层 byteSupplier
    private final boolean drainOnClose;
    /// 是否需要调用底层的 close
    private boolean needClose;

    private ClosePolicyByteSupplier(ByteSupplier byteSupplier, boolean needClose, boolean drainOnClose) {
        this.byteSupplier = byteSupplier;
        this.needClose = needClose;
        this.drainOnClose = drainOnClose;
    }

    public static ClosePolicyByteSupplier noClose(ByteSupplier byteSupplier) {
        return new ClosePolicyByteSupplier(byteSupplier, false, false);
    }

    public static ClosePolicyByteSupplier singleClose(ByteSupplier byteSupplier) {
        return new ClosePolicyByteSupplier(byteSupplier, true, false);
    }

    public static ClosePolicyByteSupplier noCloseDrain(ByteSupplier byteSupplier) {
        return new ClosePolicyByteSupplier(byteSupplier, false, true);
    }

    public static ClosePolicyByteSupplier singleCloseDrain(ByteSupplier byteSupplier) {
        return new ClosePolicyByteSupplier(byteSupplier, true, true);
    }

    @Override
    public ByteChunk get() throws ScxIOException {
        return byteSupplier.get();
    }

    @Override
    public void close() throws ScxIOException {

        // 先尝试排空 byteSupplier
        if (drainOnClose) {
            while (byteSupplier.get() != null) {
                // 什么都不做
            }
        }

        if (needClose) {
            byteSupplier.close();
            needClose = false;  // 只有成功关闭才算作 关闭
        }

    }

    public ByteSupplier byteSupplier() {
        return byteSupplier;
    }

}
