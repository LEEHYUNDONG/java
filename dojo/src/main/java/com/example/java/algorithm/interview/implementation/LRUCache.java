package com.example.java.algorithm.interview.implementation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 문제: LRU (Least Recently Used) Cache
 *
 * 실무 시나리오:
 * 제한된 용량을 가진 캐시를 구현하세요.
 * 용량이 가득 찬 경우, 가장 오랫동안 사용되지 않은 항목을 제거합니다.
 *
 * 요구사항:
 * 1. get(key) - O(1) 시간 복잡도로 값을 조회
 * 2. put(key, value) - O(1) 시간 복잡도로 값을 저장
 * 3. 용량 초과 시 LRU 항목 자동 제거
 * 4. get/put 모두 최근 사용으로 간주 (접근 순서 갱신)
 *
 * 제약사항:
 * - capacity는 양수
 * - 모든 연산은 O(1) 시간 복잡도
 *
 * 예시:
 * LRUCache cache = new LRUCache(2);
 * cache.put(1, 1);        // cache: {1=1}
 * cache.put(2, 2);        // cache: {1=1, 2=2}
 * cache.get(1);           // returns 1, cache: {2=2, 1=1} (1이 최근 사용)
 * cache.put(3, 3);        // cache: {1=1, 3=3} (2가 LRU로 제거됨)
 * cache.get(2);           // returns -1 (not found)
 * cache.put(4, 4);        // cache: {3=3, 4=4} (1이 LRU로 제거됨)
 *
 * 힌트:
 * - HashMap + 양방향 연결 리스트 (Doubly Linked List) 조합 사용
 * - HashMap으로 O(1) 조회, 연결 리스트로 순서 관리
 */
public class LRUCache {

    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head; // 더미 헤드 (가장 최근 사용)
    private final Node tail; // 더미 테일 (가장 오래 사용)

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(0, 0);
        this.tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    /**
     * 캐시에서 값을 조회합니다.
     * Thread-safe: 전체 연산을 동기화하여 원자성 보장
     *
     * @return 값이 존재하면 해당 값, 없으면 -1
     */
    public synchronized int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1;
        }
        // 노드를 헤드로 이동 (최근 사용으로 갱신)
        moveToHead(node);
        return node.value;
    }

    /**
     * 캐시에 값을 저장합니다.
     * Thread-safe: 전체 연산을 동기화하여 원자성 보장
     */
    public synchronized void put(int key, int value) {
        Node node = cache.get(key);
        LinkedHashMap<Object, Object> objectObjectLinkedHashMap = new LinkedHashMap<>();

        if (node != null) {
            // 기존 키가 존재하면 값을 갱신하고 헤드로 이동
            node.value = value;
            moveToHead(node);
        } else {
            // 새 키 추가
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addToHead(newNode);

            // 용량 초과 시 LRU 항목 제거
            if (cache.size() > capacity) {
                Node lru = removeTail();
                cache.remove(lru.key);
            }
        }
    }

    /**
     * 양방향 연결 리스트의 노드
     */
    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * 노드를 리스트에서 제거
     */
    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * 노드를 헤드 바로 뒤에 추가 (가장 최근 사용 위치)
     */
    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * 노드를 헤드로 이동 (최근 사용으로 갱신)
     */
    private void moveToHead(Node node) {
        remove(node);
        addToHead(node);
    }

    /**
     * Tail 앞의 노드 제거 (LRU 제거)
     * @return 제거된 노드
     */
    private Node removeTail() {
        Node lru = tail.prev;
        remove(lru);
        return lru;
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 현재 캐시에 저장된 항목의 개수를 반환합니다.
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * 캐시가 비어있는지 확인합니다.
     */
    public synchronized boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * 캐시에 특정 키가 존재하는지 확인합니다.
     */
    public synchronized boolean containsKey(int key) {
        return cache.containsKey(key);
    }

    /**
     * 캐시의 모든 항목을 제거합니다.
     */
    public synchronized void clear() {
        cache.clear();
        head.next = tail;
        tail.prev = head;
    }

    /**
     * 캐시의 최대 용량을 반환합니다.
     */
    public int getCapacity() {
        return capacity;
    }
}
