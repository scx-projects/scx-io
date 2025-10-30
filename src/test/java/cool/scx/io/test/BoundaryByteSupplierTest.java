package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.indexer.BitMaskByteIndexer;
import cool.scx.io.indexer.KMPByteIndexer;
import cool.scx.io.supplier.BoundaryByteSupplier;
import cool.scx.io.supplier.InputStreamByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class BoundaryByteSupplierTest {

    public static void main(String[] args) throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        test1();
        test2();
        test3();
        testBoundaryAtStart();
        testBoundaryAtEnd();
        testBoundaryAcrossChunks();
        testRepeatedBoundary();
        testEmptyStream();
        testOverlappingBoundary();
    }

    @Test
    public static void test1() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiiihelloend enden f";
        for (int i = 1; i < 1000; i = i + 1) {
            var rawByteReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));
            var newByteReader = new DefaultByteInput(new BoundaryByteSupplier(rawByteReader, new BitMaskByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true));
            var read = newByteReader.readAll();
            Assert.assertEquals(new String(read), "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiii");
            var bytes = rawByteReader.readAll();
            Assert.assertEquals(new String(bytes), "helloend enden f");
        }
    }

    @Test
    public static void test2() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiiihelloend enden f";
        for (int i = 1; i < 1000; i = i + 1) {
            var rawByteReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));
            var newByteReader = new DefaultByteInput(new BoundaryByteSupplier(rawByteReader, new BitMaskByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), false));
            var read = newByteReader.readAll();
            Assert.assertEquals(new String(read), "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiii");
            var bytes = rawByteReader.readAll();
            Assert.assertEquals(new String(bytes), "end enden f");
        }
    }

    @Test
    public static void test3() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiiihelloend enden f";
        for (int i = 1; i < 1000; i = i + 1) {
            var rawByteReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));
            var newByteReader = new DefaultByteInput(new BoundaryByteSupplier(rawByteReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true));
            var read = newByteReader.readAll();
            Assert.assertEquals(new String(read), "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiii");
            var bytes = rawByteReader.readAll();
            Assert.assertEquals(new String(bytes), "helloend enden f");
        }
    }

    /// 测试 boundary 在流开头
    @Test
    public static void testBoundaryAtStart() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "hello1234567890";
        var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 3));
        var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true);
        var result = new DefaultByteInput(supplier).readAll();
        Assert.assertEquals(new String(result), "");
        var remaining = rawReader.readAll();
        Assert.assertEquals(new String(remaining), "hello1234567890");
    }

    /// 测试 boundary 在流结尾
    @Test
    public static void testBoundaryAtEnd() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890hello";
        var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 4));
        var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true);
        var result = new DefaultByteInput(supplier).readAll();
        Assert.assertEquals(new String(result), "1234567890");
        var remaining = rawReader.readAll();
        Assert.assertEquals(new String(remaining), "hello");
    }

    /// 测试 boundary 跨多个 chunk
    @Test
    public static void testBoundaryAcrossChunks() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "12345hel" + "lo67890"; // boundary "hello" 跨 chunk
        for (int chunkSize = 1; chunkSize <= 5; chunkSize++) {
            var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), chunkSize));
            var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true);
            var result = new DefaultByteInput(supplier).readAll();
            Assert.assertEquals(new String(result), "12345");
            var remaining = rawReader.readAll();
            Assert.assertEquals(new String(remaining), "hello67890");
        }
    }

    /// 测试重复 boundary
    @Test
    public static void testRepeatedBoundary() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "abcaaaabcaaabhello";
        var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 2));
        var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("aaab".getBytes(StandardCharsets.UTF_8)), true);
        var result = new DefaultByteInput(supplier).readAll();
        Assert.assertEquals(new String(result), "abca");
        var remaining = rawReader.readAll();
        Assert.assertEquals(new String(remaining), "aaabcaaabhello");
    }

    /// 测试空流
    @Test
    public static void testEmptyStream() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "";
        var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 1));
        var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8)), true);
        var result = new DefaultByteInput(supplier).readAll();
        Assert.assertEquals(new String(result), "");
        var remaining = rawReader.readAll();
        Assert.assertEquals(new String(remaining), "");
    }

    /// 测试 boundary 重叠
    @Test
    public static void testOverlappingBoundary() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "aaaaaaab";
        var rawReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), 3));
        var supplier = new BoundaryByteSupplier(rawReader, new KMPByteIndexer("aaaab".getBytes(StandardCharsets.UTF_8)), true);
        var result = new DefaultByteInput(supplier).readAll();
        Assert.assertEquals(new String(result), "aaa");
        var remaining = rawReader.readAll();
        Assert.assertEquals(new String(remaining), "aaaab");
    }

}
