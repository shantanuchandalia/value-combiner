public class ValueCombinerTestResult {
    private String category;
    private String testName;
    private String input;
    private String result;
    private String status;
    
    public ValueCombinerTestResult(String category, String testName, String input, String result, String status) {
        this.category = category;
        this.testName = testName;
        this.input = input;
        this.result = result;
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public String getInput() {
        return input;
    }
    
    public String getResult() {
        return result;
    }
    
    public String getStatus() {
        return status;
    }
    
    @Override
    public String toString() {
        return "{" +
                "\"category\":\"" + category + "\"," +
                "\"testName\":\"" + testName + "\"," +
                "\"input\":\"" + input + "\"," +
                "\"result\":\"" + result + "\"," +
                "\"status\":\"" + status + "\"" +
                "}";
    }
}
