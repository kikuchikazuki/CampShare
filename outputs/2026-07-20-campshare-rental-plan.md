# CampShare Reservation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** ログインユーザーが用品1点を日付指定で予約し、在庫と予約履歴を安全に管理できるようにする。

**Architecture:** Flywayで予約テーブルを作成し、JPAの `RentalService` に予約の業務処理を集約する。用品行は悲観ロックで取得し、在庫減算と予約保存を同一トランザクションで完了させる。Web層は商品詳細、予約フォーム、マイページを担当する。

**Tech Stack:** Java 17, Spring Boot 3.5.16, Spring MVC, Spring Security, Spring Data JPA, Flyway, PostgreSQL 16, Thymeleaf, JUnit 5, Mockito.

## Global Constraints

- DB定義の変更はFlyway migrationのみで行い、Hibernate DDL自動更新は使わない。
- JPAを主とし、MyBatisはこのタスクでは追加しない。
- 予約は用品1点・数量1に限定する。
- 終了日は返却日として扱い、`endDate` は `startDate` より後でなければならない。
- 料金は `ChronoUnit.DAYS.between(startDate, endDate) * dailyPrice` とする。
- 予約失敗時に在庫数・予約データを変更してはならない。
- Mavenはプロジェクト内の `mvnw.cmd` で実行する。

---

## ファイル構成

- `campshare/src/main/resources/db/migration/V4__create_rentals.sql`: rentalsテーブルと整合性制約
- `campshare/src/main/java/com/example/campshare/rental/*`: 予約エンティティ、フォーム、例外、リポジトリ、サービス
- `campshare/src/main/java/com/example/campshare/gear/Gear.java`: 在庫減算操作とID取得
- `campshare/src/main/java/com/example/campshare/gear/GearRepository.java`: 悲観ロック付き検索
- `campshare/src/main/java/com/example/campshare/web/GearController.java`: 商品詳細画面
- `campshare/src/main/java/com/example/campshare/web/RentalController.java`: 予約登録とマイページ
- `campshare/src/main/resources/templates/gear-detail.html`: 商品詳細・予約導線
- `campshare/src/main/resources/templates/rental-form.html`: 日付入力フォーム
- `campshare/src/main/resources/templates/rentals.html`: 本人の予約履歴
- `campshare/src/main/resources/templates/gears.html`: 詳細画面へのリンク
- `campshare/src/test/java/com/example/campshare/...`: migration、サービス、Web層のテスト

### Task 1: 予約テーブルとドメインモデル

**Files:**
- Create: `campshare/src/main/resources/db/migration/V4__create_rentals.sql`
- Create: `campshare/src/main/java/com/example/campshare/rental/Rental.java`
- Create: `campshare/src/main/java/com/example/campshare/rental/RentalRepository.java`
- Create: `campshare/src/test/java/com/example/campshare/config/RentalMigrationTest.java`
- Modify: `campshare/src/main/java/com/example/campshare/gear/Gear.java`
- Modify: `campshare/src/main/java/com/example/campshare/user/User.java`

**Produces:** `Rental(User user, Gear gear, LocalDate startDate, LocalDate endDate, int totalPrice)` と `RentalRepository.findAllByUserOrderByCreatedAtDesc(User user)`。

- [ ] **Step 1: migrationの失敗するテストを書く**

`RentalMigrationTest` をローカルプロファイルで起動し、`SELECT COUNT(*) FROM rentals` が実行できることと、`user_id`・`gear_id`・`start_date`・`end_date`・`total_price`・`status`・`created_at` 列が存在することを検証する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=RentalMigrationTest`

Expected: `relation "rentals" does not exist` を含む失敗。

- [ ] **Step 3: 最小のDB・エンティティ実装を書く**

```sql
CREATE TABLE rentals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    gear_id BIGINT NOT NULL REFERENCES gears(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price INTEGER NOT NULL CHECK (total_price >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date > start_date)
);
```

`Rental` は `User` と `Gear` への必須 `@ManyToOne(fetch = FetchType.LAZY)`、日付、料金、状態、作成日時を持つ。`User` と `Gear` に `getId()` を追加する。

- [ ] **Step 4: migrationテストを緑にする**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=RentalMigrationTest`

Expected: `BUILD SUCCESS`。

- [ ] **Step 5: コミットする**

```powershell
git add campshare/src/main/resources/db/migration/V4__create_rentals.sql campshare/src/main/java/com/example/campshare/rental campshare/src/main/java/com/example/campshare/gear/Gear.java campshare/src/main/java/com/example/campshare/user/User.java campshare/src/test/java/com/example/campshare/config/RentalMigrationTest.java
git commit -m "feat: add rental schema"
```

### Task 2: 在庫を安全に減らす予約サービス

**Files:**
- Create: `campshare/src/main/java/com/example/campshare/rental/RentalService.java`
- Create: `campshare/src/main/java/com/example/campshare/rental/OutOfStockException.java`
- Create: `campshare/src/main/java/com/example/campshare/rental/InvalidRentalDateException.java`
- Create: `campshare/src/test/java/com/example/campshare/rental/RentalServiceTest.java`
- Modify: `campshare/src/main/java/com/example/campshare/gear/Gear.java`
- Modify: `campshare/src/main/java/com/example/campshare/gear/GearRepository.java`

**Consumes:** `RentalRepository`、`UserRepository.findByEmail(String)`、悲観ロック付き用品取得。

