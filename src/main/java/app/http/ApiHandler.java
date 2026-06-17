package app.http;

import app.chat.ChatService;
import app.file.LocalFileStorageService;
import app.model.ChatRoom;
import app.model.StoredFile;
import app.model.UserSession;
import app.session.SessionService;
import app.util.Json;
import app.websocket.ChatWebSocketServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ApiHandler implements HttpHandler {
    private final SessionService sessionService;
    private final ChatService chatService;
    private final LocalFileStorageService fileStorageService;
    private final ChatWebSocketServer websocketServer;

    public ApiHandler(
            SessionService sessionService,
            ChatService chatService,
            LocalFileStorageService fileStorageService,
            ChatWebSocketServer websocketServer
    ) {
        this.sessionService = sessionService;
        this.chatService = chatService;
        this.fileStorageService = fileStorageService;
        this.websocketServer = websocketServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = normalizePath(exchange.getRequestURI().getPath());
            String method = exchange.getRequestMethod().toUpperCase();

            if ("/api/session".equals(path) && "POST".equals(method)) {
                createSession(exchange);
                return;
            }
            if ("/api/users".equals(path) && "GET".equals(method)) {
                listUsers(exchange);
                return;
            }
            if ("/api/rooms".equals(path) && "GET".equals(method)) {
                listRooms(exchange);
                return;
            }
            if ("/api/rooms".equals(path) && "POST".equals(method)) {
                createRoom(exchange);
                return;
            }
            if ("/api/files".equals(path) && "POST".equals(method)) {
                uploadFile(exchange);
                return;
            }
            if (path.startsWith("/api/files/") && "GET".equals(method)) {
                downloadFile(exchange, path.substring("/api/files/".length()));
                return;
            }

            writeJson(exchange, 404, Json.object(
                    "error", Json.object(
                            "code", "not_found",
                            "message", "Endpoint tidak ditemukan."
                    )
            ));
        } catch (IllegalArgumentException exception) {
            writeJson(exchange, 400, Json.object(
                    "error", Json.object(
                            "code", "bad_request",
                            "message", exception.getMessage()
                    )
            ));
        } catch (IllegalStateException exception) {
            writeJson(exchange, 409, Json.object(
                    "error", Json.object(
                            "code", "conflict",
                            "message", exception.getMessage()
                    )
            ));
        }
    }

    private void createSession(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = Json.parseObject(readRequestBody(exchange));
        String username = stringValue(payload.get("username"), "username");
        boolean forceTakeover = booleanValue(payload.get("forceTakeover"));

        try {
            SessionService.SessionCreationResult result = sessionService.createSession(username, forceTakeover);
            if (result.replacedSessionId() != null) {
                websocketServer.disconnectSession(
                        result.replacedSessionId(),
                        "Sesi dengan nama ini diambil alih dari perangkat lain."
                );
            }
            writeJson(exchange, 201, sessionToMap(result.session()));
        } catch (SessionService.ActiveSessionConflictException exception) {
            writeJson(exchange, 409, Json.object(
                    "error", Json.object(
                            "code", "username_in_use",
                            "message", "Nama ini sedang aktif di perangkat lain. Ambil alih sesi untuk masuk kembali.",
                            "canTakeOver", true,
                            "username", exception.username()
                    )
            ));
        }
    }

    private void listUsers(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> users = new ArrayList<>();
        for (UserSession session : sessionService.listActiveSessions()) {
            users.add(sessionToMap(session));
        }
        writeJson(exchange, 200, Json.object("users", users));
    }

    private void listRooms(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (ChatRoom room : chatService.listRooms()) {
            rooms.add(roomToMap(room));
        }
        writeJson(exchange, 200, Json.object("rooms", rooms));
    }

    private void createRoom(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = Json.parseObject(readRequestBody(exchange));
        String roomName = stringValue(payload.get("name"), "name");
        ChatRoom room = chatService.createRoom(roomName);
        websocketServer.broadcastRoomCreated(room);
        writeJson(exchange, 201, roomToMap(room));
    }

    private void uploadFile(HttpExchange exchange) throws IOException {
        byte[] requestBody = readRequestBody(exchange, LocalFileStorageService.MAX_MULTIPART_REQUEST_BYTES);
        StoredFile storedFile = fileStorageService.storeMultipartUpload(
                exchange.getRequestHeaders().getFirst("Content-Type"),
                requestBody
        );
        writeJson(exchange, 201, fileToMap(storedFile));
    }

    private void downloadFile(HttpExchange exchange, String fileId) throws IOException {
        if (fileId == null || fileId.isBlank()) {
            writeJson(exchange, 404, Json.object(
                    "error", Json.object(
                            "code", "not_found",
                            "message", "File tidak ditemukan."
                    )
            ));
            return;
        }

        Optional<LocalFileStorageService.DownloadedFile> download = fileStorageService.findDownload(fileId.trim());
        if (download.isEmpty()) {
            writeJson(exchange, 404, Json.object(
                    "error", Json.object(
                            "code", "not_found",
                            "message", "File tidak ditemukan."
                    )
            ));
            return;
        }

        LocalFileStorageService.DownloadedFile downloadedFile = download.get();
        writeBinary(
                exchange,
                200,
                downloadedFile.metadata().contentType(),
                contentDisposition(downloadedFile.metadata().originalFilename()),
                downloadedFile.body()
        );
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private byte[] readRequestBody(HttpExchange exchange, long maxBytes) throws IOException {
        byte[] requestBody = exchange.getRequestBody().readNBytes((int) (maxBytes + 1));
        if (requestBody.length > maxBytes) {
            throw new IllegalArgumentException("Ukuran file maksimal 10 MB.");
        }
        return requestBody;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "/";
        }
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void writeJson(HttpExchange exchange, int statusCode, Map<String, Object> payload) throws IOException {
        byte[] responseBytes = Json.stringify(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(responseBytes);
        }
    }

    private void writeBinary(
            HttpExchange exchange,
            int statusCode,
            String contentType,
            String contentDisposition,
            byte[] responseBytes
    ) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition", contentDisposition);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(responseBytes);
        }
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

    private String stringValue(Object value, String fieldName) {
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' wajib berupa string.");
    }

    private boolean booleanValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        throw new IllegalArgumentException("Field 'forceTakeover' wajib berupa boolean.");
    }

    private String contentDisposition(String filename) {
        String escapedFilename = filename.replace("\\", "_").replace("\"", "'");
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + escapedFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }
}
