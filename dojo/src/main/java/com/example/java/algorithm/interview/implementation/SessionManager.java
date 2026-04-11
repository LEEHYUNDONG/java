package com.example.java.algorithm.interview.implementation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 문제: 사용자 세션 관리 시스템
 *
 * 실무 시나리오:
 * 웹 애플리케이션의 사용자 세션을 관리하는 시스템을 구현하세요.
 * 세션 타임아웃, 갱신, 로그아웃 등의 기능이 필요합니다.
 *
 * 요구사항:
 * 1. 세션 생성 (createSession) - 로그인 시
 * 2. 세션 검증 (validateSession) - 요청마다 세션 유효성 확인
 * 3. 세션 갱신 (refreshSession) - 활동 시 자동 갱신
 * 4. 세션 종료 (terminateSession) - 로그아웃 시
 * 5. 만료된 세션 정리 (cleanupExpiredSessions)
 *
 * 비즈니스 규칙:
 * - 세션 타임아웃: 마지막 활동으로부터 30분
 * - 절대 만료 시간: 세션 생성 후 24시간 (갱신해도 연장 불가)
 * - 동시 로그인 제한: 사용자당 최대 N개의 활성 세션
 * - 세션 ID는 UUID 기반으로 생성
 *
 * 세션 상태:
 * - ACTIVE: 활성 세션
 * - EXPIRED: 타임아웃으로 만료
 * - TERMINATED: 명시적 로그아웃
 *
 * 예시:
 * String sessionId = manager.createSession("user123", now);
 * manager.validateSession(sessionId, now + 10min);  // true (활성)
 * manager.refreshSession(sessionId, now + 25min);   // 세션 갱신
 * manager.validateSession(sessionId, now + 50min);  // true (갱신됨)
 * manager.validateSession(sessionId, now + 90min);  // false (타임아웃)
 */
public class SessionManager {

    public enum SessionStatus {
        ACTIVE,
        EXPIRED,
        TERMINATED
    }

    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration ABSOLUTE_TIMEOUT = Duration.ofHours(24);

    private final int maxSessionsPerUser;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> userSessionCounts = new ConcurrentHashMap<>();

    public SessionManager(int maxSessionsPerUser) {
        this.maxSessionsPerUser = maxSessionsPerUser;
    }

    /**
     * 세션 생성
     *
     * @param userId 사용자 ID
     * @param currentTime 현재 시간
     * @return 생성된 세션 ID
     * @throws IllegalStateException 최대 세션 수 초과 시
     */
    public String createSession(String userId, LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. 사용자의 현재 세션 수 확인
        // 2. maxSessionsPerUser 초과하면 예외 발생
        // 3. 세션 ID 생성 (UUID 사용)
        // 4. Session 객체 생성
        // 5. sessions에 저장
        // 6. userSessionCounts 업데이트
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 세션 유효성 검증
     *
     * @param sessionId 세션 ID
     * @param currentTime 현재 시간
     * @return true if valid, false if invalid/expired
     */
    public boolean validateSession(String sessionId, LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. sessions에서 세션 조회
        // 2. 세션이 없거나 TERMINATED면 false
        // 3. 절대 만료 시간 확인
        // 4. 상대 만료 시간 확인 (lastActivityTime + SESSION_TIMEOUT)
        // 5. 만료되었으면 상태를 EXPIRED로 변경하고 false
        // 6. 유효하면 true
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 세션 갱신 (활동 시간 업데이트)
     *
     * @param sessionId 세션 ID
     * @param currentTime 현재 시간
     * @return true if refreshed, false if session not found or expired
     */
    public boolean refreshSession(String sessionId, LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. validateSession으로 세션 유효성 확인
        // 2. 유효하면 lastActivityTime 업데이트
        // 3. 유효하지 않으면 false
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 세션 종료 (로그아웃)
     *
     * @param sessionId 세션 ID
     * @return true if terminated, false if session not found
     */
    public boolean terminateSession(String sessionId) {
        // TODO: 구현하세요
        // 힌트:
        // 1. sessions에서 세션 조회
        // 2. 세션이 있으면 상태를 TERMINATED로 변경
        // 3. userSessionCounts 감소
        // 4. sessions에서 제거
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 만료된 세션들을 정리합니다.
     *
     * @param currentTime 현재 시간
     * @return 정리된 세션 수
     */
    public int cleanupExpiredSessions(LocalDateTime currentTime) {
        // TODO: 구현하세요
        // 힌트:
        // 1. sessions를 순회하며 만료된 세션 찾기
        // 2. 만료된 세션은 제거하고 카운트
        // 3. userSessionCounts 업데이트
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 사용자의 활성 세션 수 조회
     */
    public int getActiveSessionCount(String userId) {
        return userSessionCounts.getOrDefault(userId, 0);
    }

    /**
     * 세션 정보
     */
    public static class Session {
        private final String sessionId;
        private final String userId;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime lastActivityTime;
        private volatile SessionStatus status;

        public Session(String sessionId, String userId, LocalDateTime createdAt) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.createdAt = createdAt;
            this.lastActivityTime = createdAt;
            this.status = SessionStatus.ACTIVE;
        }

        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastActivityTime() { return lastActivityTime; }
        public SessionStatus getStatus() { return status; }

        public void setLastActivityTime(LocalDateTime time) {
            this.lastActivityTime = time;
        }

        public void setStatus(SessionStatus status) {
            this.status = status;
        }
    }

    // ========== 정답 (참고용) ==========

    public String createSessionSolution(String userId, LocalDateTime currentTime) {
        int currentCount = userSessionCounts.getOrDefault(userId, 0);
        if (currentCount >= maxSessionsPerUser) {
            throw new IllegalStateException(
                "Maximum sessions per user exceeded: " + maxSessionsPerUser
            );
        }

        String sessionId = java.util.UUID.randomUUID().toString();
        Session session = new Session(sessionId, userId, currentTime);
        sessions.put(sessionId, session);
        userSessionCounts.put(userId, currentCount + 1);

        return sessionId;
    }

    public boolean validateSessionSolution(String sessionId, LocalDateTime currentTime) {
        Session session = sessions.get(sessionId);
        if (session == null || session.getStatus() == SessionStatus.TERMINATED) {
            return false;
        }

        // 절대 만료 시간 확인
        if (Duration.between(session.getCreatedAt(), currentTime)
                .compareTo(ABSOLUTE_TIMEOUT) >= 0) {
            session.setStatus(SessionStatus.EXPIRED);
            return false;
        }

        // 상대 만료 시간 확인
        if (Duration.between(session.getLastActivityTime(), currentTime)
                .compareTo(SESSION_TIMEOUT) >= 0) {
            session.setStatus(SessionStatus.EXPIRED);
            return false;
        }

        return true;
    }

    public boolean refreshSessionSolution(String sessionId, LocalDateTime currentTime) {
        if (!validateSessionSolution(sessionId, currentTime)) {
            return false;
        }

        Session session = sessions.get(sessionId);
        if (session != null) {
            session.setLastActivityTime(currentTime);
            return true;
        }
        return false;
    }

    public boolean terminateSessionSolution(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            session.setStatus(SessionStatus.TERMINATED);
            userSessionCounts.merge(session.getUserId(), -1, (old, val) -> old + val);
            return true;
        }
        return false;
    }

    public int cleanupExpiredSessionsSolution(LocalDateTime currentTime) {
        int[] count = {0};
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (!validateSessionSolution(session.getSessionId(), currentTime)) {
                userSessionCounts.merge(session.getUserId(), -1, (old, val) -> old + val);
                count[0]++;
                return true;
            }
            return false;
        });
        return count[0];
    }
}
