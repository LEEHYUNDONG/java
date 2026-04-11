package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map 계열 자료구조 성능 테스트
 * - HashMap
 * - LinkedHashMap
 * - TreeMap
 * - Hashtable
 * - ConcurrentHashMap
 */
public class MapBenchmark {

    private static final int WARMUP_ITERATIONS = 3;

    public List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            System.out.println("Testing with data size: " + size);

            // HashMap
            results.addAll(benchmarkMap("HashMap", new HashMap<>(), size));

            // LinkedHashMap
            results.addAll(benchmarkMap("LinkedHashMap", new LinkedHashMap<>(), size));

            // TreeMap
            results.addAll(benchmarkMap("TreeMap", new TreeMap<>(), size));

            // Hashtable
            results.addAll(benchmarkMap("Hashtable", new Hashtable<>(), size));

            // ConcurrentHashMap
            results.addAll(benchmarkMap("ConcurrentHashMap", new ConcurrentHashMap<>(), size));
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkMap(String name, Map<Integer, String> map, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            warmup(map, size);
        }

        // 1. Put
        map.clear();
        long putTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                map.put(i, "value" + i);
            }
        });
        results.add(new BenchmarkResult(name, "Put", size, putTime));

        // 2. Get
        fillMap(map, size);
        long getTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                map.get(i);
            }
        });
        results.add(new BenchmarkResult(name, "Get", size, getTime));

        // 3. ContainsKey
        long containsKeyTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                map.containsKey(i);
            }
        });
        results.add(new BenchmarkResult(name, "ContainsKey", size, containsKeyTime));

        // 4. Remove
        fillMap(map, size);
        long removeTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                map.remove(i);
            }
        });
        results.add(new BenchmarkResult(name, "Remove", size, removeTime));

        // 5. Iteration (keySet)
        fillMap(map, size);
        long iterateTime = measureTime(() -> {
            int sum = 0;
            for (Integer key : map.keySet()) {
                sum += key;
            }
        });
        results.add(new BenchmarkResult(name, "Iteration", size, iterateTime));

        return results;
    }

    private void warmup(Map<Integer, String> map, int size) {
        map.clear();
        for (int i = 0; i < Math.min(size, 100); i++) {
            map.put(i, "value" + i);
        }
        for (int i = 0; i < Math.min(size, 100); i++) {
            map.get(i);
        }
        map.clear();
    }

    private void fillMap(Map<Integer, String> map, int size) {
        map.clear();
        for (int i = 0; i < size; i++) {
            map.put(i, "value" + i);
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
