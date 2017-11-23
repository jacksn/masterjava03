package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.*;

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
                computeRows(matrixA, matrixB, matrixC, startRow, endRow);
                latch.countDown();
            });
        }

        latch.await();

        return matrixC;
    }

    public static int[][] concurrentMultiplyFJP(int[][] matrixA, int[][] matrixB) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        ForkJoinPool.commonPool().invoke(new MatrixTask(matrixA, matrixB, matrixC, 0, matrixSize));

        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        computeRows(matrixA, matrixB, matrixC, 0, matrixSize);

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

    private static void computeRows(int[][] matrixA, int[][] matrixB, int[][] resultMatrix, int startRow, int endRow) {
        int matrixSize = matrixA.length;
        for (int row = startRow; row < endRow; row++) {
            final int[] matrixARow = matrixA[row];
            final int[] resultMatrixRow = resultMatrix[row];
            for (int j = 0; j < matrixSize; j++) {
                final int elA = matrixARow[j];
                final int[] matrixBRow = matrixB[j];
                for (int k = 0; k < matrixSize; k++) {
                    resultMatrixRow[k] += elA * matrixBRow[k];
                }
            }
        }
    }

    private static class MatrixTask extends CountedCompleter<Void> {
        private final int[][] matrixA;
        private final int[][] matrixB;
        private final int[][] matrixC;

        private final int startRow;
        private final int endRow;

        public MatrixTask(int[][] matrixA, int[][] matrixB, int[][] matrixC, int startRow, int endRow) {
            this(null, matrixA, matrixB, matrixC, startRow, endRow);
        }

        public MatrixTask(CountedCompleter<?> completer, int[][] matrixA, int[][] matrixB, int[][] matrixC, int startRow, int endRow) {
            super(completer);
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.matrixC = matrixC;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void compute() {
            if ((endRow - startRow) < CHUNK_SIZE) {
                computeRows(matrixA, matrixB, matrixC, startRow, endRow);
            } else {
                setPendingCount(2);
                final int half = (endRow - startRow) / 2;
                new MatrixTask(this, matrixA, matrixB, matrixC, startRow, startRow + half).fork();
                new MatrixTask(this, matrixA, matrixB, matrixC, startRow + half, endRow).fork();
            }
            tryComplete();
        }
    }
}

