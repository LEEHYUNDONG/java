package com.example.java.algorithm.interview.implementation;

import com.example.java.algorithm.interview.implementation.NotificationThrottler.Channel;
import com.example.java.algorithm.interview.implementation.NotificationThrottler.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification Throttler 테스트")
class NotificationThrottlerTest {

    private NotificationThrottler throttler;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        throttler = new NotificationThrottler();
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }

    @Test
    @DisplayName("정상적인 알림 발송 허용")
    void testAllowNormalNotification() {
        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime);
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("중복 알림 차단 (5분 이내)")
    void testBlockDuplicateNotification() {
        throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime);
        throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime);

        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime.plusMinutes(2));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("중복 알림 5분 경과 후 허용")
    void testAllowDuplicateAfter5Minutes() {
        throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime);
        throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime);

        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "주문 완료", baseTime.plusMinutes(6));
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("채널별 제한 확인 - EMAIL (시간당 5개)")
    void testChannelLimitForEmail() {
        for (int i = 0; i < 5; i++) {
            boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            assertThat(allowed).isTrue();
        }

        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지6", baseTime.plusMinutes(30));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("채널별 제한 확인 - SMS (시간당 3개)")
    void testChannelLimitForSMS() {
        for (int i = 0; i < 3; i++) {
            boolean allowed = throttler.canSendSolution("user1", Channel.SMS, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.SMS, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            assertThat(allowed).isTrue();
        }

        boolean allowed = throttler.canSendSolution("user1", Channel.SMS, Priority.NORMAL, "메시지4", baseTime.plusMinutes(30));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("우선순위 HIGH - 150% 제한")
    void testHighPriorityLimit() {
        // EMAIL NORMAL: 5개, HIGH: 7개 (5 * 1.5 = 7.5 -> 7)
        for (int i = 0; i < 7; i++) {
            boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.HIGH, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.HIGH, "메시지" + i, baseTime.plusMinutes(i));
            assertThat(allowed).isTrue();
        }

        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.HIGH, "메시지8", baseTime.plusMinutes(30));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("우선순위 LOW - 50% 제한")
    void testLowPriorityLimit() {
        // EMAIL NORMAL: 5개, LOW: 2개 (5 * 0.5 = 2.5 -> 2)
        for (int i = 0; i < 2; i++) {
            boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.LOW, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.LOW, "메시지" + i, baseTime.plusMinutes(i));
            assertThat(allowed).isTrue();
        }

        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.LOW, "메시지3", baseTime.plusMinutes(30));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("우선순위 URGENT - 제한 무시")
    void testUrgentPriorityNoLimit() {
        // 채널별 제한을 초과해도 URGENT는 항상 허용
        for (int i = 0; i < 10; i++) {
            boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.URGENT, "긴급" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.URGENT, "긴급" + i, baseTime.plusMinutes(i));
            assertThat(allowed).isTrue();
        }
    }

    @Test
    @DisplayName("전역 제한 확인 (시간당 15개)")
    void testGlobalLimit() {
        for (int i = 0; i < 5; i++) {
            throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "이메일" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "이메일" + i, baseTime.plusMinutes(i));
        }
        for (int i = 0; i < 10; i++) {
            throttler.canSendSolution("user1", Channel.PUSH, Priority.NORMAL, "푸시" + i, baseTime.plusMinutes(10 + i));
            throttler.recordNotificationSolution("user1", Channel.PUSH, Priority.NORMAL, "푸시" + i, baseTime.plusMinutes(10 + i));
        }

        // 15개 도달, 추가 발송 불가
        boolean allowed = throttler.canSendSolution("user1", Channel.SMS, Priority.NORMAL, "SMS", baseTime.plusMinutes(30));
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("조용한 시간 확인 (22시 ~ 8시)")
    void testQuietHours() {
        LocalDateTime nightTime = LocalDateTime.of(2024, 1, 1, 23, 0);
        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "야간 메시지", nightTime);
        assertThat(allowed).isFalse();

        LocalDateTime earlyMorning = LocalDateTime.of(2024, 1, 1, 7, 0);
        allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "새벽 메시지", earlyMorning);
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("조용한 시간에도 URGENT는 발송 가능")
    void testUrgentInQuietHours() {
        LocalDateTime nightTime = LocalDateTime.of(2024, 1, 1, 23, 0);
        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.URGENT, "긴급 메시지", nightTime);
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("사용자별 독립적인 제한")
    void testIndependentUserLimits() {
        for (int i = 0; i < 5; i++) {
            throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
        }

        // user2는 독립적으로 동작
        boolean allowed = throttler.canSendSolution("user2", Channel.EMAIL, Priority.NORMAL, "메시지", baseTime);
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("시간 경과 후 제한 초기화")
    void testLimitResetAfterTimeWindow() {
        for (int i = 0; i < 5; i++) {
            throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
            throttler.recordNotificationSolution("user1", Channel.EMAIL, Priority.NORMAL, "메시지" + i, baseTime.plusMinutes(i));
        }

        // 1시간 후에는 다시 발송 가능
        boolean allowed = throttler.canSendSolution("user1", Channel.EMAIL, Priority.NORMAL, "새 메시지", baseTime.plusMinutes(70));
        assertThat(allowed).isTrue();
    }
}
