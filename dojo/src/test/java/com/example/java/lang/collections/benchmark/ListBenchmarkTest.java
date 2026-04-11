package com.example.java.lang.collections.benchmark;

import com.example.java.lang.collections.result.BenchmarkFormatter;
import com.example.java.lang.collections.result.BenchmarkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * List 계열 자료구조 성능 테스트
 */
@DisplayName("List 성능 벤치마크")
class ListBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 3;

    @Test
    @DisplayName("List 전체 성능 테스트")
    void testListPerformanceAll() {
        int[] dataSizes = {100, 1_000, 10_000};
        List<BenchmarkResult> results = runAllBenchmarks(dataSizes);

        System.out.println("\n" + "=".repeat(100));
        System.out.println("List Benchmark Results");
        System.out.println("=".repeat(100));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        // 테스트는 항상 성공 (성능 측정만 수행)
        assertThat(results).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1_000, 10_000})
    @DisplayName("데이터 크기별 List 성능 테스트")
    void testListPerformanceBySize(int size) {
        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Testing List with size: " + size);
        System.out.println("=".repeat(80));

        results.addAll(benchmarkList("ArrayList", new ArrayList<>(), size));
        results.addAll(benchmarkList("LinkedList", new LinkedList<>(), size));
        results.addAll(benchmarkList("Vector", new Vector<>(), size));
        results.addAll(benchmarkList("CopyOnWriteArrayList", new CopyOnWriteArrayList<>(), size));

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        assertThat(results).hasSize(24); // 4 collections * 6 operations
    }

    @Test
    @DisplayName("ArrayList vs LinkedList - Add 성능 비교")
    void testAddPerformanceComparison() {
        int size = 10_000;
        List<BenchmarkResult> results = new ArrayList<>();

        // ArrayList
        List<Integer> arrayList = new ArrayList<>();
        long arrayListTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
            }
        });
        results.add(new BenchmarkResult("ArrayList", "Add", size, arrayListTime));

        // LinkedList
        List<Integer> linkedList = new LinkedList<>();
        long linkedListTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                linkedList.add(i);
            }
        });
        results.add(new BenchmarkResult("LinkedList", "Add", size, linkedListTime));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ArrayList vs LinkedList - Add Performance");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Random Access 성능 테스트 (get by index)")
    void testRandomAccessPerformance() {
        int size = 10_000;
        List<BenchmarkResult> results = new ArrayList<>();

        // ArrayList
        List<Integer> arrayList = new ArrayList<>();
        fillList(arrayList, size);
        long arrayListTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                arrayList.get(i);
            }
        });
        results.add(new BenchmarkResult("ArrayList", "Get", size, arrayListTime));

        // LinkedList
        List<Integer> linkedList = new LinkedList<>();
        fillList(linkedList, size);
        long linkedListTime = measureTime(() -> {
            for (int i = 0; i < size; i++) {
                linkedList.get(i);
            }
        });
        results.add(new BenchmarkResult("LinkedList", "Get", size, linkedListTime));

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Random Access Performance (ArrayList vs LinkedList)");
        System.out.println("=".repeat(80));
        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);

        // ArrayList가 LinkedList보다 훨씬 빨라야 함
        assertThat(arrayListTime).isLessThan(linkedListTime);
    }

    private List<BenchmarkResult> runAllBenchmarks(int[] dataSizes) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int size : dataSizes) {
            results.addAll(benchmarkList("ArrayList", new ArrayList<>(), size));
            results.addAll(benchmarkList("LinkedList", new LinkedList<>(), size));
            results.addAll(benchmarkList("Vector", new Vector<>(), size));
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

        // 2. Add at beginning
        list.clear();
        long addFirstTime = measureTime(() -> {
            for (int i = 0; i < Math.min(size, 1000); i++) {
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

        // 5. Remove
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
        System.gc();
        long start = System.nanoTime();
        operation.run();
        long end = System.nanoTime();
        return end - start;
    }
}
