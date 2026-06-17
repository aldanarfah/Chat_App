package app.chat;

import app.model.ChatMessage;
import app.model.ChatRoom;
import app.model.StoredFile;
import app.model.UserSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ChatService {
    public static final String GENERAL_ROOM_ID = "room-general";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, ChatRoom> roomsById = new LinkedHashMap<>();
    private final Map<String, String> roomIdsByName = new LinkedHashMap<>();
    private final Map<String, Set<String>> membershipsByRoomId = new LinkedHashMap<>();
    private final Map<String, List<ChatMessage>> roomMessagesByRoomId = new LinkedHashMap<>();
    private final Map<String, List<ChatMessage>> directMessagesByConversationId = new LinkedHashMap<>();

    public ChatService() {
        createInitialRoom(GENERAL_ROOM_ID, "Umum");
    }

    public synchronized ChatRoom createRoom(String name) {
        String normalizedName = requireRoomName(name);
        String nameKey = normalizedName.toLowerCase(Locale.ROOT);
        if (roomIdsByName.containsKey(nameKey)) {
            throw new IllegalStateException("Nama room sudah ada. Gunakan nama lain.");
        }

        ChatRoom room = new ChatRoom(nextRoomId(), normalizedName, now());
        roomsById.put(room.roomId(), room);
        roomIdsByName.put(nameKey, room.roomId());
        membershipsByRoomId.put(room.roomId(), new LinkedHashSet<>());
        roomMessagesByRoomId.put(room.roomId(), new ArrayList<>());
        return room;
    }

    public synchronized List<ChatRoom> listRooms() {
        List<ChatRoom> rooms = new ArrayList<>(roomsById.values());
        rooms.sort(Comparator.comparing(ChatRoom::name, String.CASE_INSENSITIVE_ORDER));
        return rooms;
    }

    public synchronized int memberCount(String roomId) {
        return requireMemberships(roomId).size();
    }

    public synchronized RoomJoinResult joinRoom(String roomId, String sessionId) {
        ChatRoom room = requireRoom(roomId);
        Set<String> members = requireMemberships(roomId);
        members.add(sessionId);
        return new RoomJoinResult(room, new ArrayList<>(members), new ArrayList<>(roomMessagesByRoomId.get(roomId)));
    }

    public synchronized RoomLeaveResult leaveRoom(String roomId, String sessionId) {
        ChatRoom room = requireRoom(roomId);
        Set<String> members = requireMemberships(roomId);
        boolean removed = members.remove(sessionId);
        return new RoomLeaveResult(room, removed, new ArrayList<>(members));
    }

    public synchronized List<String> removeSessionFromAllRooms(String sessionId) {
        List<String> affectedRoomIds = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : membershipsByRoomId.entrySet()) {
            if (entry.getValue().remove(sessionId)) {
                affectedRoomIds.add(entry.getKey());
            }
        }
        return affectedRoomIds;
    }

    public synchronized List<String> roomMemberSessionIds(String roomId) {
        return new ArrayList<>(requireMemberships(roomId));
    }

    public synchronized List<ChatMessage> roomMessages(String roomId) {
        requireRoom(roomId);
        return new ArrayList<>(roomMessagesByRoomId.get(roomId));
    }

    public synchronized ChatMessage appendRoomMessage(String roomId, UserSession sender, String text, StoredFile file) {
        String normalizedText = normalizeText(text);
        requireRoom(roomId);
        if (!requireMemberships(roomId).contains(sender.sessionId())) {
            throw new IllegalArgumentException("Anda belum bergabung ke room tersebut.");
        }
        requireMessageContent(normalizedText, file);

        ChatMessage message = new ChatMessage(
                nextMessageId(),
                roomId,
                "room",
                sender.sessionId(),
                sender.username(),
                null,
                normalizedText,
                file,
                now()
        );
        roomMessagesByRoomId.get(roomId).add(message);
        return message;
    }

    public synchronized ChatMessage appendDirectMessage(
            UserSession sender,
            UserSession target,
            String text,
            StoredFile file
    ) {
        String normalizedText = normalizeText(text);
        if (sender.sessionId().equals(target.sessionId())) {
            throw new IllegalArgumentException("Direct message tidak bisa dikirim ke diri sendiri.");
        }
        requireMessageContent(normalizedText, file);

        String conversationId = directConversationId(sender.sessionId(), target.sessionId());
        ChatMessage message = new ChatMessage(
                nextMessageId(),
                conversationId,
                "direct",
                sender.sessionId(),
                sender.username(),
                target.sessionId(),
                normalizedText,
                file,
                now()
        );
        directMessagesByConversationId
                .computeIfAbsent(conversationId, ignored -> new ArrayList<>())
                .add(message);
        return message;
    }

    public synchronized ChatRoom requireRoom(String roomId) {
        ChatRoom room = roomsById.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room tidak ditemukan.");
        }
        return room;
    }

    public synchronized List<ChatMessage> directMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        for (List<ChatMessage> conversationMessages : directMessagesByConversationId.values()) {
            messages.addAll(conversationMessages);
        }
        messages.sort(Comparator.comparing(ChatMessage::createdAt));
        return messages;
    }

    public static String directConversationId(String firstSessionId, String secondSessionId) {
        if (firstSessionId.compareTo(secondSessionId) <= 0) {
            return "dm:" + firstSessionId + ":" + secondSessionId;
        }
        return "dm:" + secondSessionId + ":" + firstSessionId;
    }

    private void createInitialRoom(String roomId, String name) {
        ChatRoom room = new ChatRoom(roomId, name, now());
        roomsById.put(room.roomId(), room);
        roomIdsByName.put(name.toLowerCase(Locale.ROOT), room.roomId());
        membershipsByRoomId.put(room.roomId(), new LinkedHashSet<>());
        roomMessagesByRoomId.put(room.roomId(), new ArrayList<>());
    }

    private Set<String> requireMemberships(String roomId) {
        Set<String> memberships = membershipsByRoomId.get(roomId);
        if (memberships == null) {
            throw new IllegalArgumentException("Room tidak ditemukan.");
        }
        return memberships;
    }

    private String requireRoomName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama room wajib diisi.");
        }

        String trimmed = name.trim();
        if (trimmed.length() > 40) {
            throw new IllegalArgumentException("Nama room maksimal 40 karakter.");
        }

        return trimmed;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }

        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private void requireMessageContent(String text, StoredFile file) {
        if (text == null && file == null) {
            throw new IllegalArgumentException("Pesan wajib berisi teks atau file.");
        }
    }

    private String nextRoomId() {
        return "room_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String nextMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    public record RoomJoinResult(ChatRoom room, List<String> memberSessionIds, List<ChatMessage> recentMessages) {
    }

    public record RoomLeaveResult(ChatRoom room, boolean removed, List<String> remainingMemberSessionIds) {
    }
}
