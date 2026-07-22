# Local Project Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the abandoned GitHub list-view prototype with the completed local CampShare application.

**Architecture:** Use `list-view` as the PR base and migrate the local Spring Boot 3.5 application as a coherent baseline. Preserve GitHub's root-level `AGENTS.md`; exclude local configuration, IDE metadata, caches, and generated output.

**Tech Stack:** Java 17, Spring Boot 3.5.16, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Flyway, JUnit 5, MockMvc, Mockito, AssertJ.

## Global Constraints

- Copy no secrets: exclude `application-local.properties`.
- Copy no generated or local-only content: exclude `.idea/`, `target/`, `.maven-cache/`, `.superpowers/`, and `.worktrees/`.
- Keep `AGENTS.md` at the GitHub repository root.
- Target branch is `list-view`; work only on `codex/migrate-local-campshare`.

---

### Task 1: Align project metadata and build configuration

**Files:**
- Modify: `.gitignore`, `.gitattributes`, `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd`, `pom.xml`, `HELP.md`
- Preserve: `AGENTS.md`

- [ ] **Step 1: Replace the build metadata with the local Spring Boot 3.5 project files.**

Copy only the listed files from `C:\Users\kikuchi\Documents\Codex\2026-07-15\new-chat\campshare`. Do not copy `application-local.properties`.

- [ ] **Step 2: Add local-only exclusions to `.gitignore`.**

Ensure these exact lines are present:

```gitignore
application-local.properties
.maven-cache/
.superpowers/
.worktrees/
```

- [ ] **Step 3: Verify the staged metadata diff.**

Run: `git diff --check`
Expected: no output and exit code 0.

- [ ] **Step 4: Commit the metadata baseline.**

```powershell
git add .gitattributes .gitignore .mvn mvnw mvnw.cmd pom.xml HELP.md
git commit -m "build: align project with local application"
```

### Task 2: Replace production code and templates

**Files:**
- Replace: `src/main/java/com/example/campshare/**`
- Replace: `src/main/resources/application.properties`, `src/main/resources/db/migration/**`, `src/main/resources/templates/**`
- Remove: `src/main/resources/data.sql`, `src/main/resources/schema.sql`

- [ ] **Step 1: Replace tracked production Java files with the local application's feature packages.**

The resulting packages must be `admin`, `config`, `dashboard`, `gear`, `rental`, `user`, and `web`, plus `CampshareApplication.java`.

- [ ] **Step 2: Replace resources with the local application's PostgreSQL, Flyway, and Thymeleaf resources.**

Include migrations `V1__create_roles.sql` through `V5__seed_admin_user.sql` and all templates under `templates/admin/` plus the root templates.

- [ ] **Step 3: Remove prototype-only MyBatis and H2 resources.**

Remove exactly:

```text
src/main/resources/data.sql
src/main/resources/schema.sql
src/main/resources/templates/gears/gear-list.html
src/main/resources/templates/layout.html
```

- [ ] **Step 4: Verify no local database file was added.**

Run: `git status --short`
Expected: no `application-local.properties`, `.idea/`, `target/`, `.maven-cache/`, `.superpowers/`, or `.worktrees/` paths.

- [ ] **Step 5: Commit the production migration.**

```powershell
git add src/main
git commit -m "feat: migrate completed CampShare application"
```

### Task 3: Replace tests and validate the migrated application

**Files:**
- Replace: `src/test/java/com/example/campshare/**`

- [ ] **Step 1: Replace the prototype test suite with the local application's tests.**

The resulting suite includes configuration, rental, user, and web tests, and retains `CampshareApplicationTests`.

- [ ] **Step 2: Run the full test suite using a project-local Maven cache.**

```powershell
$env:MAVEN_USER_HOME="$PWD\.maven-cache"
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`, with no failures or errors.

- [ ] **Step 3: Inspect the final diff for secrets and generated content.**

```powershell
git diff list-view...HEAD --check
git status --short
git diff --cached --name-only
```

Expected: no credential file, cache, build output, or IDE metadata.

- [ ] **Step 4: Commit tests and any migration corrections.**

```powershell
git add src/test
git commit -m "test: migrate CampShare regression suite"
```

### Task 4: Publish the migration pull request

**Files:**
- Modify: none beyond the committed migration files

- [ ] **Step 1: Confirm the branch is ahead of `list-view` and clean.**

```powershell
git status -sb
git log --oneline list-view..HEAD
```

Expected: only migration commits are ahead; local-only ignored files are absent from status.

- [ ] **Step 2: Push the branch.**

```powershell
git push -u origin codex/migrate-local-campshare
```

- [ ] **Step 3: Open a pull request into `list-view`.**

Title: `feat: migrate completed CampShare application`

Body must state that the old list-view prototype is replaced with the local Spring Boot 3.5 / PostgreSQL / JPA / Flyway application and include the Maven test command.
