package tpv.gauss;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AppGaussCheck {

    private static final int LINEAR_SYSTEM_SIZE = 1024;
    private static final int ATTEMPTS = 5;
    private static final double[] VARIABLES = generateVariables();
    private static final double[][] MATRIX = generateMatrix(VARIABLES);

    public static void main(String[] args) throws InterruptedException {

        System.out.printf("\n\nLINEAR_SYSTEM_SIZE = %d%n", LINEAR_SYSTEM_SIZE);
        System.out.printf("Number of sequential executions = %d%n", ATTEMPTS);
        System.out.printf("Number of parallel executions = %d%n", ATTEMPTS);

        System.out.print("Warm-up");
        for (int i = 0; i < 2; i++) {
            System.out.print(".");
            new LinearSystemMatrix(copyOf(MATRIX)).gaussianEliminationSequential();
            new LinearSystemMatrix(copyOf(MATRIX)).gaussianEliminationSimd();
            new LinearSystemMatrix(copyOf(MATRIX)).gaussianEliminationMultithreading();
        }
        System.out.print("\n");

        System.out.println("\nRun gaussianElimination SIMD:");
        execute(LinearSystemMatrix::gaussianEliminationSimd);

        System.out.println("\nRun gaussianElimination sequentially:");
        execute(LinearSystemMatrix::gaussianEliminationSequential);

        System.out.println("\nRun gaussianElimination in parallel:");
        execute(LinearSystemMatrix::gaussianEliminationMultithreading);

        LinearSystemMatrix.executorService.shutdown();
        LinearSystemMatrix.executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static void execute(Consumer<LinearSystemMatrix> lsmConsumer) {
        var totalDuration = Duration.ZERO;
        for (int i = 0; i < ATTEMPTS; i++) {
            var linearSystem = new LinearSystemMatrix(copyOf(MATRIX));
            long startTime = System.currentTimeMillis();
            lsmConsumer.accept(linearSystem);
            Duration diff = Duration.ofMillis(System.currentTimeMillis() - startTime);
            printAttemptResult(i, diff);
            totalDuration = totalDuration.plus(diff);

            linearSystem.verify(VARIABLES);
        }
        printTotal(totalDuration);
    }


    private static void printAttemptResult(int i, Duration diff) {
        System.out.printf("Attempt-%d: %2ds %02dms %n", i + 1, diff.toSecondsPart(), diff.toMillisPart());
    }

    private static void printTotal(Duration total) {
        System.out.printf("AVERAGE: %2ds %02dms %n", total.dividedBy(ATTEMPTS).toSecondsPart(), total.dividedBy(ATTEMPTS).toMillisPart());
        System.out.printf("TOTAL: %2ds %02dms %n", total.toSecondsPart(), total.toMillisPart());
    }

    public static double[][] copyOf(double[][] matrix) {
        double[][] copy = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    public static double[] generateVariables() {
        int minVar = -10;
        int maxVar = 10;
        Random random = new Random();

        return random.ints(AppGaussCheck.LINEAR_SYSTEM_SIZE, minVar * 100, maxVar * 100)
            .mapToDouble(i -> i / 100d)
            .map(i -> Math.abs(i) < 1 ? i + Math.copySign(1, i) : i)
            .toArray();
    }

    public static double[][] generateMatrix(double[] variables) {
        int rowCount = variables.length;
        var matrix = new double[rowCount][rowCount + 1];

        Random random = new Random();
        for (int i = 0; i < rowCount; i++) {
            double rightPart = 0;
            for (int j = 0; j < rowCount; j++) {
                int factor = random.nextInt(9) + 1;
                double value = factor * variables[j];
                rightPart += value;
                matrix[i][j] = factor;
            }
            matrix[i][rowCount] = rightPart;
        }

        return matrix;
    }
}
