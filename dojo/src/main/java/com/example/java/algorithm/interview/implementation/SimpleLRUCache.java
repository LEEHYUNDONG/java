package com.example.java.algorithm.interview.implementation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LinkedHashMap을 사용한 간단한 LRU Cache 구현
 *
 * LinkedHashMap은 내부적으로 HashMap + Doubly Linked List를 사용하며,
 * accessOrder=true로 설정하면 자동으로 LRU 순서를 유지합니다.
 *
 * 장점:
 * - 코드가 훨씬 간결 (20줄 vs 150줄)
 * - 버그 발생 가능성 낮음
 * - Java 표준 라이브러리로 검증됨
 *
 * 단점:
 * - 내부 구조를 이해하기 어려움 (학습용으로는 부족)
 * - 커스터마이징 제한적
 *
 * 실무에서는 이 방식을 권장!
 */
public class SimpleLRUCache extends LinkedHashMap<Integer, Integer> {

    private final int capacity;

    /**
     * @param capacity 캐시 최대 용량
     * @param accessOrder true면 접근 순서, false면 삽입 순서
     */
    public SimpleLRUCache(int capacity) {
        super(capacity, 0.75f, true);  // accessOrder=true가 핵심!
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
    }

    /**
     * 캐시에서 값을 조회합니다.
     * LinkedHashMap이 자동으로 접근 순서를 갱신합니다.
     */
    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    /**
     * 캐시에 값을 저장합니다.
     */
    public void put(int key, int value) {
        super.put(key, value);
    }

    /**
     * 용량 초과 시 가장 오래된 항목(eldest)을 제거할지 결정
     * LinkedHashMap이 자동으로 호출합니다.
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;
    }

    /**
     * Thread-safe 버전이 필요하다면 Collections.synchronizedMap 사용
     *
     * Map<Integer, Integer> syncCache = Collections.synchronizedMap(
     *     new SimpleLRUCache(capacity)
     * );
     */
}
