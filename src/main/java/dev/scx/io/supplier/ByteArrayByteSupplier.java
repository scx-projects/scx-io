package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;

import java.util.Iterator;
import java.util.List;

/// ByteArrayByteSupplier
///
/// 不会拷贝底层 byte[] 数组.
///
/// 注意: 返回的 ByteChunk 直接引用外部数组, 调用者需保证这些数组在使用期间不被外部修改.
///
/// @author scx567888
/// @version 0.0.1
public final class ByteArrayByteSupplier implements ByteSupplier {

    private final Iterator<byte[]> byteArrayIterator;

    public ByteArrayByteSupplier(Iterable<byte[]> byteArrays) {
        this.byteArrayIterator = byteArrays.iterator();
    }

    public ByteArrayByteSupplier(byte[]... byteArrays) {
        this.byteArrayIterator = List.of(byteArrays).iterator();
    }

    @Override
    public ByteChunk get() {
        if (byteArrayIterator.hasNext()) {
            var nextArray = byteArrayIterator.next();
            return ByteChunk.of(nextArray);
        }
        return null; // 没有更多字节数组时返回 null
    }

}
