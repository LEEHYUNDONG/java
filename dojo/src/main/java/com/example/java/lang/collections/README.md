# Java Collections Performance Benchmark

Java Collections Framework의 다양한 자료구조들의 성능을 비교 테스트하는 도구입니다.

## 📁 구조

```
collections/
├── benchmark/
│   ├── ListBenchmark.java          # List 계열 테스트
│   ├── SetBenchmark.java           # Set 계열 테스트
│   ├── MapBenchmark.java           # Map 계열 테스트
│   └── QueueBenchmark.java         # Queue 계열 테스트
├── result/
│   ├── BenchmarkResult.java        # 결과 데이터
│   └── BenchmarkFormatter.java     # 결과 포맷팅
└── CollectionPerformanceTest.java  # 메인 실행 클래스
```

## 🧪 테스트 대상

### List 계열
- **ArrayList**: 동적 배열 기반
- **LinkedList**: 이중 연결 리스트
- **Vector**: 동기화된 ArrayList
- **CopyOnWriteArrayList**: 쓰기 시 복사 방식

### Set 계열
- **HashSet**: 해시 테이블 기반
- **LinkedHashSet**: 순서를 유지하는 HashSet
- **TreeSet**: Red-Black Tree 기반 (정렬됨)
- **ConcurrentSkipListSet**: 동시성 지원 정렬 Set
- **CopyOnWriteArraySet**: 쓰기 시 복사 방식

### Map 계열
- **HashMap**: 해시 테이블 기반
- **LinkedHashMap**: 순서를 유지하는 HashMap
- **TreeMap**: Red-Black Tree 기반 (정렬됨)
- **Hashtable**: 동기화된 HashMap (legacy)
- **ConcurrentHashMap**: 동시성 지원 HashMap

### Queue 계열
- **ArrayDeque**: 배열 기반 양방향 큐
- **LinkedList**: 연결 리스트 기반 큐
- **PriorityQueue**: 힙 기반 우선순위 큐
- **ConcurrentLinkedQueue**: 동시성 지원 큐
- **LinkedBlockingQueue**: 블로킹 큐
- **ArrayBlockingQueue**: 배열 기반 블로킹 큐

## 📊 측정 항목

### List
- Add (end): 끝에 추가
- Add (beginning): 앞에 추가
- Get by index: 인덱스로 조회
- Contains: 포함 여부 확인
- Remove (end): 끝에서 제거
- Iteration: 순회

### Set
- Add: 추가
- Contains: 포함 여부 확인
- Remove: 제거
- Iteration: 순회

### Map
- Put: 추가
- Get: 조회
- ContainsKey: 키 존재 여부
- Remove: 제거
- Iteration: 순회

### Queue
- Offer: 추가
- Peek: 확인 (제거 안함)
- Poll: 제거
- Iteration: 순회

## 🚀 사용 방법

### 1. 전체 테스트 실행

```java
public static void main(String[] args) {
    CollectionPerformanceTest test = new CollectionPerformanceTest();
    test.runAll();  // 모든 자료구조 테스트
}
```

### 2. 개별 테스트 실행

```java
// List만 테스트
test.runListBenchmark();

// Set만 테스트
test.runSetBenchmark();

// Map만 테스트
test.runMapBenchmark();

// Queue만 테스트
test.runQueueBenchmark();
```

### 3. 커스텀 데이터 크기

```java
int[] customSizes = {500, 5000, 50000};
ListBenchmark benchmark = new ListBenchmark();
List<BenchmarkResult> results = benchmark.runAllBenchmarks(customSizes);
```

## 📈 결과 해석

### 시간 표시
- ns (나노초): < 1,000 ns
- μs (마이크로초): 1,000 ns ~ 1,000,000 ns
- ms (밀리초): 1,000,000 ns ~ 1,000,000,000 ns
- s (초): > 1,000,000,000 ns

### Throughput (처리량)
- ops/s: operations per second
- K ops/s: 천 개 연산/초
- M ops/s: 백만 개 연산/초

### 비교 그래프
```
ArrayList     ████████ (1.00x) ← FASTEST
LinkedList    ████████████████████████████ (3.50x)
Vector        █████████████ (1.63x)
```
- 숫자가 작을수록 빠름
- 1.00x = 가장 빠른 자료구조 (기준)
- 2.00x = 기준보다 2배 느림

## 💡 주요 발견 사항 (일반적인 경우)

### List
- **랜덤 접근**: ArrayList > Vector > LinkedList
- **순차 추가**: ArrayList ≈ LinkedList > Vector
- **앞에 추가**: LinkedList > ArrayList > Vector
- **메모리**: ArrayList < Vector < LinkedList

### Set
- **추가/조회**: HashSet > LinkedHashSet > TreeSet
- **정렬 필요**: TreeSet (자동 정렬)
- **순서 유지**: LinkedHashSet
- **동시성**: ConcurrentSkipListSet

### Map
- **일반적 사용**: HashMap > LinkedHashMap > TreeMap
- **정렬 필요**: TreeMap
- **동시성**: ConcurrentHashMap
- **레거시**: Hashtable (사용 지양)

### Queue
- **일반 큐**: ArrayDeque > LinkedList
- **우선순위**: PriorityQueue
- **동시성**: ConcurrentLinkedQueue
- **블로킹**: LinkedBlockingQueue, ArrayBlockingQueue

## ⚠️ 주의사항

1. **Warmup**: 각 테스트 전 JVM warming up 수행
2. **GC**: 측정 전 System.gc() 호출
3. **환경**: 실행 환경에 따라 결과가 다를 수 있음
4. **크기**: 데이터 크기에 따라 상대적 성능이 달라질 수 있음

## 🎯 활용 예시

```java
// 랜덤 접근이 많은 경우
List<Integer> list = new ArrayList<>();  // O(1) 접근

// 앞/뒤 삽입/삭제가 많은 경우
Deque<Integer> deque = new ArrayDeque<>();

// 중복 제거 + 빠른 조회
Set<Integer> set = new HashSet<>();

// 정렬된 데이터 필요
SortedSet<Integer> sortedSet = new TreeSet<>();

// 키-값 쌍 저장
Map<String, Integer> map = new HashMap<>();

// 동시성 필요
Map<String, Integer> concurrentMap = new ConcurrentHashMap<>();
```
