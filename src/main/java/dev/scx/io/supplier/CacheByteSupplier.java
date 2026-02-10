package dev.scx.io.supplier;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.ScxInputException;

import java.util.ArrayList;
import java.util.List;

/// CacheByteSupplier
///
/// 可以缓存上游 byteSupplier, 调用 reset 可以从头回放 (指创建 CacheByteSupplier 的瞬间)
///
/// @author scx567888
/// @version 0.0.1
public final class CacheByteSupplier implements ByteSupplier {

    private final ByteSupplier byteSupplier;
    private final List<ByteChunk> cache;
    private int chunkIndex;
    private boolean isFinish;

    public CacheByteSupplier(ByteSupplier byteSupplier) {
        this.byteSupplier = byteSupplier;
        this.cache = new ArrayList<>();
        this.chunkIndex = 0;
        this.isFinish = false;
    }

    @Override
    public ByteChunk get() throws ScxInputException {

        // 1, 如果允许使用缓存, 优先使用缓存
        if (chunkIndex < cache.size()) {
            var chunk = cache.get(chunkIndex);
            chunkIndex = chunkIndex + 1;
            return chunk;
        }

        // 2, 完成了就永远返回 null
        if (isFinish) {
            return null;
        }

        var chunk = byteSupplier.get();

        // 3, 遇到 EOF
        if (chunk == null) {
            isFinish = true;
            return null;
        }

        // 4, 即使是空块 我们也 保存, 保证对上游的 0 干涉.
        cache.add(chunk);
        chunkIndex = chunkIndex + 1;
        return chunk;

    }

    @Override
    public void close() throws ScxInputException {
        // 此处依赖上游 ByteSupplier 的 close 幂等.
        byteSupplier.close();
    }

    public void reset() {
        chunkIndex = 0;
    }

}
