import java.util.ArrayList;
import java.util.List;

public class CalculationRequest {
    private List<String> inputs = new ArrayList<>();
    private String dataType;

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs == null ? new ArrayList<>() : inputs;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
