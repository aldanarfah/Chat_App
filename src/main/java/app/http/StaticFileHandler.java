package app.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public final class StaticFileHandler implements HttpHandler {
    private final PublicFileService publicFileService;

    public StaticFileHandler(PublicFileService publicFileService) {
        this.publicFileService = publicFileService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Optional<PublicFileService.StaticAsset> asset =
                publicFileService.findAsset(exchange.getRequestURI().getPath());
        if (asset.isEmpty()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        PublicFileService.StaticAsset staticAsset = asset.get();
        exchange.getResponseHeaders().set("Content-Type", staticAsset.contentType());
        exchange.sendResponseHeaders(200, staticAsset.body().length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(staticAsset.body());
        }
    }
}
