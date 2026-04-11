# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Java 학습 저장소(`java-laboratory`)로, 멀티 모듈 Gradle 프로젝트로 구성되어 있습니다:
- **architecture**: 아키텍처 패턴 비교 (헥사고날 vs 레이어드) - 광고 서빙 시스템 예제
- **dojo**: 알고리즘, 디자인 패턴, Java 언어 기능, 실전 코딩 문제

## 빌드 시스템

**기술 스택**: Gradle + Java 21

### 자주 사용하는 명령어

```bash
# 전체 프로젝트 빌드
./gradlew build

# 전체 테스트 실행
./gradlew test

# 모듈별 테스트 실행
./gradlew :architecture:test
./gradlew :dojo:test

# 특정 테스트 클래스 실행
./gradlew :dojo:test --tests "com.example.java.practice.cache.service.ProductServiceTest"

# 패턴으로 테스트 검색 실행
./gradlew :dojo:test --tests "*EventService*"

# 컬렉션 성능 벤치마크 실행 (전체)
./gradlew :dojo:test --tests "*CollectionPerformanceTestSuite"

# 개별 벤치마크 실행
./gradlew :dojo:test --tests "ListBenchmarkTest.testAddPerformanceComparison"
./gradlew :dojo:test --tests "MapBenchmarkTest.testGetPerformanceComparison"

# 빌드 캐시 정리 후 재빌드
./gradlew clean build

# 테스트 캐시 정리 후 재실행
./gradlew cleanTest test

# 참고: Windows에서는 ./gradlew 대신 gradlew.bat 사용
# 참고: Java 21이 필요하며, JAVA_HOME 설정 필요 시 명령어 앞에 추가:
# JAVA_HOME=/path/to/java21 ./gradlew test
```

## Architecture 모듈

광고 서빙 시스템을 두 가지 아키텍처로 구현하여 비교:

### 헥사고날 아키텍처 (포트-어댑터 패턴)

위치: `architecture/src/main/java/com/easttwave/pr/architecture/hexagonal/`

**핵심 원칙**: 비즈니스 로직(domain + application)을 외부 관심사로부터 포트와 어댑터를 통해 격리

**구조**:
```
hexagonal/ads/
├── management/         # 광고 생성 및 관리 bounded context
│   ├── ad/
│   │   ├── domain/           # 비즈니스 엔티티 (Ad, AdId, ImageUri)
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   ├── in/       # 유스케이스 인터페이스 (AdUsecase)
│   │   │   │   └── out/      # 저장소 인터페이스 (AdPersistencePort)
│   │   │   └── service/      # 비즈니스 로직 구현 (AdService)
│   │   └── adapter/
│   │       ├── in/
│   │       │   └── api/      # REST 컨트롤러
│   │       └── out/
│   │           └── persistence/  # JPA 구현체
│   └── campaign/
├── serving/            # 광고 서빙 bounded context
├── tracking/           # 광고 트래킹 bounded context
└── shared/            # 공유 인프라 (JPA 설정, 메시징)
```

**주요 패턴**:
- Domain 계층은 프레임워크 의존성이 없는 순수 Java
- Application 계층은 포트(인터페이스)를 통해 필요한 것을 정의
- Adapter는 포트를 구현하여 외부 시스템(DB, REST, Redis, SQS)과 연결
- 의존성 방향: 항상 내부로 (Adapter → Port, 역방향 금지)

### 레이어드 아키텍처

위치: `architecture/src/main/java/com/easttwave/pr/architecture/layered/`

전통적인 Controller → Service → Repository 구조. Service가 인프라 관심사(예: `RedisTemplate`)에 직접 의존하는 구조로, 헥사고날 아키텍처와의 한계를 비교하기 위한 예제.

### Architecture 모듈 의존성

- Spring Boot 3.1.3
- Spring Data JPA (H2 사용)
- Spring Data Redis
- SpringDoc OpenAPI
- p6spy (SQL 로깅)

## Dojo 모듈

주제별로 구성된 학습 및 실습 코드:

### 주요 카테고리

