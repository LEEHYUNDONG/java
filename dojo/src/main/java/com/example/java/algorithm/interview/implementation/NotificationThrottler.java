package com.example.java.algorithm.interview.implementation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 문제: 알림 발송 제한 시스템 (Notification Throttler)
 *
 * 실무 시나리오:
 * 사용자에게 과도한 알림이 발송되는 것을 방지하는 시스템을 구현하세요.
 * 이메일, SMS, 푸시 알림 등 다양한 채널의 알림을 제한합니다.
 *
 * 요구사항:
 * 1. 채널별 알림 발송 제한
 *    - EMAIL: 1시간에 최대 5개
 *    - SMS: 1시간에 최대 3개
 *    - PUSH: 1시간에 최대 10개
 *
 * 2. 알림 우선순위 처리
 *    - URGENT: 제한 무시하고 항상 발송
 *    - HIGH: 제한의 50% 추가 허용
 *    - NORMAL: 기본 제한 적용
 *    - LOW: 제한의 50%만 허용
 *
 * 3. 사용자별 전역 제한
 *    - 모든 채널 합쳐서 1시간에 최대 15개
 *
 * 4. 중복 알림 방지
 *    - 같은 내용의 알림은 5분 이내 재발송 불가
 *
 * 5. 조용한 시간 (Quiet Hours)
 *    - 오후 10시 ~ 오전 8시는 URGENT만 발송
 *
 * 예시:
 * throttler.canSend("user1", EMAIL, NORMAL, "주문 완료", 10:00);  // true
 * throttler.canSend("user1", EMAIL, NORMAL, "주문 완료", 10:02);  // false (중복)
 * throttler.canSend("user1", EMAIL, NORMAL, "배송 시작", 10:03);  // true
 * // ... 5개 더 발송 ...
 * throttler.canSend("user1", EMAIL, NORMAL, "리뷰 요청", 10:30);  // false (제한 초과)
 * throttler.canSend("user1", EMAIL, URGENT, "환불 완료", 10:31);  // true (긴급)
 */
public class NotificationThrottler {

    public enum Channel {
        EMAIL(5),
        SMS(3),
        PUSH(10);

        private final int hourlyLimit;

        Channel(int hourlyLimit) {
            this.hourlyLimit = hourlyLimit;
        }

        public int getHourlyLimit() {
            return hourlyLimit;
        }
    }

    public enum Priority {
        URGENT(Float.MAX_VALUE),    // 무제한
        HIGH(1.5f),                 // 150%
        NORMAL(1.0f),               // 100%
        LOW(0.5f);                  // 50%

        private final float multiplier;

        Priority(float multiplier) {
            this.multiplier = multiplier;
        }

        public float getMultiplier() {
            return multiplier;
        }
    }

    private static final int GLOBAL_HOURLY_LIMIT = 15;
    private static final Duration NOTIFICATION_WINDOW = Duration.ofHours(1);
    private static final Duration DUPLICATE_PREVENTION_WINDOW = Duration.ofMinutes(5);
    private static final int QUIET_HOUR_START = 22;  // 22시
    private static final int QUIET_HOUR_END = 8;     // 8시

    // 사용자별 알림 이력
    private final Map<String, List<NotificationRecord>> userNotifications = new ConcurrentHashMap<>();

