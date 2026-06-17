package app.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public final class PublicFileService {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".css", "text/css; charset=UTF-8",
            ".html", "text/html; charset=UTF-8",
            ".js", "application/javascript; charset=UTF-8",
            ".json", "application/json; charset=UTF-8",
            ".svg", "image/svg+xml"
    );

    public Optional<StaticAsset> findAsset(String requestPath) throws IOException {
        String normalizedPath = normalize(requestPath);
        if (normalizedPath == null) {
            return Optional.empty();
        }

        String resourcePath = "/public" + normalizedPath;
        try (InputStream resourceStream = PublicFileService.class.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                return Optional.empty();
            }

            return Optional.of(new StaticAsset(detectContentType(normalizedPath), resourceStream.readAllBytes()));
        }
    }

    private String normalize(String requestPath) {
        if (requestPath == null || requestPath.isBlank() || "/".equals(requestPath)) {
            return "/index.html";
        }

        String normalizedPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
        if (normalizedPath.contains("..")) {
            return null;
        }

        return normalizedPath;
    }

    private String detectContentType(String requestPath) {
        for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
            if (requestPath.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "application/octet-stream";
    }

    public record StaticAsset(String contentType, byte[] body) {
    }
}
