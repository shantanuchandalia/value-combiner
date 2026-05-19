import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class ValueCombinerService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String calculate(String jsonBody) {
        CalculationResponse response;
        try {
            CalculationRequest request = parseRequest(jsonBody);
            Object result = processCalculation(request.getInputs(), request.getDataType());
            response = new CalculationResponse("success", result.toString());
        } catch (Exception e) {
            response = new CalculationResponse("error", "Exception: " + e.getMessage());
        }

        return toJson(response);
    }

    private static CalculationRequest parseRequest(String jsonBody) throws JsonProcessingException {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        return OBJECT_MAPPER.readValue(jsonBody, CalculationRequest.class);
    }

    private static String toJson(CalculationResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize calculation response", e);
        }
    }

    private static Object processCalculation(List<String> inputs, String dataType) throws Exception {
        ValueCombiner combiner = new ValueCombiner();

        if (inputs == null || inputs.size() < 2) {
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
}
