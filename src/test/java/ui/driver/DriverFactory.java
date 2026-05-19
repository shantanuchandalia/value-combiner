package ui.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ui.config.TestConfig;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        return createChromeDriver();
    }

    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,900");
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        return new ChromeDriver(options);
    }
}
