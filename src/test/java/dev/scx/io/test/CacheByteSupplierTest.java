package dev.scx.io.test;

import dev.scx.io.ScxIO;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.supplier.CacheByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import static dev.scx.io.supplier.ClosePolicyByteSupplier.singleClose;

public class CacheByteSupplierTest {

    public static void main(String[] args) throws InputAlreadyClosedException, ScxInputException {
        test1();
    }

    @Test
    public static void test1() throws InputAlreadyClosedException, ScxInputException {
        var rawInput = ScxIO.createByteInput("123456789abc".getBytes());

        // cacheByteSupplier 往往需要和 singleCloseSupplier 组合使用
        var cacheByteSupplier = new CacheByteSupplier(rawInput);
        var singleCloseSupplier = singleClose(cacheByteSupplier);

        var cacheInput1 = new DefaultByteInput(singleCloseSupplier);
        var bytes1 = new String(cacheInput1.readAll());
        cacheInput1.close();

        cacheByteSupplier.reset();

        var cacheInput2 = new DefaultByteInput(singleCloseSupplier);
        var bytes2 = new String(cacheInput2.readAll());
        cacheInput2.close();

        Assert.assertEquals(bytes1, "123456789abc");
        Assert.assertEquals(bytes2, "123456789abc");
        // 底层也应该被 close 了
        Assert.assertEquals(rawInput.isClosed(), true);

    }

}
