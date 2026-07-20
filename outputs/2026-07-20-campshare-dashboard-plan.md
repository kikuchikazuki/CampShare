# CampShare Dashboard Implementation Plan

**Goal:** MyBatis集計による管理者ダッシュボードを追加する。

### Task 1: MyBatis集計

- [ ] `pom.xml` に `mybatis-spring-boot-starter` を追加する。
- [ ] `DashboardSummary` DTO と `DashboardMapper` を作り、`COUNT(*)`、`COALESCE(SUM(total_price), 0)`、在庫0件数を1つのSQLで取得する。
- [ ] `DashboardMapperTest` を追加し、ローカルPostgreSQLで集計が取得できることを確認する。

### Task 2: 管理画面

- [ ] `AdminDashboardController` を作り、`GET /admin/dashboard` でMapper結果をモデルへ渡す。
- [ ] `templates/admin/dashboard.html` を追加し、予約件数・売上・在庫切れをBootstrapカードで表示する。
- [ ] 商品管理画面にダッシュボードリンクを追加する。
- [ ] 管理者アクセスと一般ユーザー403のWebテストを追加する。

### Task 3: 確認

- [ ] `./mvnw.cmd test` を実行し、`BUILD SUCCESS` を確認する。
- [ ] 管理者で `/admin/dashboard` を開き、3指標を確認する。
- [ ] `git add .` と `git commit -m "feat: add admin dashboard"` を実行する。