    /**
     * 알림 발송 가능 여부 확인
     *
     * @param userId 사용자 ID
     * @param channel 알림 채널
     * @param priority 우선순위
     * @param content 알림 내용 (중복 체크용)
     * @param sendTime 발송 시간
     * @return true if allowed, false if throttled
     */
    public boolean canSend(String userId, Channel channel, Priority priority,
                          String content, LocalDateTime sendTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. 조용한 시간 확인 (URGENT 아니면 거부)
        // 2. 중복 알림 확인
        // 3. 채널별 제한 확인 (우선순위에 따라 조정)
        // 4. 전역 제한 확인
        // 5. 모두 통과하면 알림 기록 추가
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 알림 발송 기록 (canSend가 true일 때 호출)
     */
    public void recordNotification(String userId, Channel channel, Priority priority,
                                   String content, LocalDateTime sendTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. NotificationRecord 생성
        // 2. userNotifications에 추가
        // 3. 오래된 기록 정리 (NOTIFICATION_WINDOW 이전)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 조용한 시간 확인
     */
    private boolean isQuietHours(LocalDateTime time) {
        int hour = time.getHour();
        return hour >= QUIET_HOUR_START || hour < QUIET_HOUR_END;
    }

    /**
     * 중복 알림 확인
     */
    private boolean isDuplicate(String userId, String content, LocalDateTime sendTime) {
        List<NotificationRecord> records = userNotifications.get(userId);
        if (records == null) return false;

        LocalDateTime windowStart = sendTime.minus(DUPLICATE_PREVENTION_WINDOW);
        for (NotificationRecord record : records) {
            if (record.getSendTime().isAfter(windowStart) &&
                record.getContent().equals(content)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 채널별 제한 확인
     */
    private boolean exceedsChannelLimit(String userId, Channel channel, Priority priority,
                                       LocalDateTime sendTime) {
        List<NotificationRecord> records = userNotifications.get(userId);
        if (records == null) return false;

        LocalDateTime windowStart = sendTime.minus(NOTIFICATION_WINDOW);
        long count = records.stream()
            .filter(r -> r.getSendTime().isAfter(windowStart))
            .filter(r -> r.getChannel() == channel)
            .count();

        int limit = (int) (channel.getHourlyLimit() * priority.getMultiplier());
        return count >= limit;
    }

    /**
     * 전역 제한 확인
     */
    private boolean exceedsGlobalLimit(String userId, LocalDateTime sendTime) {
        List<NotificationRecord> records = userNotifications.get(userId);
        if (records == null) return false;

        LocalDateTime windowStart = sendTime.minus(NOTIFICATION_WINDOW);
        long count = records.stream()
            .filter(r -> r.getSendTime().isAfter(windowStart))
            .count();

        return count >= GLOBAL_HOURLY_LIMIT;
    }

    /**
     * 오래된 기록 정리
     */
    public void cleanup(String userId, LocalDateTime currentTime) {
        List<NotificationRecord> records = userNotifications.get(userId);
        if (records == null) return;

        LocalDateTime threshold = currentTime.minus(NOTIFICATION_WINDOW);
        records.removeIf(r -> r.getSendTime().isBefore(threshold));
    }

    /**
     * 알림 기록
     */
    private static class NotificationRecord {
        private final Channel channel;
        private final Priority priority;
        private final String content;
        private final LocalDateTime sendTime;

        public NotificationRecord(Channel channel, Priority priority,
                                String content, LocalDateTime sendTime) {
            this.channel = channel;
            this.priority = priority;
            this.content = content;
            this.sendTime = sendTime;
        }

        public Channel getChannel() { return channel; }
        public Priority getPriority() { return priority; }
        public String getContent() { return content; }
        public LocalDateTime getSendTime() { return sendTime; }
    }

    // ========== 정답 (참고용) ==========

    public boolean canSendSolution(String userId, Channel channel, Priority priority,
                                   String content, LocalDateTime sendTime) {
        // 1. URGENT가 아니면 조용한 시간 확인
        if (priority != Priority.URGENT && isQuietHours(sendTime)) {
            return false;
        }

        // 2. 중복 알림 확인
        if (isDuplicate(userId, content, sendTime)) {
            return false;
        }

        // 3. URGENT는 모든 제한 통과
        if (priority == Priority.URGENT) {
            return true;
        }

        // 4. 채널별 제한 확인
        if (exceedsChannelLimit(userId, channel, priority, sendTime)) {
            return false;
        }

        // 5. 전역 제한 확인
        if (exceedsGlobalLimit(userId, sendTime)) {
            return false;
        }

        return true;
    }

    public void recordNotificationSolution(String userId, Channel channel, Priority priority,
                                          String content, LocalDateTime sendTime) {
        NotificationRecord record = new NotificationRecord(channel, priority, content, sendTime);
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(record);

        // 오래된 기록 정리
        cleanup(userId, sendTime);
    }
}
