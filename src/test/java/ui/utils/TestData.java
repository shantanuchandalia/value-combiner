package ui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestData {
    private static final String DATA_FILE = "/calculation-test-data.properties";
    private static final Properties TEST_DATA = loadProperties();

    public static final CalculationData INTEGER_ADDITION =
            calculationData("integer.addition");

    public static final CalculationData DECIMAL_ADDITION =
            calculationData("decimal.addition");

    public static final CalculationData TEXT_CONCATENATION =
            calculationData("text.concatenation");

    private TestData() {
    }

    private static CalculationData calculationData(String keyPrefix) {
        return new CalculationData(
                property(keyPrefix + ".inputs").split(",", -1),
                property(keyPrefix + ".expected"));
    }

    private static String property(String key) {
        String value = TEST_DATA.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing test data property: " + key);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestData.class.getResourceAsStream(DATA_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Missing test data file: " + DATA_FILE);
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load test data file: " + DATA_FILE, e);
        }
    }

    public static final class CalculationData {
        private final String[] inputs;
        private final String expectedResult;

        private CalculationData(String[] inputs, String expectedResult) {
            this.inputs = inputs.clone();
            this.expectedResult = expectedResult;
        }

        public String[] inputs() {
            return inputs.clone();
        }

        public String expectedResult() {
            return expectedResult;
        }
    }
}
