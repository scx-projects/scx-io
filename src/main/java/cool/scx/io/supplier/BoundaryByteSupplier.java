package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteInput;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.KMPByteIndexer;

import java.util.Arrays;

// todo 待重构

/// BoundaryByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class BoundaryByteSupplier implements ByteSupplier {

    private final ByteInput byteInput;
    private final KMPByteIndexer byteIndexer;
    private final int bufferLength;
    private final boolean autoClose;
    private boolean isFinish;

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes) {
        this(byteInput, boundaryBytes, 8192, false);
    }

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes, boolean autoClose) {
        this(byteInput, boundaryBytes, 8192, autoClose);
    }

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes, int bufferLength) {
        this(byteInput, boundaryBytes, bufferLength, false);
    }

    public BoundaryByteSupplier(ByteInput byteInput, byte[] boundaryBytes, int bufferLength, boolean autoClose) {
        this.byteInput = byteInput;
        this.byteIndexer = new KMPByteIndexer(boundaryBytes);
        this.bufferLength = bufferLength;
        this.autoClose = autoClose;
        this.isFinish = false;
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException {
        //完成了就永远返回 null
        if (isFinish) {
            return null;
        }
        try {
            // 在 bufferLength 范围内查找
            var index = byteInput.indexOf(byteIndexer, bufferLength);
            // 找到了就全部返回
            var read = byteInput.readFully((int) index);
            // 找到了就要标识为完成
            isFinish = true;
            return new ByteChunk(read);
        } catch (NoMatchFoundException e) {
            // 没找到 说明可能还有 继续读取
            // 为了防止误读这里检查 matchedLength,  若为 0, 表示可以安全读
            if (byteIndexer.matchedLength() == 0) {
                var read = byteInput.readFully(bufferLength);
                return new ByteChunk(read);
            } else {
                // 已经匹配到了一部分 检查是否是 真正的匹配
                var peek = byteInput.peekFully(byteIndexer.pattern().length);
                // 完全匹配 返回 null
                var match = Arrays.equals(peek, byteIndexer.pattern());
                if (match) {
                    isFinish = true;
                    return null;
                } else {
                    // 不匹配 继续读 这里注意 skip 索引
                    byteInput.skipFully(byteIndexer.pattern().length);
                    return new ByteChunk(peek);
                }
            }
        } catch (NoMoreDataException e) {
            // 如果底层 ByteReader 没数据了, 也返回 null
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        if (autoClose) {
            byteInput.close();
        }
    }

}
