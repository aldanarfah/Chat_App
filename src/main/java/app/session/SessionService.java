package app.session;

import app.model.UserSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SessionService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, UserSession> sessionsById = new LinkedHashMap<>();
    private final Map<String, String> sessionIdsByUsername = new LinkedHashMap<>();

    public synchronized UserSession createSession(String username) {
        return createSession(username, false).session();
    }

    public synchronized SessionCreationResult createSession(String username, boolean forceTakeover) {
        String normalizedUsername = requireUsername(username);
        String usernameKey = normalizedUsername.toLowerCase(Locale.ROOT);
        String existingSessionId = sessionIdsByUsername.get(usernameKey);
        String replacedSessionId = null;
        if (existingSessionId != null) {
            UserSession existingSession = sessionsById.get(existingSessionId);
            if (existingSession != null && existingSession.isActive()) {
                if (!forceTakeover) {
                    throw new ActiveSessionConflictException(existingSession.username(), existingSession.sessionId());
                }
                replacedSessionId = existingSession.sessionId();
            }

            sessionsById.remove(existingSessionId);
            sessionIdsByUsername.remove(usernameKey);
        }

        UserSession session = new UserSession(nextSessionId(), normalizedUsername, now(), null);
        sessionsById.put(session.sessionId(), session);
        sessionIdsByUsername.put(usernameKey, session.sessionId());
        return new SessionCreationResult(session, replacedSessionId);
    }

    public synchronized SessionConnectResult connect(String sessionId) {
        UserSession existing = sessionsById.get(sessionId);
        if (existing == null) {
            return SessionConnectResult.failure("Sesi tidak ditemukan. Buat sesi baru terlebih dahulu.");
        }
        if (existing.isActive()) {
            return SessionConnectResult.failure("Sesi sedang aktif di koneksi lain.");
        }

        UserSession connected = new UserSession(
                existing.sessionId(),
                existing.username(),
                existing.createdAt(),
                now()
        );
        sessionsById.put(sessionId, connected);
        return SessionConnectResult.success(connected);
    }

    public synchronized Optional<UserSession> findSession(String sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    public synchronized Optional<UserSession> disconnect(String sessionId) {
        UserSession existing = sessionsById.get(sessionId);
        if (existing == null) {
            return Optional.empty();
        }

        UserSession disconnected = new UserSession(
                existing.sessionId(),
                existing.username(),
                existing.createdAt(),
                null
        );
        sessionsById.put(sessionId, disconnected);
        return Optional.of(disconnected);
    }

    public synchronized List<UserSession> listActiveSessions() {
        List<UserSession> activeSessions = new ArrayList<>();
        for (UserSession session : sessionsById.values()) {
            if (session.isActive()) {
                activeSessions.add(session);
            }
        }
        activeSessions.sort(Comparator.comparing(UserSession::username, String.CASE_INSENSITIVE_ORDER));
        return activeSessions;
    }

    public synchronized Optional<UserSession> requireActiveSession(String sessionId) {
        UserSession session = sessionsById.get(sessionId);
        if (session == null || !session.isActive()) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public synchronized Optional<UserSession> findLatestSessionByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String sessionId = sessionIdsByUsername.get(username.trim().toLowerCase(Locale.ROOT));
        if (sessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    private String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Nama pengguna wajib diisi.");
        }

        String trimmed = username.trim();
        if (trimmed.length() > 32) {
            throw new IllegalArgumentException("Nama pengguna maksimal 32 karakter.");
        }

        return trimmed;
    }

    private String nextSessionId() {
        return "ses_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    public record SessionConnectResult(UserSession session, String errorMessage) {
        public static SessionConnectResult success(UserSession session) {
            return new SessionConnectResult(session, null);
        }

        public static SessionConnectResult failure(String errorMessage) {
            return new SessionConnectResult(null, errorMessage);
        }

        public boolean isSuccess() {
            return session != null;
        }
    }

    public record SessionCreationResult(UserSession session, String replacedSessionId) {
    }

    public static final class ActiveSessionConflictException extends IllegalStateException {
        private final String username;
        private final String activeSessionId;

        public ActiveSessionConflictException(String username, String activeSessionId) {
            super("Nama pengguna sudah aktif di perangkat lain.");
            this.username = username;
            this.activeSessionId = activeSessionId;
        }

        public String username() {
            return username;
        }

        public String activeSessionId() {
            return activeSessionId;
        }
    }
}
