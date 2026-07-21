# Repository Guidelines Design

## Goal

Create a concise, English-language `AGENTS.md` at the CampShare project root. It will help contributors navigate the repository, run the application, follow existing conventions, write tests, and prepare reviewable changes.

## Content and Structure

The document will be titled **Repository Guidelines** and remain between 200 and 400 words. It will contain six repository-specific sections:

1. **Project Structure & Module Organization** - describe the Spring Boot packages under `src/main/java/com/example/campshare`, templates and Flyway migrations under `src/main/resources`, and mirrored tests under `src/test/java`.
2. **Build, Test, and Development Commands** - document Maven Wrapper commands for test, package, and local development, plus the local PostgreSQL requirement.
3. **Coding Style & Naming Conventions** - specify Java indentation, package naming, Spring class suffixes, template naming, and Flyway's `V<number>__description.sql` convention.
4. **Testing Guidelines** - identify JUnit 5, Spring Boot Test, MockMvc, Mockito, and AssertJ; require behavior-focused `*Test` classes and relevant regression coverage.
5. **Commit & Pull Request Guidelines** - reflect the repository's short imperative prefixes such as `feat:`, `docs:`, and `chore:`; require focused PR descriptions, verification notes, and screenshots for UI changes.
6. **Security & Configuration Tips** - prohibit committing credentials or `application-local.properties` and direct contributors to use local configuration.

## Validation

Verify the finished file is at the repository root, uses Markdown headings, contains 200 to 400 words, references only commands and paths that exist in this repository, and does not expose local credentials.
