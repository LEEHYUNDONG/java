package com.example.java.algorithm.interview.implementation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 문제: Circuit Breaker Pattern (서킷 브레이커 패턴)
 *
 * 실무 시나리오:
 * 외부 API 호출 시 장애 확산을 방지하는 Circuit Breaker를 구현하세요.
 * 마이크로서비스 아키텍처에서 필수적인 패턴입니다.
 *
 * 상태 전이:
 * CLOSED (정상) -> OPEN (차단) -> HALF_OPEN (반쯤 열림) -> CLOSED/OPEN
 *
 * 동작 원리:
 * 1. CLOSED: 정상 동작, 실패율 모니터링
 *    - 실패율이 threshold 초과 시 OPEN으로 전이
 *
 * 2. OPEN: 모든 요청 즉시 차단 (Fail-fast)
 *    - timeout 시간 경과 후 HALF_OPEN으로 전이
 *
 * 3. HALF_OPEN: 제한된 수의 요청만 허용
 *    - 성공 시 CLOSED로 복구
 *    - 실패 시 다시 OPEN
 *
 * 요구사항:
 * - failureThreshold: 실패율 임계값 (예: 50%)
 * - openTimeout: OPEN 상태 유지 시간
 * - halfOpenMaxCalls: HALF_OPEN 시 허용할 최대 요청 수
 *
 * 예시:
 * CircuitBreaker cb = new CircuitBreaker(50, 3, Duration.ofSeconds(5));
 * cb.recordSuccess();  // CLOSED 유지
 * cb.recordFailure();  // 실패 기록
 * cb.recordFailure();  // 실패율 50% -> OPEN
 * cb.allowRequest();   // false (차단)
 * // 5초 후
 * cb.allowRequest();   // true (HALF_OPEN으로 전이)
 * cb.recordSuccess();  // CLOSED로 복구
 */
public class CircuitBreaker {

    public enum State {
        CLOSED,      // 정상 동작
        OPEN,        // 차단 상태
        HALF_OPEN    // 테스트 상태
    }

    private final int failureThreshold;          // 실패율 임계값 (%)
    private final int halfOpenMaxCalls;          // HALF_OPEN 시 최대 허용 호출 수
    private final Duration openTimeout;          // OPEN -> HALF_OPEN 전이 시간

    private final AtomicReference<State> state;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;
    private final AtomicInteger halfOpenCallCount;
    private volatile LocalDateTime openedAt;     // OPEN으로 전이된 시간

    public CircuitBreaker(int failureThreshold, int halfOpenMaxCalls, Duration openTimeout) {
        this.failureThreshold = failureThreshold;
        this.halfOpenMaxCalls = halfOpenMaxCalls;
        this.openTimeout = openTimeout;
        this.state = new AtomicReference<>(State.CLOSED);
        this.successCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
        this.halfOpenCallCount = new AtomicInteger(0);
    }

    /**
     * 요청 허용 여부를 확인합니다.
     *
     * @param currentTime 현재 시간
     * @return true if allowed, false if rejected
     */
    public boolean allowRequest(LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. 현재 상태에 따라 처리
        //    - CLOSED: 항상 허용
        //    - OPEN: timeout 경과 확인
        //      - 경과했으면 HALF_OPEN으로 전이하고 허용
        //      - 아니면 거부
        //    - HALF_OPEN: halfOpenCallCount < halfOpenMaxCalls 확인
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 성공 기록
     */
    public void recordSuccess() {
        // TODO: 구현하세요
        // 힌트:
        // 1. CLOSED: successCount 증가
        // 2. HALF_OPEN: 테스트 성공 -> CLOSED로 전이, 카운터 리셋
        // 3. OPEN: 무시
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 실패 기록
     */
    public void recordFailure() {
        // TODO: 구현하세요
        // 힌트:
        // 1. CLOSED: failureCount 증가, 실패율 체크
        //    - 실패율 >= threshold이면 OPEN으로 전이
        // 2. HALF_OPEN: 테스트 실패 -> OPEN으로 전이
        // 3. OPEN: 무시
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 현재 상태 조회
     */
    public State getState() {
        return state.get();
    }

    /**
     * 현재 실패율 계산 (%)
     */
    public int getFailureRate() {
        int total = successCount.get() + failureCount.get();
        if (total == 0) return 0;
        return (failureCount.get() * 100) / total;
    }

    /**
     * 상태 및 카운터 초기화
     */
    public void reset() {
        state.set(State.CLOSED);
        successCount.set(0);
        failureCount.set(0);
        halfOpenCallCount.set(0);
        openedAt = null;
    }

    private void transitionToOpen(LocalDateTime currentTime) {
        state.set(State.OPEN);
        openedAt = currentTime;
        halfOpenCallCount.set(0);
    }

    private void transitionToHalfOpen() {
        state.set(State.HALF_OPEN);
        halfOpenCallCount.set(0);
    }

    private void transitionToClosed() {
        state.set(State.CLOSED);
        successCount.set(0);
        failureCount.set(0);
        halfOpenCallCount.set(0);
    }

    // ========== 정답 (참고용) ==========

    public boolean allowRequestSolution(LocalDateTime currentTime) {
        State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                // timeout 경과 확인
                if (Duration.between(openedAt, currentTime).compareTo(openTimeout) >= 0) {
                    transitionToHalfOpen();
                    halfOpenCallCount.incrementAndGet();
                    return true;
                }
                return false;

            case HALF_OPEN:
                int count = halfOpenCallCount.get();
                if (count < halfOpenMaxCalls) {
                    halfOpenCallCount.incrementAndGet();
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    public void recordSuccessSolution() {
        State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                successCount.incrementAndGet();
                break;

            case HALF_OPEN:
                // HALF_OPEN에서 성공 -> CLOSED로 복구
                transitionToClosed();
                break;

            case OPEN:
                // OPEN 상태에서는 요청이 차단되므로 성공이 없어야 함
                break;
        }
    }

    public void recordFailureSolution() {
        State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                failureCount.incrementAndGet();
                // 실패율 체크
                if (getFailureRate() >= failureThreshold) {
                    transitionToOpen(LocalDateTime.now());
                }
                break;

            case HALF_OPEN:
                // HALF_OPEN에서 실패 -> 다시 OPEN
                transitionToOpen(LocalDateTime.now());
                break;

            case OPEN:
                // 이미 OPEN 상태
                break;
        }
    }
}
