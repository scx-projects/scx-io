package dev.scx.io.test;

import dev.scx.io.ScxIO;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.supplier.LimitLengthByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import static dev.scx.io.ScxIO.drainOnCloseNoClose;

public class NoCloseDrainByteSupplierTest {

    public static void main(String[] args) throws InputAlreadyClosedException, ScxInputException {
        test1();
    }

    @Test
    public static void test1() throws InputAlreadyClosedException, ScxInputException {
        var rawInput = ScxIO.createByteInput("123456789".getBytes());
        // 创建 固定长度 子流
        var subInput = new DefaultByteInput(drainOnCloseNoClose(new LimitLengthByteSupplier(rawInput, 3)));
        // 不读取直接 close
        subInput.close();
        var result = new String(rawInput.readAll());
        // 检查消耗
        Assert.assertEquals(result, "456789");
        // 检查上层是否被关闭
        Assert.assertEquals(rawInput.isClosed(), false);
    }

}
