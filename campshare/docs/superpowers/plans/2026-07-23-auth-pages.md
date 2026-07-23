# CampShare Auth Pages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give login and signup a shared CampShare form-card design without changing their existing behavior.

**Architecture:** Create one auth-only stylesheet and load it in both Thymeleaf templates. Existing routes, HTTP methods, form actions, model bindings, error rendering, and controllers stay intact.

**Tech Stack:** Spring Boot 3.5.16, Thymeleaf, Spring MVC Test, CSS.

## Global Constraints

- Keep all new visual rules in `src/main/resources/static/css/auth.css`; do not use inline styles.
- Preserve `/login`, `/signup`, form actions, methods, `signupForm`, `param.error`, `registered`, and `th:errors`.
- Use forest green `#31533B`, canvas `#F8F3E8`, surface `#FFFDF8`, and accessible red error text.
- Do not modify database, security, controllers, or services.

---

### Task 1: Define the rendered authentication-page contract

**Files:**
- Modify: `src/test/java/com/example/campshare/web/LoginControllerTest.java`
- Modify: `src/test/java/com/example/campshare/web/SignupControllerTest.java`

**Interfaces:**
- Consumes: GET `/login` and GET `/signup?registered=true`.
- Produces: regression checks for the shared stylesheet and corrected success copy.

- [ ] **Step 1: Add failing content assertions**

```java
import static org.hamcrest.Matchers.containsString;

mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(containsString("/css/auth.css")));

mockMvc.perform(get("/signup").param("registered", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("signup"))
        .andExpect(content().string(containsString("/css/auth.css")))
        .andExpect(content().string(containsString("登録が完了しました。ログインしてください。")));
```

- [ ] **Step 2: Confirm the tests are red**

Run: `./mvnw test -Dtest=LoginControllerTest,SignupControllerTest`

Expected: failure because neither template renders `/css/auth.css` and the signup message differs.

- [ ] **Step 3: Commit the red test**

Run: `git add src/test/java/com/example/campshare/web/LoginControllerTest.java src/test/java/com/example/campshare/web/SignupControllerTest.java && git commit -m "test: define auth page presentation contract"`

### Task 2: Add the shared auth stylesheet

**Files:**
- Create: `src/main/resources/static/css/auth.css`

**Interfaces:**
- Consumes: `.auth-page`, `.auth-card`, `.auth-brand`, `.auth-form`, `.auth-field`, `.auth-input`, `.auth-alert`, `.auth-error`, `.auth-submit`, and `.auth-link` classes.
- Produces: a responsive, self-contained visual system for both pages.

- [ ] **Step 1: Create the CSS component rules**

```css
:root { --auth-forest: #31533B; --auth-canvas: #F8F3E8; --auth-surface: #FFFDF8; --auth-text: #26372A; --auth-muted: #718071; --auth-danger: #B42318; }
.auth-page { min-height: 100vh; margin: 0; display: grid; place-items: center; padding: 24px; background: var(--auth-canvas); color: var(--auth-text); font-family: system-ui, sans-serif; }
.auth-card { width: min(100%, 440px); padding: 32px; border-radius: 20px; background: var(--auth-surface); box-shadow: 0 14px 32px rgb(38 55 42 / 14%); }
.auth-brand { margin: 0; color: var(--auth-forest); font-family: Georgia, serif; font-size: 1.75rem; }
.auth-form { display: grid; gap: 18px; margin-top: 24px; }
.auth-field { display: grid; gap: 8px; }
.auth-input { min-height: 44px; box-sizing: border-box; width: 100%; border: 1px solid #B8C2B8; border-radius: 10px; padding: 0 12px; font: inherit; }
.auth-input:focus { outline: 3px solid rgb(88 116 90 / 35%); border-color: var(--auth-forest); }
.auth-submit { min-height: 44px; border: 0; border-radius: 10px; background: var(--auth-forest); color: #fff; font: inherit; font-weight: 700; cursor: pointer; }
.auth-alert, .auth-error { margin: 0; color: var(--auth-danger); }
.auth-alert { border-radius: 10px; padding: 12px; background: #FCE8E6; }
.auth-link { margin: 20px 0 0; color: var(--auth-muted); text-align: center; }
.auth-link a { color: var(--auth-forest); font-weight: 700; }
@media (max-width: 480px) { .auth-page { padding: 16px; } .auth-card { padding: 24px 20px; } }
```

- [ ] **Step 2: Check CSS is not embedded in the templates**

Run: `rg -n "<style|style=" src/main/resources/templates/login.html src/main/resources/templates/signup.html`

Expected: no output.

- [ ] **Step 3: Commit the stylesheet**

Run: `git add src/main/resources/static/css/auth.css && git commit -m "feat: add shared auth page stylesheet"`

### Task 3: Apply the shared auth layout

**Files:**
- Modify: `src/main/resources/templates/login.html`
- Modify: `src/main/resources/templates/signup.html`

**Interfaces:**
- Consumes: the Task 2 CSS classes plus current `param.error`, `registered`, `signupForm`, and `th:errors` values.
- Produces: two responsive pages with unchanged form submissions and links between them.

- [ ] **Step 1: Load the shared stylesheet in both heads**

```html
<link rel="stylesheet" th:href="@{/css/auth.css}">
```

- [ ] **Step 2: Use this login-card structure while retaining the existing POST contract**

```html
<body class="auth-page"><main class="auth-card">
  <p class="auth-brand">CampShare</p><h1>ログイン</h1>
  <p class="auth-alert" th:if="${param.error}">メールアドレスまたはパスワードが正しくありません。</p>
  <form class="auth-form" th:action="@{/login}" method="post">
    <div class="auth-field"><label for="email">メールアドレス</label><input class="auth-input" id="email" name="email" type="email" required></div>
    <div class="auth-field"><label for="password">パスワード</label><input class="auth-input" id="password" name="password" type="password" required></div>
    <button class="auth-submit" type="submit">ログイン</button>
  </form>
  <p class="auth-link">アカウントをお持ちでない方は <a th:href="@{/signup}">新規登録</a></p>
</main></body>
```

- [ ] **Step 3: Use matching field markup for every signup input**

```html
<div class="auth-field"><label for="displayName">表示名</label><input class="auth-input" id="displayName" type="text" th:field="*{displayName}"><p class="auth-error" th:errors="*{displayName}"></p></div>
<div class="auth-field"><label for="email">メールアドレス</label><input class="auth-input" id="email" type="email" th:field="*{email}"><p class="auth-error" th:errors="*{email}"></p></div>
<div class="auth-field"><label for="password">パスワード</label><input class="auth-input" id="password" type="password" th:field="*{password}"><p class="auth-error" th:errors="*{password}"></p></div>
```

- [ ] **Step 4: Set the success notification and reciprocal link**

```html
<p class="auth-alert" th:if="${registered}">登録が完了しました。ログインしてください。</p>
<p class="auth-link">すでにアカウントをお持ちの方は <a th:href="@{/login}">ログイン</a></p>
```

- [ ] **Step 5: Verify the green state**

Run: `./mvnw test -Dtest=LoginControllerTest,SignupControllerTest`

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Run the complete test suite**

Run: `./mvnw test`

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Manually inspect both pages**

Open `http://localhost:8080/login` and `http://localhost:8080/signup` at desktop and narrow widths. Confirm focus rings, field-level errors, submit buttons, and cross-links.

- [ ] **Step 8: Commit template changes**

Run: `git add src/main/resources/templates/login.html src/main/resources/templates/signup.html && git commit -m "feat: style CampShare authentication pages"`
