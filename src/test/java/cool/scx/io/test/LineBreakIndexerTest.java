package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.indexer.LineBreakIndexer;
import cool.scx.io.supplier.InputStreamByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class LineBreakIndexerTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var str = """
            123
            456
            789000\r
            abc\r\r
            def\r
            """;
        var rawDataInput = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 1));

        var str1 = new String(rawDataInput.readUntil(new LineBreakIndexer()));
        var str2 = new String(rawDataInput.readUntil(new LineBreakIndexer()));
        var str3 = new String(rawDataInput.readUntil(new LineBreakIndexer()));
        var str4 = new String(rawDataInput.readUntil(new LineBreakIndexer()));
        var str5 = new String(rawDataInput.readUntil(new LineBreakIndexer()));
        var str6 = new String(rawDataInput.readUntil(new LineBreakIndexer()));

        Assert.assertEquals(str1, "123");
        Assert.assertEquals(str2, "456");
        Assert.assertEquals(str3, "789000");
        Assert.assertEquals(str4, "abc");
        Assert.assertEquals(str5, "");
        Assert.assertEquals(str6, "def");

    }

}
