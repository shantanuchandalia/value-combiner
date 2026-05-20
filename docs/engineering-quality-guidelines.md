# Engineering Quality Guidelines

Use this as a crisp baseline for new Java projects. Keep it practical: start small, keep responsibilities clear, automate the important checks, and expand only when the project needs it.

## 1. Project Baseline

- Use Java 17 as the default runtime and compile target.
- Use the Maven Wrapper so builds are repeatable across machines.
- Document required Java, Maven, and environment setup in `README.md`.
- Keep application code in `src/main` and test code in `src/test`.
- Avoid machine-specific paths, ports, tokens, or credentials in source code.
- Keep generated reports, screenshots, build output, and temporary files out of source control unless intentionally published.

## 2. Architecture

Separate responsibilities early:

- Put business logic in service/domain classes.
- Keep web server, controller, Lambda, CLI, or UI code as thin adapters.
- Use explicit request and response DTOs for API boundaries.
- Keep frontend files separated into HTML, CSS, JavaScript, and config.
- Create utility classes only when the behavior is genuinely reused.
- Avoid mixing unrelated refactors with feature work.

Recommended Java layout:

```text
src/main/java/
+-- model/        request and response DTOs
+-- service/      business logic
+-- adapter/      web, Lambda, CLI, persistence, or framework glue
+-- config/       configuration loading
```

Recommended static frontend layout:

```text
src/main/resources/static/
+-- index.html
+-- styles.css
+-- app.js
+-- config.js
```

## 3. Configuration

Anything that changes by environment must be configurable:

- App port and base URL.
- API base URL.
- Browser/headless mode and timeouts.
- Allowed CORS origin.
- Report output locations when needed.
- Tokens, credentials, and secret values.

Preferred order:

1. Environment variable or system property.
2. Application/test properties file.
3. Safe local default.

Never commit real secrets.

## 4. API Contracts

- Do not parse JSON with string operations.
- Use a JSON library such as Jackson.
- Validate mandatory request fields before processing.
- Return consistent success and error responses.
- Keep user-facing errors clear and avoid exposing stack traces.
- Test malformed, empty, invalid, and unknown request cases.

Example:

```json
{
  "status": "success",
  "result": "5"
}
```

```json
{
  "status": "error",
  "result": "Need at least 2 inputs"
}
```

## 5. Testability

Business logic must be testable without a browser, server, database, or cloud runtime.

Good signs:

- Core methods are small and deterministic.
- Service tests call Java classes directly.
- Adapters translate requests/responses but do not own business rules.
- UI tests validate user journeys, not basic calculation or domain rules.

Avoid:

- Business logic only in JavaScript.
- Business logic inside Lambda/controller methods.
- Selenium as the only way to verify core behavior.

## 6. Testing Strategy

Use layered tests.

### Unit Tests

Cover pure business rules:

- Happy paths.
- Boundary values.
- Empty and null inputs where applicable.
- Invalid formats.
- Overflow/underflow.
- Rounding or precision behavior.
- Duplicate or repeated operations where relevant.

### Service/API Tests

Cover request and response behavior:

- Successful request returns the expected contract.
- Invalid input returns a predictable error.
- Malformed JSON is handled safely.
- Empty request body is handled safely.
- Unknown enum/type/action values are rejected.

### UI Smoke Tests

Keep these few and stable:

- Page loads.
- Critical user journey works.
- Required validation is visible.
- Success and failure states render correctly.

### UI Regression Tests

Add after smoke tests are stable:

- Field validation combinations.
- Add/remove/edit flows.
- History, filters, sorting, or saved state.
- Important browser-specific behavior.

## 7. UI Automation Standards

Use Page Object Model for Selenium tests.

Recommended layout:

```text
src/test/java/ui/
+-- base/
+-- config/
+-- driver/
+-- listeners/
+-- pages/
+-- tests/
+-- utils/
```

Rules:

- Test classes describe behavior, not Selenium mechanics.
- Locators live in page objects.
- Common waits live in wait utilities.
- Browser setup lives in driver/base classes.
- Screenshots are captured on UI failure.
- Use headless mode in CI.
- Start with one browser; expand only when required.

## 8. Test Data

- Keep small key-value data in `.properties` files.
- Use JSON or CSV when data becomes nested or table-shaped.
- Use Excel or database-backed data only when the project truly needs it.
- Keep test data named by scenario, not by implementation detail.
- Avoid hardcoding growing datasets inside test methods.

Example:

```properties
integer.addition.inputs=2,3
integer.addition.expected=5
text.concat.inputs=Hello ,World
text.concat.expected=Hello World
```

## 9. Reports And Evidence

Every serious automation setup should produce readable evidence.

Minimum outputs:

- Surefire/TestNG reports under `target/surefire-reports`.
- UI HTML report under `target/extent-reports` when UI tests exist.
- Failure screenshots under `target/screenshots` when UI tests exist.
- JaCoCo coverage report under `target/site/jacoco`.
- Static analysis and dependency scan reports under `target`.

