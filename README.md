# Value Combiner

A Java app with a separate static frontend and Java backend that combines values by adding integers, adding decimals, or concatenating text.

## Project Structure

```text
Java-BAsics-practice/
+-- src/
|   +-- main/
|       +-- java/
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

- Java Development Kit (JDK), Java 8 or higher
- VS Code with Extension Pack for Java
- Maven, or the included Maven Wrapper

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

Connect it to API Gateway with a `POST /api/calculate` route. The handler also returns CORS headers for browser requests.

## Run UI Automation Tests

The Selenium TestNG smoke suite is under `src/test/java/ui`.

Start the local app first:

```powershell
.\mvnw.cmd compile exec:java
```

Then run the UI tests in another terminal:

```powershell
.\mvnw.cmd test
```

If the app is running on another port:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"
```

Default UI test settings live in `src/test/resources/test.properties`.

The suite also creates an ExtentReports HTML report at:

```text
target/extent-reports/extent-report.html
```

## Generate The Static Report

`ValueCombinerReportGenerator.java` creates `value_combiner_results.html` from the built-in test cases.

```bash
mvn compile exec:java -Dexec.mainClass=ValueCombinerReportGenerator
```

## Main Files

- `ValueCombiner.java` contains the integer, decimal, and text combine logic.
- `ValueCombinerService.java` contains the reusable calculation API behavior.
- `ValueCombinerWebServer.java` serves the static frontend and API endpoint for local development.
- `ValueCombinerLambdaHandler.java` adapts the backend service for AWS Lambda/API Gateway.
- `ValueCombinerReportGenerator.java` generates the static HTML test report.
- `ValueCombinerTestResult.java` stores rows for the generated test report.
