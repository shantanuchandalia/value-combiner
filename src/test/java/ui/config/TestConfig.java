package ui.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {
    private static final String CONFIG_FILE = "/test.properties";
    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String baseUrl() {
        return get("baseUrl", "http://localhost:8080");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    public static int timeoutSeconds() {
        return Integer.parseInt(get("timeoutSeconds", "10"));
    }

    private static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }
        return PROPERTIES.getProperty(key, defaultValue).trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load " + CONFIG_FILE, e);
        }
        return properties;
    }
}
