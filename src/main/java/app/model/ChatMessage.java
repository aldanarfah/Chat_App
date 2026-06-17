package app.model;

public record ChatMessage(
        String id,
        String conversationId,
        String conversationType,
        String senderSessionId,
        String senderName,
        String targetSessionId,
        String text,
        StoredFile file,
        String createdAt
) {
}
