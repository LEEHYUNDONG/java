package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkFormatter;
import com.example.java.lang.collections.result.BenchmarkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Set 계열 자료구조 성능 테스트
 */
@DisplayName("Set 성능 벤치마크")
class SetBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 3;

    @Test
    @DisplayName("Set 전체 성능 테스트")
    void testSetPerformanceAll() {
        int[] dataSizes = {100, 1_000, 10_000};
        List<BenchmarkResult> results = runAllBenchmarks(dataSizes);

        System.out.println("\n" + "=".repeat(100));
        System.out.println("Set Benchmark Results");
        System.out.println("=".repeat(100));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        assertThat(results).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1_000, 10_000})
    @DisplayName("데이터 크기별 Set 성능 테스트")
    void testSetPerformanceBySize(int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Testing Set with size: " + size);
        System.out.println("=".repeat(80));

        results.addAll(benchmarkSet("HashSet", new HashSet<>(), size));
        results.addAll(benchmarkSet("LinkedHashSet", new LinkedHashSet<>(), size));
        results.addAll(benchmarkSet("TreeSet", new TreeSet<>(), size));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("HashSet vs TreeSet - Contains 성능 비교")
    void testContainsPerformanceComparison() {
        int size = 50_000;  // 더 큰 데이터셋으로 차이를 명확히
        List<BenchmarkResult> results = new ArrayList<>();

        // HashSet 준비 및 Warmup
        Set<Integer> hashSet = new HashSet<>();
        fillSet(hashSet, size);
        for (int w = 0; w < 5; w++) {
            for (int i = 0; i < size; i++) {
                hashSet.contains(i);
            }
        }

        // 여러 번 측정하여 최소값 사용 (JIT 최적화 후 안정된 성능)
        long hashSetTime = Long.MAX_VALUE;
        for (int run = 0; run < 3; run++) {
            long time = measureTime(() -> {
                for (int i = 0; i < size; i++) {
                    hashSet.contains(i);
                }
            });
            hashSetTime = Math.min(hashSetTime, time);
        }
        results.add(new BenchmarkResult("HashSet", "Contains", size, hashSetTime));

        // TreeSet 준비 및 Warmup
        Set<Integer> treeSet = new TreeSet<>();
        fillSet(treeSet, size);
        for (int w = 0; w < 5; w++) {
            for (int i = 0; i < size; i++) {
                treeSet.contains(i);
            }
        }

        // 여러 번 측정하여 최소값 사용
        long treeSetTime = Long.MAX_VALUE;
        for (int run = 0; run < 3; run++) {
            long time = measureTime(() -> {
                for (int i = 0; i < size; i++) {
                    treeSet.contains(i);
                }
            });
            treeSetTime = Math.min(treeSetTime, time);
        }
        results.add(new BenchmarkResult("TreeSet", "Contains", size, treeSetTime));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("HashSet vs TreeSet - Contains Performance");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        // HashSet이 TreeSet보다 빨라야 함 (O(1) vs O(log n))
        assertThat(hashSetTime).isLessThan(treeSetTime);
    }

    private List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            results.addAll(benchmarkSet("HashSet", new HashSet<>(), size));
            results.addAll(benchmarkSet("LinkedHashSet", new LinkedHashSet<>(), size));
            results.addAll(benchmarkSet("TreeSet", new TreeSet<>(), size));
            results.addAll(benchmarkSet("ConcurrentSkipListSet", new ConcurrentSkipListSet<>(), size));

            if (size <= 1000) {
                results.addAll(benchmarkSet("CopyOnWriteArraySet", new CopyOnWriteArraySet<>(), size));
            }
        }

        return results;
    }

    private List<BenchmarkResult> benchmarkSet(String name, Set<Integer> set, int size) {
        List<BenchmarkResult> results = new ArrayList<>();

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
