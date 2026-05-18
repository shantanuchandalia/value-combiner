package ui.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public final class ScreenshotUtil {
    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");

    private ScreenshotUtil() {
    }

    public static Path capture(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot)) {
            return null;
        }

        try {
            Files.createDirectories(SCREENSHOT_DIR);
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = SCREENSHOT_DIR.resolve(fileNameFor(testName));
            Files.copy(screenshot.toPath(), destination);
            return destination;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save screenshot for " + testName, e);
        }
    }

    private static String fileNameFor(String testName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        String safeName = testName.replaceAll("[^a-zA-Z0-9.-]", "_");
        return safeName + "-" + timestamp + ".png";
    }
}
