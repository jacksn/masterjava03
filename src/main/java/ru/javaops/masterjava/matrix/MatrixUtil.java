package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static ru.javaops.masterjava.matrix.MainMatrix.THREAD_COUNT;

public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[][] transposedMatrixB = transpose(matrixB);

        final CountDownLatch latch;

        final int chunkCount = matrixSize / THREAD_COUNT;

        if (chunkCount == 0) {
            latch = new CountDownLatch(1);
            executor.submit(new MultiplyTask(matrixA, transposedMatrixB, matrixC, 0, matrixSize, latch));
        } else {
            latch = new CountDownLatch(chunkCount);
            for (int i = 0; i < chunkCount; i++) {
                executor.submit(new MultiplyTask(matrixA, transposedMatrixB, matrixC, i * THREAD_COUNT, (i + 1) * THREAD_COUNT, latch));
            }
        }

        latch.await();

        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[][] transposedMatrixB = transpose(matrixB);

        for (int i = 0; i < matrixSize; i++) {
            int[] matrixARow = matrixA[i];
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                int[] matrixBCol = transposedMatrixB[j];
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixARow[k] * matrixBCol[k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] transpose(int[][] matrix) {
        final int matrixSize = matrix.length;
        final int[][] transposedMatrix = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                transposedMatrix[i][j] = matrix[j][i];
            }
        }

        return transposedMatrix;
    }

    private static class MultiplyTask implements Runnable {
        private final int[][] matrixA;
        private final int[][] transposedMatrixB;
        private final int[][] resultMatrix;

        private final int startRow;
        private final int endRow;

        private final CountDownLatch latch;

        public MultiplyTask(int[][] matrixA, int[][] transposedMatrixB, int[][] resultMatrix, int startRow, int endRow, CountDownLatch latch) {
            this.matrixA = matrixA;
            this.transposedMatrixB = transposedMatrixB;
            this.resultMatrix = resultMatrix;
            this.startRow = startRow;
            this.endRow = endRow;
            this.latch = latch;
        }

        @Override
        public void run() {
            final int matrixSize = matrixA.length;
            for (int i = startRow; i < endRow; i++) {
                int[] matrixARow = matrixA[i];
                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    int[] matrixBCol = transposedMatrixB[j];
                    for (int k = 0; k < matrixSize; k++) {
                        sum += matrixARow[k] * matrixBCol[k];
                    }
                    resultMatrix[i][j] = sum;
                }
            }
            latch.countDown();
        }
    }
}
