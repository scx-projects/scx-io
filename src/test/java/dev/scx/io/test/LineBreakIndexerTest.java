package dev.scx.io.test;

import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMatchFoundException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.indexer.LineBreakByteIndexer;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.supplier.InputStreamByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class LineBreakIndexerTest {

    public static void main(String[] args) throws NoMoreDataException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException {
        test1();
    }

    @Test
    public static void test1() throws NoMoreDataException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException {
        var str = """
            123
            456
            789000\r
            abc\r\r

            def\r
            """;

        for (int i = 1; i < 1000; i = i + 1) {
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
