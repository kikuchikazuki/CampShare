# CampShare Task 8: 管理者向け用品管理設計

## 目的

管理者だけが用品を追加・編集・削除できる商品管理画面を作る。一般ユーザーの予約画面とは分け、ロールによる認可をデモで明確に示す。

## 管理者アカウント

Flywayの `V5__seed_admin_user.sql` で、以下の開発・デモ用アカウントを初期登録する。

| 項目 | 値 |
| --- | --- |
| 表示名 | CampShare 管理者 |
| メールアドレス | `admin@campshare.local` |
| パスワード | `Admin123!` |
| ロール | `ROLE_ADMIN` |

パスワードは既存の `BCryptPasswordEncoder` と互換のハッシュ `$2a$10$5Euj.lEuo9u3w1gXN1o2Ye.YVUbIJgkEd.2vMJrOAIPQMaWQ3DUo6` として保存する。一般ユーザーのサインアップでは必ず `ROLE_USER` を付与し、管理者アカウントは作れない。

## 認可

Spring Securityで `/admin/**` を `hasRole("ADMIN")` に限定する。一般ユーザーまたは未ログインユーザーがアクセスした場合は管理操作を行えない。用品一覧・商品詳細・予約は従来どおり一般公開する。

ログイン中のユーザーのロールをトップ画面で判定し、管理者だけに「商品管理」リンクを表示する。リンクを隠すだけでなく、URL側の認可も必ず行う。

## 管理画面

| URL | 操作 |
| --- | --- |
| `GET /admin/gears` | 管理用用品一覧 |
| `GET /admin/gears/new` | 追加フォーム |
| `POST /admin/gears` | 用品を追加 |
| `GET /admin/gears/{id}/edit` | 編集フォーム |
| `POST /admin/gears/{id}` | 用品を更新 |
| `POST /admin/gears/{id}/delete` | 用品を削除 |

追加・編集フォームでは、名前、カテゴリ、説明、日額、在庫数、画像URLを入力する。名前・カテゴリ・説明・画像URLは空欄不可、日額と在庫数は0以上の整数にする。成功後は管理用用品一覧へ戻り、完了メッセージを表示する。

## 削除ルール

予約履歴が1件でもある用品は削除しない。`RentalRepository.existsByGearId(Long gearId)` で確認し、該当する場合は管理一覧に「予約履歴があるため削除できません」と表示する。

予約履歴がない用品だけを物理削除する。これにより、既存の予約履歴の用品名を安全に表示し続けられる。

## コンポーネント分割

- `AdminGearForm`: 入力値とBean Validation
- `AdminGearService`: 追加・更新・削除の業務ルールとトランザクション
- `AdminGearController`: 管理用URL、フォーム、成功・失敗メッセージ
- `Gear`: 管理用の作成・更新メソッド
- `RentalRepository`: 削除可否の問い合わせ
- `admin/gears.html` / `admin/gear-form.html`: Bootstrapの管理UI

## テスト方針

- サービス: 正常な追加・更新、予約済み用品の削除拒否、未予約用品の削除。
- Web: 管理者は `/admin/gears` を開ける、一般ユーザーは403、フォームの入力不正は登録されない。
- DB移行: 管理者アカウントが `ROLE_ADMIN` として作成される。

## 完了条件

- 指定の管理者アカウントでログインできる。
- 管理者だけが用品のCRUDを行える。
- 一般ユーザーは管理URLにアクセスできない。
- 予約履歴のある用品は削除できない。
