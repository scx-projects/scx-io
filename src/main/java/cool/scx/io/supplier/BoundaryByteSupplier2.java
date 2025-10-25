package cool.scx.io.supplier;

import cool.scx.io.ByteChunk;
import cool.scx.io.ByteChunkQueue;
import cool.scx.io.ByteInput;
import cool.scx.io.consumer.ByteChunkByteConsumer;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.KMPByteIndexer;


/// BoundaryByteSupplier
///
/// @author scx567888
/// @version 0.0.1
public final class BoundaryByteSupplier2 implements ByteSupplier {

    private final ByteInput byteInput;
    private final KMPByteIndexer byteIndexer;
    private final boolean autoClose;
    private final ByteChunkByteConsumer consumer;
    private final ByteChunkQueue cache;
    private boolean isFinish;

    public BoundaryByteSupplier2(ByteInput byteInput, byte[] boundaryBytes) {
        this(byteInput, boundaryBytes, false);
    }

    public BoundaryByteSupplier2(ByteInput byteInput, byte[] boundaryBytes, boolean autoClose) {
        this.byteInput = byteInput;
        this.byteIndexer = new KMPByteIndexer(boundaryBytes);
        this.autoClose = autoClose;
        this.consumer = new ByteChunkByteConsumer();
        this.cache = new ByteChunkQueue();
        this.isFinish = this.byteIndexer.isEmptyPattern();
    }

    @Override
    public ByteChunk get() throws AlreadyClosedException, ScxIOException, NoMoreDataException {
        //完成了就永远返回 null
        if (isFinish) {
            return null;
        }
        byteInput.read(consumer, Long.MAX_VALUE);
        var byteChunk = consumer.byteChunk();
        var i = byteIndexer.indexOf(byteChunk);
        return byteChunk;
    }

    @Override
    public void close() throws Exception {
        if (autoClose) {
            byteInput.close();
        }
    }

}
