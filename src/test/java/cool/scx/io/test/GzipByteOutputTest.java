package cool.scx.io.test;

import cool.scx.io.OutputStreamByteOutput;
import cool.scx.io.adapter.ByteOutputAdapter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GzipByteOutputTest {

    public static void main(String[] args) throws IOException {
        test1();
    }

    @Test
    public static void test1() throws IOException {
        var bao = new ByteArrayOutputStream();
        var gzipByteOutput = new OutputStreamByteOutput(bao);

        var byteOutput = ByteOutputAdapter.outputStreamToByteOutput(new GZIPOutputStream(ByteOutputAdapter.byteOutputToOutputStream(gzipByteOutput)));

        byteOutput.write(new byte[]{'a', 'b', 'c', 'd'});

        Assert.assertEquals(bao.toByteArray(), new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1});

    }

}
