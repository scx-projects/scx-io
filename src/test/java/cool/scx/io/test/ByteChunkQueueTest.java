package cool.scx.io.test;

import cool.scx.io.ByteChunk;
import cool.scx.io.support.ByteChunkQueue;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ByteChunkQueueTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var s = new ByteChunkQueue();
        s.append(new ByteChunk("abc".getBytes()));
        s.append(new ByteChunk("123".getBytes()));
        var next1 = s.next();
        var next2 = s.next();
        var next3 = s.next();
        Assert.assertEquals(next1.toString(), "abc");
        Assert.assertEquals(next2.toString(), "123");
        Assert.assertNull(next3);
    }

}
