package de.krawallgraben.nullable.benchmark;

import de.krawallgraben.nullable.RobustValueIteratorList;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class ListBenchmark {

    @Param({"1000"})
    private int size;

    private List<String> stdList;
    private List<String> concurrentList;
    private RobustValueIteratorList<String> projectList;

    @Setup(Level.Trial)
    public void setup() {
        stdList = new ArrayList<>();
        concurrentList = new CopyOnWriteArrayList<>();
        projectList = new RobustValueIteratorList<>();

        for (int i = 0; i < size; i++) {
            String val = "value" + i;
            stdList.add(val);
            concurrentList.add(val);
            projectList.add(val);
        }
    }

    // Iteration
    @Benchmark
    public void iterateStdList(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : stdList) {
            bh.consume(s);
        }
    }

    @Benchmark
    public void iterateConcurrentList(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : concurrentList) {
            bh.consume(s);
        }
    }

    @Benchmark
    public void iterateProjectList(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : projectList) {
            bh.consume(s);
        }
    }

    // Read (Get)
    @Benchmark
    public void getStdList(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(stdList.get(i));
        }
    }

    @Benchmark
    public void getConcurrentList(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(concurrentList.get(i));
        }
    }

    @Benchmark
    public void getProjectList(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(projectList.get(i));
        }
    }

    // Write (Add) - append to end
    // Note: CopyOnWriteArrayList will be very slow here.
    @Benchmark
    public void addStdList(org.openjdk.jmh.infra.Blackhole bh) {
        // We add and then remove to keep size somewhat constant or just add?
        // Adding indefinitely will cause OOM.
        // We can just add one element. Benchmarks run repeatedly.
        // But setup runs once per trial.
        // Better: create a list inside the benchmark method? No, that measures creation.
        // We will just add one element at the end.
        stdList.add("new");
        // Clean up to prevent unlimited growth during measurement
        stdList.remove(stdList.size() - 1);
    }

    @Benchmark
    public void addConcurrentList(org.openjdk.jmh.infra.Blackhole bh) {
        concurrentList.add("new");
        concurrentList.remove(concurrentList.size() - 1);
    }

    @Benchmark
    public void addProjectList(org.openjdk.jmh.infra.Blackhole bh) {
        projectList.add("new");
        projectList.remove(projectList.size() - 1);
    }
}
