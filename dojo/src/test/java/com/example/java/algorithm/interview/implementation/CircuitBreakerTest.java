package com.example.java.algorithm.interview.implementation;

import com.example.java.algorithm.interview.implementation.CircuitBreaker.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Circuit Breaker 테스트")
class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        circuitBreaker = new CircuitBreaker(50, 3, Duration.ofSeconds(5));
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("초기 상태는 CLOSED")
    void testInitialStateClosed() {
        assertThat(circuitBreaker.getState()).isEqualTo(State.CLOSED);
    }

    @Test
    @DisplayName("CLOSED 상태에서 요청 허용")
    void testAllowRequestInClosedState() {
        assertThat(circuitBreaker.allowRequestSolution(baseTime)).isTrue();
    }

    @Test
    @DisplayName("실패율 임계값 초과 시 OPEN으로 전이")
    void testTransitionToOpenOnFailureThreshold() {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        assertThat(circuitBreaker.getState()).isEqualTo(State.CLOSED);
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(33);

        circuitBreaker.recordFailureSolution();  // 실패율 50% (2/4)
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);
    }

    @Test
    @DisplayName("OPEN 상태에서 요청 차단")
    void testRejectRequestInOpenState() {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isFalse();
    }

    @Test
    @DisplayName("OPEN -> HALF_OPEN 전이 (timeout 경과)")
    void testTransitionToHalfOpenAfterTimeout() throws InterruptedException {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        // timeout 경과 전
        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isFalse();
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);

        // timeout 경과 후
        Thread.sleep(5100);  // 5초 대기
        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isTrue();
        assertThat(circuitBreaker.getState()).isEqualTo(State.HALF_OPEN);
    }

    @Test
    @DisplayName("HALF_OPEN 상태에서 제한된 요청만 허용")
    void testLimitedRequestsInHalfOpen() throws InterruptedException {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        // HALF_OPEN으로 전이
        Thread.sleep(5100);
        circuitBreaker.allowRequestSolution(LocalDateTime.now());

        // halfOpenMaxCalls = 3
        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isTrue();
        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isTrue();
        assertThat(circuitBreaker.allowRequestSolution(LocalDateTime.now())).isFalse();
    }

    @Test
    @DisplayName("HALF_OPEN에서 성공 시 CLOSED로 복구")
    void testTransitionToClosedOnSuccessInHalfOpen() throws InterruptedException {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        Thread.sleep(5100);
        circuitBreaker.allowRequestSolution(LocalDateTime.now());  // HALF_OPEN

        circuitBreaker.recordSuccessSolution();  // CLOSED로 복구
        assertThat(circuitBreaker.getState()).isEqualTo(State.CLOSED);
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("HALF_OPEN에서 실패 시 다시 OPEN")
    void testTransitionToOpenOnFailureInHalfOpen() {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN
        circuitBreaker.allowRequestSolution(baseTime.plusSeconds(5));  // HALF_OPEN

        circuitBreaker.recordFailureSolution();  // 다시 OPEN
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);
    }

    @Test
    @DisplayName("실패율 계산")
    void testFailureRateCalculation() {
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(0);

        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(33);  // 1/3 = 33%

        circuitBreaker.recordFailureSolution();
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(50);  // 2/4 = 50%
    }

    @Test
    @DisplayName("reset 메서드")
    void testReset() {
        circuitBreaker.recordSuccessSolution();
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        circuitBreaker.reset();
        assertThat(circuitBreaker.getState()).isEqualTo(State.CLOSED);
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("OPEN 상태에서 성공 기록 무시")
    void testIgnoreSuccessInOpenState() {
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        circuitBreaker.recordSuccessSolution();  // 무시됨
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);
    }

    @Test
    @DisplayName("OPEN 상태에서 실패 기록 무시")
    void testIgnoreFailureInOpenState() {
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();  // OPEN

        int failureRateBefore = circuitBreaker.getFailureRate();
        circuitBreaker.recordFailureSolution();  // 무시됨
        assertThat(circuitBreaker.getFailureRate()).isEqualTo(failureRateBefore);
    }

    @Test
    @DisplayName("다양한 실패율 임계값 테스트")
    void testDifferentFailureThresholds() {
        CircuitBreaker cb30 = new CircuitBreaker(30, 3, Duration.ofSeconds(5));
        cb30.recordSuccessSolution();
        cb30.recordSuccessSolution();
        cb30.recordSuccessSolution();
        cb30.recordFailureSolution();
        cb30.recordFailureSolution();  // 실패율 40% (2/5)

        assertThat(cb30.getState()).isEqualTo(State.OPEN);  // 30% 임계값 초과
    }

    @Test
    @DisplayName("연속 OPEN -> HALF_OPEN -> OPEN 전이")
    void testMultipleOpenHalfOpenCycles() throws InterruptedException {
        // 첫 번째 OPEN
        circuitBreaker.recordFailureSolution();
        circuitBreaker.recordFailureSolution();
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);

        // HALF_OPEN으로 전이
        Thread.sleep(5100);
        circuitBreaker.allowRequestSolution(LocalDateTime.now());
        assertThat(circuitBreaker.getState()).isEqualTo(State.HALF_OPEN);

        // 실패로 다시 OPEN
        circuitBreaker.recordFailureSolution();
        assertThat(circuitBreaker.getState()).isEqualTo(State.OPEN);

        // 다시 HALF_OPEN으로 전이
        Thread.sleep(5100);
        circuitBreaker.allowRequestSolution(LocalDateTime.now());
        assertThat(circuitBreaker.getState()).isEqualTo(State.HALF_OPEN);

        // 성공으로 CLOSED 복구
        circuitBreaker.recordSuccessSolution();
        assertThat(circuitBreaker.getState()).isEqualTo(State.CLOSED);
    }
}
