package app.websocket;

import app.chat.ChatService;
import app.file.LocalFileStorageService;
import app.model.StoredFile;
import app.model.ChatRoom;
import app.model.UserSession;
import app.session.SessionService;
import app.util.Json;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatWebSocketServerTest {
    private ChatWebSocketServer server;
    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (server != null) {
            server.stop(1000);
        }
    }

    @Test
    void routesRoomMessagesOnlyToRoomMembers() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");
        UserSession joko = sessionService.createSession("Joko");
        ChatRoom belajarRoom = chatService.createRoom("Belajar");
        ChatRoom santaiRoom = chatService.createRoom("Santai");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());
        TestClient jokoClient = connectClient(joko.sessionId());

        budiClient.send(event("room.join", Json.object("roomId", belajarRoom.roomId())));
        sitiClient.send(event("room.join", Json.object("roomId", belajarRoom.roomId())));
        jokoClient.send(event("room.join", Json.object("roomId", santaiRoom.roomId())));

        assertTrue(budiClient.awaitEvent("room.joined", payload -> true, Duration.ofSeconds(5)).isPresent());
        assertTrue(sitiClient.awaitEvent("room.joined", payload -> true, Duration.ofSeconds(5)).isPresent());
        assertTrue(jokoClient.awaitEvent("room.joined", payload -> true, Duration.ofSeconds(5)).isPresent());

        budiClient.send(event("room.message.send", Json.object(
                "conversationId", belajarRoom.roomId(),
                "text", "Halo tim belajar"
        )));

        assertTrue(budiClient.awaitEvent(
                "room.message.receive",
                payload -> "Halo tim belajar".equals(payload.get("text")),
                Duration.ofSeconds(5)
        ).isPresent());
        assertTrue(sitiClient.awaitEvent(
                "room.message.receive",
                payload -> "Halo tim belajar".equals(payload.get("text")),
                Duration.ofSeconds(5)
        ).isPresent());
        assertFalse(jokoClient.awaitEvent(
                "room.message.receive",
                payload -> "Halo tim belajar".equals(payload.get("text")),
                Duration.ofSeconds(1)
        ).isPresent());

        budiClient.closeBlocking();
        sitiClient.closeBlocking();
        jokoClient.closeBlocking();
    }

    @Test
    void routesDirectMessagesOnlyToSenderAndTarget() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");
        UserSession joko = sessionService.createSession("Joko");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());
        TestClient jokoClient = connectClient(joko.sessionId());

        budiClient.send(event("dm.message.send", Json.object(
                "targetSessionId", siti.sessionId(),
                "text", "Halo Siti"
        )));

        assertTrue(budiClient.awaitEvent(
                "dm.message.receive",
                payload -> "Halo Siti".equals(payload.get("text"))
                        && siti.sessionId().equals(payload.get("targetSessionId")),
                Duration.ofSeconds(5)
        ).isPresent());
        assertTrue(sitiClient.awaitEvent(
                "dm.message.receive",
                payload -> "Halo Siti".equals(payload.get("text"))
                        && siti.sessionId().equals(payload.get("targetSessionId")),
                Duration.ofSeconds(5)
        ).isPresent());
        assertFalse(jokoClient.awaitEvent(
                "dm.message.receive",
                payload -> "Halo Siti".equals(payload.get("text")),
                Duration.ofSeconds(1)
        ).isPresent());

        budiClient.closeBlocking();
        sitiClient.closeBlocking();
        jokoClient.closeBlocking();
    }

    @Test
    void returnsErrorWhenRoomMessageIsSentWithoutMembership() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        budiClient.send(event("room.message.send", Json.object(
                "conversationId", ChatService.GENERAL_ROOM_ID,
                "text", "Belum join nih"
        )));

        assertTrue(budiClient.awaitEvent(
                "error",
                payload -> "bad_request".equals(payload.get("code"))
                        && String.valueOf(payload.get("message")).contains("belum bergabung"),
                Duration.ofSeconds(5)
        ).isPresent());

        budiClient.closeBlocking();
    }

    @Test
    void disconnectSessionSendsTakeoverErrorToOldClient() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        server.disconnectSession(budi.sessionId(), "Sesi diambil alih.");

        assertTrue(budiClient.awaitEvent(
                "error",
                payload -> "session_taken_over".equals(payload.get("code"))
                        && String.valueOf(payload.get("message")).contains("diambil alih"),
                Duration.ofSeconds(5)
        ).isPresent());

        budiClient.closeBlocking();
    }

    @Test
    void restoresDirectHistoryInSessionReadyAfterUserRejoinsWithSameName() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());

        budiClient.send(event("dm.message.send", Json.object(
                "targetSessionId", siti.sessionId(),
                "text", "Halo lama"
        )));

        assertTrue(budiClient.awaitEvent(
                "dm.message.receive",
                payload -> "Halo lama".equals(payload.get("text")),
                Duration.ofSeconds(5)
        ).isPresent());

        budiClient.closeBlocking();

        UserSession budiBaru = sessionService.createSession("Budi");
        TestClient budiBaruClient = new TestClient(new URI("ws://localhost:" + server.getPort() + "/ws?sessionId=" + budiBaru.sessionId()));
        budiBaruClient.connectBlocking(5, TimeUnit.SECONDS);

        assertTrue(budiBaruClient.awaitEvent(
                "session.ready",
                payload -> hasDirectHistory(payload, "Halo lama", "Siti"),
                Duration.ofSeconds(5)
        ).isPresent());

        budiBaruClient.closeBlocking();
        sitiClient.closeBlocking();
    }

    @Test
    void broadcastsNewRoomsToConnectedUsers() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());

        ChatRoom roomBaru = chatService.createRoom("Diskusi");
        server.broadcastRoomCreated(roomBaru);

        assertTrue(budiClient.awaitEvent(
                "room.created",
                payload -> hasRoom(payload, roomBaru.roomId(), "Diskusi"),
                Duration.ofSeconds(5)
        ).isPresent());
        assertTrue(sitiClient.awaitEvent(
                "room.created",
                payload -> hasRoom(payload, roomBaru.roomId(), "Diskusi"),
                Duration.ofSeconds(5)
        ).isPresent());

        budiClient.closeBlocking();
        sitiClient.closeBlocking();
    }

    @Test
    void routesRoomFileMessagesToRoomMembers() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");
        ChatRoom belajarRoom = chatService.createRoom("Belajar");
        StoredFile file = fileStorageService.store(
                "ringkasan.txt",
                "text/plain",
                "Halo file".getBytes(StandardCharsets.UTF_8)
        );

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());

        budiClient.send(event("room.join", Json.object("roomId", belajarRoom.roomId())));
        sitiClient.send(event("room.join", Json.object("roomId", belajarRoom.roomId())));

        assertTrue(budiClient.awaitEvent("room.joined", payload -> true, Duration.ofSeconds(5)).isPresent());
        assertTrue(sitiClient.awaitEvent("room.joined", payload -> true, Duration.ofSeconds(5)).isPresent());

        budiClient.send(event("room.message.send", Json.object(
                "conversationId", belajarRoom.roomId(),
                "text", "Ini file belajar",
                "fileId", file.fileId()
        )));

        assertTrue(budiClient.awaitEvent(
                "room.message.receive",
                payload -> file.fileId().equals(payload.get("fileId"))
                        && "Ini file belajar".equals(payload.get("text"))
                        && hasFileMetadata(payload, file.fileId(), "ringkasan.txt"),
                Duration.ofSeconds(5)
        ).isPresent());
        assertTrue(sitiClient.awaitEvent(
                "room.message.receive",
                payload -> file.fileId().equals(payload.get("fileId"))
                        && hasFileMetadata(payload, file.fileId(), "ringkasan.txt"),
                Duration.ofSeconds(5)
        ).isPresent());

        budiClient.closeBlocking();
        sitiClient.closeBlocking();
    }

    @Test
    void routesDirectFileMessagesOnlyToSenderAndTarget() throws Exception {
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        UserSession budi = sessionService.createSession("Budi");
        UserSession siti = sessionService.createSession("Siti");
        UserSession joko = sessionService.createSession("Joko");
        StoredFile file = fileStorageService.store(
                "dokumen.pdf",
                "application/pdf",
                "pdf-data".getBytes(StandardCharsets.UTF_8)
        );

        startServer(sessionService, chatService, fileStorageService);

        TestClient budiClient = connectClient(budi.sessionId());
        TestClient sitiClient = connectClient(siti.sessionId());
        TestClient jokoClient = connectClient(joko.sessionId());

        budiClient.send(event("dm.message.send", Json.object(
                "targetSessionId", siti.sessionId(),
                "fileId", file.fileId()
        )));

        assertTrue(budiClient.awaitEvent(
                "dm.message.receive",
                payload -> siti.sessionId().equals(payload.get("targetSessionId"))
                        && file.fileId().equals(payload.get("fileId"))
                        && hasFileMetadata(payload, file.fileId(), "dokumen.pdf"),
                Duration.ofSeconds(5)
        ).isPresent());
        assertTrue(sitiClient.awaitEvent(
                "dm.message.receive",
                payload -> siti.sessionId().equals(payload.get("targetSessionId"))
                        && file.fileId().equals(payload.get("fileId"))
                        && hasFileMetadata(payload, file.fileId(), "dokumen.pdf"),
                Duration.ofSeconds(5)
        ).isPresent());
        assertFalse(jokoClient.awaitEvent(
                "dm.message.receive",
                payload -> file.fileId().equals(payload.get("fileId")),
                Duration.ofSeconds(1)
        ).isPresent());

        budiClient.closeBlocking();
        sitiClient.closeBlocking();
        jokoClient.closeBlocking();
    }

    private void startServer(
            SessionService sessionService,
            ChatService chatService,
            LocalFileStorageService fileStorageService
    ) throws Exception {
        int port = reservePort();
        server = new ChatWebSocketServer(new InetSocketAddress(port), sessionService, chatService, fileStorageService);
        server.start();
        assertTrue(server.awaitStarted(5, TimeUnit.SECONDS));
    }

    private TestClient connectClient(String sessionId) throws Exception {
        TestClient client = new TestClient(new URI("ws://localhost:" + server.getPort() + "/ws?sessionId=" + sessionId));
        client.connectBlocking(5, TimeUnit.SECONDS);
        assertTrue(client.awaitEvent("session.ready", payload -> true, Duration.ofSeconds(5)).isPresent());
        return client;
    }

    private int reservePort() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    private String event(String type, Map<String, Object> payload) {
        return Json.stringify(Json.object("type", type, "payload", payload));
    }

    @SuppressWarnings("unchecked")
    private boolean hasFileMetadata(Map<String, Object> payload, String fileId, String filename) {
        Object fileValue = payload.get("file");
        if (!(fileValue instanceof Map<?, ?> fileMap)) {
            return false;
        }
        return fileId.equals(fileMap.get("fileId")) && filename.equals(fileMap.get("filename"));
    }

    @SuppressWarnings("unchecked")
    private boolean hasDirectHistory(Map<String, Object> payload, String text, String targetName) {
        Object directConversationsValue = payload.get("directConversations");
        if (!(directConversationsValue instanceof Iterable<?> directConversations)) {
            return false;
        }

        for (Object conversationValue : directConversations) {
            if (!(conversationValue instanceof Map<?, ?> conversationMap)) {
                continue;
            }
            if (!targetName.equals(conversationMap.get("targetName"))) {
                continue;
            }
            Object messagesValue = conversationMap.get("messages");
            if (!(messagesValue instanceof Iterable<?> messages)) {
                continue;
            }
            for (Object messageValue : messages) {
                if (messageValue instanceof Map<?, ?> messageMap && text.equals(messageMap.get("text"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean hasRoom(Map<String, Object> payload, String roomId, String roomName) {
        Object roomValue = payload.get("room");
        if (!(roomValue instanceof Map<?, ?> roomMap)) {
            return false;
        }
        return roomId.equals(roomMap.get("roomId")) && roomName.equals(roomMap.get("name"));
    }

    private static final class TestClient extends WebSocketClient {
        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private final CountDownLatch openLatch = new CountDownLatch(1);

        private TestClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            openLatch.countDown();
        }

        @Override
        public void onMessage(String message) {
            messages.offer(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            messages.offer("ERROR:" + Objects.toString(ex.getMessage(), "unknown"));
        }

        @Override
        public boolean connectBlocking(long timeout, TimeUnit timeUnit) throws InterruptedException {
            boolean connected = super.connectBlocking(timeout, timeUnit);
            if (connected) {
                openLatch.await(timeout, timeUnit);
            }
            return connected;
        }

        private Optional<Map<String, Object>> awaitEvent(
                String type,
                Predicate<Map<String, Object>> payloadMatcher,
                Duration timeout
        ) throws InterruptedException {
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                String rawMessage = messages.poll(200, TimeUnit.MILLISECONDS);
                if (rawMessage == null || rawMessage.startsWith("ERROR:")) {
                    continue;
                }

                Map<String, Object> event = Json.parseObject(rawMessage);
                if (!type.equals(event.get("type"))) {
                    continue;
                }

                Map<String, Object> payload = objectValue(event.get("payload"));
                if (payloadMatcher.test(payload)) {
                    return Optional.of(payload);
                }
            }
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> objectValue(Object value) {
            return (Map<String, Object>) value;
        }
    }
}
