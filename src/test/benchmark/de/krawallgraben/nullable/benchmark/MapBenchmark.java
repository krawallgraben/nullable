package de.krawallgraben.nullable.benchmark;

import de.krawallgraben.nullable.NullableConcurrentMap;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class MapBenchmark {

    @Param({"1000"})
    private int size;

    private Map<String, String> stdMap;
    private Map<String, String> concurrentMap;
    private Map<String, String> projectMap;

    private String[] keys;
    private String[] values;

    @Setup(Level.Trial)
    public void setup() {
        stdMap = new HashMap<>();
        concurrentMap = new ConcurrentHashMap<>();
        projectMap = new NullableConcurrentMap<>(new ConcurrentHashMap<>());

        keys = new String[size];
        values = new String[size];

        for (int i = 0; i < size; i++) {
            keys[i] = "key" + i;
            values[i] = "value" + i;
            stdMap.put(keys[i], values[i]);
            concurrentMap.put(keys[i], values[i]);
            projectMap.put(keys[i], values[i]);
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

    // Write (Put) - we use a fresh map or put same keys?
    // Benchmark 'put' usually modifies state. JMH handles this but for read/write mix it's complex.
    // We will simulate "put" by overwriting existing keys to avoid resizing overhead dominating.

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
