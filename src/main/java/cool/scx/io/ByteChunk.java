package cool.scx.io;

/// ByteChunk
///
/// @author scx567888
/// @version 0.0.1
public final class ByteChunk {

    public final byte[] bytes;
    public final int start;
    public final int end;
    public final int length;

    public ByteChunk(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public ByteChunk(byte[] bytes, int start, int end) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.length = end - start;
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
