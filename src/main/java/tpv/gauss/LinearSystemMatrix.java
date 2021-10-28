package tpv.gauss;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

public class LinearSystemMatrix {

    public static final ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() - 1);

    private final int mRows;
    private final int mColumns;
    private final double[][] matrix;
    private double[] solution;

    public LinearSystemMatrix(double[][] matrix) {
        this.mRows = matrix.length;
        this.mColumns = matrix[0].length - 1;
        this.matrix = matrix;
    }

    public double[] gaussianEliminationMultithreading() {
        for (int curRowIdx = 0; curRowIdx < mRows; curRowIdx++) {
            swapRowsIfNeeded(curRowIdx);

            final int curRowIdxF = curRowIdx;
            var l = new ArrayList<Future<?>>();

            for (int i = curRowIdxF + 1; i < matrix.length; i++) {
                double alpha = matrix[i][curRowIdxF] / matrix[curRowIdxF][curRowIdxF];

                int finalI = i;
                Future<?> f = executorService.submit(() -> {
                    for (int j = curRowIdxF; j < matrix[0].length; j++) {
                        matrix[finalI][j] -= alpha * matrix[curRowIdxF][j];
                    }
                });
                l.add(f);
            }
            //wait for all rows update
            for (Future<?> future : l) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        return backSubstitution();
    }

    public double[] gaussianEliminationSequential() {
        for (int curRowIdx = 0; curRowIdx < mRows; curRowIdx++) {
            swapRowsIfNeeded(curRowIdx);

            for (int i = curRowIdx + 1; i < matrix.length; i++) {
                double alpha = matrix[i][curRowIdx] / matrix[curRowIdx][curRowIdx];
                for (int j = curRowIdx; j < matrix[0].length; j++) {
                    matrix[i][j] -= alpha * matrix[curRowIdx][j];
                }
            }
        }

        return backSubstitution();
    }

    public double[] gaussianEliminationSimd() {
        VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int maxOffset = matrix[0].length - species.length();

        for (int curRowIdx = 0; curRowIdx < mRows; curRowIdx++) {
            swapRowsIfNeeded(curRowIdx);

            int currentRowLength = matrix[0].length - curRowIdx;
            int leftover = currentRowLength % species.length();

            for (int nxtRowIdx = curRowIdx + 1; nxtRowIdx < matrix.length; nxtRowIdx++) {
                double alpha = matrix[nxtRowIdx][curRowIdx] / matrix[curRowIdx][curRowIdx];

                // multiply current row on alpha and subtract from next row
                for (int offset = curRowIdx; offset <= maxOffset; offset += species.length()) {
                    DoubleVector currentRowVector = DoubleVector.fromArray(species, matrix[curRowIdx], offset);
                    DoubleVector nextRowVector = DoubleVector.fromArray(species, matrix[nxtRowIdx], offset);

                    currentRowVector
                        .mul(alpha)
                        .sub(nextRowVector)
                        .intoArray(matrix[nxtRowIdx], offset);
                }

                if (leftover != 0) {
                    VectorMask<Double> mask = species.indexInRange(0, leftover);
                    int offset = matrix[curRowIdx].length - mask.trueCount();
                    DoubleVector currentRowVector = DoubleVector.fromArray(species, matrix[curRowIdx], offset, mask);
                    DoubleVector nextRowVector = DoubleVector.fromArray(species, matrix[nxtRowIdx], offset, mask);

                    currentRowVector.mul(alpha)
                        .sub(nextRowVector) // next row
                        .intoArray(matrix[nxtRowIdx], offset, mask);

                }
            }
        }

        return backSubstitution();
    }

    public static String no(double[] arr) {
        return Arrays.stream(arr)
            .map(d -> BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).doubleValue())
            .mapToObj(String::valueOf)
            .toList().toString();
    }

    public void verify(double[] expected) {
        var minDiff = 0.0005;
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] - this.solution[i] > minDiff) {
                throw new RuntimeException(String.format("Incorrect solution at index: %d. Expected: %f, actual: %f", i, expected[i], this.solution[i]));
            }
        }
    }

    private double[] backSubstitution() {
        double[] result = new double[mColumns];

        for (int i = mRows - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < mColumns; j++) {
                sum += matrix[i][j] * result[j];
            }

            double value = (matrix[i][mColumns] - sum) / matrix[i][i];
            try {
                result[i] = BigDecimal.valueOf(value)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.solution = result;

        return result;
    }

    private void swapRowsIfNeeded(int curRowIdx) {
        // Initialize maximum index for pivot
        int maxRowIndex = curRowIdx;
        // find greater amplitude for pivot if any
        for (int nxtRow = maxRowIndex + 1; nxtRow < matrix.length; nxtRow++) {
            if (Math.abs(matrix[nxtRow][curRowIdx]) > Math.abs(matrix[maxRowIndex][curRowIdx])) {
                maxRowIndex = nxtRow;
            }
        }
        // Swap the greatest value row with current row
        if (maxRowIndex != curRowIdx) {
            double[] temp = matrix[curRowIdx];
            matrix[curRowIdx] = matrix[maxRowIndex];
            matrix[maxRowIndex] = temp;
        }
    }

}

