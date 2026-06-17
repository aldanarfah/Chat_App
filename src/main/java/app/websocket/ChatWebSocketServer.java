package app.websocket;

import app.chat.ChatService;
import app.file.LocalFileStorageService;
import app.model.ChatMessage;
import app.model.ChatRoom;
import app.model.StoredFile;
import app.model.UserSession;
import app.session.SessionService;
import app.util.Json;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ChatWebSocketServer extends WebSocketServer {
    private final SessionService sessionService;
    private final ChatService chatService;
    private final LocalFileStorageService fileStorageService;
    private final Map<WebSocket, String> sessionIdsByConnection = new ConcurrentHashMap<>();
    private final Map<String, WebSocket> connectionsBySessionId = new ConcurrentHashMap<>();
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    public ChatWebSocketServer(InetSocketAddress address) {
        this(address, new SessionService(), new ChatService(), new LocalFileStorageService(Paths.get("uploads")));
    }

    public ChatWebSocketServer(
            InetSocketAddress address,
            SessionService sessionService,
            ChatService chatService,
            LocalFileStorageService fileStorageService
    ) {
        super(address);
        this.sessionService = sessionService;
        this.chatService = chatService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {
        RequestContext requestContext = parseRequest(connection.getResourceDescriptor());
        if (!"/ws".equals(requestContext.path())) {
            rejectConnection(connection, "invalid_endpoint", "Gunakan endpoint WebSocket /ws.");
            return;
        }

        String sessionId = requestContext.queryParams().get("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            rejectConnection(connection, "missing_session", "Parameter sessionId wajib dikirim.");
            return;
        }

        SessionService.SessionConnectResult connectResult = sessionService.connect(sessionId.trim());
        if (!connectResult.isSuccess()) {
            rejectConnection(connection, "invalid_session", connectResult.errorMessage());
            return;
        }

        WebSocket previousConnection = connectionsBySessionId.putIfAbsent(sessionId.trim(), connection);
        if (previousConnection != null) {
            rejectConnection(connection, "duplicate_connection", "Sesi sedang aktif di koneksi lain.");
            return;
        }

        sessionIdsByConnection.put(connection, sessionId.trim());
        UserSession session = connectResult.session();
        sendEvent(connection, "session.ready", Json.object(
                "session", sessionToMap(session),
                "activeUsers", activeUsersPayload(),
                "rooms", roomsPayload(),
                "directConversations", directConversationsPayload(session)
        ));
        broadcastEvent("presence.joined", Json.object(
                "user", sessionToMap(session),
                "activeUsers", activeUsersPayload()
        ));
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        String sessionId = sessionIdsByConnection.remove(connection);
        if (sessionId == null) {
            return;
        }

        connectionsBySessionId.remove(sessionId, connection);
        Optional<UserSession> session = sessionService.disconnect(sessionId);
        List<String> affectedRoomIds = chatService.removeSessionFromAllRooms(sessionId);
        for (String roomId : affectedRoomIds) {
            broadcastRoomMembershipUpdated(roomId);
        }
        session.ifPresent(disconnectedSession -> broadcastEvent("presence.left", Json.object(
                "user", sessionToMap(disconnectedSession),
                "activeUsers", activeUsersPayload()
        )));
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        String sessionId = sessionIdsByConnection.get(connection);
        if (sessionId == null) {
            sendError(connection, "unauthorized", "Sesi belum aktif.");
            return;
        }

        Optional<UserSession> senderSession = sessionService.findSession(sessionId);
        if (senderSession.isEmpty()) {
            sendError(connection, "invalid_session", "Sesi tidak ditemukan.");
            return;
        }

        try {
            Map<String, Object> request = Json.parseObject(message);
            String eventType = stringValue(request.get("type"), "type");
            Map<String, Object> payload = objectValue(request.get("payload"), "payload");
            routeMessage(connection, senderSession.get(), eventType, payload);
        } catch (IllegalArgumentException exception) {
            sendError(connection, "bad_request", exception.getMessage());
        }
    }

    @Override
    public void onError(WebSocket connection, Exception exception) {
        System.err.println("WebSocket error: " + exception.getMessage());
    }

    @Override
    public void onStart() {
        startedLatch.countDown();
        System.out.println("WebSocket server started on port " + getPort());
    }

    public boolean awaitStarted(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return startedLatch.await(timeout, timeUnit);
    }

    public void disconnectSession(String sessionId, String message) {
        WebSocket connection = connectionsBySessionId.get(sessionId);
        if (connection == null) {
            return;
        }

        sendError(connection, "session_taken_over", message);
        connection.close(4001, message);
    }

    public void broadcastRoomCreated(ChatRoom room) {
        broadcastEvent("room.created", Json.object(
                "room", roomToMap(room)
        ));
    }

    private void routeMessage(
            WebSocket connection,
            UserSession senderSession,
            String eventType,
            Map<String, Object> payload
    ) {
        switch (eventType) {
            case "room.join" -> handleRoomJoin(connection, senderSession, payload);
            case "room.leave" -> handleRoomLeave(connection, senderSession, payload);
            case "room.message.send" -> handleRoomMessage(senderSession, payload);
            case "dm.message.send" -> handleDirectMessage(senderSession, payload);
            default -> sendError(connection, "unknown_event", "Tipe event tidak dikenali: " + eventType);
        }
    }

    private void handleRoomJoin(WebSocket connection, UserSession senderSession, Map<String, Object> payload) {
        String roomId = stringValue(payload.get("roomId"), "payload.roomId");
        ChatService.RoomJoinResult result = chatService.joinRoom(roomId, senderSession.sessionId());
        sendEvent(connection, "room.joined", Json.object(
                "room", roomToMap(result.room()),
                "members", usersPayload(result.memberSessionIds()),
                "recentMessages", messagesPayload(result.recentMessages())
        ));
        broadcastRoomMembershipUpdated(roomId);
    }

    private void handleRoomLeave(WebSocket connection, UserSession senderSession, Map<String, Object> payload) {
        String roomId = stringValue(payload.get("roomId"), "payload.roomId");
        ChatService.RoomLeaveResult result = chatService.leaveRoom(roomId, senderSession.sessionId());
        sendEvent(connection, "room.left", Json.object(
                "roomId", result.room().roomId(),
                "removed", result.removed(),
                "members", usersPayload(result.remainingMemberSessionIds())
        ));
        broadcastRoomMembershipUpdated(roomId);
    }

    private void handleRoomMessage(UserSession senderSession, Map<String, Object> payload) {
        String roomId = stringValue(payload.get("conversationId"), "payload.conversationId");
        String text = optionalStringValue(payload.get("text"), "payload.text");
        StoredFile file = resolveFile(payload.get("fileId"));
        ChatMessage message = chatService.appendRoomMessage(roomId, senderSession, text, file);
        broadcastToSessionIds(chatService.roomMemberSessionIds(roomId), "room.message.receive", messageToMap(message));
    }

    private void handleDirectMessage(UserSession senderSession, Map<String, Object> payload) {
        String targetSessionId = stringValue(payload.get("targetSessionId"), "payload.targetSessionId");
        String text = optionalStringValue(payload.get("text"), "payload.text");
        StoredFile file = resolveFile(payload.get("fileId"));
        UserSession targetSession = sessionService.requireActiveSession(targetSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Pengguna tujuan tidak aktif."));
        ChatMessage message = chatService.appendDirectMessage(senderSession, targetSession, text, file);
        Set<String> recipientIds = new LinkedHashSet<>();
        recipientIds.add(senderSession.sessionId());
        recipientIds.add(targetSession.sessionId());
        broadcastToSessionIds(recipientIds, "dm.message.receive", messageToMap(message));
    }

    private void broadcastRoomMembershipUpdated(String roomId) {
        ChatRoom room = chatService.requireRoom(roomId);
        List<String> memberSessionIds = chatService.roomMemberSessionIds(roomId);
        Collection<String> recipients = new LinkedHashSet<>(memberSessionIds);
        broadcastToSessionIds(recipients, "room.membership.updated", Json.object(
                "room", roomToMap(room),
                "members", usersPayload(memberSessionIds)
        ));
    }

    private void rejectConnection(WebSocket connection, String code, String message) {
        sendError(connection, code, message);
        connection.close(1008, message);
    }

    private void sendError(WebSocket connection, String code, String message) {
        sendEvent(connection, "error", Json.object(
                "code", code,
                "message", message
        ));
    }

    private void sendEvent(WebSocket connection, String type, Map<String, Object> payload) {
        connection.send(Json.stringify(Json.object(
                "type", type,
                "payload", payload
        )));
    }

    private void broadcastEvent(String type, Map<String, Object> payload) {
        String serialized = Json.stringify(Json.object("type", type, "payload", payload));
        for (WebSocket client : sessionIdsByConnection.keySet()) {
            client.send(serialized);
        }
    }

    private void broadcastToSessionIds(Collection<String> sessionIds, String type, Map<String, Object> payload) {
        String serialized = Json.stringify(Json.object("type", type, "payload", payload));
        for (String sessionId : sessionIds) {
            WebSocket client = connectionsBySessionId.get(sessionId);
            if (client != null) {
                client.send(serialized);
            }
        }
    }

    private List<Map<String, Object>> activeUsersPayload() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (UserSession session : sessionService.listActiveSessions()) {
            users.add(sessionToMap(session));
        }
        return users;
    }

    private List<Map<String, Object>> roomsPayload() {
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (ChatRoom room : chatService.listRooms()) {
            rooms.add(roomToMap(room));
        }
        return rooms;
    }

    private List<Map<String, Object>> usersPayload(List<String> sessionIds) {
        List<Map<String, Object>> users = new ArrayList<>();
        for (String sessionId : sessionIds) {
            sessionService.findSession(sessionId).ifPresent(session -> users.add(sessionToMap(session)));
        }
        return users;
    }

    private List<Map<String, Object>> messagesPayload(List<ChatMessage> messages) {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (ChatMessage message : messages) {
            payload.add(messageToMap(message));
        }
        return payload;
    }

    private List<Map<String, Object>> directConversationsPayload(UserSession currentSession) {
        Map<String, List<ChatMessage>> groupedMessages = new LinkedHashMap<>();
        for (ChatMessage message : chatService.directMessages()) {
            String counterpartName = directCounterpartName(message, currentSession.username());
            if (counterpartName == null || counterpartName.isBlank()) {
                continue;
            }
            groupedMessages.computeIfAbsent(counterpartName, ignored -> new ArrayList<>()).add(message);
        }

        List<Map<String, Object>> payload = new ArrayList<>();
        for (Map.Entry<String, List<ChatMessage>> entry : groupedMessages.entrySet()) {
            String counterpartName = entry.getKey();
            List<ChatMessage> messages = entry.getValue();
            String counterpartSessionId = directCounterpartSessionId(messages, currentSession.username(), counterpartName);
            if (counterpartSessionId == null || counterpartSessionId.isBlank()) {
                continue;
            }

            String conversationId = ChatService.directConversationId(currentSession.sessionId(), counterpartSessionId);
            List<Map<String, Object>> conversationMessages = new ArrayList<>();
            messages.sort(Comparator.comparing(ChatMessage::createdAt));
            for (ChatMessage message : messages) {
                conversationMessages.add(messageToMap(remapDirectMessage(
                        message,
                        currentSession,
                        counterpartSessionId,
                        counterpartName,
                        conversationId
                )));
            }

            Optional<UserSession> counterpartSession = sessionService.findLatestSessionByUsername(counterpartName);
            payload.add(Json.object(
                    "conversationId", conversationId,
                    "targetSessionId", counterpartSessionId,
                    "targetName", counterpartName,
                    "active", counterpartSession.map(UserSession::isActive).orElse(false),
                    "messages", conversationMessages
            ));
        }
        return payload;
    }

    private ChatMessage remapDirectMessage(
            ChatMessage message,
            UserSession currentSession,
            String counterpartSessionId,
            String counterpartName,
            String conversationId
    ) {
        boolean outgoing = currentSession.username().equalsIgnoreCase(message.senderName());
        return new ChatMessage(
                message.id(),
                conversationId,
                message.conversationType(),
                outgoing ? currentSession.sessionId() : counterpartSessionId,
                outgoing ? currentSession.username() : counterpartName,
                outgoing ? counterpartSessionId : currentSession.sessionId(),
                message.text(),
                message.file(),
                message.createdAt()
        );
    }

    private String directCounterpartName(ChatMessage message, String currentUsername) {
        if (currentUsername.equalsIgnoreCase(message.senderName())) {
            return sessionService.findSession(message.targetSessionId())
                    .map(UserSession::username)
                    .orElse(null);
        }

        return sessionService.findSession(message.targetSessionId())
                .filter(targetSession -> currentUsername.equalsIgnoreCase(targetSession.username()))
                .map(targetSession -> message.senderName())
                .orElse(null);
    }

    private String directCounterpartSessionId(
            List<ChatMessage> messages,
            String currentUsername,
            String counterpartName
    ) {
        Optional<UserSession> preferredSession = sessionService.findLatestSessionByUsername(counterpartName);
        if (preferredSession.isPresent()) {
            return preferredSession.get().sessionId();
        }

        for (ChatMessage message : messages) {
            if (currentUsername.equalsIgnoreCase(message.senderName())) {
                return message.targetSessionId();
            }
            Optional<UserSession> targetSession = sessionService.findSession(message.targetSessionId());
            if (targetSession.isPresent() && currentUsername.equalsIgnoreCase(targetSession.get().username())) {
                return message.senderSessionId();
            }
        }
        return null;
    }

    private Map<String, Object> sessionToMap(UserSession session) {
        return Json.object(
                "sessionId", session.sessionId(),
                "username", session.username(),
                "createdAt", session.createdAt(),
                "connectedAt", session.connectedAt(),
                "active", session.isActive()
        );
    }

    private Map<String, Object> roomToMap(ChatRoom room) {
        return Json.object(
                "roomId", room.roomId(),
                "name", room.name(),
                "createdAt", room.createdAt(),
                "memberCount", chatService.memberCount(room.roomId())
        );
    }

    private Map<String, Object> messageToMap(ChatMessage message) {
        return Json.object(
                "id", message.id(),
                "conversationId", message.conversationId(),
                "conversationType", message.conversationType(),
                "senderSessionId", message.senderSessionId(),
                "senderName", message.senderName(),
                "targetSessionId", message.targetSessionId(),
                "text", message.text(),
                "fileId", message.file() == null ? null : message.file().fileId(),
                "file", message.file() == null ? null : fileToMap(message.file()),
                "createdAt", message.createdAt()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> objectValue(Object value, String fieldName) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> result = new ConcurrentHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' wajib berupa object.");
    }

    private String stringValue(Object value, String fieldName) {
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' wajib berupa string.");
    }

    private String optionalStringValue(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        return stringValue(value, fieldName);
    }

    private StoredFile resolveFile(Object fileIdValue) {
        String fileId = optionalStringValue(fileIdValue, "payload.fileId");
        if (fileId == null || fileId.isBlank()) {
            return null;
        }
        return fileStorageService.findFile(fileId.trim())
                .orElseThrow(() -> new IllegalArgumentException("File tidak ditemukan."));
    }

    private Map<String, Object> fileToMap(StoredFile file) {
        return Json.object(
                "fileId", file.fileId(),
                "filename", file.originalFilename(),
                "contentType", file.contentType(),
                "sizeBytes", file.sizeBytes(),
                "downloadUrl", file.downloadUrl(),
                "uploadedAt", file.uploadedAt()
        );
    }

    private RequestContext parseRequest(String resourceDescriptor) {
        if (resourceDescriptor == null || resourceDescriptor.isBlank()) {
            return new RequestContext("/", Map.of());
        }

        String[] parts = resourceDescriptor.split("\\?", 2);
        Map<String, String> queryParams = new ConcurrentHashMap<>();
        if (parts.length == 2 && !parts[1].isBlank()) {
            for (String pair : parts[1].split("&")) {
                if (pair.isBlank()) {
                    continue;
                }
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }

        return new RequestContext(parts[0], queryParams);
    }

    private record RequestContext(String path, Map<String, String> queryParams) {
    }
}
