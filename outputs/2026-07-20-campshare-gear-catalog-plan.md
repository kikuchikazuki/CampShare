# CampShare Gear Catalog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan in one batched task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create and display six public camp-rental gear records.

**Architecture:** Flyway V3 owns the `gears` table and seed records. Spring Data JPA reads ordered records; a public controller renders Bootstrap cards through Thymeleaf.

**Tech Stack:** Java 17, Spring Boot 3.5.16, JPA, Flyway, PostgreSQL, Thymeleaf, Bootstrap.

## Global Constraints

- Do not modify V1 or V2; keep `spring.jpa.hibernate.ddl-auto=none`.
- V3 creates `gears` and inserts exactly six records.
- `/gears` stays publicly accessible.
- Run Maven through `mvnw.cmd` with the local Maven cache.

---

### Task 6: Gear Catalog

**Files:**

- Create: `campshare/src/main/resources/db/migration/V3__create_gears.sql`
- Create: `campshare/src/main/java/com/example/campshare/gear/Gear.java`
- Create: `campshare/src/main/java/com/example/campshare/gear/GearRepository.java`
- Create: `campshare/src/main/java/com/example/campshare/web/GearController.java`
- Create: `campshare/src/main/resources/templates/gears.html`
- Modify: `campshare/src/main/resources/templates/home.html`
- Create: `campshare/src/test/java/com/example/campshare/config/GearMigrationTest.java`
- Create: `campshare/src/test/java/com/example/campshare/web/GearControllerTest.java`

- [ ] **Step 1: Add failing migration and controller tests**

`GearMigrationTest` asserts `SELECT COUNT(*) FROM gears` is `6`. `GearControllerTest` asserts `GET /gears` returns view `gears` with model attribute `gears`.

- [ ] **Step 2: Confirm red state**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test -Dtest=GearMigrationTest,GearControllerTest
```

Expected: `BUILD FAILURE` because V3, `Gear`, and `GearController` are absent.

- [ ] **Step 3: Implement catalog**

Create V3 with columns `id`, `name`, `category`, `description`, `daily_price INTEGER CHECK (daily_price >= 0)`, `stock_count INTEGER CHECK (stock_count >= 0)`, `image_url`, and `created_at`; insert tent, tarp, lantern, sleeping bag, table, and chair. Add a `Gear` entity, `findAllByOrderByIdAsc()` repository method, public `GET /gears`, Bootstrap card template, and a `/gears` link on the home page.

- [ ] **Step 4: Confirm green state and browser output**

```powershell
$env:MAVEN_USER_HOME = "$PWD\.maven-cache"
.\mvnw.cmd test
```

Expected: `BUILD SUCCESS`. Open `http://localhost:8080/gears` and confirm six responsive gear cards.
