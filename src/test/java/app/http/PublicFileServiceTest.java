package app.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicFileServiceTest {
    private final PublicFileService publicFileService = new PublicFileService();

    @Test
    void servesIndexHtmlAtRootPath() throws IOException {
        Optional<PublicFileService.StaticAsset> asset = publicFileService.findAsset("/");
        String html = new String(asset.orElseThrow().body(), StandardCharsets.UTF_8);

        assertTrue(asset.isPresent());
        assertEquals("text/html; charset=UTF-8", asset.get().contentType());
        assertTrue(html.contains("Polinema Chat Dashboard"));
        assertTrue(html.contains("/styles.css"));
        assertTrue(html.contains("Masuk ke Chat"));
    }

    @Test
    void returnsEmptyForMissingOrUnsafePaths() throws IOException {
        assertTrue(publicFileService.findAsset("/missing.js").isEmpty());
        assertTrue(publicFileService.findAsset("/../secret.txt").isEmpty());
    }
}
