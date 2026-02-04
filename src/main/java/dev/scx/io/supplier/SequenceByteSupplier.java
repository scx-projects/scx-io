package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

import java.util.Iterator;
import java.util.List;

/// SequenceByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class SequenceByteSupplier implements ByteSupplier {

    private final Iterator<ByteSupplier> iterator;
    private ByteSupplier currentSupplier;

    public SequenceByteSupplier(ByteSupplier... suppliers) {
        this(List.of(suppliers));
    }

    /// @param dataSupplierIterable ByteSupplier 迭代器.
    public SequenceByteSupplier(Iterable<ByteSupplier> dataSupplierIterable) {
        // 创建迭代器
        this.iterator = dataSupplierIterable.iterator();
        if (this.iterator.hasNext()) {
            this.currentSupplier = this.iterator.next();
        }
    }

    @Override
    public ByteChunk get() throws ScxInputException {
        while (currentSupplier != null) {
            // 读取当前块.
            var byteChunk = currentSupplier.get();
            if (byteChunk != null) {
                return byteChunk;
            }
            // 切换之前 close 当前块.
            currentSupplier.close();

            if (iterator.hasNext()) {
                currentSupplier = iterator.next();
            } else {
                currentSupplier = null;
            }
        }
        return null; // 所有 ByteSupplier 都返回 null, 表示结束
    }

    @Override
    public ByteChunk borrow() throws ScxInputException {
        while (currentSupplier != null) {
            // 读取当前块.
            var byteChunk = currentSupplier.borrow();
            if (byteChunk != null) {
                return byteChunk;
            }
            // 切换之前 close 当前块.
            currentSupplier.close();

            if (iterator.hasNext()) {
                currentSupplier = iterator.next();
            } else {
                currentSupplier = null;
            }
        }
        return null; // 所有 ByteSupplier 都返回 null, 表示结束
    }

    /// 需要保证在任何情况下都能尽可能的关闭 所有的资源(包括剩余的)
    @Override
    public void close() throws ScxInputException {
        ScxInputException ex = null;
        // 关闭剩余的所有 supplier
        while (currentSupplier != null) {
            try {
                currentSupplier.close();
            } catch (ScxInputException e) {
                if (ex == null) {
                    ex = e;
                } else {
                    ex.addSuppressed(e);
                }
            }
            if (iterator.hasNext()) {
                currentSupplier = iterator.next();
            } else {
                currentSupplier = null;
            }
        }

        if (ex != null) {
            throw ex;
        }

    }

}
