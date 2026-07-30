# ローカル設定example導入 設計

## 目的

DB接続情報の実値をGitへ含めず、新しいPCやworktreeで必要な設定項目と作成手順が分かるようにする。

## ファイル構成

- `application-local.example.properties`
  - Git管理する。
  - PostgreSQL接続に必要なプロパティを記載する。
  - ユーザー名とパスワードは `YOUR_USERNAME`、`YOUR_PASSWORD` のようなダミー値にする。
- `application-local.properties`
  - Git管理しない。
  - exampleをコピーして作成し、利用者が実際の接続情報を入力する。
  - 現在のローカルファイルは変更・削除しない。
- `.gitignore`
  - `application-local.properties` の除外設定を維持する。
- `README.md`
  - Git管理へ追加する。
  - exampleをコピーするPowerShellコマンドと、コピー先だけに実値を書く手順を説明する。

## セキュリティ

- exampleとREADMEへ実際のDBユーザー名、パスワード、秘密情報を記載しない。
- `application-local.properties` がGit追跡対象になっていないことを検証する。

## 検証

- exampleに必要な4項目がある。
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`
  - `spring.datasource.driver-class-name`
- READMEのコピーコマンドが実際のファイル名と一致する。
- `git check-ignore application-local.properties` が成功する。
- Git差分に実際のローカル設定値が含まれない。
