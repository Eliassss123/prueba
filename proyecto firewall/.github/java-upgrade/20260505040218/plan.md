# Upgrade Plan: proyecto firewall (20260505040218)

- **Generated**: 2026-05-05 04:02:18
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 21.0.10: C:\Program Files\Java\jdk-21.0.10\bin (current project JDK, used by step 1 and final validation)

**Build Tools**
- Maven 3.9.14: C:\maven\apache-maven-3.9.14\bin
- Maven Wrapper (alertas): 3.9.11 → compatible with Java 21
- Maven Wrapper (reportes): 3.9.14
- Maven Wrapper (geolocalizacion): 3.9.14

## Guidelines

- The project should target the latest LTS Java runtime only; currently that is Java 21.
- Use installed JDK 21 and Maven 3.9+ for validation of modules without a wrapper.
- Where wrappers are present, ensure their distribution URLs remain compatible with Java 21.

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260505040218
- Run tests before and after the upgrade: true

## Upgrade Goals

- Java runtime upgraded and validated at Java 21 (latest LTS)
- Confirm all existing service modules build and pass tests on Java 21

## Technology Stack

| Technology/Dependency    | Current | Min Compatible | Why Incompatible |
| ------------------------ | ------- | -------------- | ---------------- | 
| Java                     | 21      | 21             | User-requested latest LTS runtime |
| Spring Boot              | 4.0.5   | 4.0.5          | Already compatible with Java 21 |
| Maven                    | 3.9.14  | 3.9.0          | Required for stable Java 21 builds |
| Maven Wrapper (alertas)  | 3.9.11  | 3.9.0          | Compatible with Java 21 |
| Maven Wrapper (reportes) | 3.9.14  | 3.9.0          | Compatible with Java 21 |
| Maven Wrapper (geolocalizacion) | 3.9.14 | 3.9.0 | Compatible with Java 21 |
| Spring Cloud             | 2025.1.0 | 2025.1.0      | Already aligned with Spring Boot 4.0.5 |

## Derived Upgrades

- None required: the project already targets Java 21 and uses Spring Boot 4.0.5.
- Recommended: standardize Maven wrapper distribution versions where wrapper files are present to improve reproducibility.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Confirm JDK 21 and Maven 3.9+ are available, and identify which modules rely on system Maven vs wrapper files.
  - **Changes to Make**:
    - Validate installed JDK and Maven versions.
    - Confirm wrapper coverage in `alertas`, `reportes`, and `geolocalizacion`.
  - **Verification**: `java -version && mvn -version`

- Step 2: Setup Baseline
  - **Rationale**: Verify current module build health on Java 21 before declaring the runtime upgrade complete.
  - **Changes to Make**: None.
  - **Verification**: `mvn -f alertas/pom.xml clean test-compile -q && mvn -f reportes/pom.xml clean test-compile -q && mvn -f geolocalizacion/pom.xml clean test-compile -q && mvn -f api-gateway/pom.xml clean test-compile -q && mvn -f eureka-service/pom.xml clean test-compile -q`

- Step 3: Standardize Maven Wrapper for Reproducibility
  - **Rationale**: Ensure wrapper-based modules use a Maven distribution version compatible with Java 21 and aligned with installed tooling.
  - **Changes to Make**:
    - Update `alertas/.mvn/wrapper/maven-wrapper.properties` to use Maven 3.9.14.
  - **Verification**: `mvn -f alertas/pom.xml -q compile`

- Step 4: Final Validation
  - **Rationale**: Confirm the Java runtime upgrade goal is met with complete compilation and test success across all service modules.
  - **Changes to Make**: Resolve any issues found during validation.
  - **Verification**: `mvn -f alertas/pom.xml clean test && mvn -f reportes/pom.xml clean test && mvn -f geolocalizacion/pom.xml clean test && mvn -f api-gateway/pom.xml clean test && mvn -f eureka-service/pom.xml clean test`

## Key Challenges

- **No Git repository available**
  - **Challenge**: Changes cannot be recorded with branch/commit metadata.
  - **Strategy**: Track all modifications in the upgrade summary and preserve the plan/progress files in `.github/java-upgrade/20260505040218`.

- **Separate module builds, no root aggregator**
  - **Challenge**: Each service must be validated independently, increasing verification scope.
  - **Strategy**: Run compile/test commands per module and document failures module-by-module.

- **Partial Maven wrapper coverage**
  - **Challenge**: `api-gateway` and `eureka-service` lack wrapper properties, so those modules depend on the system Maven installation.
  - **Strategy**: Use installed Maven 3.9.14 for modules without wrappers and align wrappered modules to the same Maven distribution version.

- **Potential `lombok.version` property risk**
  - **Challenge**: `reportes` and `geolocalizacion` reference `${lombok.version}` in Maven plugin configuration without a local property definition.
  - **Strategy**: Verify build behavior during baseline; if unresolved, add an explicit `lombok.version` property or adjust plugin configuration.

- **Current runtime already matches the target**
  - **Challenge**: The upgrade path is effectively a validation exercise, so the main risk is failing to detect hidden build or dependency issues.
  - **Strategy**: Perform full test suite validation on Java 21 and report any runtime inconsistencies.
