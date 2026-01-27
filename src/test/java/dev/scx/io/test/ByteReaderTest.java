package dev.scx.io.test;

import dev.scx.io.ByteChunk;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.NoMatchFoundException;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.supplier.ByteArrayByteSupplier;
import dev.scx.io.supplier.SequenceByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class ByteReaderTest {

    public static void main(String[] args) throws NoMoreDataException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException {
        test1();
        test2();
        test3();
        test4();
        test5();
    }

    @Test
    public static void test1() throws NoMoreDataException, InputAlreadyClosedException, ScxInputException, NoMatchFoundException {
        var dataReader = new DefaultByteInput(new ByteArrayByteSupplier("11112345678".getBytes(StandardCharsets.UTF_8)));

        //不会影响读取
        dataReader.peek(99);

        dataReader.indexOf("1".getBytes(StandardCharsets.UTF_8));

        var index = dataReader.readUntil("123".getBytes());

        //第二次应该匹配失败
        Assert.assertThrows(NoMatchFoundException.class, () -> {
            var index1 = dataReader.readUntil("123".getBytes());
        });
    }

    @Test
    public static void test2() throws NoMoreDataException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException {
        var sp = new ByteArrayByteSupplier(
            "1234567890".getBytes(),
            "abcdefghi".getBytes(),
            "jklmnopqrst".getBytes(),
            "uvwzyz".getBytes(),
            "ddf".getBytes(),
            "1".getBytes(),
            "3".getBytes(),
            "5".getBytes(),
            "7".getBytes(),
            "9".getBytes()
        );

        var dataReader = new DefaultByteInput(sp);

        Assert.assertEquals(dataReader.indexOf("1".getBytes()).index, 0);
        Assert.assertEquals(dataReader.indexOf("123456789".getBytes()).index, 0);
        Assert.assertEquals(dataReader.indexOf("789".getBytes()).index, 6);
        Assert.assertEquals(dataReader.indexOf("df".getBytes()).index, 37);
        Assert.assertEquals(dataReader.indexOf("ijklmnopqrstu".getBytes()).index, 18);
        try {
            //应该匹配失败
            dataReader.indexOf("?".getBytes());
            throw new AssertionError();
        } catch (NoMatchFoundException _) {

        }

        Assert.assertEquals(dataReader.indexOf("3579".getBytes()).index, 40);

    }

    @Test
    public static void test3() {
        //测试资源耗尽攻击
        var dataReader = new DefaultByteInput(() -> ByteChunk.of("aaaaaa".getBytes()));
        //最大只搜索 100 字节
        Assert.assertThrows(NoMatchFoundException.class, () -> {
            byte[] bytes = dataReader.readUntil("\r\n".getBytes(), 100);
        });
    }

    @Test
    public static void test4() throws NoMoreDataException, InputAlreadyClosedException, ScxInputException {
        var d1 = new ByteArrayByteSupplier("123456aaabbb".getBytes());
        var d2 = new ByteArrayByteSupplier("cccddd456789".getBytes());
        var dataReader = new DefaultByteInput(new SequenceByteSupplier(d1, d2));
        var read = dataReader.readUpTo(Integer.MAX_VALUE);
        Assert.assertEquals(new String(read), "123456aaabbbcccddd456789");
    }

    @Test
    public static void test5() throws NoMoreDataException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException {
        var d1 = new ByteArrayByteSupplier("123456aaabbb".getBytes());
        var d2 = new ByteArrayByteSupplier("cccddd456789".getBytes());
        var dataReader = new DefaultByteInput(new SequenceByteSupplier(d1, d2));
        var read = dataReader.indexOf("bbbccc".getBytes());
        Assert.assertEquals(read.index, 9);
    }


}
