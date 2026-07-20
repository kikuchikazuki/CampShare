# CampShare User Signup Design

## Goal

公開の新規登録画面から一般ユーザーを作成できるようにする。登録されたパスワードは平文で保存せず、BCryptでハッシュ化する。

## Scope

- Flywayで`users`テーブルを追加する。
- JPAの`User`・`Role`エンティティとRepositoryを追加する。
- `/signup`の表示と登録処理を追加する。
- 登録されたアカウントには必ず`ROLE_USER`を割り当てる。
- メールアドレス重複と入力不備を画面で通知する。

## Out of Scope

- ログイン、ログアウト、認可設定
- 管理者アカウントを公開画面から作る機能
- 電話番号、住所、プロフィール編集

## Database

Flyway migration `V2__create_users.sql` creates `users`.

| Column | Type | Constraints | Purpose |
| --- | --- | --- | --- |
| `id` | `BIGINT` | identity primary key | User ID |
| `display_name` | `VARCHAR(100)` | not null | Shown name |
| `email` | `VARCHAR(255)` | not null, unique | Login identifier |
| `password_hash` | `VARCHAR(100)` | not null | BCrypt password hash |
| `role_id` | `BIGINT` | not null, foreign key to `roles(id)` | One assigned role |
| `created_at` | `TIMESTAMP` | not null, default current timestamp | Created time |

One user has exactly one role. The `role_id` foreign key preserves the relation to the existing `roles` table. The application always assigns the existing `ROLE_USER` record for public signup.

## Application Design

- `Role` and `User` are JPA entities. `User.role` is a required `@ManyToOne` association.
- `RoleRepository` finds a role by its name. `UserRepository` checks whether an email already exists.
- `SignupForm` carries the display name, email, and raw password from the form and uses Bean Validation.
- `SignupService` is transactional. It rejects duplicate emails, obtains `ROLE_USER`, hashes the password with `BCryptPasswordEncoder`, and saves the user.
- `SignupController` serves `GET /signup` and handles `POST /signup`.
- On validation or duplicate-email failure, the controller redisplays `signup.html`; the raw password is never redisplayed.
- On success, the controller redirects to `/signup?registered=true` and the page displays a one-time success message.

## Dependencies

- `spring-boot-starter-validation` for form validation
- `spring-security-crypto` for BCrypt only; full Spring Security login configuration is intentionally deferred

## Verification

1. Show that the `users` table is absent before V2 migration.
2. Apply V2 and verify columns, unique email, and the `role_id` foreign key.
3. Test successful signup, duplicate-email rejection, and BCrypt storage at the service layer.
4. Test signup form display, validation error rendering, and successful POST handling at the web layer.
5. Run the complete Maven test suite and manually open `/signup`.
