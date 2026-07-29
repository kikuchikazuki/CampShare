# 標準ポート8080 設計書

## 目的

CampShareの標準HTTPポートを、Spring Bootの標準ポートである8080へ戻す。

## 設計

Gitで管理されている `src/main/resources/application.properties` から
`server.port=8081` を削除する。8080はSpring Bootの標準値であるため、
`server.port=8080` は明記しない。

`application-local.properties` にもポート設定を追加しない。異なるローカルポートが
必要な開発者は、Git管理対象外である同ファイルへ設定を追加できる。これにより、
リポジトリ全体の標準ポートには影響しない。

既存のREADMEは、すでに `http://localhost:8080/` を案内しているため、
内容を変更しない。

## 検証

- Git管理対象の設定ファイルに、有効な `server.port` がないことを確認する。
- `application-local.properties` に、有効な `server.port` がないことを確認する。
- Mavenの全テストを実行する。
