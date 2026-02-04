package dev.scx.io.test;

import dev.scx.io.ScxIO;
import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.ByteArrayByteOutput;
import dev.scx.io.output.LengthBoundedByteOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TransferToTest {

    public static void main() throws ScxOutputException, InputAlreadyClosedException, ScxInputException, OutputAlreadyClosedException {
        test1();
        test2();
        test3();
    }

    @Test
    public static void test1() throws ScxOutputException, InputAlreadyClosedException, ScxInputException, OutputAlreadyClosedException {
        var input = ScxIO.createByteInput(
            "123456789".getBytes(),
            "abcdefg".getBytes(),
            "!@#$%^*&(*(".getBytes(),
            "😀🥀😏👩❤👸⚡⛑".getBytes(),
            "o((>ω< ))o(╬▔皿▔)╯(～￣(OO)￣)ブ（︶^︶）".getBytes(),
            "     \r\n\t\n\r\t".getBytes()
        );
        var output = new ByteArrayByteOutput();
        ScxIO.transferToAll(input, output);
        var bytes = output.bytes();
        Assert.assertEquals(bytes.length, 126);
    }

    @Test
    public static void test2() throws OutputAlreadyClosedException {
        var input = ScxIO.createByteInput("123456789".getBytes());
        var output = new ByteArrayByteOutput();
        output.close();
        Assert.assertThrows(OutputAlreadyClosedException.class, () -> ScxIO.transferToAll(input, output));
    }

    @Test
    public static void test3() {
        var input = ScxIO.createByteInput("123456789".getBytes());
        var output = new LengthBoundedByteOutput(new ByteArrayByteOutput(), 3, 3);
        Assert.assertThrows(ScxOutputException.class, () -> ScxIO.transferToAll(input, output));
    }

}
