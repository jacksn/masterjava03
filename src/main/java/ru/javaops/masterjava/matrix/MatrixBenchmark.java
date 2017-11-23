package ru.javaops.masterjava.matrix;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode({Mode.SingleShotTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(1)
@Fork(5)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class MatrixBenchmark {

    // Matrix size
    private static final int MATRIX_SIZE = 1000;

//    @Param({"10"})
    private int threadCount = 10;

    private static int[][] matrixA;
    private static int[][] matrixB;

    @Setup
    public void setUp() {
        matrixA = MatrixUtil.create(MATRIX_SIZE);
        matrixB = MatrixUtil.create(MATRIX_SIZE);
    }

    private ExecutorService executor;

//    @Benchmark
    public int[][] singleThreadMultiplyOpt() throws Exception {
        return MatrixUtil.singleThreadMultiply(matrixA, matrixB);
    }

    @Benchmark
    public int[][] concurrentMultiply() throws Exception {
        return MatrixUtil.concurrentMultiply(matrixA, matrixB, executor);
    }

    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(threadCount);
    }

    @TearDown
    public void tearDown() {
        executor.shutdown();
    }
}
/*
Benchmark                                (threadCount)  Mode  Cnt    Score   Error  Units
MatrixBenchmark.concurrentMultiply                   2    ss   25  117,186 ±  1,210  ms/op
MatrixBenchmark.concurrentMultiply                   4    ss   25   64,194 ±  1,728  ms/op
MatrixBenchmark.concurrentMultiply                   8    ss   25   62,603 ±  1,596  ms/op
MatrixBenchmark.concurrentMultiply                  12    ss   25   62,340 ±  0,909  ms/op
MatrixBenchmark.singleThreadMultiplyOpt              2    ss   25  241,068 ± 19,372  ms/op
MatrixBenchmark.singleThreadMultiplyOpt              4    ss   25  233,630 ±  2,760  ms/op
MatrixBenchmark.singleThreadMultiplyOpt              8    ss   25  234,850 ±  3,355  ms/op
MatrixBenchmark.singleThreadMultiplyOpt             12    ss   25  233,162 ±  2,910  ms/op
*/