# CampShare Task 9: 管理者ダッシュボード設計

## 目的

管理者がレンタル状況をひと目で把握できるダッシュボードを作り、授業で使用したMyBatisによる集計SQLを活用する。

## 画面・認可

- URLは `GET /admin/dashboard`。
- `ROLE_ADMIN` のみアクセス可能とし、一般ユーザーは403。
- 商品管理画面からダッシュボードへ移動できる。

## 表示する集計

| 指標 | SQL上の定義 |
| --- | --- |
| 総予約件数 | `rentals` の全件数 |
| 予約売上合計 | `rentals.total_price` の合計。0件なら0 |
| 在庫切れ用品数 | `gears.stock_count = 0` の件数 |

## 実装構成

MyBatis Mapperに集計SQLを置き、結果を `DashboardSummary` DTOへマッピングする。`AdminDashboardController` はDTOをテンプレートへ渡し、Bootstrapの3枚カードで数値を表示する。JPAは既存の更新処理、MyBatisは読み取り専用集計という役割分担にする。

## テスト

- MyBatis Mapperが集計値を返すこと。
- 管理者が画面を開けること。
- 一般ユーザーは `/admin/dashboard` で403となること。

## 完了条件

管理者ログイン後、総予約件数・売上合計・在庫切れ用品数がカード表示される。
