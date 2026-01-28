package dev.scx.io.test;

import dev.scx.io.ByteChunk;
import dev.scx.io.ScxIO;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.EagerByteArrayByteOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class GzipByteOutputTest {

    public static void main(String[] args) throws IOException, OutputAlreadyClosedException, ScxOutputException {
        test1();
    }

    @Test
    public static void test1() throws IOException, OutputAlreadyClosedException, ScxOutputException {
        var bao = new EagerByteArrayByteOutput();
        var byteOutput = ScxIO.gzipByteOutput(bao);

        // 测试 空块写入
        byteOutput.write(new byte[0]);

        byteOutput.write(ByteChunk.of("abcd".getBytes()));
        // 测试 空块写入
        byteOutput.write(new byte[0]);

        Assert.assertEquals(bao.bytes(), new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1});

    }

}
