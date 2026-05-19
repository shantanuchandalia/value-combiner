import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ValueCombinerLambdaHandler {
    /**
     * AWS Lambda entry point. API Gateway passes the HTTP request details in the event map.
     */
    public Map<String, Object> handleRequest(Map<String, Object> event) {
        String method = extractHttpMethod(event);

        // Browsers send OPTIONS before POST when calling an API on another domain.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return response(204, "");
        }

        String body = extractBody(event);

        // Keep calculation logic outside the Lambda adapter so it can be reused locally.
        String responseJson = ValueCombinerService.calculate(body);
        return response(200, responseJson);
    }

    private String extractBody(Map<String, Object> event) {
        Object bodyValue = event.get("body");
        String body = bodyValue == null ? "{}" : String.valueOf(bodyValue);
        Object base64EncodedValue = event.get("isBase64Encoded");
        if (Boolean.TRUE.equals(base64EncodedValue) || "true".equalsIgnoreCase(String.valueOf(base64EncodedValue))) {
            byte[] decoded = Base64.getDecoder().decode(body);
            return new String(decoded, StandardCharsets.UTF_8);
        }
        return body;
    }

    @SuppressWarnings("unchecked")
    private String extractHttpMethod(Map<String, Object> event) {
        // REST API Gateway commonly sends the method as a top-level "httpMethod".
        Object topLevelMethod = event.get("httpMethod");
        if (topLevelMethod != null) {
            return String.valueOf(topLevelMethod);
        }

        Object requestContextValue = event.get("requestContext");
        if (!(requestContextValue instanceof Map)) {
            return "";
        }

        Map<String, Object> requestContext = (Map<String, Object>) requestContextValue;
        Object httpValue = requestContext.get("http");
        if (httpValue instanceof Map) {
            // HTTP API Gateway v2 stores it under requestContext.http.method.
            Map<String, Object> http = (Map<String, Object>) httpValue;
            return String.valueOf(http.getOrDefault("method", ""));
        }

        // Fallback for other API Gateway shapes.
        return String.valueOf(requestContext.getOrDefault("httpMethod", ""));
    }

    private Map<String, Object> response(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // CORS headers let a separately hosted frontend call this Lambda API.
        headers.put("Access-Control-Allow-Origin", allowedOrigin());
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", headers);
        response.put("body", body);
        return response;
    }

    private String allowedOrigin() {
        String origin = System.getenv("ALLOWED_ORIGIN");
        if (origin == null || origin.trim().isEmpty()) {
            return "http://localhost:8080";
        }
        return origin.trim();
    }
}
