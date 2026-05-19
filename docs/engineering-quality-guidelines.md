# Engineering And QA Guidelines For New Projects

Use this document as a reusable checklist when starting a new project. It captures the engineering, testing, reporting, and deployment discipline followed in the Value Combiner project.

## Java 17 Baseline

Use Java 17 as the default runtime and compile target for new Java projects.

Minimum expectations:

- Set Maven compiler release to `17`.
- Use a local JDK 17 for development and CI.
- Keep beginner-friendly code readable, but allow Java 17 language and runtime compatibility.
- Document the Java version in the README and build configuration.
- Avoid relying on machine-specific JDK defaults.

## 1. Start With A Clean Architecture

Separate responsibilities from the beginning:

- Keep frontend and backend code separate.
- Keep HTML, CSS, and JavaScript in separate files.
- Keep business logic independent from web server, Lambda, controller, or UI adapter code.
- Keep request and response models as explicit DTOs instead of passing unstructured maps or manually parsing strings.
- Keep test code under `src/test` and application code under `src/main`.

Recommended backend structure:

```text
src/main/java/
+-- model/request-response DTOs
+-- service/business logic
+-- local server/controller adapter
+-- cloud/Lambda adapter
+-- utility classes only when genuinely reusable
```

Recommended frontend structure:

```text
src/main/resources/static/
+-- index.html
+-- styles.css
+-- app.js
+-- config.js
```

## 2. Design For Local And Cloud Hosting

Build the application so it can run locally and also move to cloud hosting without rewriting core logic.

For web apps:

- Serve static frontend locally for development.
- Keep API calls configurable through `config.js`, environment variables, or system properties.
- Keep backend APIs stateless when possible.
- Avoid storing session counters or UI-only state in backend services unless the product requires it.
- Keep CORS configurable through an environment variable such as `ALLOWED_ORIGIN`.
- Treat local server classes and cloud handler classes as thin adapters around the same service layer.

For AWS-style deployment:

```text
Static frontend -> S3/CloudFront
API endpoint     -> API Gateway
Backend logic    -> Lambda
Shared logic     -> service classes
```

## 3. Use Explicit Data Contracts

Do not manually parse JSON with string operations.

Preferred approach:

- Create request DTOs.
- Create response DTOs.
- Use a JSON library such as Jackson.
- Validate mandatory request fields.
- Return consistent success and error response formats.

Example response shape:

```json
{
  "status": "success",
  "result": "5"
}
```

Error responses should also follow the same contract:

```json
{
  "status": "error",
  "result": "Exception: Need at least 2 inputs"
}
```

## 4. Keep Business Logic Testable

Business logic should be callable directly from unit tests without starting a server, browser, or cloud runtime.

Good signs:

- Core classes have small public methods.
- Service classes can be tested with plain Java tests.
- Web server and Lambda handlers only translate requests and responses.
- Browser tests are not the only way to validate logic.

Avoid:

- Putting calculations directly inside UI JavaScript only.
- Putting logic directly inside Lambda handler methods.
- Requiring Selenium to validate basic business rules.

## 5. Testing Strategy

Use a layered test approach.

### Unit Tests

Purpose:

- Validate pure business logic.
- Cover success, failure, boundary, and edge cases.
- Run fast without browser or server.

Examples:

- Integer addition.
- Integer overflow and underflow.
- Decimal addition.
- NaN and infinite decimal rejection.
- String concatenation.
- Null handling.

### Service/API Tests

Purpose:

- Validate request parsing.
- Validate response contracts.
- Validate API success and error behavior without browser automation.

Examples:

- Successful integer request returns `status=success`.
- Invalid number returns `status=error`.
- Malformed JSON returns `status=error`.
- Empty request body returns `status=error`.
- Unknown data type returns `status=error`.

### UI Smoke Tests

Purpose:

- Validate the user journey in the browser.
- Keep these focused and stable.

Examples:

- Page loads.
- Integer calculation works from UI.
- Decimal calculation works from UI.
- Text concatenation works from UI.
- Required validation message appears.
- Success and failure counters update.
- History drawer displays useful messages.

### UI Regression Tests

Purpose:

- Expand browser coverage after smoke tests are stable.
- Cover field validation, add/remove input behavior, history, and important edge cases.

## 6. UI Automation Framework Standards

Use Page Object Model for Selenium tests.

Recommended structure:

