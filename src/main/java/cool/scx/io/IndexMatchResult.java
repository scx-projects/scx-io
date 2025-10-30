package cool.scx.io;

/// IndexMatchResult
///
/// @author scx567888
/// @version 0.0.1
public final class IndexMatchResult {

    public static final IndexMatchResult EMPTY_MATCH_RESULT = new IndexMatchResult(0, 0);

    public final long index;

    public final int matchedLength;

    public IndexMatchResult(long index, int matchedLength) {
        this.index = index;
        this.matchedLength = matchedLength;
    }

}
