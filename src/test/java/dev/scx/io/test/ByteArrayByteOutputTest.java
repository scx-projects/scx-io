package dev.scx.io.test;

import dev.scx.io.output.EagerByteArrayByteOutput;
import dev.scx.io.output.LazyByteArrayByteOutput;
import org.testng.annotations.Test;

public class ByteArrayByteOutputTest {

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    /// 这种情况下 大概率 Lazy 更快.
    @Test
    public static void test1() {
        byte[] data = new byte[8192 * 2];

        for (int j = 0; j < 50; j++) {
            var start = System.nanoTime();
            var s = new EagerByteArrayByteOutput();

            for (int i = 0; i < 5000; i++) {
                s.write(data);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;
            start = System.nanoTime();

            var s1 = new LazyByteArrayByteOutput();

            for (int i = 0; i < 5000; i++) {
                s1.write(data);
            }

            byte[] bytes1 = s1.bytes();

            var lTime = System.nanoTime() - start;

            System.out.println("大数据块少次数写入: Eager : " + (eTime / 1000_000) + " Lazy : " + (lTime / 1000_000) + " (" + (eTime < lTime ? "Eager" : "Lazy") + " 更快)");
        }

    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    /// 这种情况下 不一定谁快, 但是 Lazy 会占用更多的内存(可能多很多), 而且 (性能/内存) 波动会很大 因为内部持有大量的 ByteChunkNode.
    @Test
    public static void test2() {
        byte[] data = new byte[64];

        for (int j = 0; j < 50; j++) {
            var start = System.nanoTime();
            var s = new EagerByteArrayByteOutput();

            for (int i = 0; i < 500000; i++) {
                s.write(data);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;
            start = System.nanoTime();

            var s1 = new LazyByteArrayByteOutput();

            for (int i = 0; i < 500000; i++) {
                s1.write(data);
            }

            byte[] bytes1 = s1.bytes();

            var lTime = System.nanoTime() - start;

            System.out.println("小数据块多次数写入: Eager : " + (eTime / 1000_000) + " Lazy : " + (lTime / 1000_000) + " (" + (eTime < lTime ? "Eager" : "Lazy") + " 更快)");
        }
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    /// 这种情况 Lazy 几乎零成本.
    @Test
    public static void test3() {
        byte[] data = new byte[8192 * 2];

        for (int j = 0; j < 50; j++) {
            var start = System.nanoTime();

            for (int i = 0; i < 50000; i++) {
                var s = new EagerByteArrayByteOutput();
                s.write(data);
                byte[] bytes = s.bytes();
            }

            var eTime = System.nanoTime() - start;
            start = System.nanoTime();

            for (int i = 0; i < 50000; i++) {
                var s1 = new LazyByteArrayByteOutput();
                s1.write(data);
                byte[] bytes1 = s1.bytes();
            }

            var lTime = System.nanoTime() - start;

            System.out.println("临时创建只写入一次: Eager : " + (eTime / 1000_000) + " Lazy : " + (lTime / 1000_000) + " (" + (eTime < lTime ? "Eager" : "Lazy") + " 更快)");
        }
    }

    /// 此测试不保证准确性, 仅用于粗略观察不同写入模式下的性能趋势, 不应作为最终性能结论.
    /// 这种情况下 Eager 应该会更快, 原因一样是因为 Lazy 中的 ByteChunkNode 会有很大的内存占用和性能损耗.
    @Test
    public static void test4() {

        for (int j = 0; j < 50; j++) {
            var start = System.nanoTime();
            var s = new EagerByteArrayByteOutput();

            for (int i = 0; i < 500000; i++) {
                s.write((byte) 88);
            }
            byte[] bytes = s.bytes();

            var eTime = System.nanoTime() - start;
            start = System.nanoTime();

            var s1 = new LazyByteArrayByteOutput();

            for (int i = 0; i < 500000; i++) {
                s1.write((byte) 88);
            }

            byte[] bytes1 = s1.bytes();

            var lTime = System.nanoTime() - start;

            System.out.println("单字节多次数写入: Eager : " + (eTime / 1000_000) + " Lazy : " + (lTime / 1000_000) + " (" + (eTime < lTime ? "Eager" : "Lazy") + " 更快)");
        }
    }

}
