package app.session;

import app.model.UserSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionServiceTest {
    @Test
    void allowsUsernameToBeReusedAfterDisconnect() {
        SessionService sessionService = new SessionService();

        UserSession firstSession = sessionService.createSession("Budi");
        sessionService.connect(firstSession.sessionId());
        sessionService.disconnect(firstSession.sessionId());

        UserSession secondSession = sessionService.createSession("Budi");

        assertEquals("Budi", secondSession.username());
        assertNotEquals(firstSession.sessionId(), secondSession.sessionId());
    }

    @Test
    void rejectsUsernameReuseWhileSessionIsStillActive() {
        SessionService sessionService = new SessionService();

        UserSession firstSession = sessionService.createSession("Siti");
        sessionService.connect(firstSession.sessionId());

        SessionService.ActiveSessionConflictException exception =
                assertThrows(SessionService.ActiveSessionConflictException.class, () -> sessionService.createSession("Siti"));
        assertEquals("Nama pengguna sudah aktif di perangkat lain.", exception.getMessage());
        assertEquals(firstSession.sessionId(), exception.activeSessionId());
    }

    @Test
    void allowsTakeoverWhenUsernameIsStillActive() {
        SessionService sessionService = new SessionService();

        UserSession firstSession = sessionService.createSession("Dina");
        sessionService.connect(firstSession.sessionId());

        SessionService.SessionCreationResult result = sessionService.createSession("Dina", true);

        assertEquals(firstSession.sessionId(), result.replacedSessionId());
        assertEquals("Dina", result.session().username());
        assertNull(result.session().connectedAt());
    }
}
