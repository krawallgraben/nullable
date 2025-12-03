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
    @Benchmark
    public void addStdList(org.openjdk.jmh.infra.Blackhole bh) {
        stdList.add("new");
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

    // Delete (Remove from middle)
    @Benchmark
    public void removeStdList(org.openjdk.jmh.infra.Blackhole bh) {
        stdList.remove(size / 2);
        stdList.add("value" + (size / 2));
    }

    @Benchmark
    public void removeConcurrentList(org.openjdk.jmh.infra.Blackhole bh) {
        concurrentList.remove(size / 2);
        concurrentList.add("value" + (size / 2));
    }

    @Benchmark
    public void removeProjectList(org.openjdk.jmh.infra.Blackhole bh) {
        projectList.remove(size / 2);
        projectList.add("value" + (size / 2));
    }
}