```text
src/test/java/ui/
+-- base/
|   +-- BaseTest.java
+-- config/
|   +-- TestConfig.java
+-- driver/
|   +-- DriverFactory.java
|   +-- DriverManager.java
+-- listeners/
|   +-- ExtentReportListener.java
+-- pages/
|   +-- FeaturePage.java
+-- tests/
|   +-- FeatureSmokeTest.java
+-- utils/
    +-- ScreenshotUtil.java
    +-- WaitUtil.java
    +-- TestData.java
```

Rules:

- Test classes should express business behavior, not Selenium mechanics.
- Locators belong in page objects.
- Common waits belong in wait utilities.
- Browser setup belongs in driver factory/base test classes.
- Test data should be externalized when it starts growing.
- Screenshots should be captured automatically on failure.
- Use headless mode for CI.
- Start with one browser for stability, then expand only when required.

## 7. Test Data Management

Keep small test data in properties files when key-value format is enough.

Example:

```properties
integer.addition.inputs=2,3
integer.addition.expected=5
decimal.addition.inputs=2.5,3.25
decimal.addition.expected=5.75
text.concatenation.inputs=Hello ,World
text.concatenation.expected=Hello World
```

Use a `TestData` utility class to load and expose named data objects.

Move to JSON, CSV, Excel, or database-backed test data only when the project genuinely needs it.

## 8. Reporting Standards

Every automation framework should produce human-readable and machine-readable reports.

Minimum reports:

- TestNG/Surefire reports under `target/surefire-reports`.
- ExtentReports HTML report under `target/extent-reports`.
- Screenshots under `target/screenshots`.
- JaCoCo coverage report under `target/site/jacoco`.

ExtentReports should include:

- Test name.
- Test status.
- Failure reason.
- Screenshot for UI failures.
- Browser/environment information.
- Suite or category information.

## 9. Coverage Standards

Use JaCoCo for Java line and branch coverage.

Important rule:

- JaCoCo measures `src/main` application code.
- Tests from `src/test` execute the application code.
- Test classes themselves should not be the coverage target.

Recommended command:

```powershell
.\mvnw.cmd clean verify "-Pcoverage"
```

Recommended coverage scope:

- Include service and business logic classes.
- Include DTOs if they are part of the API contract.
- Exclude local server adapters, Lambda adapters, report generators, and framework glue unless they contain meaningful logic.

Suggested targets:

- Line coverage: `85%+`
- Branch coverage: `70%+`

Do not chase 100% blindly. Evaluate every missed line:

- Is it business critical?
- Is it an error path users may hit?
- Is it a boundary condition?
- Is it adapter code better covered by integration tests?
- Is it generated/simple getter-setter code with low risk?

## 10. Static Analysis Standards

Use static analysis to catch defects, duplication, and maintainability issues before review.

For Java projects, include:

- Checkstyle for coding conventions.
- PMD for code smells, complexity, and maintainability issues.
- CPD for duplicated logic.
- SpotBugs for likely defects and risky implementation patterns.
- SonarQube or SonarCloud for consolidated code quality, duplication, coverage, and maintainability tracking.

Minimum expectations:

- No blocker or critical issues.
- No high-priority SpotBugs findings unless explicitly justified.
- No duplicated logic above the agreed threshold without review.
- Complexity warnings reviewed before merge.
- AI-generated code must receive extra scrutiny for duplicate methods, overlong classes, dead code, and weak abstractions.

Recommended commands:

```powershell
.\mvnw.cmd verify "-Pquality"
```

Optional SonarQube/SonarCloud command:

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

## 11. Security Quality Checklist

Treat security as a shift-left activity, especially for cloud-hosted apps.

Minimum checklist:

- Validate and sanitize external inputs.
- Avoid secrets, tokens, passwords, and credentials in source control.
- Validate authentication and authorization on protected APIs.
- Scan dependencies for known vulnerabilities.
- Review API error responses for stack traces or sensitive internal details.
- Review logs for sensitive data leakage.
- Use OWASP Dependency-Check or an equivalent dependency scanner.
- Document any vulnerability suppression with a reason and expiry date.

Recommended command:

```powershell
.\mvnw.cmd verify "-Psecurity-scan"
```

For IAM/auth-heavy applications, also cover:

- Role and permission boundaries.
- Session expiry and token validation.
- Unauthorized and forbidden access paths.
- Least-privilege cloud permissions.

## 12. Logging And Observability Standards

Cloud-native applications need logs that support debugging, operations, and audit review.

Guidelines:

- Prefer structured logs for production services.
- Include correlation IDs or request IDs for API flows.
- Use consistent severity levels such as `INFO`, `WARN`, and `ERROR`.
- Do not log secrets, tokens, passwords, PII, or full authentication headers.
- Keep logs CloudWatch/Splunk-ready by using predictable fields and timestamps.
- Preserve enough context to investigate failures without exposing sensitive data.
- For Lambda/API Gateway systems, pass or create a request correlation ID at the edge and include it in downstream logs.

