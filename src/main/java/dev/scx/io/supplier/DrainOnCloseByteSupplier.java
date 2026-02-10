package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

/// DrainOnCloseByteSupplier
///
/// 关闭之前会排空上游流.
///
/// @author scx567888
/// @version 0.0.1
public final class DrainOnCloseByteSupplier implements ByteSupplier {

    private final ByteSupplier byteSupplier;
    private boolean alreadyDrain;

    public DrainOnCloseByteSupplier(ByteSupplier byteSupplier) {
        this.byteSupplier = byteSupplier;
        this.alreadyDrain = false;
    }

    @Override
    public ByteChunk get() throws ScxInputException {
        return byteSupplier.get();
    }

    @Override
    public ByteChunk borrow() throws ScxInputException {
        return byteSupplier.borrow();
    }

    @Override
    public void close() throws ScxInputException {

        if (!alreadyDrain) {
            // 先尝试排空 byteSupplier
            while (byteSupplier.borrow() != null) {
                // 什么都不做
            }
            // 排空成功 才算
            alreadyDrain = true;
        }

        // 此处依赖上游 ByteSupplier 的 close 幂等.
        byteSupplier.close();

    }

}
