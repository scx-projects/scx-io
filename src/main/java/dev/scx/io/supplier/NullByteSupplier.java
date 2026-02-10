package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;

/// NullByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class NullByteSupplier implements ByteSupplier {

    public static final NullByteSupplier NULL_BYTE_SUPPLIER = new NullByteSupplier();

    private NullByteSupplier() {

    }

    @Override
    public ByteChunk get() {
        return null;
    }

}
