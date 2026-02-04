package dev.scx.io.test;

import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxOutputException;
import dev.scx.io.output.ByteArrayByteOutput;
import org.testng.annotations.Test;

public class ByteArrayByteOutputTest {

    public static void main(String[] args) throws ScxOutputException, OutputAlreadyClosedException {
        test1();
        test2();
        test3();
        test4();
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    @Test
    public static void test1() throws ScxOutputException, OutputAlreadyClosedException {
        byte[] data = new byte[8192 * 2];

        for (int j = 0; j < 50; j = j + 1) {
            var start = System.nanoTime();
            var s = new ByteArrayByteOutput();

            for (int i = 0; i < 5000; i = i + 1) {
                s.write(data);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;

            System.out.println("大数据块少次数写入 : " + (eTime / 1000_000));
        }

    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    @Test
    public static void test2() throws ScxOutputException, OutputAlreadyClosedException {
        byte[] data = new byte[64];

        for (int j = 0; j < 50; j = j + 1) {
            var start = System.nanoTime();
            var s = new ByteArrayByteOutput();

            for (int i = 0; i < 500000; i = i + 1) {
                s.write(data);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;

            System.out.println("小数据块多次数写入 : " + (eTime / 1000_000));
        }
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    @Test
    public static void test3() throws ScxOutputException, OutputAlreadyClosedException {
        byte[] data = new byte[8192 * 2];

        for (int j = 0; j < 50; j = j + 1) {
            var start = System.nanoTime();

            for (int i = 0; i < 50000; i = i + 1) {
                var s = new ByteArrayByteOutput();
                s.write(data);
                byte[] bytes = s.bytes();
            }

            var eTime = System.nanoTime() - start;

            System.out.println("临时创建只写入一次 : " + (eTime / 1000_000));
        }
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    @Test
    public static void test4() throws OutputAlreadyClosedException {

        for (int j = 0; j < 50; j = j + 1) {
            var start = System.nanoTime();
            var s = new ByteArrayByteOutput();

            for (int i = 0; i < 500000; i = i + 1) {
                s.write((byte) 88);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;

            System.out.println("单字节多次数写入 : " + (eTime / 1000_000));
        }
    }

}
