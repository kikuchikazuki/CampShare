# CampShare

キャンプ用品をオンラインで閲覧・予約できるレンタルWebアプリケーションです。

## 主な機能

- ユーザー新規登録、ログイン、ログアウト
- キャンプ用品の一覧・詳細表示
- ログインユーザーによる日付指定の予約
- トランザクションと悲観ロックによる在庫減算・在庫切れ防止
- マイページでの予約履歴表示
- 管理者ロールによる商品管理（追加・編集・削除）
- 予約済み用品の削除防止
- MyBatisによる管理者ダッシュボード集計

## 技術スタック

- Java 17
- Spring Boot 3.5.16
- Spring Security
- Spring Data JPA
- MyBatis
- Thymeleaf / Bootstrap 5
- PostgreSQL 16
- Flyway
- Maven Wrapper

## 起動方法

### 1. ローカル設定ファイルを作成

exampleをコピーします。

```powershell
Copy-Item application-local.example.properties application-local.properties
```

コピー後、`application-local.properties` の `YOUR_USERNAME` と
`YOUR_PASSWORD` を実際のDB接続情報へ置き換えてください。
`application-local.example.properties` には実際の認証情報を書かないでください。
`application-local.properties` はGit管理対象外です。

### 2. テスト

```powershell
$env:MAVEN_USER_HOME="$PWD\.maven-cache"
.\mvnw.cmd test
```

### 3. アプリ起動

IntelliJ IDEAで `CampshareApplication` を起動し、ブラウザで `http://localhost:8080/` を開きます。

## デモ用アカウント

| 種別 | メールアドレス | パスワード |
| --- | --- | --- |
| 管理者 | `admin@campshare.local` | `Admin123!` |
| 一般ユーザー | サインアップ画面から作成 | 登録したパスワード |

## 管理者機能

管理者でログインすると、トップページの「商品管理」から以下を操作できます。

- 用品の追加・編集・削除
- 予約件数、予約売上、在庫切れ用品数のダッシュボード確認

予約履歴がある用品は削除できません。

## データベース設計

- `roles`: ロール（`ROLE_USER` / `ROLE_ADMIN`）
- `users`: ユーザー情報とロール
- `gears`: レンタル用品、日額、在庫数
- `rentals`: 予約、利用期間、合計料金

スキーマ変更はFlyway migrationで管理しています。
