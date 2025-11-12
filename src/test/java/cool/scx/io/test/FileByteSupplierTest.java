package cool.scx.io.test;

import cool.scx.io.ScxIO;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileByteSupplierTest {

    public static void main(String[] args) throws IOException {
        test1();
    }

    @Test
    public static void test1() throws IOException {
        var tempFile = File.createTempFile("test1", ".tmp");
        tempFile.deleteOnExit(); // JVM 退出时删除

        // 写入临时数据
        try (var raf = new RandomAccessFile(tempFile, "rw")) {
            raf.write("123456789abc".getBytes());
        }

        // 测试完全读取
        try (var byteInput1 = ScxIO.createByteInput(tempFile)) {
            var str1 = new String(byteInput1.readAll());

            Assert.assertEquals(str1, "123456789abc");
        }

        // 测试 部分读取
        try (var byteInput1 = ScxIO.createByteInput(tempFile, 3, 5)) {
            var str1 = new String(byteInput1.readAll());

            Assert.assertEquals(str1, "45678");
        }

    }

}
