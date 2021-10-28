package tpv.gauss;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

public class GaussBenchmark {

    private static final double[] VARIABLES = AppGaussCheck.generateVariables();
    private static final double[][] MATRIX = AppGaussCheck.generateMatrix(VARIABLES);

    @State(Scope.Benchmark)
    public static class MyState {

        LinearSystemMatrix linearSystemMatrix;

        @Setup
        public void setUp() {
            linearSystemMatrix = new LinearSystemMatrix(AppGaussCheck.copyOf(MATRIX));
        }

        @TearDown
        public void tearDown() throws InterruptedException {
            LinearSystemMatrix.executorService.shutdown();
            LinearSystemMatrix.executorService.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void a_sequential(MyState state, Blackhole blackhole) {
        blackhole.consume(state.linearSystemMatrix.gaussianEliminationSequential());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void b_vectorApi(MyState state, Blackhole blackhole) {
        blackhole.consume(state.linearSystemMatrix.gaussianEliminationSimd());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void c_multithreading(MyState state, Blackhole blackhole) {
        blackhole.consume(state.linearSystemMatrix.gaussianEliminationMultithreading());
    }
}
