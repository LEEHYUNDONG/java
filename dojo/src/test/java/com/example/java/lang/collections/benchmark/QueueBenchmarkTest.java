package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkFormatter;
import com.example.java.lang.collections.result.BenchmarkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Queue 계열 자료구조 성능 테스트
 */
@DisplayName("Queue 성능 벤치마크")
class QueueBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 3;

    @Test
    @DisplayName("Queue 전체 성능 테스트")
    void testQueuePerformanceAll() {
        int[] dataSizes = {100, 1_000, 10_000};
        List<BenchmarkResult> results = runAllBenchmarks(dataSizes);

        System.out.println("\n" + "=".repeat(100));
        System.out.println("Queue Benchmark Results");
        System.out.println("=".repeat(100));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        assertThat(results).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1_000, 10_000})
    @DisplayName("데이터 크기별 Queue 성능 테스트")
    void testQueuePerformanceBySize(int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Testing Queue with size: " + size);
        System.out.println("=".repeat(80));

        results.addAll(benchmarkQueue("ArrayDeque", new ArrayDeque<>(), size));
        results.addAll(benchmarkQueue("LinkedList", new LinkedList<>(), size));
        results.addAll(benchmarkQueue("PriorityQueue", new PriorityQueue<>(), size));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("ArrayDeque vs LinkedList - Offer/Poll 성능 비교")
    void testOfferPollPerformanceComparison() {
        int size = 10_000;
        List<BenchmarkResult> results = new ArrayList<>();

        // ArrayDeque - Offer
        Queue<Integer> arrayDeque = new ArrayDeque<>();
        long arrayDequeOfferTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                arrayDeque.offer(i);
            }
        });
        results.add(new BenchmarkResult("ArrayDeque", "Offer", size, arrayDequeOfferTime));

        // LinkedList - Offer
        Queue<Integer> linkedList = new LinkedList<>();
        long linkedListOfferTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                linkedList.offer(i);
            }
        });
        results.add(new BenchmarkResult("LinkedList", "Offer", size, linkedListOfferTime));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ArrayDeque vs LinkedList - Offer Performance");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Concurrent Queue 성능 테스트")
    void testConcurrentQueuePerformance() {
        int size = 10_000;
        List<BenchmarkResult> results = new ArrayList<>();

        results.addAll(benchmarkQueue("ArrayDeque", new ArrayDeque<>(), size));
        results.addAll(benchmarkQueue("ConcurrentLinkedQueue", new ConcurrentLinkedQueue<>(), size));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ArrayDeque vs ConcurrentLinkedQueue");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).isNotEmpty();
    }

    private List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            results.addAll(benchmarkQueue("ArrayDeque", new ArrayDeque<>(), size));
            results.addAll(benchmarkQueue("LinkedList", new LinkedList<>(), size));
            results.addAll(benchmarkQueue("PriorityQueue", new PriorityQueue<>(), size));
            results.addAll(benchmarkQueue("ConcurrentLinkedQueue", new ConcurrentLinkedQueue<>(), size));
            results.addAll(benchmarkQueue("LinkedBlockingQueue", new LinkedBlockingQueue<>(), size));
            results.addAll(benchmarkQueue("ArrayBlockingQueue", new ArrayBlockingQueue<>(size), size));
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkQueue(String name, Queue<Integer> queue, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            warmup(queue, size);
        }

        // 1. Offer
        queue.clear();
        long offerTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                queue.offer(i);
            }
        });
        results.add(new BenchmarkResult(name, "Offer", size, offerTime));

        // 2. Peek
        fillQueue(queue, size);
        long peekTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                queue.peek();
            }
        });
        results.add(new BenchmarkResult(name, "Peek", size, peekTime));

        // 3. Poll
        fillQueue(queue, size);
        long pollTime = measureTime(() -> {
            while (!queue.isEmpty()) {
                queue.poll();
            }
        });
        results.add(new BenchmarkResult(name, "Poll", size, pollTime));

        // 4. Iteration
        fillQueue(queue, size);
        long iterateTime = measureTime(() -> {
            int sum = 0;
            for (Integer num : queue) {
                sum += num;
            }
        });
        results.add(new BenchmarkResult(name, "Iteration", size, iterateTime));

        return results;
    }

    private void warmup(Queue<Integer> queue, int size) {
        queue.clear();
        for (int i = 0; i < Math.min(size, 100); i++) {
            queue.offer(i);
        }
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    private void fillQueue(Queue<Integer> queue, int size) {
        queue.clear();
        for (int i = 0; i < size; i++) {
            queue.offer(i);
        }
    }

    private long measureTime(Runnable operation) {
        System.gc();
        long start = System.nanoTime();
        operation.run();
        long end = System.nanoTime();
        return end - start;
    }
}