Reports should show:

- Test name and status.
- Failure reason.
- Screenshot for UI failures.
- Browser/environment details for UI runs.
- Suite/category information when useful.

## 10. Coverage

Use JaCoCo for Java line and branch coverage.

Principles:

- Measure `src/main` application code, not test classes.
- Prioritize service/domain classes and API contracts.
- Exclude adapters, generated code, report generators, and framework glue unless they contain meaningful logic.
- Review missed coverage instead of chasing 100%.

Suggested targets:

- Line coverage: `85%+`
- Branch coverage: `70%+`

Ask for every meaningful miss:

- Is it business critical?
- Is it an error path users may hit?
- Is it a boundary condition?
- Is it better covered by integration/UI tests?

## 11. Static Analysis

For Java projects, use:

- Checkstyle for style and conventions.
- PMD for maintainability and complexity issues.
- CPD for duplication.
- SpotBugs for likely defects.
- SonarQube or SonarCloud when the project needs consolidated tracking.

Minimum quality bar:

- No blocker or critical findings.
- No high-priority SpotBugs findings without documented reason.
- Duplication reviewed before merge.
- Complexity warnings reviewed before merge.
- AI-generated code reviewed for repeated methods, dead code, weak abstractions, and overlong classes.

## 12. Security

Treat security checks as part of normal engineering, not a final phase.

Checklist:

- Validate external inputs.
- Keep secrets out of source control.
- Scan dependencies for known vulnerabilities.
- Review suppressions with a reason and expiry date.
- Do not return stack traces or internal details to users.
- Do not log passwords, tokens, PII, or full auth headers.
- Validate authentication and authorization for protected APIs.
- Use least-privilege permissions for cloud resources.

## 13. Logging And Observability

- Prefer structured logs for production services.
- Include correlation/request IDs for API flows.
- Use consistent levels such as `INFO`, `WARN`, and `ERROR`.
- Log enough context to debug failures.
- Never log secrets or sensitive personal data.
- Keep logs compatible with CloudWatch, Splunk, or the chosen platform.
- Add production-grade logging before cloud deployment; do not overbuild it for a tiny practice app.

## 14. Error Handling

- Validate inputs early.
- Fail with clear, consistent messages.
- Keep user-facing errors safe and concise.
- Log server-side details where useful.
- Test both success and failure paths.
- Keep error contracts stable so clients and tests can rely on them.

## 15. Code Quality

Before calling a feature complete:

- Methods are small and focused.
- Classes have one clear responsibility.
- Names are explicit and readable.
- Duplicated logic is removed or justified.
- Libraries are used for parsing/serialization instead of manual string handling.
- Comments explain non-obvious decisions only.
- Temporary debug code is removed.
- README/docs are updated when behavior, setup, or architecture changes.

## 16. Review Checklist

Before merge or handoff:

- App compiles.
- Unit and service/API tests pass.
- UI smoke tests pass when the feature is user-facing.
- Coverage was generated and reviewed.
- Static analysis passed or findings were reviewed.
- Dependency/security scan passed or findings were documented.
- Logs and errors do not leak sensitive data.
- Reports are generated correctly.
- Environment-specific values are configurable.
- README and relevant docs are current.

## 17. Recommended Maven Commands

Compile tests:

```powershell
.\mvnw.cmd test-compile
```

Run tests:

```powershell
.\mvnw.cmd test
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

Run Sonar analysis:

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Start a local Java app when configured with `exec-maven-plugin`:

```powershell
.\mvnw.cmd compile exec:java
```

Run tests against a custom local URL:

```powershell
.\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"
```

## 18. Definition Of Done

A feature is done when:

- It works locally.
- Core logic has unit/service coverage.
- User-facing behavior has smoke coverage where practical.
- Important failure paths are tested.
- Coverage, static analysis, and dependency scan results are reviewed.
- Error handling and logs are safe.
- Reports are available.
- Documentation is updated.
- The change is committed with a clear message when the work is ready to share.

## 19. First-Cut Setup For New Java Web Projects

Start with:

- Maven Wrapper.
- Java 17 compiler release.
- Clear `src/main` and `src/test` separation.
- Separate frontend files.
- Service layer for business logic.
- DTOs for request/response contracts.
- Local adapter such as web server/controller.
- Cloud adapter only when the deployment target is known.
- TestNG or JUnit for tests.
- Selenium Page Object framework only when UI testing is needed.
- ExtentReports or equivalent UI report when UI tests exist.
- JaCoCo coverage profile.
- Checkstyle, PMD/CPD, and SpotBugs quality profile.
- OWASP Dependency-Check or equivalent security scan profile.
- README with setup, run, test, coverage, and quality commands.

Build the smallest useful version first. Add heavier framework pieces only when they reduce risk or save repeated work.
