package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.LineBreakByteIndexer;
import cool.scx.io.supplier.InputStreamByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class LineBreakIndexerTest {

    public static void main(String[] args) throws NoMoreDataException, AlreadyClosedException, NoMatchFoundException, ScxIOException {
        test1();
    }

    @Test
    public static void test1() throws NoMoreDataException, AlreadyClosedException, NoMatchFoundException, ScxIOException {
        var str = """
            123
            456
            789000\r
            abc\r\r

            def\r
            """;

        for (int i = 1; i < 1000; i++) {
            var rawDataInput = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));

            var indexer = new LineBreakByteIndexer();
            var str1 = new String(rawDataInput.readUntil(indexer));
            var str2 = new String(rawDataInput.readUntil(indexer));
            var str3 = new String(rawDataInput.readUntil(indexer));
            var str4 = new String(rawDataInput.readUntil(indexer));
            var str5 = new String(rawDataInput.readUntil(indexer));
            var str6 = new String(rawDataInput.readUntil(indexer));

            Assert.assertEquals(str1, "123");
            Assert.assertEquals(str2, "456");
            Assert.assertEquals(str3, "789000");
            Assert.assertEquals(str4, "abc\r");
            Assert.assertEquals(str5, "");
            Assert.assertEquals(str6, "def");
        }

    }

}
