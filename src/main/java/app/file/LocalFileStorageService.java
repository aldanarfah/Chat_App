package app.file;

import app.model.StoredFile;
import app.util.FilenameSanitizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class LocalFileStorageService {
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    public static final long MAX_MULTIPART_REQUEST_BYTES = MAX_FILE_SIZE_BYTES + (64L * 1024L);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg",
            "pdf", "txt", "zip"
    );

    private final Path uploadsRoot;
    private final Map<String, StoredFile> filesById = new LinkedHashMap<>();

    public LocalFileStorageService(Path uploadsRoot) {
        this.uploadsRoot = uploadsRoot.toAbsolutePath().normalize();
    }

    public synchronized StoredFile storeMultipartUpload(String contentTypeHeader, byte[] requestBody) throws IOException {
        MultipartUploadParser.UploadPart uploadPart = MultipartUploadParser.parseSingleFile(contentTypeHeader, requestBody);
        return store(uploadPart.filename(), uploadPart.contentType(), uploadPart.bytes());
    }

    public synchronized StoredFile store(String originalFilename, String contentType, byte[] bytes) throws IOException {
        String safeFilename = FilenameSanitizer.sanitize(originalFilename);
        byte[] fileBytes = bytes == null ? new byte[0] : bytes;
        validateFile(safeFilename, contentType, fileBytes);

        Files.createDirectories(uploadsRoot);

        String fileId = nextFileId();
        String storedFilename = fileId + extensionSuffix(safeFilename);
        Path targetPath = uploadsRoot.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(uploadsRoot)) {
            throw new IllegalStateException("Lokasi penyimpanan file tidak valid.");
        }

        Files.write(targetPath, fileBytes, StandardOpenOption.CREATE_NEW);

        StoredFile storedFile = new StoredFile(
                fileId,
                safeFilename,
                storedFilename,
                detectContentType(safeFilename, contentType),
                fileBytes.length,
                "/api/files/" + fileId,
                now()
        );
        filesById.put(fileId, storedFile);
        return storedFile;
    }

    public synchronized Optional<StoredFile> findFile(String fileId) {
        return Optional.ofNullable(filesById.get(fileId));
    }

    public synchronized Optional<DownloadedFile> findDownload(String fileId) throws IOException {
        StoredFile storedFile = filesById.get(fileId);
        if (storedFile == null) {
            return Optional.empty();
        }

        Path filePath = uploadsRoot.resolve(storedFile.storedFilename()).normalize();
        if (!filePath.startsWith(uploadsRoot) || !Files.exists(filePath)) {
            return Optional.empty();
        }

        return Optional.of(new DownloadedFile(storedFile, Files.readAllBytes(filePath)));
    }

    private void validateFile(String safeFilename, String contentType, byte[] bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("File tidak boleh kosong.");
        }
        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Ukuran file maksimal 10 MB.");
        }
        if (!isAllowedFileType(safeFilename, contentType)) {
            throw new IllegalArgumentException("Format file tidak didukung. Gunakan gambar, PDF, TXT, atau ZIP.");
        }
    }

    private boolean isAllowedFileType(String filename, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (normalizedContentType.startsWith("image/")) {
            return true;
        }
        if (ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            return true;
        }

        String extension = extensionOf(filename);
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private String detectContentType(String filename, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (normalizedContentType.startsWith("image/") || ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            return normalizedContentType;
        }

        return switch (extensionOf(filename)) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf(';');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator).trim();
        }
        return normalized;
    }

    private String extensionSuffix(String filename) {
        String extension = extensionOf(filename);
        return extension.isBlank() ? "" : "." + extension;
    }

    private String extensionOf(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String nextFileId() {
        return "file_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    public record DownloadedFile(StoredFile metadata, byte[] body) {
    }
}
