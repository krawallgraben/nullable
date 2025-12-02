package de.krawallgraben.nullable.benchmark;

import de.krawallgraben.nullable.NullableSortedConcurrentMap;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class SortedMapBenchmark {

    @Param({"1000"})
    private int size;

    private SortedMap<String, String> stdMap;
    private SortedMap<String, String> concurrentMap;
    private SortedMap<String, String> projectMap;

    private String[] keys;

    @Setup(Level.Trial)
    public void setup() {
        stdMap = new TreeMap<>();
        concurrentMap = new ConcurrentSkipListMap<>();
        projectMap = new NullableSortedConcurrentMap<>(new ConcurrentSkipListMap<>());

        keys = new String[size];

        for (int i = 0; i < size; i++) {
            keys[i] = String.format("%05d", i); // Ensure sorting order
            String val = "value" + i;
            stdMap.put(keys[i], val);
            concurrentMap.put(keys[i], val);
            projectMap.put(keys[i], val);
        }
    }

    // Iteration
    @Benchmark
    public void iterateStdMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (Map.Entry<String, String> entry : stdMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void iterateConcurrentMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (Map.Entry<String, String> entry : concurrentMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void iterateProjectMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (Map.Entry<String, String> entry : projectMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    // Read (Get)
    @Benchmark
    public void getStdMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(stdMap.get(keys[i]));
        }
    }

    @Benchmark
    public void getConcurrentMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(concurrentMap.get(keys[i]));
        }
    }

    @Benchmark
    public void getProjectMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(projectMap.get(keys[i]));
        }
    }

    // Write (Put)
    @Benchmark
    public void putStdMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(stdMap.put(keys[i], "newValue"));
        }
    }

    @Benchmark
    public void putConcurrentMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(concurrentMap.put(keys[i], "newValue"));
        }
    }

    @Benchmark
    public void putProjectMap(org.openjdk.jmh.infra.Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(projectMap.put(keys[i], "newValue"));
        }
    }
}
