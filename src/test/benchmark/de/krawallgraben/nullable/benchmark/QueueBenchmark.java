package de.krawallgraben.nullable.benchmark;

import de.krawallgraben.nullable.NullableConcurrentQueue;
import org.openjdk.jmh.annotations.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class QueueBenchmark {

    @Param({"1000"})
    private int size;

    private Queue<String> stdQueue;
    private Queue<String> concurrentQueue;
    private Queue<String> projectQueue;

    @Setup(Level.Trial)
    public void setup() {
        stdQueue = new LinkedList<>();
        concurrentQueue = new ConcurrentLinkedQueue<>();
        projectQueue = new NullableConcurrentQueue<>(new ConcurrentLinkedQueue<>());

        for (int i = 0; i < size; i++) {
            String val = "value" + i;
            stdQueue.offer(val);
            concurrentQueue.offer(val);
            projectQueue.offer(val);
        }
    }

    // Iteration
    @Benchmark
    public void iterateStdQueue(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : stdQueue) {
            bh.consume(s);
        }
    }

    @Benchmark
    public void iterateConcurrentQueue(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : concurrentQueue) {
            bh.consume(s);
        }
    }

    @Benchmark
    public void iterateProjectQueue(org.openjdk.jmh.infra.Blackhole bh) {
        for (String s : projectQueue) {
            bh.consume(s);
        }
    }

    // Read/Write (Offer/Poll) simulation
    // Since queues are for processing, we often poll and offer.
    // pure "get" is usually not supported or is iteration.
    // We will benchmark offer/poll cycle.

    @Benchmark
    public void offerPollStdQueue(org.openjdk.jmh.infra.Blackhole bh) {
        stdQueue.offer("new");
        bh.consume(stdQueue.poll());
    }

    @Benchmark
    public void offerPollConcurrentQueue(org.openjdk.jmh.infra.Blackhole bh) {
        concurrentQueue.offer("new");
        bh.consume(concurrentQueue.poll());
    }

    @Benchmark
    public void offerPollProjectQueue(org.openjdk.jmh.infra.Blackhole bh) {
        projectQueue.offer("new");
        bh.consume(projectQueue.poll());
    }
}
