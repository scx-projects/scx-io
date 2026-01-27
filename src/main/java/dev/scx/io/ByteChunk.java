package dev.scx.io;

/// ByteChunk
///
/// 表示 byte[] 上的一个区间视图 [start, end).
///
/// - ByteChunk 本身不拥有数据, 多个 ByteChunk 可能共享同一个 backing byte[].
/// - 所有索引参数 (如 getByte / toString / subChunk 中的索引) 均为 "相对索引", 即相对于当前 ByteChunk 的起点 (逻辑索引 0).
/// - ByteChunk 支持通过 subChunk 在当前视图基础上继续创建子视图, 不会发生数据拷贝.
///
/// 为了性能考虑, 本类不做任何边界检查, 调用者需保证参数合法.
///
/// @author scx567888
/// @version 0.0.1
public final class ByteChunk {

    public static final ByteChunk EMPTY_BYTE_CHUNK = ByteChunk.of();

    public final byte[] bytes;
    public final int start;
    public final int end;
    public final int length;

    private ByteChunk(byte[] bytes, int start, int end) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.length = end - start;
    }

    public static ByteChunk of(byte... bytes) {
        return new ByteChunk(bytes, 0, bytes.length);
    }

    public static ByteChunk of(byte[] bytes, int start, int end) {
        return new ByteChunk(bytes, start, end);
    }

    public byte getByte(int index) {
        return bytes[start + index];
    }

    public byte[] getBytes(int start, int end) {
        if (start == 0 && end == this.length && this.start == 0 && this.length == this.bytes.length) {
            return this.bytes;
        }
        var data = new byte[end - start];
        System.arraycopy(this.bytes, this.start + start, data, 0, data.length);
        return data;
    }

    public byte[] getBytes() {
        return getBytes(0, length);
    }

    public String toString(int start, int end) {
        return new String(bytes, this.start + start, end - start);
    }

    @Override
    public String toString() {
        return toString(0, length);
    }

    public ByteChunk subChunk(int start, int end) {
        if (start == 0 && end == this.length) {
            return this;
        }
        return new ByteChunk(this.bytes, this.start + start, this.start + end);
    }

}
