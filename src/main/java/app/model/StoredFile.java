package app.model;

public record StoredFile(
        String fileId,
        String originalFilename,
        String storedFilename,
        String contentType,
        long sizeBytes,
        String downloadUrl,
        String uploadedAt
) {
}
