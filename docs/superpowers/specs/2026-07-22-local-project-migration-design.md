# Local Project Migration Design

## Goal

Make the completed local CampShare application the source of truth in GitHub. Replace the abandoned list-view prototype while preserving the repository's merged `AGENTS.md` guide.

## Scope

Copy the local application's production source, tests, Maven Wrapper, `pom.xml`, database migrations, templates, and shared project metadata into a branch based on `list-view`. The resulting project uses Spring Boot 3.5, PostgreSQL, JPA, Flyway, Spring Security, and Thymeleaf.

Do not copy IDE metadata, build output, Maven caches, linked worktrees, local database configuration, or passwords. In particular, `application-local.properties` remains local and ignored.

## Migration Steps

1. Compare the local application with the GitHub prototype and replace tracked application files with the local versions.
2. Preserve `AGENTS.md` and reconcile `.gitignore` so local-only files stay untracked.
3. Run the complete Maven test suite using a project-local Maven cache.
4. Review the final diff for credentials and generated files.
5. Commit the migration, push `codex/migrate-local-campshare`, and open a pull request to `list-view`.

## Success Criteria

The branch contains the local application's 62 source and test files, no credentials or generated files, and a passing Maven test suite. Merging the pull request makes future clones show the full CampShare application rather than the old list-view prototype.
