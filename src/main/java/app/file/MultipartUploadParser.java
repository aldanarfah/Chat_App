package app.file;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class MultipartUploadParser {
    private MultipartUploadParser() {
    }

    static UploadPart parseSingleFile(String contentTypeHeader, byte[] requestBody) {
        String boundary = extractBoundary(contentTypeHeader);
        String rawBody = new String(requestBody, StandardCharsets.ISO_8859_1);
        String delimiter = "--" + boundary;
        int cursor = 0;

        while (true) {
            int partBoundary = rawBody.indexOf(delimiter, cursor);
            if (partBoundary < 0) {
                break;
            }

            int afterBoundary = partBoundary + delimiter.length();
            if (rawBody.startsWith("--", afterBoundary)) {
                break;
            }
            if (rawBody.startsWith("\r\n", afterBoundary)) {
                afterBoundary += 2;
            }

            int headerEnd = rawBody.indexOf("\r\n\r\n", afterBoundary);
            if (headerEnd < 0) {
                throw new IllegalArgumentException("Format upload file tidak valid.");
            }

            Map<String, String> headers = parseHeaders(rawBody.substring(afterBoundary, headerEnd));
            int dataStart = headerEnd + 4;
            int nextBoundary = rawBody.indexOf("\r\n" + delimiter, dataStart);
            if (nextBoundary < 0) {
                throw new IllegalArgumentException("Format upload file tidak valid.");
            }

            byte[] partBytes = Arrays.copyOfRange(requestBody, dataStart, nextBoundary);
            ContentDisposition disposition = parseContentDisposition(headers.get("content-disposition"));
            if ("file".equals(disposition.name()) && disposition.filename() != null && !disposition.filename().isBlank()) {
                String partContentType = headers.getOrDefault("content-type", "application/octet-stream");
                return new UploadPart(disposition.filename(), partContentType, partBytes);
            }

            cursor = nextBoundary + 2;
        }

        throw new IllegalArgumentException("Field 'file' wajib dikirim.");
    }

    private static String extractBoundary(String contentTypeHeader) {
        if (contentTypeHeader == null) {
            throw new IllegalArgumentException("Gunakan multipart/form-data untuk upload file.");
        }

        String[] parts = contentTypeHeader.split(";");
        if (parts.length == 0 || !"multipart/form-data".equalsIgnoreCase(parts[0].trim())) {
            throw new IllegalArgumentException("Gunakan multipart/form-data untuk upload file.");
        }

        for (int index = 1; index < parts.length; index++) {
            String[] keyValue = parts[index].trim().split("=", 2);
            if (keyValue.length == 2 && "boundary".equalsIgnoreCase(keyValue[0].trim())) {
                String boundary = keyValue[1].trim();
                if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                if (!boundary.isBlank()) {
                    return boundary;
                }
            }
        }

        throw new IllegalArgumentException("Boundary upload file tidak ditemukan.");
    }

    private static Map<String, String> parseHeaders(String rawHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String line : rawHeaders.split("\r\n")) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String name = line.substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(separator + 1).trim();
            headers.put(name, value);
        }
        return headers;
    }

    private static ContentDisposition parseContentDisposition(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Header upload file tidak lengkap.");
        }

        String name = null;
        String filename = null;
        String[] segments = headerValue.split(";");
        for (String segment : segments) {
            String[] keyValue = segment.trim().split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0].trim().toLowerCase(Locale.ROOT);
            String value = stripQuotes(keyValue[1].trim());
            if ("name".equals(key)) {
                name = value;
            } else if ("filename".equals(key)) {
                filename = value;
            }
        }

        return new ContentDisposition(name, filename);
    }

    private static String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    record UploadPart(String filename, String contentType, byte[] bytes) {
    }

    private record ContentDisposition(String name, String filename) {
    }
}
