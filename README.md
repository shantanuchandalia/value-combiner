# Value Combiner

A Java app with a separate static frontend and Java backend that combines values by adding integers, adding decimals, or concatenating text.

## Project Structure

```text
Java-BAsics-practice/
+-- src/
|   +-- main/
|       +-- java/
|           +-- CalculationRequest.java
|           +-- CalculationResponse.java
|           +-- ValueCombiner.java
|           +-- ValueCombinerService.java
|           +-- ValueCombinerWebServer.java
|           +-- ValueCombinerLambdaHandler.java
|           +-- ValueCombinerReportGenerator.java
|           +-- ValueCombinerTestResult.java
|       +-- resources/
|           +-- static/
|               +-- index.html
|               +-- styles.css
|               +-- app.js
|               +-- config.js
+-- pom.xml
+-- README.md
```

## Requirements

- Java Development Kit (JDK) 17
- VS Code with Extension Pack for Java
- Maven, or the included Maven Wrapper

Make sure `JAVA_HOME` and `PATH` point to JDK 17 before running Maven. The build enforces this requirement.

On this Windows machine, JDK 17 is installed at:

```text
C:\Program Files\OpenLogic\jdk-17.0.18.8-hotspot
```

For the current PowerShell session, run:

```powershell
.\scripts\use-java17.ps1
```

Or set it manually:

```powershell
$env:JAVA_HOME = "C:\Program Files\OpenLogic\jdk-17.0.18.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

## Run The Dashboard Locally

The local development server starts at `http://localhost:8080`. It serves the static frontend and exposes the backend endpoint at `/api/calculate`.

Using Maven:

```bash
mvn compile exec:java
```

Using the Maven Wrapper on Windows:

```powershell
.\mvnw.cmd compile exec:java
```

Using the Maven Wrapper on macOS/Linux:

```bash
./mvnw compile exec:java
```

If port `8080` is already in use, stop the existing server or change the port in `ValueCombinerWebServer.java`.

You can also pass a different port:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.args=8081"
```

## Frontend

The frontend files are in `src/main/resources/static`:

- `index.html` contains the page structure.
- `styles.css` contains all styles.
- `app.js` contains browser behavior and API calls.
- `config.js` contains `window.VALUE_COMBINER_API_BASE_URL`.

For local development, `config.js` can stay empty because the frontend calls `/api/calculate` on the same origin. For AWS hosting, deploy the static files to S3/CloudFront and set `VALUE_COMBINER_API_BASE_URL` to your API Gateway base URL, for example:

```javascript
window.VALUE_COMBINER_API_BASE_URL = "https://your-api-id.execute-api.region.amazonaws.com";
```

## Backend For AWS Lambda

Deploy `ValueCombinerLambdaHandler` as the Lambda handler:

```text
ValueCombinerLambdaHandler::handleRequest
```

Connect it to API Gateway with a `POST /api/calculate` route. The handler also returns CORS headers for browser requests and supports base64-encoded API Gateway bodies.

Set `ALLOWED_ORIGIN` for deployed environments:

```text
ALLOWED_ORIGIN=https://your-cloudfront-domain
```

The backend uses request/response DTOs with Jackson:

- `CalculationRequest.java`
- `CalculationResponse.java`

Pass/exception counters are intentionally owned by the frontend session instead of the backend, keeping the Lambda API stateless.

## Run UI Automation Tests

The TestNG suite includes fast core/API tests plus Selenium Chrome UI smoke tests.

Start the local app first:

```powershell
.\mvnw.cmd compile exec:java
```

Then run the tests in another terminal:

```powershell
.\mvnw.cmd test
```

If the app is running on another port:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"
```

Default UI test settings live in `src/test/resources/test.properties`.
Calculation test data lives in `src/test/resources/calculation-test-data.properties`.

The Selenium suite is Chrome-only by design for this practice framework.

The suite also creates an ExtentReports HTML report at:

```text
target/extent-reports/extent-report.html
```

## Run Coverage

JaCoCo coverage is available through the `coverage` Maven profile. This profile runs the fast core/API tests only, so it does not require a browser or a running local web server.

```powershell
.\mvnw.cmd clean verify "-Pcoverage"
```

Coverage reports are generated at:

```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
target/site/jacoco/jacoco.csv
```

The coverage profile focuses on application logic and excludes local-server, Lambda-adapter, and static-report adapter classes from the report.

## Run Static Quality Checks

The `quality` Maven profile runs Checkstyle, PMD/CPD, and SpotBugs.

```powershell
.\mvnw.cmd verify "-Pquality"
```

Quality reports are generated under `target/` and should be reviewed before merging larger changes.

## Run Security Scan

The `security-scan` Maven profile runs OWASP Dependency-Check for known dependency vulnerabilities.

```powershell
.\mvnw.cmd verify "-Psecurity-scan"
```

Dependency-Check reports are generated under `target/`. High or critical findings should be fixed or explicitly documented with a reviewed suppression.

For faster NVD updates, set an API key before running the scan:

```powershell
$env:NVD_API_KEY = "your-nvd-api-key"
```

The profile reads `NVD_API_KEY` from the environment and reuses the local NVD cache for one week.

## Run SonarQube Or SonarCloud Analysis

Sonar analysis is optional locally because it requires a SonarQube/SonarCloud project and token.

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

## Generate The Static Report

`ValueCombinerReportGenerator.java` creates `value_combiner_results.html` from the built-in test cases.

```bash
mvn compile exec:java -Dexec.mainClass=ValueCombinerReportGenerator
```

## Main Files

- `ValueCombiner.java` contains the integer, decimal, and text combine logic.
- `CalculationRequest.java` and `CalculationResponse.java` define the API DTOs.
- `ValueCombinerService.java` contains the reusable calculation API behavior.
- `ValueCombinerWebServer.java` serves the static frontend and API endpoint for local development.
- `ValueCombinerLambdaHandler.java` adapts the backend service for AWS Lambda/API Gateway.
- `ValueCombinerReportGenerator.java` generates the static HTML test report.
- `ValueCombinerTestResult.java` stores rows for the generated test report.
