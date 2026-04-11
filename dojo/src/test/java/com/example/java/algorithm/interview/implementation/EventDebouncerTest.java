package com.example.java.algorithm.interview.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Event Debouncer 테스트")
class EventDebouncerTest {

    private EventDebouncer debouncer;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        debouncer = new EventDebouncer(Duration.ofSeconds(2));
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("이벤트 등록")
    void testDebounceEvent() {
        debouncer.debounceSolution("search", "a", baseTime);

        assertThat(debouncer.getPendingEventCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 키로 새 이벤트 발생 시 이전 이벤트 취소")
    void testDebounceCancelPreviousEvent() {
        debouncer.debounceSolution("search", "a", baseTime);
        debouncer.debounceSolution("search", "ab", baseTime.plusNanos(500_000_000));
        debouncer.debounceSolution("search", "abc", baseTime.plusSeconds(1));

        assertThat(debouncer.getPendingEventCount()).isEqualTo(1);

        // delay 경과 후 마지막 이벤트만 실행
        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(3));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abc");
    }

    @Test
    @DisplayName("delay 시간 경과 전에는 실행 불가")
    void testNoExecutionBeforeDelay() {
        debouncer.debounceSolution("search", "abc", baseTime);

        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(1));
        assertThat(executable).isEmpty();
    }

    @Test
    @DisplayName("delay 시간 경과 후 실행 가능")
    void testExecutionAfterDelay() {
        debouncer.debounceSolution("search", "abc", baseTime);

        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abc");
    }

    @Test
    @DisplayName("실행 후 대기 이벤트 제거")
    void testRemovePendingEventAfterExecution() {
        debouncer.debounceSolution("search", "abc", baseTime);

        debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(debouncer.getPendingEventCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("키별 독립적인 디바운싱")
    void testIndependentDebouncing() {
        debouncer.debounceSolution("search", "abc", baseTime);
        debouncer.debounceSolution("resize", "100x200", baseTime.plusNanos(500_000_000));

        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abc");

        executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(3));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("resize")).isEqualTo("100x200");
    }

    @Test
    @DisplayName("취소된 이벤트는 실행되지 않음")
    void testCancelledEventsNotExecuted() {
        debouncer.debounceSolution("search", "a", baseTime);
        debouncer.debounceSolution("search", "ab", baseTime.plusNanos(500_000_000));
        debouncer.debounceSolution("search", "abc", baseTime.plusSeconds(1));

        // delay 경과 후 "a", "ab"는 취소되어 실행 안됨
        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).isEmpty();  // "abc"는 아직 delay 경과 안됨

        executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(3));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abc");
    }

    @Test
    @DisplayName("여러 키의 이벤트 동시 실행")
    void testMultipleKeysExecuteSimultaneously() {
        debouncer.debounceSolution("search", "abc", baseTime);
        debouncer.debounceSolution("resize", "100x200", baseTime);
        debouncer.debounceSolution("scroll", "500", baseTime);

        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).hasSize(3);
        assertThat(executable.get("search")).isEqualTo("abc");
        assertThat(executable.get("resize")).isEqualTo("100x200");
        assertThat(executable.get("scroll")).isEqualTo("500");
    }

    @Test
    @DisplayName("연속적인 이벤트 발생 시나리오")
    void testContinuousEventScenario() {
        debouncer.debounceSolution("search", "a", baseTime);                          // t=0
        debouncer.debounceSolution("search", "ab", baseTime.plusNanos(500_000_000));        // t=0.5
        debouncer.debounceSolution("search", "abc", baseTime.plusSeconds(1));        // t=1
        debouncer.debounceSolution("search", "abcd", baseTime.plusNanos(1_500_000_000));     // t=1.5

        // t=2: "a"만 실행 가능하지만 이미 취소됨
        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).isEmpty();

        // t=3: "ab", "abc"도 취소됨
        executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(3));
        assertThat(executable).isEmpty();

        // t=3.5: "abcd" 실행 (t=1.5 + 2초)
        executable = debouncer.getExecutableEventsSolution(baseTime.plusNanos(3_500_000_000L));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abcd");
    }

    @Test
    @DisplayName("clear 메서드")
    void testClear() {
        debouncer.debounceSolution("search", "abc", baseTime);
        debouncer.debounceSolution("resize", "100x200", baseTime);

        debouncer.clear();
        assertThat(debouncer.getPendingEventCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("짧은 delay 테스트")
    void testShortDelay() {
        EventDebouncer shortDebouncer = new EventDebouncer(Duration.ofMillis(100));

        shortDebouncer.debounceSolution("key", "value1", baseTime);
        shortDebouncer.debounceSolution("key", "value2", baseTime.plusNanos(50_000_000));

        Map<String, String> executable = shortDebouncer.getExecutableEventsSolution(baseTime.plusNanos(100_000_000));
        assertThat(executable).isEmpty();

        executable = shortDebouncer.getExecutableEventsSolution(baseTime.plusNanos(150_000_000));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("key")).isEqualTo("value2");
    }

    @Test
    @DisplayName("delay 경계 테스트")
    void testDelayBoundary() {
        debouncer.debounceSolution("search", "abc", baseTime);

        // 정확히 2초 경과
        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("search")).isEqualTo("abc");
    }

    @Test
    @DisplayName("여러 번 실행 가능 조회")
    void testMultipleExecutableEventQueries() {
        debouncer.debounceSolution("key1", "value1", baseTime);
        debouncer.debounceSolution("key2", "value2", baseTime.plusSeconds(1));

        // t=2: key1만 실행
        Map<String, String> executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(2));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("key1")).isEqualTo("value1");

        // t=3: key2 실행
        executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(3));
        assertThat(executable).hasSize(1);
        assertThat(executable.get("key2")).isEqualTo("value2");

        // 더 이상 실행할 이벤트 없음
        executable = debouncer.getExecutableEventsSolution(baseTime.plusSeconds(4));
        assertThat(executable).isEmpty();
    }
}
