# Repository Guidelines

## Project Structure & Module Organization

CampShare is a Java 17 Spring Boot application. Production code lives in `src/main/java/com/example/campshare`, organized by feature (`gear`, `rental`, `user`, `admin`, `dashboard`) with MVC controllers in `web` and shared configuration in `config`. Thymeleaf views are in `src/main/resources/templates`; static assets belong in `src/main/resources/static`. Database changes use Flyway scripts in `src/main/resources/db/migration`. Tests mirror the production packages under `src/test/java/com/example/campshare`.

## Build, Test, and Development Commands

Use the checked-in Maven Wrapper from the project root. In PowerShell, first set a project-local Maven cache:

`$env:MAVEN_USER_HOME="$PWD\.maven-cache"`

Then run:

- `.\mvnw.cmd test` runs the complete test suite.
- `.\mvnw.cmd clean package` rebuilds and creates the executable JAR in `target/`.
- `.\mvnw.cmd spring-boot:run` starts the application at `http://localhost:8080`.
- `.\mvnw.cmd -Dtest=RentalServiceTest test` runs one test class.

Local execution requires PostgreSQL and an ignored `application-local.properties` file containing datasource settings.

## Coding Style & Naming Conventions

Use four-space indentation, UTF-8, and one public Java type per file. Package names are lowercase; classes use PascalCase and methods use camelCase. Follow existing Spring suffixes such as `Controller`, `Service`, `Repository`, `Form`, and `Config`. Name templates in lowercase kebab-case. Flyway migrations must follow `V<number>__description.sql`; never edit an applied migration.

## Testing Guidelines

Tests use JUnit 5, Spring Boot Test, MockMvc, Mockito, AssertJ, and Spring Security Test. Name classes `*Test` and tests after observable behavior, for example `gearDetailReturnsDetailView`. Add regression coverage for controller routes, authorization rules, service transactions, validation, and migrations. Run the complete suite before submitting changes.

## Commit & Pull Request Guidelines

Follow the existing short, imperative convention: `feat: add rental reservations`, `docs: add dashboard plan`, or `chore: ignore local worktrees`. Keep commits focused. Pull requests should explain the behavior change, list verification commands, link related issues, and include screenshots for template or CSS changes.

## Security & Configuration Tips

Never commit passwords, local database URLs, IDE metadata, build output, or `application-local.properties`. Preserve CSRF protection and role checks when changing authenticated or `/admin/**` routes.
