package dev.scx.io.test;

import dev.scx.io.ScxIO;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.EagerByteArrayByteOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static dev.scx.io.ScxIO.gzip;

public class GzipByteOutputTest {

    public static void main(String[] args) throws IOException, OutputAlreadyClosedException, ScxOutputException {
        test1();
        test2();
        test3();
    }

    @Test
    public static void test1() throws IOException, OutputAlreadyClosedException, ScxOutputException {
        var bao = new EagerByteArrayByteOutput();
        var byteOutput = ScxIO.gzipByteOutput(bao);

        // 测试 空块写入
        byteOutput.write(new byte[0]);

        for (int i = 0; i < 1000; i++) {
            byteOutput.write(("abcd" + i).getBytes());
        }

        // 测试 空块写入
        byteOutput.write(new byte[0]);

        byteOutput.close();

        // jdk 的写入方式
        var ba = new ByteArrayOutputStream();
        var gba = new GZIPOutputStream(ba);
        // 测试 空块写入
        gba.write(new byte[0]);

        for (int i = 0; i < 1000; i++) {
            gba.write(("abcd" + i).getBytes());
        }
        // 测试 空块写入
        gba.write(new byte[0]);

        gba.close();

        // 测试 2者 结果是否相等
        Assert.assertEquals(bao.bytes(), ba.toByteArray());
    }

    // 测试空流
    @Test
    public static void test2() throws IOException, OutputAlreadyClosedException, ScxOutputException {
        var bao = new EagerByteArrayByteOutput();
        var byteOutput = ScxIO.gzipByteOutput(bao);

        byteOutput.close();

        // jdk 的写入方式
        var ba = new ByteArrayOutputStream();
        var gba = new GZIPOutputStream(ba);

        gba.close();

        // 测试 2者 结果是否相等
        Assert.assertEquals(bao.bytes(), ba.toByteArray());
    }

    @Test
    public static void test3() {
        var data = "abcdef👩.123133👱‍♂️🎎🖼🎞🎾🏓🥌⛸".repeat(300).getBytes();
        byte[] gzip = gzip(data);
        Assert.assertEquals(gzip.length, 153);
    }

}