Do not force a heavy production logging framework into a small practice app unless the app needs it. Add the standard before production deployment.

## 13. Boundary And Edge Case Discipline

For every feature, think beyond the happy path.

Include:

- Minimum valid input.
- Maximum valid input.
- Empty input.
- Null input where applicable.
- Invalid format.
- Multiple values.
- Large values.
- Overflow/underflow.
- Special decimal values such as NaN and infinity.
- Duplicate submissions.
- User-visible success and failure messages.

For UI flows, validate both:

- The calculated result.
- The message or state shown to the user.

## 14. Configuration Over Hardcoding

Values that change across environments should not be hardcoded.

Make these configurable:

- Base URL.
- Headless mode.
- Browser timeout.
- API base URL.
- Allowed CORS origin.
- Report output paths if needed.
- Email/reporting credentials, if implemented later.

Prefer this order:

1. System property or environment variable.
2. Test/application properties file.
3. Safe local default.

## 15. Error Handling Standards

Good error handling should be predictable and testable.

Guidelines:

- Validate inputs early.
- Return consistent error response formats.
- Do not leak stack traces to the frontend.
- Log useful server-side details.
- Keep user-facing messages clear.
- Test both success and failure responses.

## 16. Code Quality Standards

Follow these rules before calling a feature complete:

- Keep methods small and focused.
- Give classes one clear responsibility.
- Prefer explicit names over clever names.
- Avoid duplicated logic.
- Avoid manual string parsing when libraries exist.
- Keep comments meaningful and limited.
- Do not mix unrelated refactors with feature changes.
- Do not leave temporary debug code.
- Keep generated files and reports out of source control unless intentionally needed.
- Run static analysis before considering a feature complete.

## 17. Review Checklist

Before merging or pushing a change, check:

- Does the app still compile?
- Do unit and service tests pass?
- Do UI smoke tests pass where applicable?
- Do static quality checks pass or have reviewed findings?
- Has dependency/security scanning been reviewed?
- Are logs free of secrets and sensitive data?
- Are reports generated correctly?
- Is coverage acceptable for the changed area?
- Is test data externalized if it is likely to grow?
- Are frontend, backend, and test responsibilities separated?
- Are environment-specific values configurable?
- Is the README updated with new commands or setup steps?
- Are docs updated when architecture changes?

## 18. Suggested Maven Commands

Compile:

```powershell
.\mvnw.cmd test-compile
```

Run all configured tests:

```powershell
.\mvnw.cmd test
```

Run fast core/API tests:

```powershell
.\mvnw.cmd "-Dtest=ValueCombinerUnitTest,ValueCombinerServiceTest" test
```

Run coverage:

```powershell
.\mvnw.cmd clean verify "-Pcoverage"
```

Run static quality checks:

```powershell
.\mvnw.cmd verify "-Pquality"
```

Run dependency/security scan:

```powershell
.\mvnw.cmd verify "-Psecurity-scan"
```

Run SonarQube/SonarCloud analysis:

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Start local app:

```powershell
.\mvnw.cmd compile exec:java
```

Start local app on a different port:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.args=8081"
```

Run UI tests against a different URL:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"
```

## 19. Definition Of Done

A feature is done when:

- The implementation works locally.
- Unit/service tests cover core logic.
- UI tests cover the critical user journey if the feature is user-facing.
- Failure paths are tested where meaningful.
- Coverage has been reviewed, not just generated.
- Static analysis has been run and reviewed.
- Dependency/security scan findings are fixed or documented.
- Logging and error handling do not expose sensitive data.
- Reports are available.
- Documentation is updated.
- The code is committed with a clear message.

## 20. Recommended First-Cut Project Setup

For any new Java web project, start with:

- Maven wrapper.
- Clear `src/main` and `src/test` separation.
- Separate frontend files.
- Service layer for business logic.
- DTOs for request/response contracts.
- Local web server or controller adapter.
- Cloud adapter if deployment target is known.
- TestNG or JUnit test suite.
- Selenium Page Object framework if UI testing is needed.
- ExtentReports for UI reporting.
- JaCoCo profile for coverage.
- Checkstyle, PMD, CPD, and SpotBugs profiles for static quality.
- OWASP Dependency-Check profile for dependency scanning.
- Logging and observability standards before cloud deployment.
- README with run, test, coverage, and deployment instructions.

Build the smallest useful version first, then expand coverage and framework capability as the app grows.
