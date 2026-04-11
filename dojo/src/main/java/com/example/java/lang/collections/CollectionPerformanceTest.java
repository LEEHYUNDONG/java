package com.example.java.lang.collections;

import com.example.java.lang.collections.benchmark.*;
import com.example.java.lang.collections.result.BenchmarkFormatter;
import com.example.java.lang.collections.result.BenchmarkResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Java Collections 성능 테스트 메인 클래스
 *
 * 실행 방법:
 * - 전체 테스트: runAll()
 * - List만: runListBenchmark()
 * - Set만: runSetBenchmark()
 * - Map만: runMapBenchmark()
 * - Queue만: runQueueBenchmark()
 */
public class CollectionPerformanceTest {

    // 테스트할 데이터 크기들
    private static final int[] SMALL_SIZES = {100, 1_000};
    private static final int[] MEDIUM_SIZES = {100, 1_000, 10_000};
    private static final int[] LARGE_SIZES = {100, 1_000, 10_000, 100_000};

    public static void main(String[] args) {
        CollectionPerformanceTest test = new CollectionPerformanceTest();

        System.out.println("=".repeat(100));
        System.out.println("Java Collections Framework - Performance Benchmark");
        System.out.println("=".repeat(100));
        System.out.println();

        // 전체 테스트 실행 (시간이 오래 걸림)
        // test.runAll();

        // 개별 테스트 (원하는 것만 선택)
        test.runListBenchmark();
        // test.runSetBenchmark();
        // test.runMapBenchmark();
        // test.runQueueBenchmark();
    }

    /**
     * 모든 벤치마크 실행
     */
    public void runAll() {
        List<BenchmarkResult> allResults = new ArrayList<>();

        System.out.println("\n" + "=".repeat(100));
        System.out.println("Running ALL Benchmarks...");
        System.out.println("=".repeat(100));

        allResults.addAll(runListBenchmark());
        allResults.addAll(runSetBenchmark());
        allResults.addAll(runMapBenchmark());
        allResults.addAll(runQueueBenchmark());

        System.out.println("\n\n");
        System.out.println("=".repeat(100));
        System.out.println("OVERALL RESULTS");
        System.out.println("=".repeat(100));

        BenchmarkFormatter.printResults(allResults);
        BenchmarkFormatter.printComparison(allResults);
        BenchmarkFormatter.printSummary(allResults);
    }

    /**
     * List 벤치마크 실행
     */
    public List<BenchmarkResult> runListBenchmark() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("List Benchmark (ArrayList, LinkedList, Vector, CopyOnWriteArrayList)");
        System.out.println("=".repeat(100));

        ListBenchmark benchmark = new ListBenchmark();
        List<BenchmarkResult> results = benchmark.runAllBenchmarks(MEDIUM_SIZES);

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        return results;
    }

    /**
     * Set 벤치마크 실행
     */
    public List<BenchmarkResult> runSetBenchmark() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("Set Benchmark (HashSet, LinkedHashSet, TreeSet, ConcurrentSkipListSet)");
        System.out.println("=".repeat(100));

        SetBenchmark benchmark = new SetBenchmark();
        List<BenchmarkResult> results = benchmark.runAllBenchmarks(MEDIUM_SIZES);

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        return results;
    }

    /**
     * Map 벤치마크 실행
     */
    public List<BenchmarkResult> runMapBenchmark() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("Map Benchmark (HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap)");
        System.out.println("=".repeat(100));

        MapBenchmark benchmark = new MapBenchmark();
        List<BenchmarkResult> results = benchmark.runAllBenchmarks(MEDIUM_SIZES);

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        return results;
    }

    /**
     * Queue 벤치마크 실행
     */
    public List<BenchmarkResult> runQueueBenchmark() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("Queue Benchmark (ArrayDeque, LinkedList, PriorityQueue, Concurrent Queues)");
        System.out.println("=".repeat(100));

        QueueBenchmark benchmark = new QueueBenchmark();
        List<BenchmarkResult> results = benchmark.runAllBenchmarks(MEDIUM_SIZES);

        BenchmarkFormatter.printResults(results);
        BenchmarkFormatter.printComparison(results);
        BenchmarkFormatter.printSummary(results);

        return results;
    }
}
