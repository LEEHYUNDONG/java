package com.example.java.algorithm.interview.implementation;

import com.example.java.algorithm.interview.implementation.SessionManager.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Session Manager 테스트")
class SessionManagerTest {

    private SessionManager sessionManager;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(3); // 최대 3개 세션
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }

    @Test
    @DisplayName("세션 생성")
    void testCreateSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        assertThat(sessionId).isNotNull();
        assertThat(sessionManager.getActiveSessionCount("user1")).isEqualTo(1);
    }

    @Test
    @DisplayName("세션 검증 - 활성 세션")
    void testValidateActiveSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(10));
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("세션 검증 - 타임아웃")
    void testSessionTimeout() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        // 30분 초과 시 타임아웃
        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(31));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("세션 갱신")
    void testRefreshSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        // 25분 후 갱신
        boolean refreshed = sessionManager.refreshSessionSolution(sessionId, baseTime.plusMinutes(25));
        assertThat(refreshed).isTrue();

        // 갱신 후 50분 시점 (갱신으로부터 25분)에는 여전히 유효
        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(50));
        assertThat(valid).isTrue();

        // 갱신으로부터 31분 후에는 타임아웃
        valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(56));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("절대 만료 시간 확인")
    void testAbsoluteTimeout() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        // 계속 갱신하더라도 24시간 후에는 만료
        for (int i = 1; i <= 47; i++) {
            sessionManager.refreshSessionSolution(sessionId, baseTime.plusMinutes(i * 30));
        }

        // 24시간 경과
        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusHours(24));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("세션 종료")
    void testTerminateSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        boolean terminated = sessionManager.terminateSessionSolution(sessionId);
        assertThat(terminated).isTrue();
        assertThat(sessionManager.getActiveSessionCount("user1")).isEqualTo(0);

        // 종료된 세션은 유효하지 않음
        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(1));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("최대 세션 수 제한")
    void testMaxSessionsPerUser() {
        sessionManager.createSessionSolution("user1", baseTime);
        sessionManager.createSessionSolution("user1", baseTime.plusSeconds(1));
        sessionManager.createSessionSolution("user1", baseTime.plusSeconds(2));

        assertThatThrownBy(() -> sessionManager.createSessionSolution("user1", baseTime.plusSeconds(3)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Maximum sessions per user exceeded");
    }

    @Test
    @DisplayName("만료된 세션 정리")
    void testCleanupExpiredSessions() {
        String session1 = sessionManager.createSessionSolution("user1", baseTime);
        String session2 = sessionManager.createSessionSolution("user1", baseTime.plusMinutes(10));
        String session3 = sessionManager.createSessionSolution("user2", baseTime);

        // 31분 후 정리 (session1과 session3만 타임아웃, session2는 21분 경과)
        int cleaned = sessionManager.cleanupExpiredSessionsSolution(baseTime.plusMinutes(31));
        assertThat(cleaned).isEqualTo(2);
        assertThat(sessionManager.getActiveSessionCount("user1")).isEqualTo(1);
        assertThat(sessionManager.getActiveSessionCount("user2")).isEqualTo(0);
    }

    @Test
    @DisplayName("종료된 세션 재검증 불가")
    void testCannotValidateTerminatedSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);
        sessionManager.terminateSessionSolution(sessionId);

        boolean valid = sessionManager.validateSessionSolution(sessionId, baseTime.plusMinutes(1));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("만료된 세션 갱신 불가")
    void testCannotRefreshExpiredSession() {
        String sessionId = sessionManager.createSessionSolution("user1", baseTime);

        boolean refreshed = sessionManager.refreshSessionSolution(sessionId, baseTime.plusMinutes(31));
        assertThat(refreshed).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 세션 검증")
    void testValidateNonExistentSession() {
        boolean valid = sessionManager.validateSessionSolution("non-existent-id", baseTime);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 세션 종료")
    void testTerminateNonExistentSession() {
        boolean terminated = sessionManager.terminateSessionSolution("non-existent-id");
        assertThat(terminated).isFalse();
    }

    @Test
    @DisplayName("사용자별 독립적인 세션 관리")
    void testIndependentUserSessions() {
        sessionManager.createSessionSolution("user1", baseTime);
        sessionManager.createSessionSolution("user1", baseTime.plusSeconds(1));
        sessionManager.createSessionSolution("user2", baseTime);

        assertThat(sessionManager.getActiveSessionCount("user1")).isEqualTo(2);
        assertThat(sessionManager.getActiveSessionCount("user2")).isEqualTo(1);
    }

    @Test
    @DisplayName("세션 종료 후 새 세션 생성 가능")
    void testCreateNewSessionAfterTermination() {
        String session1 = sessionManager.createSessionSolution("user1", baseTime);
        sessionManager.createSessionSolution("user1", baseTime.plusSeconds(1));
        sessionManager.createSessionSolution("user1", baseTime.plusSeconds(2));

        // 최대 세션 수 도달
        assertThatThrownBy(() -> sessionManager.createSessionSolution("user1", baseTime.plusSeconds(3)))
            .isInstanceOf(IllegalStateException.class);

        // 하나 종료 후 새로 생성 가능
        sessionManager.terminateSessionSolution(session1);
        String newSession = sessionManager.createSessionSolution("user1", baseTime.plusSeconds(4));
        assertThat(newSession).isNotNull();
        assertThat(sessionManager.getActiveSessionCount("user1")).isEqualTo(3);
    }
}
