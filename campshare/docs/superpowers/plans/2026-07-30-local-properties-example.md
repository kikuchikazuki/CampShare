# Local Properties Example Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide a Git-tracked database configuration template and documented copy workflow without exposing real local credentials.

**Architecture:** A root-level `application-local.example.properties` defines the required property names with safe placeholders. The ignored `application-local.properties` remains the runtime source of real values, while README and a static contract test keep the setup workflow reproducible.

**Tech Stack:** Java 17, JUnit 5, Spring Boot properties, Git, PowerShell, Markdown

## Global Constraints

- Never read, modify, delete, print, stage, or commit the existing `application-local.properties`.
- Never place real database usernames, passwords, or other secrets in tracked files.
- Keep `application-local.properties` ignored by Git.
- Track `README.md` and `application-local.example.properties`.
- Use `YOUR_USERNAME` and `YOUR_PASSWORD` as the credential placeholders.

---

### Task 1: Add the Safe Local Configuration Workflow

**Files:**
- Create: `application-local.example.properties`
- Create: `src/test/java/com/example/campshare/config/LocalConfigurationDocumentationTest.java`
- Modify: `README.md`
- Verify: `.gitignore`

**Interfaces:**
- Consumes: `spring.config.import=optional:file:./application-local.properties` from `src/main/resources/application.properties`.
- Produces: a copyable root-level template and the PowerShell command `Copy-Item application-local.example.properties application-local.properties`.

- [ ] **Step 1: Write the failing documentation contract test**

```java
package com.example.campshare.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalConfigurationDocumentationTest {

    @Test
    void exampleContainsSafeDatabaseConfigurationPlaceholders() throws IOException {
        String example = Files.readString(Path.of("application-local.example.properties"));

        assertTrue(example.contains("spring.datasource.url=jdbc:postgresql://localhost:5432/campshare"));
        assertTrue(example.contains("spring.datasource.username=YOUR_USERNAME"));
        assertTrue(example.contains("spring.datasource.password=YOUR_PASSWORD"));
        assertTrue(example.contains("spring.datasource.driver-class-name=org.postgresql.Driver"));
        assertFalse(example.contains("campshare_app"));
        assertFalse(example.contains("Admin123!"));
    }

    @Test
    void readmeDocumentsCopyingTheExampleWithoutEditingIt() throws IOException {
        String readme = Files.readString(Path.of("README.md"));

        assertTrue(readme.contains(
                "Copy-Item application-local.example.properties application-local.properties"));
        assertTrue(readme.contains("application-local.example.properties"));
        assertTrue(readme.contains("application-local.properties"));
    }

    @Test
    void realLocalPropertiesRemainIgnored() throws IOException {
        String gitignore = Files.readString(Path.of(".gitignore"));

        assertTrue(gitignore.lines()
                .map(String::trim)
                .anyMatch(line -> line.equals("application-local.properties")));
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=LocalConfigurationDocumentationTest test
```

Expected: FAIL because `application-local.example.properties` does not exist and README lacks the copy command.

- [ ] **Step 3: Add the tracked example**

Create `application-local.example.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/campshare
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
```

- [ ] **Step 4: Update the README setup instructions**

Replace the existing instruction to manually create `application-local.properties` with:

````markdown
### 1. ローカル設定ファイルを作成

exampleをコピーします。

```powershell
Copy-Item application-local.example.properties application-local.properties
```

コピー後、`application-local.properties` の `YOUR_USERNAME` と
`YOUR_PASSWORD` を実際のDB接続情報へ置き換えてください。
`application-local.example.properties` には実際の認証情報を書かないでください。
`application-local.properties` はGit管理対象外です。
````

Preserve every unrelated README section and its existing UTF-8 Japanese text.

- [ ] **Step 5: Run the focused test and verify it passes**

Run:

```powershell
.\mvnw.cmd -Dtest=LocalConfigurationDocumentationTest test
```

Expected: BUILD SUCCESS with three passing tests.

- [ ] **Step 6: Verify the real local file is ignored**

Run:

```powershell
git check-ignore application-local.properties
```

Expected output:

```text
application-local.properties
```

- [ ] **Step 7: Check the staged scope without exposing local values**

Run:

```powershell
git status --short
git diff --check
```

Expected: the implementation scope contains the example, README, and contract test; `application-local.properties` is absent.

- [ ] **Step 8: Run the full test suite**

Run:

```powershell
.\mvnw.cmd test
```

Expected: BUILD SUCCESS with no failures or errors.

- [ ] **Step 9: Commit the safe setup workflow**

```powershell
git add application-local.example.properties README.md src/test/java/com/example/campshare/config/LocalConfigurationDocumentationTest.java
git commit -m "docs: add local properties example"
```
