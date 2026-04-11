package com.example.java.algorithm.interview.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayName("LRU Cache 테스트")
class LRUCacheTest {

    private LRUCache cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache(2);
    }

    @Test
    @DisplayName("기본 put과 get 동작")
    void testBasicPutAndGet() {
        cache.put(1, 1);
        cache.put(2, 2);

        assertThat(cache.get(1)).isEqualTo(1);
        assertThat(cache.get(2)).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 키 조회 시 -1 반환")
    void testGetNonExistentKey() {
        assertThat(cache.get(1)).isEqualTo(-1);
    }

    @Test
    @DisplayName("용량 초과 시 LRU 항목 제거")
    void testEvictLRUWhenCapacityExceeded() {
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);  // 1이 제거됨

        assertThat(cache.get(1)).isEqualTo(-1);
        assertThat(cache.get(2)).isEqualTo(2);
        assertThat(cache.get(3)).isEqualTo(3);
    }

    @Test
    @DisplayName("get 호출 시 최근 사용으로 갱신")
    void testGetUpdatesAccessOrder() {
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);      // 1을 최근 사용으로 갱신
        cache.put(3, 3);   // 2가 제거됨

        assertThat(cache.get(1)).isEqualTo(1);
        assertThat(cache.get(2)).isEqualTo(-1);
        assertThat(cache.get(3)).isEqualTo(3);
    }

    @Test
    @DisplayName("기존 키 값 업데이트")
    void testUpdateExistingKey() {
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 10);  // 값 갱신

        assertThat(cache.get(1)).isEqualTo(10);
    }

    @Test
    @DisplayName("기존 키 업데이트 시 최근 사용으로 갱신")
    void testUpdateMovesToHead() {
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 10);  // 1을 최근 사용으로 갱신
        cache.put(3, 3);   // 2가 제거됨

        assertThat(cache.get(1)).isEqualTo(10);
        assertThat(cache.get(2)).isEqualTo(-1);
        assertThat(cache.get(3)).isEqualTo(3);
    }

    @Test
    @DisplayName("용량 1인 캐시 테스트")
    void testCapacityOne() {
        LRUCache singleCache = new LRUCache(1);
        singleCache.put(1, 1);
        singleCache.put(2, 2);

        assertThat(singleCache.get(1)).isEqualTo(-1);
        assertThat(singleCache.get(2)).isEqualTo(2);
    }

    @Test
    @DisplayName("복잡한 시나리오 테스트")
    void testComplexScenario() {
        LRUCache cache = new LRUCache(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);      // 사용 순서: 2, 3, 1
        cache.put(4, 4);   // 2가 제거됨, 사용 순서: 3, 1, 4
        cache.get(2);
        assertThat(cache.get(2)).isEqualTo(-1);
        cache.get(3);      // 사용 순서: 1, 4, 3
        cache.put(5, 5);   // 1이 제거됨

        assertThat(cache.get(1)).isEqualTo(-1);
        assertThat(cache.get(3)).isEqualTo(3);
        assertThat(cache.get(4)).isEqualTo(4);
        assertThat(cache.get(5)).isEqualTo(5);
    }

    @Test
    @DisplayName("동시 읽기/쓰기 - Thread Safety")
    void testConcurrentReadWrite() throws InterruptedException {
        LRUCache cache = new LRUCache(100);
        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        int key = threadId * operationsPerThread + j;
                        cache.put(key, key * 10);
                        cache.get(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 캐시가 정상 동작하는지 확인 (예외 없이 실행 완료)
        assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @DisplayName("동시 LRU 제거 - Race Condition 방지")
    void testConcurrentEviction() throws InterruptedException {
        LRUCache cache = new LRUCache(10);
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    // 모든 스레드가 동시에 put 시도
                    for (int j = 0; j < 100; j++) {
                        cache.put(threadId * 100 + j, j);
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 예외 발생 시 실패
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 모든 스레드 동시 시작
        doneLatch.await(10, TimeUnit.SECONDS);

        // 모든 스레드가 예외 없이 완료되어야 함
        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시 get과 put - 일관성 검증")
    void testConcurrentGetPutConsistency() throws InterruptedException {
        LRUCache cache = new LRUCache(5);
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        // 초기 데이터 설정
        for (int i = 0; i < 5; i++) {
            cache.put(i, i * 100);
        }

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        int key = j % 10;

                        // put과 get을 반복
                        cache.put(key, key * 100);
                        int value = cache.get(key);

                        // 값이 있으면 올바른 값이어야 함
                        if (value != -1 && value != key * 100) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(10, TimeUnit.SECONDS);

        // 데이터 일관성 오류가 없어야 함
        assertThat(errorCount.get()).isEqualTo(0);
    }

    // ========== 유틸리티 메서드 테스트 ==========

    @Test
    @DisplayName("size() 메서드 테스트")
    void testSize() {
        assertThat(cache.size()).isEqualTo(0);
        cache.put(1, 1);
        assertThat(cache.size()).isEqualTo(1);
        cache.put(2, 2);
        assertThat(cache.size()).isEqualTo(2);
        cache.put(3, 3); // capacity 초과, LRU 제거
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("isEmpty() 메서드 테스트")
    void testIsEmpty() {
        assertThat(cache.isEmpty()).isTrue();
        cache.put(1, 1);
        assertThat(cache.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("containsKey() 메서드 테스트")
    void testContainsKey() {
        assertThat(cache.containsKey(1)).isFalse();
        cache.put(1, 1);
        assertThat(cache.containsKey(1)).isTrue();
        cache.put(2, 2);
        cache.put(3, 3); // 1이 제거됨
        assertThat(cache.containsKey(1)).isFalse();
        assertThat(cache.containsKey(2)).isTrue();
        assertThat(cache.containsKey(3)).isTrue();
    }

    @Test
    @DisplayName("clear() 메서드 테스트")
    void testClear() {
        cache.put(1, 1);
        cache.put(2, 2);
        assertThat(cache.size()).isEqualTo(2);

        cache.clear();
        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.isEmpty()).isTrue();
        assertThat(cache.get(1)).isEqualTo(-1);
    }

    @Test
    @DisplayName("getCapacity() 메서드 테스트")
    void testGetCapacity() {
        LRUCache cache = new LRUCache(5);
        assertThat(cache.getCapacity()).isEqualTo(5);
    }

    @Test
    @DisplayName("음수 capacity로 생성 시 예외 발생")
    void testNegativeCapacity() {
        assertThat(catchThrowable(() -> new LRUCache(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capacity must be positive");

        assertThat(catchThrowable(() -> new LRUCache(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
