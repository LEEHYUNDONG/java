package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkResult;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue/Deque 계열 자료구조 성능 테스트
 * - ArrayDeque
 * - LinkedList
 * - PriorityQueue
 * - ConcurrentLinkedQueue
 * - LinkedBlockingQueue
 * - ArrayBlockingQueue
 */
public class QueueBenchmark {

    private static final int WARMUP_ITERATIONS = 3;

    public List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            System.out.println("Testing with data size: " + size);

            // ArrayDeque
            results.addAll(benchmarkQueue("ArrayDeque", new ArrayDeque<>(), size));

            // LinkedList
            results.addAll(benchmarkQueue("LinkedList", new LinkedList<>(), size));

            // PriorityQueue
            results.addAll(benchmarkQueue("PriorityQueue", new PriorityQueue<>(), size));

            // ConcurrentLinkedQueue
            results.addAll(benchmarkQueue("ConcurrentLinkedQueue", new ConcurrentLinkedQueue<>(), size));

            // LinkedBlockingQueue
            results.addAll(benchmarkQueue("LinkedBlockingQueue", new LinkedBlockingQueue<>(), size));

            // ArrayBlockingQueue
            results.addAll(benchmarkQueue("ArrayBlockingQueue", new ArrayBlockingQueue<>(size), size));
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkQueue(String name, Queue<Integer> queue, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            warmup(queue, size);
        }

        // 1. Offer (add)
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

        // 3. Poll (remove)
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
