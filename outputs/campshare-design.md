# CampShare 設計仕様

## 目的とスコープ

CampShareは、キャンプ用品を閲覧・予約できるレンタルWebアプリケーションです。一般ユーザーは商品閲覧、セットレンタル、お気に入り、予約、予約履歴を利用します。管理者は商品CRUD、在庫・予約状態管理、集計ダッシュボードを利用します。

認証はSpring Securityで実装します。

- `ROLE_USER`: 商品閲覧、お気に入り、予約、マイページの予約履歴。
- `ROLE_ADMIN`: 上記に加え、商品CRUD、在庫・予約状態管理、集計ダッシュボード。

## キラー機能とデモ

1. **キャンプスタイル別セットレンタル**: 「ソロ初心者セット」「ファミリー1泊セット」などを一括で予約へ追加します。
2. **日程に応じた在庫判定**: 重複する予約の日程・数量を確認し、在庫が足りる場合だけ予約を確定します。
3. **お気に入りとマイページ**: 気になる用品を保存し、予約履歴と予約状態を確認します。

5分デモは、商品一覧→ログイン→セット予約→在庫不足エラー→正常予約→マイページ→管理者の商品・予約管理→一般ユーザーの管理画面アクセス拒否、の順で実演します。

## データモデル

```text
users 1 -- * rentals 1 -- * rental_items * -- 1 gears
users * -- * roles
users 1 -- * favorites * -- 1 gears
gears * -- 1 gear_categories
rental_sets 1 -- * rental_set_items * -- 1 gears
```

| テーブル | 主な列 | 役割 |
|---|---|---|
| `users` | `id`, `name`, `email`, `password`, `enabled`, `created_at` | アカウント。メールアドレスは一意、パスワードはBCryptハッシュで保存。 |
| `roles` / `user_roles` | `id`, `name` / `user_id`, `role_id` | ロールとユーザーの対応。 |
| `gear_categories` | `id`, `name` | テント、寝袋、ランタンなどの分類。 |
| `gears` | `id`, `name`, `description`, `daily_price`, `total_stock`, `image_url`, `category_id`, `is_active` | レンタル用品。 |
| `rentals` | `id`, `user_id`, `start_date`, `end_date`, `status`, `total_amount`, `created_at` | 予約ヘッダー。`RESERVED`、`CANCELLED`、`RENTED`、`RETURNED`を管理。 |
| `rental_items` | `id`, `rental_id`, `gear_id`, `quantity`, `daily_price` | 予約明細。予約時の日額を保存。 |
| `favorites` | `user_id`, `gear_id`, `created_at` | お気に入り。ユーザー・用品の組は一意。 |
| `rental_sets` / `rental_set_items` | `id`, `name`, `...` / `set_id`, `gear_id`, `quantity` | セットの定義と内訳。 |

セット選択時は内訳を`rental_items`へ展開するため、単品とセットを同じ在庫判定に通せます。

## 予約・例外処理

予約確定は`@Transactional`を付けたServiceで行います。

1. `start_date < end_date`を検証する。
2. 対象日程と重複し、`RESERVED`または`RENTED`である予約明細を用品ごとに集計する。
3. `total_stock - 重複予約の数量合計`が希望数量以上であることを確認する。
4. 在庫不足なら業務例外を送出し、予約全体を保存しない。
5. 足りる場合だけ予約と予約明細を保存する。

用品取得には悲観ロック（`PESSIMISTIC_WRITE`）を使い、同時予約による在庫超過を防ぎます。キャンセル時は状態を`CANCELLED`に変更し、在庫計算対象から除外します。存在しない商品は`GearNotFoundException`を送出し、`@ControllerAdvice`で404画面を返します。

## JPAとMyBatisの使い分け

通常のCRUD・予約処理はSpring Data JPAで実装します。Entity、Repository、Service、Controller、DTO/Formを分離します。

MyBatisはCRUDに混ぜず、管理者ダッシュボードの読み取り専用集計に限定します。

- 現在の予約数、貸出中件数、返却待ち件数
- 人気用品ランキング
- カテゴリ別予約数

`AdminDashboardMapper`がJOIN・集計SQLを担当することで、JPAとMyBatisを使い分ける理由を明確にします。

## 技術スタック

- Java 17以上、Spring Boot 3.x、Maven
- PostgreSQL 16
- Spring Web、Thymeleaf、Spring Security、Validation
- Spring Data JPAを通常の永続化に利用
- MyBatis Frameworkを管理画面の集計に利用
- LombokはJPA Entityで`@Data`を避け、原則`@Getter`を利用
- Spring Boot DevToolsを開発時のみ利用

`JDBC API`は`spring-boot-starter-data-jpa`が内部利用するため個別追加しません。H2 Databaseは使用しません。

## application.properties

```properties
spring.application.name=campshare

spring.datasource.url=jdbc:postgresql://localhost:5432/campshare
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:変更してください}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

spring.thymeleaf.cache=false

mybatis.configuration.map-underscore-to-camel-case=true
mybatis.mapper-locations=classpath:/mappers/**/*.xml
```

開発初期は`ddl-auto=update`を使用し、提出前にはデータをバックアップして`validate`へ変更します。実際のDBパスワードは環境変数で渡し、Git管理する設定ファイルには保存しません。

## テスト方針

- 在庫不足時に予約が保存されないこと。
- キャンセル後、同日程の在庫として再利用できること。
- 未ログインでは予約できず、一般ユーザーは管理画面にアクセスできないこと。
- 存在しない商品で404画面になること。
- MyBatisのダッシュボード集計結果が正しいこと。
