package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkResult;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Set 계열 자료구조 성능 테스트
 * - HashSet
 * - LinkedHashSet
 * - TreeSet
 * - ConcurrentSkipListSet
 * - CopyOnWriteArraySet
 */
public class SetBenchmark {

    private static final int WARMUP_ITERATIONS = 3;

    public List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            System.out.println("Testing with data size: " + size);

            // HashSet
            results.addAll(benchmarkSet("HashSet", new HashSet<>(), size));

            // LinkedHashSet
            results.addAll(benchmarkSet("LinkedHashSet", new LinkedHashSet<>(), size));

            // TreeSet
            results.addAll(benchmarkSet("TreeSet", new TreeSet<>(), size));

            // ConcurrentSkipListSet
            results.addAll(benchmarkSet("ConcurrentSkipListSet", new ConcurrentSkipListSet<>(), size));

            // CopyOnWriteArraySet (작은 사이즈만)
            if (size <= 1000) {
                results.addAll(benchmarkSet("CopyOnWriteArraySet", new CopyOnWriteArraySet<>(), size));
            }
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkSet(String name, Set<Integer> set, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            warmup(set, size);
        }

        // 1. Add
        set.clear();
        long addTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                set.add(i);
            }
        });
        results.add(new BenchmarkResult(name, "Add", size, addTime));

        // 2. Contains
        fillSet(set, size);
        long containsTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                set.contains(i);
            }
        });
        results.add(new BenchmarkResult(name, "Contains", size, containsTime));

        // 3. Remove
        fillSet(set, size);
        long removeTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                set.remove(i);
            }
        });
        results.add(new BenchmarkResult(name, "Remove", size, removeTime));

        // 4. Iteration
        fillSet(set, size);
        long iterateTime = measureTime(() -> {
            int sum = 0;
            for (Integer num : set) {
                sum += num;
            }
        });
        results.add(new BenchmarkResult(name, "Iteration", size, iterateTime));

        return results;
    }

    private void warmup(Set<Integer> set, int size) {
        set.clear();
        for (int i = 0; i < Math.min(size, 100); i++) {
            set.add(i);
        }
        for (int i = 0; i < Math.min(size, 100); i++) {
            set.contains(i);
        }
        set.clear();
    }

    private void fillSet(Set<Integer> set, int size) {
        set.clear();
        for (int i = 0; i < size; i++) {
            set.add(i);
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
