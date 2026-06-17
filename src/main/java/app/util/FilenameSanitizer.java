package app.util;

import java.text.Normalizer;

public final class FilenameSanitizer {
    private static final int MAX_FILENAME_LENGTH = 120;

    private FilenameSanitizer() {
    }

    public static String sanitize(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }

        String normalized = Normalizer.normalize(originalFilename, Normalizer.Form.NFKC)
                .replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }

        normalized = normalized.replaceAll("[\\p{Cntrl}]", "");
        normalized = normalized.replaceAll("[^\\p{Alnum}._ -]", "_");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        normalized = normalized.replaceAll("^[. ]+", "");
        normalized = normalized.replaceAll("[. ]+$", "");

        if (normalized.isBlank()) {
            normalized = "file";
        }

        return limitLength(normalized);
    }

    private static String limitLength(String filename) {
        if (filename.length() <= MAX_FILENAME_LENGTH) {
            return filename;
        }

        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == filename.length() - 1) {
            return filename.substring(0, MAX_FILENAME_LENGTH);
        }

        String extension = filename.substring(extensionIndex);
        int baseLength = Math.max(1, MAX_FILENAME_LENGTH - extension.length());
        return filename.substring(0, baseLength) + extension;
    }
}
