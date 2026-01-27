package dev.scx.io;

/// ByteMatchResult
///
/// @author scx567888
/// @version 0.0.1
public final class ByteMatchResult {

    public final long index;
    public final int matchedLength;

    public ByteMatchResult(long index, int matchedLength) {
        this.index = index;
        this.matchedLength = matchedLength;
    }

    @Override
    public String toString() {
        return "ByteMatchResult[index=" + index + ", matchedLength=" + matchedLength + ']';
    }

}
