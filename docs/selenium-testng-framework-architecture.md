# Selenium TestNG UI Automation Framework Architecture

This document describes the implemented automation framework for the Value Combiner app using Selenium WebDriver, TestNG, Maven, and ExtentReports.

## Goals

- Test core calculation logic, API/service behavior, and browser UI flows.
- Keep tests readable through Page Object Model.
- Support local execution against `ValueCombinerWebServer`.
- Support future execution against deployed AWS frontend/API URLs.
- Generate useful reports and screenshots on failure.
- Keep framework code separate from application code.

## Proposed Test Scope

Initial automated scope:

- Core integer, decimal, and text calculation checks.
- API/service success and error response checks.
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
    |   +-- ValueCombinerUnitTest.java
    |   +-- ValueCombinerServiceTest.java
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
        +-- calculation-test-data.properties
        +-- testng.xml
        +-- test.properties
```

## Architecture Layers

```text
Core/API TestNG tests and UI TestNG tests
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
- Leave screenshot capture to the ExtentReports listener.
- Keep test setup consistent across all UI tests.

### `DriverFactory`

Creates Selenium `WebDriver` instances.

Responsibilities:

- Create ChromeDriver instances.
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
- Provide values such as base URL, timeout, and headless mode.

Example properties:

```properties
baseUrl=http://localhost:8080
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

`ValueCombinerUnitTest`

- Core integer addition.
- Overflow handling.
- Decimal addition.
- Invalid decimal handling.
- Text concatenation.
- Null text handling.

`ValueCombinerServiceTest`

- Successful API response.
- Invalid calculation error response.
- Malformed JSON error response.

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

`TestData`

- Loads calculation data from `src/test/resources/calculation-test-data.properties`.
- Exposes named data objects to tests.

`WaitUtil`

- Central wrapper around `WebDriverWait`.
- Keeps explicit waits consistent.

`ScreenshotUtil`

- Saves screenshots under `target/screenshots`.
- Names screenshots by test method and timestamp.

## Maven Dependencies

Primary dependencies:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-chrome-driver</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-support</artifactId>
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
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <scope>test</scope>
</dependency>
```

The main app also uses Jackson for DTO-based JSON parsing and serialization.

Surefire runs TestNG through `src/test/resources/testng.xml`:

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

## Current `testng.xml`

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Value Combiner UI Suite" parallel="false">
    <listeners>
        <listener class-name="ui.listeners.ExtentReportListener"/>
    </listeners>
    <test name="Core and API Tests">
        <classes>
            <class name="ValueCombinerUnitTest"/>
            <class name="ValueCombinerServiceTest"/>
        </classes>
    </test>
    <test name="UI Smoke Tests">
        <classes>
            <class name="ui.tests.ValueCombinerCalculationTest"/>
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

Override URL or headless mode:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"
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
- JaCoCo line and branch coverage under `target/site/jacoco`.

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
coverage checks
start local app
ui regression tests
publish reports
```

## Coverage

Coverage uses JaCoCo through the Maven `coverage` profile:

```powershell
.\mvnw.cmd clean verify "-Pcoverage"
```

The coverage profile runs `coverage-testng.xml`, which includes the core and API tests only. Browser UI tests remain part of the regular `testng.xml` suite because Selenium execution validates end-to-end behavior rather than contributing stable Java line coverage.

Initial report paths:

```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
target/site/jacoco/jacoco.csv
```

## Design Principles

- Page Object Model: keeps locators and UI actions out of test classes.
- Single Responsibility: driver creation, config, page behavior, and tests stay separate.
- Reusability: common setup, waits, and screenshots are shared.
- Configuration Over Hardcoding: base URL, headless mode, and test data are configurable.
- Fail Fast With Evidence: failed tests capture screenshots.
- Local And Cloud Friendly: same suite can run against localhost or deployed AWS URL.

## First Implementation Milestone

Implemented first cut:

- Add Maven dependencies and Surefire plugin.
- Add `testng.xml`.
- Add `test.properties`.
- Add `DriverFactory`, `DriverManager`, `BaseTest`, and `ValueCombinerPage`.
- Add core/API checks and three UI smoke tests:
  - integer addition
  - decimal addition
  - text concatenation

Next expansion: validation tests, history drawer tests, CI execution, and deployed-environment smoke runs.
