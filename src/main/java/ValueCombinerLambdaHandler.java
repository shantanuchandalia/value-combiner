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

        // API Gateway stores the JSON request payload in the "body" field.
        Object bodyValue = event.get("body");
        String body = bodyValue == null ? "{}" : String.valueOf(bodyValue);

        // Keep calculation logic outside the Lambda adapter so it can be reused locally.
        String responseJson = ValueCombinerService.calculate(body);
        return response(200, responseJson);
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
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", headers);
        response.put("body", body);
        return response;
    }
}
