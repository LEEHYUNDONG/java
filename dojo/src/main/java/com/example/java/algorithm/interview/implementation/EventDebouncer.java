package com.example.java.algorithm.interview.implementation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 문제: Event Debouncer (이벤트 디바운싱)
 *
 * 실무 시나리오:
 * 짧은 시간에 반복적으로 발생하는 이벤트를 필터링하는 Debouncer를 구현하세요.
 * 예: 검색창 자동완성, 윈도우 리사이즈 이벤트 처리 등
 *
 * 동작 방식:
 * - 이벤트가 발생하면 일정 시간(delay) 동안 대기
 * - 대기 중 같은 키로 새 이벤트가 오면 이전 이벤트는 취소하고 타이머 리셋
 * - delay 시간 동안 새 이벤트가 없으면 마지막 이벤트만 실행
 *
 * 요구사항:
 * 1. 키별로 독립적인 디바운싱 적용
 * 2. 마지막 이벤트만 실행 (중간 이벤트는 무시)
 * 3. 실행 대상 이벤트는 별도로 추적
 * 4. Thread-safe
 *
 * 예시:
 * EventDebouncer debouncer = new EventDebouncer(Duration.ofSeconds(2));
 *
 * debouncer.debounce("search", "a", t=0s);      // 대기 시작
 * debouncer.debounce("search", "ab", t=0.5s);   // "a" 취소, "ab" 대기
 * debouncer.debounce("search", "abc", t=1s);    // "ab" 취소, "abc" 대기
 * // t=3s 시점에 "abc"만 실행됨 (마지막 이벤트로부터 2초 경과)
 *
 * debouncer.debounce("resize", "100x200", t=0s);  // 독립적으로 동작
 */
public class EventDebouncer {

    private final Duration delay;
    private final Map<String, ScheduledEvent> pendingEvents;

    public EventDebouncer(Duration delay) {
        this.delay = delay;
        this.pendingEvents = new ConcurrentHashMap<>();
    }

    /**
     * 이벤트를 디바운스 처리합니다.
     *
     * @param key 이벤트 키 (같은 키의 이벤트는 디바운싱됨)
     * @param eventData 이벤트 데이터
     * @param eventTime 이벤트 발생 시간
     */
    public void debounce(String key, String eventData, LocalDateTime eventTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. 해당 키의 기존 이벤트가 있으면 취소됨을 표시
        // 2. 새로운 ScheduledEvent를 생성하여 pendingEvents에 저장
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 지정된 시간 기준으로 실행되어야 할 이벤트를 반환합니다.
     * (실제로는 스케줄러가 자동으로 실행하겠지만, 테스트를 위해 수동으로 확인)
     *
     * @param currentTime 현재 시간
     * @return 실행할 이벤트 데이터들 (키별)
     */
    public Map<String, String> getExecutableEvents(LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. pendingEvents를 순회
        // 2. scheduledTime <= currentTime이고 취소되지 않은 이벤트 찾기
        // 3. 실행 가능한 이벤트들을 Map으로 반환
        // 4. 반환한 이벤트는 pendingEvents에서 제거
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 디바운스 예약된 이벤트
     */
    public static class ScheduledEvent {
        private final String eventData;
        private final LocalDateTime scheduledTime;
        private volatile boolean cancelled;

        public ScheduledEvent(String eventData, LocalDateTime scheduledTime) {
            this.eventData = eventData;
            this.scheduledTime = scheduledTime;
            this.cancelled = false;
        }

        public String getEventData() {
            return eventData;
        }

        public LocalDateTime getScheduledTime() {
            return scheduledTime;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            this.cancelled = true;
        }
    }

    /**
     * 테스트용: 대기 중인 이벤트 수
     */
    public int getPendingEventCount() {
        return pendingEvents.size();
    }

    /**
     * 테스트용: 모든 이벤트 초기화
     */
    public void clear() {
        pendingEvents.clear();
    }

    // ========== 정답 (참고용) ==========

    public void debounceSolution(String key, String eventData, LocalDateTime eventTime) {
        // 기존 이벤트 취소
        ScheduledEvent oldEvent = pendingEvents.get(key);
        if (oldEvent != null) {
            oldEvent.cancel();
        }

        // 새 이벤트 등록
        LocalDateTime scheduledTime = eventTime.plus(delay);
        ScheduledEvent newEvent = new ScheduledEvent(eventData, scheduledTime);
        pendingEvents.put(key, newEvent);
    }

    public Map<String, String> getExecutableEventsSolution(LocalDateTime currentTime) {
        Map<String, String> executableEvents = new ConcurrentHashMap<>();

        pendingEvents.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            ScheduledEvent event = entry.getValue();

            // 취소된 이벤트는 제거
            if (event.isCancelled()) {
                return true;
            }

            // 실행 시간이 된 이벤트
            if (!event.getScheduledTime().isAfter(currentTime)) {
                executableEvents.put(key, event.getEventData());
                return true;
            }

            return false;
        });

        return executableEvents;
    }
}
