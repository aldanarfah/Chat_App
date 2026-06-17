package app.model;

public record UserSession(String sessionId, String username, String createdAt, String connectedAt) {
    public boolean isActive() {
        return connectedAt != null && !connectedAt.isBlank();
    }
}
