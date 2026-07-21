# Repository Guidelines Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a concise, repository-specific contributor guide at `AGENTS.md`.

**Architecture:** This is a single documentation deliverable at the project root. Its claims are derived from the existing Maven build, Spring Boot directory layout, test stack, Git history, and ignored local configuration.

**Tech Stack:** Markdown, Java 17, Spring Boot 3.5.16, Maven Wrapper, JUnit 5, PostgreSQL, Flyway

## Global Constraints

- Title the document `Repository Guidelines`.
- Keep the guide in English and between 200 and 400 words.
- Use only commands, paths, and conventions verified in this repository.
- Do not include credentials or contents of `application-local.properties`.

---

### Task 1: Create and validate the contributor guide

**Files:**
- Create: `AGENTS.md`

**Interfaces:**
- Consumes: `pom.xml`, `.gitignore`, `src/main`, `src/test`, and recent Git commit subjects.
- Produces: A root-level Markdown guide for human and agent contributors.

- [ ] **Step 1: Verify the guide does not already exist**

Run:

```powershell
Test-Path AGENTS.md
```

Expected: `False`.

- [ ] **Step 2: Create `AGENTS.md` with the approved content**

```markdown
# Repository Guidelines

## Project Structure & Module Organization

CampShare is a Java 17 Spring Boot application. Production code lives in `src/main/java/com/example/campshare`, organized by feature (`gear`, `rental`, `user`, `admin`, `dashboard`) with MVC controllers in `web` and shared configuration in `config`. Thymeleaf views are in `src/main/resources/templates`; static assets belong in `src/main/resources/static`. Database changes use Flyway scripts in `src/main/resources/db/migration`. Tests mirror the production packages under `src/test/java/com/example/campshare`.

## Build, Test, and Development Commands

Use the checked-in Maven Wrapper from the project root:

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
```

- [ ] **Step 3: Validate structure, word count, and repository cleanliness**

Run:

```powershell
$text = Get-Content AGENTS.md -Raw
(($text -split '\s+') | Where-Object { $_ }).Count
Select-String -Path AGENTS.md -Pattern '^# Repository Guidelines$','^## Project Structure','^## Build','^## Coding Style','^## Testing','^## Commit','^## Security'
git diff --check -- AGENTS.md
git status --short
```

Expected: word count between 200 and 400; all seven headings found; `git diff --check` exits 0; only `AGENTS.md` and pre-existing unrelated files are uncommitted.

- [ ] **Step 4: Commit the guide**

```powershell
git add -- AGENTS.md
git commit -m "docs: add repository contributor guide"
```
