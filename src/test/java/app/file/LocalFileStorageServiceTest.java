package app.file;

import app.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void storesAllowedFilesAndSanitizesFilename() throws IOException {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir.resolve("uploads"));

        StoredFile storedFile = storageService.store(
                "../../cat photo?.png",
                "image/png",
                "png-data".getBytes(StandardCharsets.UTF_8)
        );

        assertEquals("cat photo_.png", storedFile.originalFilename());
        assertEquals("image/png", storedFile.contentType());
        assertEquals("/api/files/" + storedFile.fileId(), storedFile.downloadUrl());
    }

    @Test
    void rejectsUnsupportedFileTypes() {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir.resolve("uploads"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> storageService.store(
                "script.exe",
                "application/octet-stream",
                "not-allowed".getBytes(StandardCharsets.UTF_8)
        ));

        assertEquals("Format file tidak didukung. Gunakan gambar, PDF, TXT, atau ZIP.", exception.getMessage());
    }

    @Test
    void rejectsFilesLargerThanTenMegabytes() {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir.resolve("uploads"));
        byte[] oversized = new byte[(int) LocalFileStorageService.MAX_FILE_SIZE_BYTES + 1];

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> storageService.store(
                "arsip.zip",
                "application/zip",
                oversized
        ));

        assertEquals("Ukuran file maksimal 10 MB.", exception.getMessage());
    }
}
