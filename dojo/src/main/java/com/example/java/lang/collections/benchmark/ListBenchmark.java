package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkResult;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * List 계열 자료구조 성능 테스트
 * - ArrayList
 * - LinkedList
 * - Vector
 * - CopyOnWriteArrayList
 */
public class ListBenchmark {

    private static final int WARMUP_ITERATIONS = 3;

    public List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            System.out.println("Testing with data size: " + size);

            // ArrayList
            results.addAll(benchmarkList("ArrayList", new ArrayList<>(), size));

            // LinkedList
            results.addAll(benchmarkList("LinkedList", new LinkedList<>(), size));

            // Vector
            results.addAll(benchmarkList("Vector", new Vector<>(), size));

            // CopyOnWriteArrayList
            results.addAll(benchmarkList("CopyOnWriteArrayList", new CopyOnWriteArrayList<>(), size));
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkList(String name, List<Integer> list, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            warmup(list, size);
        }

        // 1. Add (끝에 추가)
        list.clear();
        long addTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
        });
        results.add(new BenchmarkResult(name, "Add (end)", size, addTime));

        // 2. Add at beginning (앞에 추가)
        list.clear();
        long addFirstTime = measureTime(() -> {
            for (int i = 0; i < Math.min(size, 1000); i++) {  // 너무 느려서 1000개로 제한
                list.add(0, i);
            }
        });
        results.add(new BenchmarkResult(name, "Add (beginning)", Math.min(size, 1000), addFirstTime));

        // 3. Get by index
        fillList(list, size);
        long getTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                list.get(i);
            }
        });
        results.add(new BenchmarkResult(name, "Get by index", size, getTime));

        // 4. Contains
        long containsTime = measureTime(() -> {
            for (int i = 0; i < Math.min(size, 1000); i++) {
                list.contains(i);
            }
        });
        results.add(new BenchmarkResult(name, "Contains", Math.min(size, 1000), containsTime));

        // 5. Remove by index (끝에서)
        fillList(list, size);
        long removeTime = measureTime(() -> {
            for (int i = 0; i < Math.min(size, 1000); i++) {
                if (!list.isEmpty()) {
                    list.remove(list.size() - 1);
                }
            }
        });
        results.add(new BenchmarkResult(name, "Remove (end)", Math.min(size, 1000), removeTime));

        // 6. Iteration
        fillList(list, size);
        long iterateTime = measureTime(() -> {
            int sum = 0;
            for (Integer num : list) {
                sum += num;
            }
        });
        results.add(new BenchmarkResult(name, "Iteration", size, iterateTime));

        return results;
    }

    private void warmup(List<Integer> list, int size) {
        list.clear();
        for (int i = 0; i < Math.min(size, 100); i++) {
            list.add(i);
        }
        for (int i = 0; i < Math.min(size, 100); i++) {
            list.get(i);
        }
        list.clear();
    }

    private void fillList(List<Integer> list, int size) {
        list.clear();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
    }

    private long measureTime(Runnable operation) {
        System.gc();  // GC 먼저 수행
        long start = System.nanoTime();
        operation.run();
        long end = System.nanoTime();
        return end - start;
    }
}
