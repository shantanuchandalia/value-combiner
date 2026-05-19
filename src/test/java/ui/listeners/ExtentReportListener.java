package ui.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import ui.config.TestConfig;
import ui.driver.DriverManager;
import ui.utils.ScreenshotUtil;

public class ExtentReportListener implements ITestListener {
    private static final Path REPORT_PATH = Paths.get("target", "extent-reports", "extent-report.html");
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static ExtentReports extentReports;

    @Override
    public synchronized void onStart(ITestContext context) {
        if (extentReports != null) {
            return;
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH.toString());
        sparkReporter.config().setDocumentTitle("Value Combiner UI Automation Report");
        sparkReporter.config().setReportName("Value Combiner Automation Tests");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Suite", context.getSuite().getName());
        extentReports.setSystemInfo("Base URL", TestConfig.baseUrl());
        extentReports.setSystemInfo("Browser", "chrome");
        extentReports.setSystemInfo("Headless", String.valueOf(TestConfig.headless()));
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extentReports.createTest(result.getMethod().getMethodName());
        test.assignCategory(result.getTestClass().getName());
        test.assignCategory(result.getTestContext().getName());
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().log(Status.PASS, "Test passed.");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        test.log(Status.FAIL, result.getThrowable());
        attachScreenshotIfAvailable(test, result);
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        CURRENT_TEST.get().log(Status.SKIP, result.getThrowable());
        CURRENT_TEST.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    private void attachScreenshotIfAvailable(ExtentTest test, ITestResult result) {
        try {
            WebDriver driver = DriverManager.getDriver();
            Path screenshotPath = ScreenshotUtil.capture(driver, result.getMethod().getMethodName());
            if (screenshotPath != null) {
                test.addScreenCaptureFromPath(screenshotPath.toString(), "Failure screenshot");
            }
        } catch (Exception e) {
            test.log(Status.WARNING, "Unable to attach screenshot: " + e.getMessage());
        }
    }
}