**Produces:** `void reserve(String email, Long gearId, LocalDate startDate, LocalDate endDate)`。

- [ ] **Step 1: 失敗するサービス単体テストを書く**

Mockitoで、在庫3・日額2500円の用品とユーザーを返すようにし、予約後に `Gear.decreaseStock()` 相当で在庫2、`RentalRepository.save()` の料金5000円で保存されることを検証する。別テストで在庫0は `OutOfStockException`、同日または終了日が前の日付は `InvalidRentalDateException`、どちらも `save()` されないことを検証する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=RentalServiceTest`

Expected: `RentalService` が未作成のためコンパイル失敗。

- [ ] **Step 3: 最小のサービス実装を書く**

`GearRepository` に以下を追加する。

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select g from Gear g where g.id = :id")
Optional<Gear> findByIdForUpdate(@Param("id") Long id);
```

`Gear.decreaseStock()` は0以下なら `OutOfStockException` を送出し、それ以外は1減らす。`RentalService.reserve` は `@Transactional` とし、メールでユーザーを取得、ロック付きで用品を取得、日付を検証、在庫を減らし、日数と料金を計算して `Rental` を保存する。

- [ ] **Step 4: サービステストを緑にする**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=RentalServiceTest`

Expected: `BUILD SUCCESS`、予約成功・在庫0・日付不正の全ケースが成功。

- [ ] **Step 5: コミットする**

```powershell
git add campshare/src/main/java/com/example/campshare/rental campshare/src/main/java/com/example/campshare/gear/Gear.java campshare/src/main/java/com/example/campshare/gear/GearRepository.java campshare/src/test/java/com/example/campshare/rental/RentalServiceTest.java
git commit -m "feat: add safe rental service"
```

### Task 3: 予約画面・商品詳細・マイページ

**Files:**
- Create: `campshare/src/main/java/com/example/campshare/rental/RentalForm.java`
- Create: `campshare/src/main/java/com/example/campshare/web/RentalController.java`
- Create: `campshare/src/main/resources/templates/gear-detail.html`
- Create: `campshare/src/main/resources/templates/rental-form.html`
- Create: `campshare/src/main/resources/templates/rentals.html`
- Create: `campshare/src/test/java/com/example/campshare/web/RentalControllerTest.java`
- Modify: `campshare/src/main/java/com/example/campshare/web/GearController.java`
- Modify: `campshare/src/main/resources/templates/gears.html`
- Modify: `campshare/src/main/resources/templates/home.html`

**Produces:** `GET /gears/{gearId}`、`GET/POST /gears/{gearId}/rentals/new`、`GET /mypage/rentals`。

- [ ] **Step 1: 失敗するWebテストを書く**

`RentalControllerTest` で、未認証の `GET /gears/1/rentals/new` が `/login` へリダイレクトすること、認証済みのフォーム表示が `rental-form` を返すこと、認証済みの `GET /mypage/rentals` が `rentals` と `rentals` モデル属性を返すことを検証する。

- [ ] **Step 2: テストが失敗することを確認する**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test -Dtest=RentalControllerTest`

Expected: `RentalController` 未作成のため失敗。

- [ ] **Step 3: 最小の画面実装を書く**

`RentalForm` は `@NotNull LocalDate startDate/endDate` を持つ。コントローラーは `@AuthenticationPrincipal UserDetails` のメールを `RentalService.reserve` に渡す。成功時は `redirect:/mypage/rentals?reserved=true`、`OutOfStockException` と `InvalidRentalDateException` はフォームへ戻して `errorMessage` を表示する。

`gear-detail.html` は用品名、画像、説明、日額、残在庫、予約リンクを表示する。`rental-form.html` は開始日・終了日の `input type="date"` とCSRFトークンを含むPOSTフォームにする。`rentals.html` は完了メッセージと、商品名、利用期間、料金、状態の履歴一覧を表示する。`gears.html` の各カードに詳細リンクを追加し、`home.html` のログイン中表示にマイページリンクを追加する。

- [ ] **Step 4: Webテストと全テストを緑にする**

Run: `cd campshare; $env:MAVEN_USER_HOME="$PWD\.maven-cache"; .\mvnw.cmd test`

Expected: `BUILD SUCCESS`、Failures 0、Errors 0。

- [ ] **Step 5: ブラウザで一連の操作を確認する**

1. アプリを起動し、`http://localhost:8080/gears` を開く。
2. 任意の用品の詳細から予約フォームへ進み、2日以上の期間で送信する。
3. マイページに予約が表示され、用品一覧の在庫が1減っていることを確認する。
4. 在庫0の商品、または終了日が開始日以前の入力で予約できないことを確認する。
5. ログアウト後に予約URLを開くとログイン画面へ移動することを確認する。

- [ ] **Step 6: コミットする**

```powershell
git add campshare/src/main/java/com/example/campshare/rental/RentalForm.java campshare/src/main/java/com/example/campshare/web campshare/src/main/resources/templates campshare/src/test/java/com/example/campshare/web/RentalControllerTest.java
git commit -m "feat: add rental pages and history"
```

## 実装後の最終確認

- [ ] ローカルPostgreSQLに `rentals` テーブルが存在する。
- [ ] `./mvnw.cmd test` が成功する。
- [ ] 予約成功時のみ在庫が1減る。
- [ ] 予約履歴はログイン本人のものだけが表示される。
- [ ] `application-local.properties` はGitに含まれない。
