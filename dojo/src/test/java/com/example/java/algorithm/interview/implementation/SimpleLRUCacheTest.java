package com.example.java.algorithm.interview.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleLRUCache (LinkedHashMap 기반) 테스트")
class SimpleLRUCacheTest {

    private SimpleLRUCache cache;

    @BeforeEach
    void setUp() {
        cache = new SimpleLRUCache(2);
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
    @DisplayName("LinkedHashMap 내장 기능 활용")
    void testLinkedHashMapFeatures() {
        cache.put(1, 1);
        cache.put(2, 2);

        // size, isEmpty, containsKey 등 모두 사용 가능
        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.isEmpty()).isFalse();
        assertThat(cache.containsKey(1)).isTrue();

        cache.clear();
        assertThat(cache.isEmpty()).isTrue();
    }
}
