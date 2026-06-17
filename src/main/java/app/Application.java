package app;

import app.chat.ChatService;
import app.file.LocalFileStorageService;
import app.http.ApiHandler;
import app.http.PublicFileService;
import app.http.StaticFileHandler;
import app.session.SessionService;
import app.websocket.ChatWebSocketServer;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Paths;

public final class Application {
    private static final String DEFAULT_BIND_HOST = "0.0.0.0";
    private static final int HTTP_PORT = 8080;
    private static final int WEBSOCKET_PORT = 8887;

    private Application() {
    }

    public static void main(String[] args) throws Exception {
        String bindHost = envOrDefault("APP_BIND_HOST", DEFAULT_BIND_HOST);
        SessionService sessionService = new SessionService();
        ChatService chatService = new ChatService();
        LocalFileStorageService fileStorageService = new LocalFileStorageService(Paths.get("uploads"));

        ChatWebSocketServer websocketServer =
                new ChatWebSocketServer(
                        new InetSocketAddress(bindHost, WEBSOCKET_PORT),
                        sessionService,
                        chatService,
                        fileStorageService
                );
        websocketServer.start();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(bindHost, HTTP_PORT), 0);
        httpServer.createContext("/api/", new ApiHandler(sessionService, chatService, fileStorageService, websocketServer));
        httpServer.createContext("/", new StaticFileHandler(new PublicFileService()));
        httpServer.setExecutor(null);
        httpServer.start();

        System.out.println("Bind host    : " + bindHost);
        System.out.println("HTTP local   : http://localhost:" + HTTP_PORT);
        System.out.println("HTTP VPN/LAN : http://<vpn-ip-atau-host>:" + HTTP_PORT);
        System.out.println("WebSocket    : ws://<vpn-ip-atau-host>:" + WEBSOCKET_PORT + "/ws?sessionId=...");
        System.out.println("Catatan      : Pastikan port 8080 dan 8887 diizinkan di VPN/firewall.");
        System.out.println("Press Ctrl+C to stop.");
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
