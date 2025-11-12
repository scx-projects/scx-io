package cool.scx.io;

import java.nio.charset.Charset;

/// ByteChunk
///
/// 为了性能考虑 所有方法均没有 边界检查, 使用时请注意 参数正确
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

    public static ByteChunk of(String str) {
        return of(str.getBytes());
    }

    public static ByteChunk of(String str, Charset charset) {
        return of(str.getBytes(charset));
    }

    /// 相对 索引 0 起始
    public byte getByte(int index) {
        return bytes[start + index];
    }

    /// 相对 索引 0 起始
    public byte[] getBytes(int start, int end) {
        if (start == 0 && end == length && this.start == 0 && length == bytes.length) {
            return bytes;
        }
        var data = new byte[end - start];
        System.arraycopy(bytes, this.start + start, data, 0, data.length);
        return data;
    }

    /// 相对 索引 0 起始
    public byte[] getBytes(int start) {
        return getBytes(start, length);
    }

    /// 相对 索引 0 起始
    public byte[] getBytes() {
        return getBytes(0, length);
    }

    /// 相对 索引 0 起始
    public String toString(int start, int end) {
        return new String(bytes, this.start + start, end - start);
    }

    /// 相对 索引 0 起始
    public String toString(int start) {
        return toString(start, length);
    }

    @Override
    public String toString() {
        return toString(0, length);
    }

    /// 相对 索引 0 起始
    public ByteChunk subChunk(int start, int end) {
        if (start == 0 && end == length) {
            return this;
        }
        return new ByteChunk(bytes, this.start + start, this.start + end);
    }

}
