package dev.scx.io.consumer;

import dev.scx.io.ByteChunk;

import java.util.Arrays;

/// EagerByteArrayByteConsumer
///
/// eager 模式: 每次 accept 都立即将数据拷贝到内部连续 byte[] (类似 [java.io.ByteArrayOutputStream]).
/// 适合: 小块数据 + 高频 accept, 或需要频繁获取最终 bytes()/chunk() 的场景.
/// 代价: 随着数据增长可能发生扩容与数组复制; 在大块数据快速增长时, 复制成本可能显著提高, 可能慢于 [LazyByteArrayByteConsumer].
///
/// 说明: 具体性能取决于 chunk 大小与 accept 次数; 本说明仅描述适用场景与代价.
///
/// @author scx567888
/// @version 0.0.1
public final class EagerByteArrayByteConsumer implements ByteConsumer {

    private byte[] bytes;
    private int total;

    public EagerByteArrayByteConsumer() {
        this(32);
    }

    public EagerByteArrayByteConsumer(int size) {
        this.bytes = new byte[size];
        this.total = 0;
    }

    /// 确保容量
    ///
    /// @param minCapacity 最小需要的容量
    private void ensureCapacity(int minCapacity) {
        // 旧容量
        int oldCapacity = bytes.length;
        // 最小需要的扩容长度
        int minGrowth = minCapacity - oldCapacity;
        // 不需要扩容
        if (minGrowth <= 0) {
            return;
        }
        // 计算新容量
        // 至少满足本次写入所需容量(minGrowth)
        // 若本次增长较小, 则按翻倍策略(oldCapacity)扩容, 减少后续扩容次数.
        var newLength = oldCapacity + Math.max(minGrowth, oldCapacity);
        // 扩容 bytes
        bytes = Arrays.copyOf(bytes, newLength);
    }

    @Override
    public boolean accept(ByteChunk chunk) {
        ensureCapacity(total + chunk.length);
        System.arraycopy(chunk.bytes, chunk.start, bytes, total, chunk.length);
        total += chunk.length;
        return true;
    }

    public byte[] bytes() {
        return Arrays.copyOf(bytes, total);
    }

    public int size() {
        return total;
    }

}
