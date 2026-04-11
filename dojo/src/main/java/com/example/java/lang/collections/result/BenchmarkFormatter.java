package com.example.java.lang.collections.result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 벤치마크 결과를 보기 좋게 포맷팅하는 클래스
 */
public class BenchmarkFormatter {

    public static void printResults(List<BenchmarkResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results to display");
            return;
        }

        // 작업별로 그룹화
        Map<String, List<BenchmarkResult>> groupedByOperation = results.stream()
            .collect(Collectors.groupingBy(BenchmarkResult::getOperation));

        groupedByOperation.forEach((operation, operationResults) -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Operation: " + operation);
            System.out.println("=".repeat(80));

            // 데이터 크기별로 그룹화
            Map<Integer, List<BenchmarkResult>> groupedBySize = operationResults.stream()
                .collect(Collectors.groupingBy(BenchmarkResult::getDataSize));

            groupedBySize.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int size = entry.getKey();
                    List<BenchmarkResult> sizeResults = entry.getValue();

                    System.out.println("\nData Size: " + formatNumber(size));
                    System.out.println("-".repeat(80));
                    System.out.printf("%-25s %15s %20s%n", "Collection", "Time", "Throughput");
                    System.out.println("-".repeat(80));

                    // 시간 순으로 정렬 (빠른 순)
                    sizeResults.stream()
                        .sorted(Comparator.comparingLong(BenchmarkResult::getTimeNanos))
                        .forEach(result -> {
                            System.out.printf("%-25s %15s %20s%n",
                                result.getCollectionName(),
                                result.getFormattedTime(),
                                result.getFormattedThroughput());
                        });
                });
        });

        System.out.println("\n" + "=".repeat(80));
    }

    public static void printComparison(List<BenchmarkResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results to display");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("PERFORMANCE COMPARISON (Relative Speed)");
        System.out.println("=".repeat(100));

        Map<String, List<BenchmarkResult>> groupedByOperation = results.stream()
            .collect(Collectors.groupingBy(BenchmarkResult::getOperation));

        groupedByOperation.forEach((operation, operationResults) -> {
            System.out.println("\n" + operation + ":");

            Map<Integer, List<BenchmarkResult>> groupedBySize = operationResults.stream()
                .collect(Collectors.groupingBy(BenchmarkResult::getDataSize));

            groupedBySize.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int size = entry.getKey();
                    List<BenchmarkResult> sizeResults = entry.getValue();

                    // 가장 빠른 결과 찾기
                    BenchmarkResult fastest = sizeResults.stream()
                        .min(Comparator.comparingLong(BenchmarkResult::getTimeNanos))
                        .orElse(null);

                    if (fastest == null) return;

                    System.out.println("  Size " + formatNumber(size) + ":");
                    sizeResults.stream()
                        .sorted(Comparator.comparingLong(BenchmarkResult::getTimeNanos))
                        .forEach(result -> {
                            double ratio = (double) result.getTimeNanos() / fastest.getTimeNanos();
                            String bar = generateBar(ratio, 50);
                            String marker = result == fastest ? " ← FASTEST" : "";
                            System.out.printf("    %-20s %s (%.2fx)%s%n",
                                result.getCollectionName(), bar, ratio, marker);
                        });
                });
        });

        System.out.println("\n" + "=".repeat(100));
    }

    private static String generateBar(double ratio, int maxLength) {
        int length = (int) Math.min(ratio * 10, maxLength);
        return "█".repeat(length);
    }

    private static String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }

    public static void printSummary(List<BenchmarkResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results to display");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("SUMMARY - Winner by Category");
        System.out.println("=".repeat(80));

        Map<String, List<BenchmarkResult>> groupedByOperation = results.stream()
            .collect(Collectors.groupingBy(BenchmarkResult::getOperation));

        groupedByOperation.forEach((operation, operationResults) -> {
            BenchmarkResult fastest = operationResults.stream()
                .min(Comparator.comparingLong(BenchmarkResult::getTimeNanos))
                .orElse(null);

            if (fastest != null) {
                System.out.printf("%-30s : %s (%s)%n",
                    operation,
                    fastest.getCollectionName(),
                    fastest.getFormattedTime());
            }
        });

        System.out.println("=".repeat(80));
    }
}
