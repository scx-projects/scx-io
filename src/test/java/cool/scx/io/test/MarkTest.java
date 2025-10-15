package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.adapter.ByteInputAdapter;
import cool.scx.io.supplier.ByteArrayByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class MarkTest {

    public static void main(String[] args) throws IOException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws IOException {
        var data = "你好".repeat(100) + "终结符";
        var s = new DefaultByteInput(new ByteArrayByteSupplier(data.getBytes()));
        s.mark();
        var b1 = s.readUntil("终结符".getBytes());
        s.reset();
        byte[] b2 = s.readUntil("终结符".getBytes());
        Assert.assertEquals(b1, b2);
    }

    @Test
    public static void test2() throws IOException {
        var data = "你好".repeat(100) + "终结符";
        var s = ByteInputAdapter.byteInputToInputStream(new DefaultByteInput(new ByteArrayByteSupplier(data.getBytes())));
        s.mark(0);
        var b1 = s.readNBytes(200);
        s.reset();
        byte[] b2 = s.readNBytes(200);
        Assert.assertEquals(b1, b2);
    }

}
