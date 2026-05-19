# Engineering And QA Guidelines For New Projects

Use this document as a reusable checklist when starting a new project. It captures the engineering, testing, reporting, and deployment discipline followed in the Value Combiner project.

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

## 10. Boundary And Edge Case Discipline

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

## 11. Configuration Over Hardcoding

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

## 12. Error Handling Standards

Good error handling should be predictable and testable.

Guidelines:

- Validate inputs early.
- Return consistent error response formats.
- Do not leak stack traces to the frontend.
- Log useful server-side details.
- Keep user-facing messages clear.
- Test both success and failure responses.

## 13. Code Quality Standards

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

## 14. Review Checklist

Before merging or pushing a change, check:

- Does the app still compile?
- Do unit and service tests pass?
- Do UI smoke tests pass where applicable?
- Are reports generated correctly?
- Is coverage acceptable for the changed area?
- Is test data externalized if it is likely to grow?
- Are frontend, backend, and test responsibilities separated?
- Are environment-specific values configurable?
- Is the README updated with new commands or setup steps?
- Are docs updated when architecture changes?

## 15. Suggested Maven Commands

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

## 16. Definition Of Done

A feature is done when:

- The implementation works locally.
- Unit/service tests cover core logic.
- UI tests cover the critical user journey if the feature is user-facing.
- Failure paths are tested where meaningful.
- Coverage has been reviewed, not just generated.
- Reports are available.
- Documentation is updated.
- The code is committed with a clear message.

## 17. Recommended First-Cut Project Setup

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
- README with run, test, coverage, and deployment instructions.

Build the smallest useful version first, then expand coverage and framework capability as the app grows.
