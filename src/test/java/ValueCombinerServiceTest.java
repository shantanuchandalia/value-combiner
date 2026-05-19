import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValueCombinerServiceTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void shouldReturnSuccessfulCalculationResponse() throws Exception {
        CalculationResponse response = calculate("{\"inputs\":[\"2\",\"3\"],\"dataType\":\"integer\"}");

        Assert.assertEquals(response.getStatus(), "success");
        Assert.assertEquals(response.getResult(), "5");
    }

    @Test
    public void shouldReturnErrorResponseForInvalidCalculation() throws Exception {
        CalculationResponse response = calculate("{\"inputs\":[\"abc\",\"3\"],\"dataType\":\"integer\"}");

        Assert.assertEquals(response.getStatus(), "error");
        Assert.assertTrue(response.getResult().contains("Exception:"));
    }

    @Test
    public void shouldReturnErrorResponseForMalformedJson() throws Exception {
        CalculationResponse response = calculate("{not-json}");

        Assert.assertEquals(response.getStatus(), "error");
        Assert.assertTrue(response.getResult().contains("Exception:"));
    }

    private CalculationResponse calculate(String requestJson) throws Exception {
        String responseJson = ValueCombinerService.calculate(requestJson);
        return OBJECT_MAPPER.readValue(responseJson, CalculationResponse.class);
    }
}
