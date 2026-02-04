package dev.scx.io.test;

import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.ByteArrayByteOutput;
import dev.scx.io.output.LengthBoundedByteOutput;
import dev.scx.io.output.NoCloseByteOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ByteOutputTest {

    public static void main(String[] args) throws OutputAlreadyClosedException, ScxOutputException {
        test1();
        test2();
        test3();
        test4();
    }

    @Test
    public static void test1() throws OutputAlreadyClosedException, ScxOutputException {
        var defaultByteOutput = new ByteArrayByteOutput();
        try (defaultByteOutput) {

            defaultByteOutput.write("abc".getBytes());
            defaultByteOutput.write("def".getBytes());
            defaultByteOutput.write("ghi".getBytes());
        }

        var str = new String(defaultByteOutput.bytes());
        Assert.assertEquals(str, "abcdefghi");
        Assert.assertEquals(defaultByteOutput.isClosed(), true);
    }


    @Test
    public static void test2() throws OutputAlreadyClosedException, ScxOutputException {
        var defaultByteOutput = new ByteArrayByteOutput();
        var lengthBoundedOutput = new LengthBoundedByteOutput(defaultByteOutput, 6, 6);
        // 测试多写入
        try (lengthBoundedOutput) {

            lengthBoundedOutput.write("abc".getBytes());
            lengthBoundedOutput.write("def".getBytes());

            Assert.assertThrows(ScxOutputException.class, () -> lengthBoundedOutput.write("ghi".getBytes()));
        }

    }

    @Test
    public static void test3() {
        var defaultByteOutput = new ByteArrayByteOutput();
        var lengthBoundedOutput = new LengthBoundedByteOutput(defaultByteOutput, 6, 6);
        //测试少写入
        Assert.assertThrows(ScxOutputException.class, () -> {
            try (lengthBoundedOutput) {

                lengthBoundedOutput.write("abc".getBytes());
                lengthBoundedOutput.write("de".getBytes());

            }
        });
    }

    @Test
    public static void test4() throws OutputAlreadyClosedException, ScxOutputException {
        var defaultByteOutput = new ByteArrayByteOutput();
        var noCloseOutput = new NoCloseByteOutput(defaultByteOutput);
        //测试 不关闭

        try (noCloseOutput) {

            noCloseOutput.write("abc".getBytes());
            noCloseOutput.write("de".getBytes());

        }
        Assert.assertEquals(defaultByteOutput.isClosed(), false);
        Assert.assertEquals(noCloseOutput.isClosed(), true);

    }

}
