package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkFormatter;
import com.example.java.lang.collections.result.BenchmarkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Map 계열 자료구조 성능 테스트
 */
@DisplayName("Map 성능 벤치마크")
class MapBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 3;

    @Test
    @DisplayName("Map 전체 성능 테스트")
    void testMapPerformanceAll() {
        int[] dataSizes = {100, 1_000, 10_000};
        List<BenchmarkResult> results = runAllBenchmarks(dataSizes);

        System.out.println("\n" + "=".repeat(100));
        System.out.println("Map Benchmark Results");
        System.out.println("=".repeat(100));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        assertThat(results).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1_000, 10_000})
    @DisplayName("데이터 크기별 Map 성능 테스트")
    void testMapPerformanceBySize(int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Testing Map with size: " + size);
        System.out.println("=".repeat(80));

        results.addAll(benchmarkMap("HashMap", new HashMap<>(), size));
        results.addAll(benchmarkMap("LinkedHashMap", new LinkedHashMap<>(), size));
        results.addAll(benchmarkMap("TreeMap", new TreeMap<>(), size));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("HashMap vs TreeMap - Get 성능 비교")
    void testGetPerformanceComparison() {
        int size = 50_000;  // 더 큰 데이터셋으로 차이를 명확히
        List<BenchmarkResult> results = new ArrayList<>();

        // HashMap 준비 및 Warmup
        Map<Integer, String> hashMap = new HashMap<>();
        fillMap(hashMap, size);
        for (int w = 0; w < 5; w++) {
            for (int i = 0; i < size; i++) {
                hashMap.get(i);
            }
        }

        // 여러 번 측정하여 최소값 사용 (JIT 최적화 후 안정된 성능)
        long hashMapTime = Long.MAX_VALUE;
        for (int run = 0; run < 3; run++) {
            long time = measureTime(() -> {
                for (int i = 0; i < size; i++) {
                    hashMap.get(i);
                }
            });
            hashMapTime = Math.min(hashMapTime, time);
        }
        results.add(new BenchmarkResult("HashMap", "Get", size, hashMapTime));

        // TreeMap 준비 및 Warmup
        Map<Integer, String> treeMap = new TreeMap<>();
        fillMap(treeMap, size);
        for (int w = 0; w < 5; w++) {
            for (int i = 0; i < size; i++) {
                treeMap.get(i);
            }
        }

        // 여러 번 측정하여 최소값 사용
        long treeMapTime = Long.MAX_VALUE;
        for (int run = 0; run < 3; run++) {
            long time = measureTime(() -> {
                for (int i = 0; i < size; i++) {
                    treeMap.get(i);
                }
            });
            treeMapTime = Math.min(treeMapTime, time);
        }
        results.add(new BenchmarkResult("TreeMap", "Get", size, treeMapTime));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("HashMap vs TreeMap - Get Performance");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        // HashMap이 TreeMap보다 빨라야 함
        assertThat(hashMapTime).isLessThan(treeMapTime);
    }

    @Test
    @DisplayName("ConcurrentHashMap 성능 테스트")
    void testConcurrentHashMapPerformance() {
        int size = 10_000;
        List<BenchmarkResult> results = new ArrayList<>();

        results.addAll(benchmarkMap("HashMap", new HashMap<>(), size));
        results.addAll(benchmarkMap("ConcurrentHashMap", new ConcurrentHashMap<>(), size));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("HashMap vs ConcurrentHashMap");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).isNotEmpty();
    }

    private List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            results.addAll(benchmarkMap("HashMap", new HashMap<>(), size));
            results.addAll(benchmarkMap("LinkedHashMap", new LinkedHashMap<>(), size));
            results.addAll(benchmarkMap("TreeMap", new TreeMap<>(), size));
            results.addAll(benchmarkMap("Hashtable", new Hashtable<>(), size));
            results.addAll(benchmarkMap("ConcurrentHashMap", new ConcurrentHashMap<>(), size));
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkMap(String name, Map<Integer, String> map, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

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

        // 5. Iteration
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
