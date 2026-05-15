import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValueCombinerService {
    private static int passCount = 0;
    private static int exceptionCount = 0;

    public static String calculate(String jsonBody) {
        List<String> inputs = parseJsonArray(jsonBody, "inputs");
        String dataType = parseJsonString(jsonBody, "dataType");

        String status;
        String resultText;
        try {
            Object result = processCalculation(inputs, dataType);
            status = "success";
            resultText = result.toString();
            passCount++;
        } catch (Exception e) {
            status = "error";
            resultText = "Exception: " + e.getMessage();
            exceptionCount++;
        }

        return "{"
            + "\"status\": \"" + escapeJson(status) + "\","
            + "\"result\": \"" + escapeJson(resultText) + "\","
            + "\"passCount\": " + passCount + ","
            + "\"exceptionCount\": " + exceptionCount
            + "}";
    }

    private static Object processCalculation(List<String> inputs, String dataType) throws Exception {
        ValueCombiner combiner = new ValueCombiner();

        if (inputs.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 inputs");
        }

        if ("integer".equals(dataType)) {
            int result = Integer.parseInt(inputs.get(0));
            for (int i = 1; i < inputs.size(); i++) {
                result = combiner.add(result, Integer.parseInt(inputs.get(i)));
            }
            return result;
        } else if ("double".equals(dataType)) {
            double result = Double.parseDouble(inputs.get(0));
            for (int i = 1; i < inputs.size(); i++) {
                result = combiner.add(result, Double.parseDouble(inputs.get(i)));
            }
            return result;
        } else if ("string".equals(dataType)) {
            String result = inputs.get(0);
            for (int i = 1; i < inputs.size(); i++) {
                result = combiner.add(result, inputs.get(i));
            }
            return result;
        }

        throw new IllegalArgumentException("Unknown data type: " + dataType);
    }

    private static List<String> parseJsonArray(String body, String key) {
        String startKey = "\"" + key + "\"";
        int index = body.indexOf(startKey);
        if (index < 0) {
            return Collections.emptyList();
        }
        int arrayStart = body.indexOf('[', index);
        int arrayEnd = body.indexOf(']', arrayStart);
        if (arrayStart < 0 || arrayEnd < 0) {
            return Collections.emptyList();
        }
        String arrayBody = body.substring(arrayStart + 1, arrayEnd).trim();
        List<String> values = new ArrayList<>();
        if (arrayBody.isEmpty()) {
            return values;
        }
        boolean inString = false;
        boolean escaped = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < arrayBody.length(); i++) {
            char c = arrayBody.charAt(i);
            if (escaped) {
                current.append(unescapeJsonCharacter(c));
                escaped = false;
                continue;
            }
            if (inString && c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (c == ',' && !inString) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            if (inString || (!inString && c != ' ')) {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            values.add(current.toString());
        }
        return values;
    }

    private static char unescapeJsonCharacter(char c) {
        if (c == 'n') {
            return '\n';
        }
        if (c == 'r') {
            return '\r';
        }
        if (c == 't') {
            return '\t';
        }
        return c;
    }

    private static String parseJsonString(String body, String key) {
        String startKey = "\"" + key + "\"";
        int index = body.indexOf(startKey);
        if (index < 0) {
            return "";
        }
        int colon = body.indexOf(':', index);
        int quoteStart = body.indexOf('"', colon + 1);
        if (quoteStart < 0) {
            return "";
        }
        int quoteEnd = body.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) {
            return "";
        }
        return body.substring(quoteStart + 1, quoteEnd);
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
