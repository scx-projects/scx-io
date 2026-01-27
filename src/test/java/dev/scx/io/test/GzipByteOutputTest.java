package dev.scx.io.test;

import dev.scx.io.ByteChunk;
import dev.scx.io.ScxIO;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.OutputStreamByteOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GzipByteOutputTest {

    public static void main(String[] args) throws IOException, OutputAlreadyClosedException, ScxOutputException {
        test1();
    }

    @Test
    public static void test1() throws IOException, OutputAlreadyClosedException, ScxOutputException {
        var bao = new ByteArrayOutputStream();
        var gzipByteOutput = new OutputStreamByteOutput(bao);

        var byteOutput = ScxIO.gzipByteOutput(gzipByteOutput);

        byteOutput.write(ByteChunk.of("abcd".getBytes()));

        Assert.assertEquals(bao.toByteArray(), new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1});

    }

}
