# Selenium TestNG UI Automation Framework Architecture

This document proposes a UI automation framework for the Value Combiner app using Selenium WebDriver, TestNG, and Maven. It is intentionally a review document first; implementation can be added after this structure is approved.

## Goals

- Test the browser UI for integer, decimal, text, and validation flows.
- Keep tests readable through Page Object Model.
- Support local execution against `ValueCombinerWebServer`.
- Support future execution against deployed AWS frontend/API URLs.
- Generate useful reports and screenshots on failure.
- Keep framework code separate from application code.

## Proposed Test Scope

Initial UI regression scenarios:

- Page loads successfully.
- Integer calculation returns expected sum.
- Decimal calculation returns expected sum.
- Text calculation returns expected concatenated value.
- Empty input validation displays the expected alert.
- Add input button allows up to four inputs.
- Remove input button appears after more than two inputs.
- Exception count increments when invalid data is submitted.
- Success count increments when valid data is submitted.
- History drawer shows success and exception messages.

## Proposed Directory Structure

```text
src/
+-- test/
    +-- java/
    |   +-- ui/
    |       +-- base/
    |       |   +-- BaseTest.java
    |       +-- config/
    |       |   +-- TestConfig.java
    |       +-- driver/
    |       |   +-- DriverFactory.java
    |       |   +-- DriverManager.java
    |       +-- pages/
    |       |   +-- ValueCombinerPage.java
    |       +-- tests/
    |       |   +-- ValueCombinerCalculationTest.java
    |       |   +-- ValueCombinerValidationTest.java
    |       |   +-- ValueCombinerHistoryTest.java
    |       +-- utils/
    |           +-- ScreenshotUtil.java
    |           +-- WaitUtil.java
    +-- resources/
        +-- testng.xml
        +-- test.properties
```

## Architecture Layers

```text
TestNG test classes
        |
        v
Page objects
        |
        v
Reusable Selenium utilities
        |
        v
Driver factory and config
        |
        v
Browser
        |
        v
Value Combiner UI
```

## Component Responsibilities

### `BaseTest`

Owns common test setup and teardown.

Responsibilities:

- Start browser before each test method.
- Navigate to base URL.
- Quit browser after each test method.
- Capture screenshot when a test fails.
- Keep test setup consistent across all UI tests.

### `DriverFactory`

Creates Selenium `WebDriver` instances.

Responsibilities:

- Create Chrome, Edge, or Firefox drivers.
- Support headless mode.
- Apply common browser options.
- Avoid test classes directly constructing browser drivers.

### `DriverManager`

Stores the current thread's `WebDriver`.

Responsibilities:

- Make driver access consistent.
- Support future parallel execution.
- Prevent tests from sharing the same browser session accidentally.

### `TestConfig`

Reads test configuration.

Responsibilities:

- Read values from `test.properties`.
- Allow override from Maven command-line system properties.
- Provide values such as browser, base URL, timeout, and headless mode.

Example properties:

```properties
baseUrl=http://localhost:8080
browser=chrome
headless=false
timeoutSeconds=10
```

### `ValueCombinerPage`

Represents the browser page.

Responsibilities:

- Hold locators for buttons, inputs, result area, counters, and drawer.
- Provide user-level methods such as:
  - `selectIntegerType()`
  - `selectDecimalType()`
  - `selectTextType()`
  - `enterValues(String... values)`
  - `clickCalculate()`
  - `getResultText()`
  - `getSuccessCount()`
  - `getExceptionCount()`
  - `openSuccessHistory()`
  - `openExceptionHistory()`

Tests should not directly use raw Selenium locators unless there is a good reason.

### Test Classes

`ValueCombinerCalculationTest`

- Valid integer addition.
- Valid decimal addition.
- Valid text concatenation.
- Three-value and four-value calculations.

`ValueCombinerValidationTest`

- Empty input alert.
- Invalid integer input.
- Invalid decimal input.
- Add/remove input behavior.

`ValueCombinerHistoryTest`

- Success counter updates.
- Exception counter updates.
- Drawer displays correct messages.

### Utilities

`WaitUtil`

- Central wrapper around `WebDriverWait`.
- Keeps explicit waits consistent.

`ScreenshotUtil`

- Saves screenshots under `target/screenshots`.
- Names screenshots by test method and timestamp.

## Maven Dependencies To Add Later

Proposed dependencies:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.x.x</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.x.x</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.x.x</version>
    <scope>test</scope>
</dependency>
```

Proposed plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <suiteXmlFiles>
            <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
        </suiteXmlFiles>
    </configuration>
</plugin>
```

Exact versions should be finalized when we implement, based on the Java version used by the project and available dependency compatibility.

## Proposed `testng.xml`

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Value Combiner UI Suite" parallel="false">
    <test name="UI Regression">
        <classes>
            <class name="ui.tests.ValueCombinerCalculationTest"/>
            <class name="ui.tests.ValueCombinerValidationTest"/>
            <class name="ui.tests.ValueCombinerHistoryTest"/>
        </classes>
    </test>
</suite>
```

Parallel execution can be enabled later after the framework is stable:

```xml
<suite name="Value Combiner UI Suite" parallel="methods" thread-count="2">
```

## Execution Model

Local execution flow:

1. Start the app:

```powershell
.\mvnw.cmd compile exec:java
```

2. Run UI tests in another terminal:

```powershell
.\mvnw.cmd test
```

Override browser or URL:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dbrowser=edge" "-Dheadless=true"
```

AWS execution flow:

```powershell
.\mvnw.cmd test "-DbaseUrl=https://your-cloudfront-url" "-Dheadless=true"
```

## Reporting

Initial reporting:

- TestNG default reports under `target/surefire-reports`.
- Screenshots under `target/screenshots`.
- ExtentReports HTML report under `target/extent-reports/extent-report.html`.

Possible future reporting:

- Allure for CI-friendly reporting and history.

Recommendation: keep TestNG default reports for raw output and use ExtentReports for the human-readable smoke test report.

## CI/CD Readiness

For GitHub Actions later:

- Run tests in headless Chrome.
- Start the local server before UI tests, or test against a deployed environment.
- Publish surefire reports and screenshots as workflow artifacts.

Suggested pipeline stages:

```text
compile
unit/API checks
start local app
ui regression tests
publish reports
```

## Design Principles

- Page Object Model: keeps locators and UI actions out of test classes.
- Single Responsibility: driver creation, config, page behavior, and tests stay separate.
- Reusability: common setup, waits, and screenshots are shared.
- Configuration Over Hardcoding: base URL, browser, and headless mode are configurable.
- Fail Fast With Evidence: failed tests capture screenshots.
- Local And Cloud Friendly: same suite can run against localhost or deployed AWS URL.

## First Implementation Milestone

Recommended first cut:

- Add Maven dependencies and Surefire plugin.
- Add `testng.xml`.
- Add `test.properties`.
- Add `DriverFactory`, `DriverManager`, `BaseTest`, and `ValueCombinerPage`.
- Add three smoke tests:
  - integer addition
  - decimal addition
  - text concatenation

After those pass, expand into validation and history tests.
