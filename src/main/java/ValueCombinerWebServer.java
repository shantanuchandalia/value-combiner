import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ValueCombinerWebServer {
    private static final String STATIC_ROOT = "/static";
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put(".html", "text/html; charset=UTF-8");
        CONTENT_TYPES.put(".css", "text/css; charset=UTF-8");
        CONTENT_TYPES.put(".js", "application/javascript; charset=UTF-8");
        CONTENT_TYPES.put(".json", "application/json; charset=UTF-8");
    }

    /**
     * Starts a small local server for development at http://localhost:8080.
     * In production, host the static files separately and deploy ValueCombinerLambdaHandler to AWS Lambda.
     */
    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/calculate", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendCorsPreflight(exchange);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                handleCalculation(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                serveStaticFile(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Value Combiner dashboard: http://localhost:" + port);
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }

        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return Integer.parseInt(envPort);
        }

        return 8080;
    }

    private static void handleCalculation(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        String responseJson = ValueCombinerService.calculate(requestBody);
        sendResponse(exchange, 200, "application/json; charset=UTF-8", responseJson.getBytes(StandardCharsets.UTF_8));
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static void serveStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || "/".equals(path)) {
            path = "/index.html";
        }

        if (path.contains("..")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String resourcePath = STATIC_ROOT + path;
        InputStream resource = ValueCombinerWebServer.class.getResourceAsStream(resourcePath);
        if (resource == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        try (InputStream input = resource) {
            byte[] responseBytes = readAllBytes(input);
            sendResponse(exchange, 200, contentTypeFor(path), responseBytes);
        }
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static String contentTypeFor(String path) {
        for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
            if (path.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "application/octet-stream";
    }

    private static void sendCorsPreflight(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String contentType,
            byte[] responseBytes) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        exchange.getResponseHeaders().set("Pragma", "no-cache");
        exchange.getResponseHeaders().set("Expires", "0");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowedOrigin());
    }

    private static String allowedOrigin() {
        String origin = System.getenv("ALLOWED_ORIGIN");
        if (origin == null || origin.trim().isEmpty()) {
            return "http://localhost:8080";
        }
        return origin.trim();
    }
}
