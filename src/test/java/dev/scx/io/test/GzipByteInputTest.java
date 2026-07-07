package dev.scx.io.test;

import dev.scx.io.ScxIO;
import dev.scx.io.exception.*;
import dev.scx.io.input.DefaultByteInput;
import dev.scx.io.supplier.ByteArrayByteSupplier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static dev.scx.io.ScxIO.gzip;

public class GzipByteInputTest {

    public static void main(String[] args) throws IOException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException, ScxOutputException {
        test1();
    }

    @Test
    public static void test1() throws IOException, InputAlreadyClosedException, NoMatchFoundException, ScxInputException, ScxOutputException {
        var rawStr = "1234567890😀🥀👨‍🦰🌵中文!@$%^&*()()";
        var rawData = rawStr.getBytes();

        var gzipData = gzip(rawData);

        var s = new ArrayList<byte[]>();
        for (int i = 0; i < 100; i = i + 1) {
            s.add(gzipData);
        }

        s.add(gzip("\r\n".getBytes()));

        var gzipByteInput = new DefaultByteInput(new ByteArrayByteSupplier(s));

        var byteInput = ScxIO.gzipByteInput(gzipByteInput);

        while (true) {
            try {
                // 每次 读取的应该都是 和 提供器提供的一样 (也就是非阻塞读取)
                var data = byteInput.readUntil("\r\n".getBytes());
                var str = new String(data);

                Assert.assertEquals(str, rawStr.repeat(100));
            } catch (NoMoreDataException e) {
                // 忽略
                break;
            }
        }

    }

}
