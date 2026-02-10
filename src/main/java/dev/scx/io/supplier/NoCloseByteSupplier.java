package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

/// NoCloseByteSupplier
///
/// 隔离 close.
///
/// @author scx567888
/// @version 0.0.1
public final class NoCloseByteSupplier implements ByteSupplier {

    private final ByteSupplier byteSupplier;

    public NoCloseByteSupplier(ByteSupplier byteSupplier) {
        this.byteSupplier = byteSupplier;
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
        // 什么都不做.
    }

}
