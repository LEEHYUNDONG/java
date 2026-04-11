package com.example.java.lang.collections.result;

/**
 * 벤치마크 결과를 저장하는 클래스
 */
public class BenchmarkResult {
    private final String collectionName;
    private final String operation;
    private final int dataSize;
    private final long timeNanos;
    private final double throughput; // operations per second

    public BenchmarkResult(String collectionName, String operation, int dataSize, long timeNanos) {
        this.collectionName = collectionName;
        this.operation = operation;
        this.dataSize = dataSize;
        this.timeNanos = timeNanos;
        this.throughput = calculateThroughput(dataSize, timeNanos);
    }

    private double calculateThroughput(int operations, long timeNanos) {
        if (timeNanos == 0) return 0;
        return (operations * 1_000_000_000.0) / timeNanos;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getOperation() {
        return operation;
    }

    public int getDataSize() {
        return dataSize;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public long getTimeMillis() {
        return timeNanos / 1_000_000;
    }

    public double getThroughput() {
        return throughput;
    }

    public String getFormattedTime() {
        if (timeNanos < 1_000) {
            return timeNanos + " ns";
        } else if (timeNanos < 1_000_000) {
            return String.format("%.2f μs", timeNanos / 1_000.0);
        } else if (timeNanos < 1_000_000_000) {
            return String.format("%.2f ms", timeNanos / 1_000_000.0);
        } else {
            return String.format("%.2f s", timeNanos / 1_000_000_000.0);
        }
    }

    public String getFormattedThroughput() {
        if (throughput >= 1_000_000) {
            return String.format("%.2f M ops/s", throughput / 1_000_000);
        } else if (throughput >= 1_000) {
            return String.format("%.2f K ops/s", throughput / 1_000);
        } else {
            return String.format("%.2f ops/s", throughput);
        }
    }
}
