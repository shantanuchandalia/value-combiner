# Shippable Test Execution Report

## Execution Identity

| Field | Value |
|---|---|
| Report file | `SHIP_TEST_EXECUTION_20260519_2340_e23f46e_JAVA17_QUALITY_BASELINE.md` |
| Naming convention | `SHIP_TEST_EXECUTION_YYYYMMDD_HHMM_<commit>_<release-scope>.md` |
| Purpose | Non-overwriting evidence file for a logically shippable project checkpoint |
| Commit reference | `e23f46e` |
| Working state | Java 17 static quality/security baseline commit |
| Execution date/time | `2026-05-19 23:40 IST` |
| Runtime | OpenLogic OpenJDK `17.0.18` |
| Local app URL for UI tests | `http://localhost:8081` |

## Execution Summary

| Checkpoint | Command | Result | Evidence |
|---|---|---:|---|
| Java 17 validation | `.\scripts\use-java17.ps1; .\mvnw.cmd validate` | PASS | Maven Enforcer accepted JDK 17 |
| Full functional and UI suite | `.\scripts\use-java17.ps1; .\mvnw.cmd test "-DbaseUrl=http://localhost:8081" "-Dheadless=true"` | PASS | `12` tests, `0` failures, `0` errors, `0` skipped |
| Coverage suite | `.\scripts\use-java17.ps1; .\mvnw.cmd clean verify "-Pcoverage"` | PASS | `9` tests, `0` failures, `0` errors, `0` skipped |
| Static quality gate | `.\scripts\use-java17.ps1; .\mvnw.cmd verify "-Pquality" "-DskipTests"` | PASS | Checkstyle `0` violations, PMD PASS, CPD PASS, SpotBugs `0` findings |
| Dependency security scan | `.\scripts\use-java17.ps1; .\mvnw.cmd verify "-Psecurity-scan" "-DskipTests"` | DEFERRED | Profile configured; first NVD update requires long download or `NVD_API_KEY` |

## Test Suite Results

| Suite / Class | Test Type | Test Count | Passed | Failed | Skipped | Notes |
|---|---|---:|---:|---:|---:|---|
| `ValueCombinerUnitTest` | Unit | 6 | 6 | 0 | 0 | Core integer, decimal, and string behavior |
| `ValueCombinerServiceTest` | Service/API | 3 | 3 | 0 | 0 | JSON request/response success and error behavior |
| `ValueCombinerCalculationTest` | Selenium UI smoke | 3 | 3 | 0 | 0 | Browser calculation flows using Chrome headless |
| Full TestNG suite | Combined | 12 | 12 | 0 | 0 | Executed against local app on port `8081` |
| Coverage TestNG suite | Unit + service/API | 9 | 9 | 0 | 0 | Browser tests intentionally excluded from coverage profile |

## Coverage Results

| Metric | Covered | Missed | Coverage |
|---|---:|---:|---:|
| Instruction coverage | 230 | 121 | 65.5% |
| Branch coverage | 22 | 26 | 45.8% |
| Line coverage | 62 | 20 | 75.6% |

Coverage report path:

```text
target/site/jacoco/index.html
```

## Static Quality Results

| Tool | Scope | Result | Notes |
|---|---|---:|---|
| Checkstyle | Main + test Java sources | PASS | `0` violations |
| PMD | Main + test Java sources | PASS | Ruleset loaded successfully |
| CPD | Main + test Java sources | PASS | No duplicate block above configured threshold |
| SpotBugs | Compiled classes | PASS | `0` bug instances, `0` errors |

## Known Notes

| Item | Status | Reason / Follow-up |
|---|---|---|
| OWASP Dependency-Check first run | Deferred | NVD update was slow without an API key. Set `NVD_API_KEY` and rerun `.\mvnw.cmd verify "-Psecurity-scan" "-DskipTests"`. |
| Selenium CDP warning | Accepted | Chrome version is newer than Selenium's bundled CDP mapping. Tests still passed. |
| SLF4J no-provider warning | Accepted | Current test/reporting stack logs with SLF4J but no provider is configured. No functional impact observed. |

## Shippability Decision

| Decision | Value |
|---|---|
| Functional tests | PASS |
| Coverage generation | PASS |
| Static quality | PASS |
| Security scan | CONFIGURED, deferred for NVD API/cache completion |
| Overall checkpoint | SHIPPABLE for Java 17 quality-baseline work, with dependency scan pending API-key-assisted execution |
