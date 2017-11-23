package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static ru.javaops.masterjava.matrix.MainMatrix.CHUNK_SIZE;

public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int chunkCount = matrixSize / CHUNK_SIZE;

        if (chunkCount == 0) {
            chunkCount++;
        }

        final CountDownLatch latch = new CountDownLatch(chunkCount);

        for (int i = 0; i < chunkCount; i++) {
            final int startRow = i * CHUNK_SIZE;
            final int endRow = (i + 1) * CHUNK_SIZE;

            executor.submit(() -> {
                for (int row = startRow; row < endRow; row++) {
                    final int[] matrixARow = matrixA[row];
                    final int[] matrixCRow = matrixC[row];
                    for (int j = 0; j < matrixSize; j++) {
                        final int elA = matrixARow[j];
                        final int[] matrixBRow = matrixB[j];
                        for (int k = 0; k < matrixSize; k++) {
                            matrixCRow[k] += elA * matrixBRow[k];
                        }
                    }
                }
                latch.countDown();
            });
        }

        latch.await();

        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            final int[] matrixARow = matrixA[i];
            final int[] matrixCRow = matrixC[i];
            for (int j = 0; j < matrixSize; j++) {
                final int elA = matrixARow[j];
                final int[] matrixBRow = matrixB[j];
                for (int k = 0; k < matrixSize; k++) {
                    matrixCRow[k] += elA * matrixBRow[k];
                }
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
}
