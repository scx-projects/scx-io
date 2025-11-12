package cool.scx.io.test;

import cool.scx.io.ByteChunk;
import cool.scx.io.OutputStreamByteOutput;
import cool.scx.io.ScxIO;
import cool.scx.io.exception.AlreadyClosedException;
import cool.scx.io.exception.ScxIOException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GzipByteOutputTest {

    public static void main(String[] args) throws IOException, AlreadyClosedException, ScxIOException {
        test1();
    }

    @Test
    public static void test1() throws IOException, AlreadyClosedException, ScxIOException {
        var bao = new ByteArrayOutputStream();
        var gzipByteOutput = new OutputStreamByteOutput(bao);

        var byteOutput = ScxIO.gzipByteOutput(gzipByteOutput);

        byteOutput.write(ByteChunk.of("abcd"));

        Assert.assertEquals(bao.toByteArray(), new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1});

    }

}
