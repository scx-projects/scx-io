package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.ScxIO;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.NoMatchFoundException;
import cool.scx.io.exception.NoMoreDataException;
import cool.scx.io.exception.ScxIOException;
import cool.scx.io.supplier.ByteArrayByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class MarkTest {

    public static void main(String[] args) throws IOException, NoMoreDataException, AlreadyClosedException, NoMatchFoundException, ScxIOException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws IOException, AlreadyClosedException, NoMoreDataException, NoMatchFoundException, ScxIOException {
        var data = "你好".repeat(100) + "终结符";
        var s = new DefaultByteInput(new ByteArrayByteSupplier(data.getBytes()));
        var mark = s.mark();
        var b1 = s.readUntil("终结符".getBytes());
        mark.reset();
        byte[] b2 = s.readUntil("终结符".getBytes());
        Assert.assertEquals(b1, b2);
    }

    @Test
    public static void test2() throws IOException {
        var data = "你好".repeat(100) + "终结符";
        var s = ScxIO.byteInputToInputStream(new DefaultByteInput(new ByteArrayByteSupplier(data.getBytes())));
        s.mark(0);
        var b1 = s.readNBytes(200);
        s.reset();
        byte[] b2 = s.readNBytes(200);
        Assert.assertEquals(b1, b2);
    }

}
