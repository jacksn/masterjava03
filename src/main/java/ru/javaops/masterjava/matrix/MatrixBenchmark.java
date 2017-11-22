package ru.javaops.masterjava.matrix;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode({Mode.SingleShotTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(1)
@Fork(10)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class MatrixBenchmark {

    // Matrix size
    private static final int MATRIX_SIZE = 1000;

    @Param({"3", "4", "10"})
    private int threadCount;

    private static int[][] matrixA;
    private static int[][] matrixB;

    @Setup
    public void setUp() {
        matrixA = MatrixUtil.create(MATRIX_SIZE);
        matrixB = MatrixUtil.create(MATRIX_SIZE);
    }

    private ExecutorService executor;

    @Benchmark
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
MatrixBenchmark.concurrentMultiply                   3    ss  100   91,624 ± 1,574  ms/op
MatrixBenchmark.concurrentMultiply                   4    ss  100   70,066 ± 1,098  ms/op
MatrixBenchmark.concurrentMultiply                  10    ss  100   70,248 ± 2,083  ms/op
MatrixBenchmark.singleThreadMultiplyOpt              3    ss  100  261,399 ± 5,686  ms/op
MatrixBenchmark.singleThreadMultiplyOpt              4    ss  100  261,453 ± 5,806  ms/op
MatrixBenchmark.singleThreadMultiplyOpt             10    ss  100  261,650 ± 5,544  ms/op
 */