**algorithm/**: 알고리즘 연습 및 인터뷰 문제
- `dfs/`, `recursive/`: 트리/그래프 탐색 알고리즘
- `leetcode/`: LeetCode 문제 풀이
- `string/`: 문자열 처리 알고리즘
- `interview/payment/`: 인터뷰 스타일 코딩 문제 (예: DuplicatePaymentDetector)
- `interview/implementation/`: 실전 시스템 구현 문제 (LRUCache, RateLimiter, CircuitBreaker 등 9가지)
  - **중요**: RateLimiter는 Guava Striped Lock을 사용한 동시성 제어 구현
  - 모든 클래스는 포괄적인 JUnit 테스트 포함 (동시성 테스트 포함)

**designpattern/**: GoF 디자인 패턴 예제
- `_01_singleton/`, `_02_factorymethod/`, `_03_composite/` 등
- 각 패턴은 before/after 예제 및 docs.md 포함

**effectivejava/**: Effective Java 책 예제
- 아이템 번호로 구성 (예: `item02/` - Builder 패턴)

**lang/**: Java 언어 기능 및 고급 주제
- `advanced/_1/thread/`: 포괄적인 스레딩 예제
  - `cas/`: Compare-and-swap, 원자적 연산, 스핀락
  - `executor/`: 스레드 풀, Future, 거부 정책
  - `sync/`: 동기화, Lock, LockSupport
  - `collection/`: 스레드 안전 컬렉션
- `advanced/_2/io/`: I/O 연산
- `advanced/_3/lambda/`: 람다 표현식
- `basic/`: 핵심 Java 개념 (다형성 등)
- `stream/`, `generic/`: 스트림 및 제네릭
- `collections/`: **Java Collections 성능 벤치마크 프레임워크** (JUnit 테스트 기반)
  - `benchmark/`: List, Set, Map, Queue 벤치마크 테스트
  - `result/`: BenchmarkResult, BenchmarkFormatter
  - README.md에 실행 방법 및 활용법 상세 설명 (한글)

**practice/**: 실전 프로그래밍 챌린지
- `cache/`: TTL 및 동시성 제어가 있는 캐싱 시스템 구현 (한글 README.md)
- `junit/`: 커스텀 JUnit 프레임워크 구현
- `collections/`: 컬렉션 프레임워크 연습
- `ratelimiter/`: Resilience4j를 사용한 이벤트 생성 시스템 (Rate Limiting 포함)
- `aop/`: 프록시를 활용한 관점 지향 프로그래밍
- `masking/`: Jackson을 사용한 데이터 마스킹

참고: 일부 practice 모듈은 한글 README.md에 상세한 요구사항 포함

### Dojo 모듈 의존성

- Spring Boot (web, AOP)
- Jackson (JSON 처리)
- Resilience4j (Rate Limiting)
- AssertJ (유창한 assertion)
- Guava (Striped Lock 등 유틸리티)
- JUnit Platform Suite (성능 벤치마크 테스트 스위트)

## 테스트

모든 테스트는 JUnit 5(`org.junit.jupiter`) 사용. 테스트 파일은 `src/test/java/` 아래에 소스 구조를 미러링.

`@DisplayName` 어노테이션으로 설명적인 테스트명 작성.

### 성능 벤치마크 테스트

`dojo/src/test/java/com/example/java/lang/collections/` 패키지:
- **JUnit 기반** 벤치마크 (실행 가능한 테스트)
- List, Set, Map, Queue 구현체별 성능 비교
- Warmup iteration 및 JIT 최적화 고려
- 성능 assertion 포함 (예: `assertThat(hashMapTime).isLessThan(treeMapTime)`)
- 시각적 출력: 실행 시간, 처리량, 상대 성능 막대 그래프

### 동시성 테스트

RateLimiter 등 동시성 관련 클래스는 `CountDownLatch`를 사용한 멀티스레드 테스트 포함:
- Race condition 검증
- Thread-safety 확인
- Striped Lock 성능 검증

## 코드 패턴 가이드

### 헥사고날 아키텍처 작성 시

1. **Domain 객체**는 프레임워크 의존성 없는 순수 Java
2. **Usecase 인터페이스** (`application/port/in/`)는 애플리케이션이 무엇을 하는지 정의
3. **Port 인터페이스** (`application/port/out/`)는 애플리케이션이 인프라로부터 무엇이 필요한지 정의
4. **Service**는 유스케이스를 구현하며 포트 인터페이스에만 의존
5. **Adapter**는 포트를 구현하며 프레임워크 특화 관심사 처리
6. **Value Object** (예: `AdId`, `ImageUri`, `DateRange`)로 도메인 개념 캡슐화

### 일반 패턴

- DTO 및 불변 값 객체는 record 사용
- Lombok 사용 가능 (`@Getter`, `@RequiredArgsConstructor` 등)
- architecture 모듈은 bounded context 분리 준수
- 테스트는 엣지 케이스를 포함한 포괄적인 테스트 케이스 작성

### 동시성 코드 작성 시

- 전역 락보다는 **Striped Lock** 사용 (Guava 제공)
  - 예: `Striped<Lock> stripedLock = Striped.lock(128)`
  - 사용자별/키별로 분산된 락 제공
- `ConcurrentHashMap` 사용 시 내부 연산의 원자성 고려
- 통계는 `AtomicLong` 사용
- 동시성 테스트는 반드시 작성 (실제 race condition 검증)

### 성능 벤치마크 작성 시

- Warmup iteration 필수 (JIT 최적화)
- `System.gc()` 호출로 GC 영향 최소화
- 여러 번 측정하여 최소값 사용 (안정된 성능)
- 데이터 크기를 충분히 크게 (차이 명확히)
- Assertion으로 Big-O 복잡도 검증 (`O(1)` vs `O(log n)`)

## 프로젝트 구조

루트 `build.gradle`은 공통 의존성 정의:
- Lombok (모든 서브프로젝트)
- JUnit 5 (모든 서브프로젝트)

각 모듈은 자체 `build.gradle`로 모듈별 의존성 관리.

## 참고 사항

- 두 모듈 모두 `bootJar { enabled = false }` - 실행 가능한 애플리케이션이 아닌 라이브러리/학습 프로젝트
- architecture 모듈은 교육 목적으로 hexagonal과 layered 두 예제 모두 포함
- 디자인 패턴 예제는 `docs.md` 파일에 상세 문서 포함
- practice 모듈(cache, junit, collections 등)은 한글 README.md에 문제 설명 및 요구사항 포함
- Collections 성능 벤치마크는 학습용, 면접 준비용, 프로젝트 의사결정용으로 활용 가능
- 상대적 성능 비교에 집중 (절대값은 환경에 따라 달라질 수 있음)
