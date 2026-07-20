# CampShare Admin Gear Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理者だけがキャンプ用品を安全に追加・編集・削除できるようにする。

**Architecture:** Spring Securityが `/admin/**` を `ROLE_ADMIN` に制限する。`AdminGearService` が用品変更と削除可否を担当し、Thymeleafの管理画面はフォーム入力・結果表示だけを担当する。

**Tech Stack:** Java 17, Spring Boot 3.5.16, Spring Security, Spring Data JPA, Flyway, PostgreSQL 16, Thymeleaf, JUnit 5, Mockito.

## Global Constraints

- スキーマ・初期データはFlyway migrationのみで変更する。
- 管理者アカウントは `admin@campshare.local / Admin123! / ROLE_ADMIN` とする。
- `/admin/**` は `hasRole("ADMIN")`、一般ユーザーは403とする。
- 予約履歴のある用品は削除しない。
- Mavenは `campshare/mvnw.cmd` を使用する。

---

### Task 1: 管理者アカウントとURL認可

**Files:**
- Create: `campshare/src/main/resources/db/migration/V5__seed_admin_user.sql`
- Create: `campshare/src/test/java/com/example/campshare/config/AdminUserMigrationTest.java`
- Modify: `campshare/src/main/java/com/example/campshare/config/SecurityConfig.java`
- Modify: `campshare/src/main/java/com/example/campshare/web/HomeController.java`
- Modify: `campshare/src/main/resources/templates/home.html`

**Produces:** adminユーザーと、`/admin/**` の管理者限定認可。

- [ ] **Step 1: 失敗するmigration・認可テストを書く**

`AdminUserMigrationTest` で `admin@campshare.local` が `ROLE_ADMIN` を持つことをSQLで確認する。Webテストでは一般ユーザーの `/admin/gears` が403となることを確認する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=AdminUserMigrationTest`

Expected: adminユーザー未作成による失敗。

- [ ] **Step 3: migrationとSecurity設定を実装する**

```sql
INSERT INTO users (display_name, email, password_hash, role_id)
SELECT 'CampShare 管理者', 'admin@campshare.local',
       '$2a$10$5Euj.lEuo9u3w1gXN1o2Ye.YVUbIJgkEd.2vMJrOAIPQMaWQ3DUo6', id
FROM roles WHERE name = 'ROLE_ADMIN';
```

`SecurityConfig` に `.requestMatchers("/admin/**").hasRole("ADMIN")` を追加する。`HomeController` はログインユーザーの権限をモデルへ渡し、`home.html` は管理者だけに商品管理リンクを表示する。

- [ ] **Step 4: 関連テストを緑にする**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=AdminUserMigrationTest,AdminGearControllerTest`

Expected: `BUILD SUCCESS`。

### Task 2: 管理用の用品サービス

**Files:**
- Create: `campshare/src/main/java/com/example/campshare/admin/AdminGearForm.java`
- Create: `campshare/src/main/java/com/example/campshare/admin/AdminGearService.java`
- Create: `campshare/src/main/java/com/example/campshare/admin/GearCannotBeDeletedException.java`
- Create: `campshare/src/test/java/com/example/campshare/admin/AdminGearServiceTest.java`
- Modify: `campshare/src/main/java/com/example/campshare/gear/Gear.java`
- Modify: `campshare/src/main/java/com/example/campshare/rental/RentalRepository.java`

**Produces:** `create(AdminGearForm)`, `update(Long, AdminGearForm)`, `delete(Long)`。

- [ ] **Step 1: 失敗するサービス単体テストを書く**

追加・更新時にフォームの値を用品へ反映すること、`existsByGearId(id)` がtrueなら `GearCannotBeDeletedException` で削除しないこと、falseなら `GearRepository.delete` を呼ぶことをMockitoで確認する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=AdminGearServiceTest`

Expected: `AdminGearService` 未作成によるコンパイル失敗。

- [ ] **Step 3: 最小のサービス実装を書く**

`AdminGearForm` に `@NotBlank` の name/category/description/imageUrl、`@NotNull @PositiveOrZero` の dailyPrice/stockCountを置く。`Gear` に作成コンストラクタと `update(...)` を追加し、`RentalRepository` に `boolean existsByGearId(Long gearId)` を追加する。サービスの削除は `@Transactional` とする。

- [ ] **Step 4: サービステストを緑にする**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=AdminGearServiceTest`

Expected: `BUILD SUCCESS`。

### Task 3: 管理画面のCRUD

**Files:**
- Create: `campshare/src/main/java/com/example/campshare/web/AdminGearController.java`
- Create: `campshare/src/main/resources/templates/admin/gears.html`
- Create: `campshare/src/main/resources/templates/admin/gear-form.html`
- Create: `campshare/src/test/java/com/example/campshare/web/AdminGearControllerTest.java`

**Produces:** `GET /admin/gears`、`GET /admin/gears/new`、`POST /admin/gears`、`GET /admin/gears/{id}/edit`、`POST /admin/gears/{id}`、`POST /admin/gears/{id}/delete`。

- [ ] **Step 1: 失敗するWebテストを書く**

管理者が一覧・新規フォームを開けること、一般ユーザーが403になること、正常な追加が一覧へリダイレクトすること、削除拒否で一覧にエラーメッセージが出ることをMockMvcで確認する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=AdminGearControllerTest`

Expected: `AdminGearController` 未作成による失敗。

- [ ] **Step 3: コントローラーとテンプレートを実装する**

コントローラーは `@Valid` と `BindingResult` で入力エラー時にフォームへ戻す。成功時は `redirect:/admin/gears?created=true`、`updated=true`、`deleted=true` を使う。削除拒否時は `redirect:/admin/gears?deleteError=true` とする。すべてのPOSTフォームにCSRFトークンを含める。

- [ ] **Step 4: 全テストとブラウザ確認を行う**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test`

Expected: `BUILD SUCCESS`、Failures 0、Errors 0。

ブラウザでは管理者でログインし、商品追加・編集・未予約商品の削除、予約済み商品の削除拒否、一般ユーザーの403を確認する。

- [ ] **Step 5: コミットする**

```powershell
git add campshare
git commit -m "feat: add admin gear management"
```
