package cool.scx.io.test;

import cool.scx.io.DefaultByteInput;
import cool.scx.io.ScxIO;
import cool.scx.io.supplier.LimitLengthByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import static cool.scx.io.supplier.ClosePolicyByteSupplier.noCloseDrain;

public class NoCloseDrainByteSupplierTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var rawInput = ScxIO.createByteInput("123456789".getBytes());
        // 创建 固定长度 子流
        var subInput = new DefaultByteInput(noCloseDrain(new LimitLengthByteSupplier(rawInput, 3)));
        // 不读取直接 close
        subInput.close();
        var result = new String(rawInput.readAll());
        // 检查消耗
        Assert.assertEquals(result, "456789");
    }

}
