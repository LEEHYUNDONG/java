package com.example.java.lang.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * 모든 Collection 성능 테스트를 한번에 실행하는 Test Suite
 *
 * 실행 방법:
 * ./gradlew :dojo:test --tests "*CollectionPerformanceTestSuite"
 *
 * 또는 개별 실행:
 * ./gradlew :dojo:test --tests "*ListBenchmarkTest"
 * ./gradlew :dojo:test --tests "*SetBenchmarkTest"
 * ./gradlew :dojo:test --tests "*MapBenchmarkTest"
 * ./gradlew :dojo:test --tests "*QueueBenchmarkTest"
 */
@Suite
@SelectPackages("com.example.java.lang.collections.benchmark")
@DisplayName("Java Collections 전체 성능 테스트 Suite")
public class CollectionPerformanceTestSuite {

    // Test Suite는 @SelectPackages 어노테이션으로 자동 실행
    // 별도의 코드 불필요

    /**
     * README:
     *
     * 이 Test Suite는 다음 테스트들을 모두 실행합니다:
     * - ListBenchmarkTest: ArrayList, LinkedList, Vector, CopyOnWriteArrayList
     * - SetBenchmarkTest: HashSet, LinkedHashSet, TreeSet, ConcurrentSkipListSet
     * - MapBenchmarkTest: HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap
     * - QueueBenchmarkTest: ArrayDeque, LinkedList, PriorityQueue, Concurrent Queues
     *
     * 각 테스트는 다양한 데이터 크기(100, 1K, 10K)로 성능을 측정하고
     * 결과를 시각적으로 출력합니다.
     */
}
