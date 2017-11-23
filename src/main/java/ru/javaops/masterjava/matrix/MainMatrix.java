package ru.javaops.masterjava.matrix;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Matrix size: 1000 <br>
 * Average single thread time, sec: 0,445 <br>
 * Average concurrent thread time, sec: 0,129 <br>
 * <br>
 * Optimized, removing transposition <br>
 * Average single thread time, sec: 0,232 <br>
 * Average concurrent thread time, sec: 0,072 <br>
 */
public class MainMatrix {
    private static final int MATRIX_SIZE = 1000;
    public static final int THREAD_COUNT = 8;
    public static final int CHUNK_SIZE = 20;
    private static final int PASS_COUNT = 5;

    private final static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
        final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);

        double singleThreadSum = 0.;
        double concurrentThreadSum = 0.;
        int count = 1;

        while (count <= PASS_COUNT) {
            System.out.println("Pass " + count);
            long start = System.currentTimeMillis();
            final int[][] matrixC = MatrixUtil.singleThreadMultiply(matrixA, matrixB);
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time, sec: %.3f", duration);
            singleThreadSum += duration;

            start = System.currentTimeMillis();
//            final int[][] concurrentMatrixC = MatrixUtil.concurrentMultiply(matrixA, matrixB, executor);
            final int[][] concurrentMatrixC = MatrixUtil.concurrentMultiplyFJP(matrixA, matrixB);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Concurrent thread time, sec: %.3f", duration);
            concurrentThreadSum += duration;

            if (!MatrixUtil.compare(matrixC, concurrentMatrixC)) {
                System.err.println("Comparison failed");
                break;
            }
            count++;
        }
        executor.shutdown();
        out("\nAverage single thread time, sec: %.3f", singleThreadSum / (count - 1) * 1.);
        out("Average concurrent thread time, sec: %.3f", concurrentThreadSum / (count - 1) * 1.);
    }

    private static void out(String format, double ms) {
        System.out.println(String.format(format, ms));
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int el : row) {
                System.out.printf("%4d", el);
            }
            System.out.println();
        }
        System.out.println();
    }
}