# Java Collections Performance Benchmark (JUnit Test Version)

JUnit 5를 사용한 Java Collections Framework 성능 벤치마크 테스트입니다.

## 📁 구조

```
test/java/com/example/java/lang/collections/
├── benchmark/
│   ├── ListBenchmarkTest.java      # List 성능 테스트
│   ├── SetBenchmarkTest.java       # Set 성능 테스트
│   ├── MapBenchmarkTest.java       # Map 성능 테스트
│   └── QueueBenchmarkTest.java     # Queue 성능 테스트
├── result/
│   ├── BenchmarkResult.java        # 결과 데이터 클래스
│   └── BenchmarkFormatter.java     # 결과 포맷팅
└── CollectionPerformanceTestSuite.java  # 전체 테스트 Suite
```

## 🚀 실행 방법

### 1. 전체 테스트 실행

```bash
# 모든 Collection 성능 테스트 실행
./gradlew :dojo:test --tests "*CollectionPerformanceTestSuite"
```

### 2. 개별 테스트 실행

```bash
# List만 테스트
./gradlew :dojo:test --tests "*ListBenchmarkTest"

# Set만 테스트
./gradlew :dojo:test --tests "*SetBenchmarkTest"

# Map만 테스트
./gradlew :dojo:test --tests "*MapBenchmarkTest"

# Queue만 테스트
./gradlew :dojo:test --tests "*QueueBenchmarkTest"
```

### 3. 특정 테스트 메서드만 실행

```bash
# ArrayList vs LinkedList 비교
./gradlew :dojo:test --tests "ListBenchmarkTest.testAddPerformanceComparison"

# HashSet vs TreeSet 비교
./gradlew :dojo:test --tests "SetBenchmarkTest.testContainsPerformanceComparison"

# HashMap vs TreeMap 비교
./gradlew :dojo:test --tests "MapBenchmarkTest.testGetPerformanceComparison"

# ArrayDeque vs LinkedList 비교
./gradlew :dojo:test --tests "QueueBenchmarkTest.testOfferPollPerformanceComparison"
```

### 4. 데이터 크기별 테스트 (ParameterizedTest)

```bash
# 100, 1000, 10000 크기로 각각 테스트
./gradlew :dojo:test --tests "ListBenchmarkTest.testListPerformanceBySize"
```

## 📊 테스트 종류

### ListBenchmarkTest
- **testListPerformanceAll**: 전체 List 성능 테스트
- **testListPerformanceBySize**: 데이터 크기별 테스트 (100, 1K, 10K)
- **testAddPerformanceComparison**: ArrayList vs LinkedList Add 비교
- **testRandomAccessPerformance**: Random Access 성능 (assertion 포함)

### SetBenchmarkTest
- **testSetPerformanceAll**: 전체 Set 성능 테스트
- **testSetPerformanceBySize**: 데이터 크기별 테스트
- **testContainsPerformanceComparison**: HashSet vs TreeSet (assertion 포함)

### MapBenchmarkTest
- **testMapPerformanceAll**: 전체 Map 성능 테스트
- **testMapPerformanceBySize**: 데이터 크기별 테스트
- **testGetPerformanceComparison**: HashMap vs TreeMap (assertion 포함)
- **testConcurrentHashMapPerformance**: ConcurrentHashMap 성능

### QueueBenchmarkTest
- **testQueuePerformanceAll**: 전체 Queue 성능 테스트
- **testQueuePerformanceBySize**: 데이터 크기별 테스트
- **testOfferPollPerformanceComparison**: ArrayDeque vs LinkedList
- **testConcurrentQueuePerformance**: Concurrent Queue 성능

## ✅ Assertion (검증)

일부 테스트는 성능 assertion을 포함합니다:

```java
// ArrayList가 LinkedList보다 Random Access가 빨라야 함
assertThat(arrayListTime).isLessThan(linkedListTime);

// HashSet이 TreeSet보다 Contains가 빨라야 함 (O(1) vs O(log n))
assertThat(hashSetTime).isLessThan(treeSetTime);

// HashMap이 TreeMap보다 Get이 빨라야 함
assertThat(hashMapTime).isLessThan(treeMapTime);
```

## 📈 출력 예시

```
================================================================================
Operation: Add (end)
================================================================================

Data Size: 10.0K
--------------------------------------------------------------------------------
Collection                           Time           Throughput
--------------------------------------------------------------------------------
ArrayList                         1.99 ms         5.02 M ops/s
LinkedList                        1.15 ms         8.72 M ops/s
Vector                            1.34 ms         7.49 M ops/s
CopyOnWriteArrayList             53.63 ms       186.45 K ops/s

================================================================================
PERFORMANCE COMPARISON (Relative Speed)
================================================================================

Add (end):
  Size 10.0K:
    LinkedList           ██████████ (1.00x) ← FASTEST
    Vector               █████████████████ (1.73x)
    ArrayList            ██████████████████████ (2.24x)
    CopyOnWriteArrayList ██████████████████████████████████████████████████ (60.59x)
```

## 🎯 IDE에서 실행

### IntelliJ IDEA
1. 테스트 클래스 열기
2. 클래스명 옆의 ▶️ 버튼 클릭
3. 또는 특정 메서드의 ▶️ 버튼 클릭

### VS Code
1. Testing 패널 열기
2. Collections 테스트 찾기
3. Run 버튼 클릭

## 💡 활용 방법

### 1. 학습용
- 각 자료구조의 실제 성능 차이 확인
- Big-O 이론과 실제 성능 비교
- 상황별 최적 자료구조 선택 학습

### 2. 면접 준비
- 자료구조 성능 특성 이해
- "언제 ArrayList를 쓰고 언제 LinkedList를 쓰나요?" 답변 준비
- 실제 벤치마크 결과로 설명

### 3. 프로젝트 의사결정
- 실제 사용 패턴에 맞는 자료구조 선택
- 성능 병목 지점 파악
- 최적화 전후 비교

## ⚠️ 주의사항

1. **환경 의존성**: 실행 환경(CPU, 메모리, JVM 버전)에 따라 결과가 달라질 수 있음
2. **Warmup**: JIT 컴파일러 최적화를 위해 warmup 수행
3. **GC 영향**: System.gc() 호출로 최소화하지만 완전히 제거 불가
4. **상대적 비교**: 절대값보다는 자료구조 간 상대적 성능 비교에 집중

## 🔧 커스터마이징

### 데이터 크기 변경
```java
@ValueSource(ints = {50, 500, 5000, 50000})  // 원하는 크기로 수정
void testListPerformanceBySize(int size) {
    // ...
}
```

### 새로운 비교 테스트 추가
```java
@Test
@DisplayName("나만의 비교 테스트")
void testMyCustomComparison() {
    // 원하는 자료구조 비교 로직
}
```

## 📚 참고

- [Oracle Collections Guide](https://docs.oracle.com/javase/tutorial/collections/)
- [Java Collections Framework](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html)
