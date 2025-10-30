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
    }

    @Test
    public static void test1() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiiihelloend enden f";
        for (int i = 1; i < 1000; i = i + 1) {
            var rawByteReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));
            var newByteReader = new DefaultByteInput(new BoundaryByteSupplier(rawByteReader, new BitMaskByteIndexer("hello".getBytes(StandardCharsets.UTF_8))));
            var read = newByteReader.readAll();
            Assert.assertEquals(new String(read), "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiii");
        }
    }

    @Test
    public static void test2() throws NoMoreDataException, AlreadyClosedException, ScxIOException {
        var str = "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiiihelloend enden f";
        for (int i = 1; i < 1000; i = i + 1) {
            var rawByteReader = new DefaultByteInput(new InputStreamByteSupplier(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), i));
            var newByteReader = new DefaultByteInput(new BoundaryByteSupplier(rawByteReader, new KMPByteIndexer("hello".getBytes(StandardCharsets.UTF_8))));
            var read = newByteReader.readAll();
            Assert.assertEquals(new String(read), "1234567890888866661111aaaabhellhellhellhhhhheeeeelllbbcccdddeeefffggghhhiii");
        }
    }

}
