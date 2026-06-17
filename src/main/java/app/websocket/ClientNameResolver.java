package app.websocket;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class ClientNameResolver {
    private static final String DEFAULT_NAME = "anonymous";

    private ClientNameResolver() {
    }

    public static String resolve(String resourceDescriptor) {
        if (resourceDescriptor == null || resourceDescriptor.isBlank() || !resourceDescriptor.contains("name=")) {
            return DEFAULT_NAME;
        }

        try {
            URI uri = URI.create("ws://localhost" + resourceDescriptor);
            String query = uri.getQuery();
            if (query == null || query.isBlank()) {
                return DEFAULT_NAME;
            }

            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2 && "name".equals(parts[0])) {
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    return value.isBlank() ? DEFAULT_NAME : value;
                }
            }
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_NAME;
        }

        return DEFAULT_NAME;
    }
}
