package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.exception.ScxIOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/// SequenceByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class SequenceByteSupplier implements ByteSupplier {

    private final Collection<ByteSupplier> dataSupplierList;
    private final boolean closeOnSwitch;
    private final Iterator<ByteSupplier> iterator;
    private ByteSupplier currentSupplier;

    public SequenceByteSupplier(ByteSupplier... suppliers) {
        this(List.of(suppliers), true);
    }

    /// @param dataSupplierList ByteSupplier 列表
    /// @param closeOnSwitch    是否在切换到下一个的时候关闭上一个
    public SequenceByteSupplier(Collection<ByteSupplier> dataSupplierList, boolean closeOnSwitch) {
        this.dataSupplierList = dataSupplierList;
        this.closeOnSwitch = closeOnSwitch;
        // 创建迭代器
        this.iterator = this.dataSupplierList.iterator();
        if (this.iterator.hasNext()) {
            this.currentSupplier = this.iterator.next();
        }
    }

    @Override
    public ByteChunk get() throws ScxIOException {
        while (currentSupplier != null) {
            var dataNode = currentSupplier.get();
            if (dataNode != null) {
                return dataNode;
            }
            if (closeOnSwitch) {
                currentSupplier.close();
            }
            if (iterator.hasNext()) {
                currentSupplier = iterator.next();
            } else {
                currentSupplier = null;
            }
        }
        return null; // 所有 DataSupplier 都返回 null, 表示结束
    }

    /// 需要保证在任何情况下都能尽可能的关闭 所有的资源(包括剩余的)
    @Override
    public void close() throws ScxIOException {
        ScxIOException ex = null;

        if (closeOnSwitch) {
            while (currentSupplier != null) {
                try {
                    currentSupplier.close();
                } catch (ScxIOException e) {
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
        } else {
            for (var byteSupplier : dataSupplierList) {
                try {
                    byteSupplier.close();
                } catch (ScxIOException e) {
                    if (ex == null) {
                        ex = e;
                    } else {
                        ex.addSuppressed(e);
                    }
                }
            }
        }

        if (ex != null) {
            throw ex;
        }

    }

}
