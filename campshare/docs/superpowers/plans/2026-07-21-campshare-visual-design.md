# CampShare Visual Design Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the approved warm forest-themed design to every existing CampShare page while preserving all routes, model bindings, security rules, and business behavior.

**Architecture:** Keep Spring Boot controllers and domain services unchanged. Compose the Thymeleaf pages from shared public/admin fragments, retain Bootstrap 5 for grid utilities, and centralize the CampShare visual system and responsive behavior in one static stylesheet. Verify the HTML contract with a resource-level JUnit test, retain the existing MVC/service tests, and visually inspect rendered pages at desktop and mobile widths.

**Tech Stack:** Java 17, Spring Boot 3.5.16, Thymeleaf, Spring Security, Bootstrap 5.3.3, CSS, JUnit 5, Maven Wrapper

## Global Constraints

- Preserve every existing URL, controller method, model attribute, form action, CSRF field, validation binding, authentication rule, authorization rule, and reservation/inventory rule.
- Do not add database changes, search, filters, payments, reservation cancellation, or new business features.
- Treat desktop and smartphone layouts as equal priorities.
- Use `#31533B` for primary forest, `#58745A` for primary light, `#F8F3E8` for canvas, `#FFFDF8` for surface, `#E98B45` for the reservation accent, `#26372A` for text, and `#718071` for muted text.
- Reserve orange for customer reservation actions; use forest green for normal actions and red for errors or destructive actions.
- Keep product images bound to the existing `imageUrl`. Store the hero as a local optimized WebP without baked-in text.
- Replace any corrupted Japanese presentation text encountered in the edited templates with clear UTF-8 Japanese copy.
- Do not introduce a JavaScript framework or a Thymeleaf layout dependency.

---

### Task 1: Establish the shared visual foundation

**Files:**
- Create: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/templates/fragments/admin-layout.html`
- Create: `src/main/resources/static/css/campshare.css`
- Create: `src/main/java/com/example/campshare/web/NavigationModelAdvice.java`
- Create: `src/test/java/com/example/campshare/web/NavigationModelAdviceTest.java`
- Create: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: Spring Security `Authentication`, Bootstrap 5.3.3 from CDN, and existing routes `/`, `/gears`, `/mypage/rentals`, `/login`, `/signup`, `/logout`, `/admin/dashboard`, `/admin/gears`.
- Produces: Global model attributes `email: String?` and `admin: boolean`; Thymeleaf fragments `siteHead(title)`, `siteHeader()`, `siteFooter()`, `flash(type, message)`, `adminHeader(active)`; and CSS classes prefixed with `cs-` for later tasks.

- [ ] **Step 1: Add the failing shared-foundation contract test**

Create this complete test class:

```java
package com.example.campshare.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class VisualTemplateContractTest {

    private String resource(String path) throws IOException {
        return new ClassPathResource(path)
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void sharedLayoutDefinesBrandNavigationAndStylesheet() throws IOException {
        String layout = resource("templates/fragments/layout.html");
        assertThat(layout)
                .contains("th:fragment=\"siteHead(title)\"")
                .contains("th:fragment=\"siteHeader()\"")
                .contains("th:fragment=\"siteFooter()\"")
                .contains("/css/campshare.css")
                .contains("CampShare");
    }

    @Test
    void adminLayoutDefinesAdminNavigation() throws IOException {
        String layout = resource("templates/fragments/admin-layout.html");
        assertThat(layout)
                .contains("th:fragment=\"adminHeader(active)\"")
                .contains("/admin/dashboard")
                .contains("/admin/gears");
    }

    @Test
    void stylesheetDefinesApprovedTokensAndResponsiveRules() throws IOException {
        String css = resource("static/css/campshare.css");
        assertThat(css)
                .contains("--cs-forest: #31533b")
                .contains("--cs-accent: #e98b45")
                .contains("--cs-canvas: #f8f3e8")
                .contains(".cs-button--reservation")
                .contains("@media (max-width: 767.98px)")
                .contains("prefers-reduced-motion");
    }
}
```

Also create this failing unit test for the global navigation state:

```java
package com.example.campshare.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NavigationModelAdviceTest {

    private final NavigationModelAdvice advice = new NavigationModelAdvice();

    @Test
    void anonymousNavigationHasNoEmailOrAdminAccess() {
        AnonymousAuthenticationToken anonymous = mock(AnonymousAuthenticationToken.class);
        assertThat(advice.email(anonymous)).isNull();
        assertThat(advice.admin(anonymous)).isFalse();
    }

    @Test
    void adminNavigationExposesEmailAndAdminAccess() {
        TestingAuthenticationToken admin = new TestingAuthenticationToken(
                "admin@campshare.local", "password", "ROLE_ADMIN");
        assertThat(advice.email(admin)).isEqualTo("admin@campshare.local");
        assertThat(advice.admin(admin)).isTrue();
    }
}
```

- [ ] **Step 2: Run the contract test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest,NavigationModelAdviceTest test
```

Expected: FAIL because the advice, two fragment files, and stylesheet do not exist.

- [ ] **Step 3: Add global authentication state for shared navigation**

Create this presentation-only controller advice; it does not change routes or business logic:

```java
package com.example.campshare.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavigationModelAdvice {

    @ModelAttribute("email")
    public String email(Authentication authentication) {
        return isSignedIn(authentication) ? authentication.getName() : null;
    }

    @ModelAttribute("admin")
    public boolean admin(Authentication authentication) {
        return isSignedIn(authentication) && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isSignedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
```

- [ ] **Step 4: Create the shared public and admin fragments**

Implement `layout.html` as a valid UTF-8 Thymeleaf fragment document containing:

```html
<th:block xmlns:th="http://www.thymeleaf.org">
    <th:block th:fragment="siteHead(title)">
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title th:text="${title}">CampShare</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link th:href="@{/css/campshare.css}" rel="stylesheet">
    </th:block>
    <header class="cs-site-header" th:fragment="siteHeader()">
        <div class="cs-container cs-site-header__inner">
            <a class="cs-brand" th:href="@{/}" aria-label="CampShare トップへ">CampShare</a>
            <nav class="cs-site-nav" aria-label="メインナビゲーション">
                <a th:href="@{/gears}">ギアを探す</a>
                <a th:href="@{/mypage/rentals}" th:if="${email != null}">予約履歴</a>
                <a th:href="@{/login}" th:if="${email == null}">ログイン</a>
                <a class="cs-button cs-button--small" th:href="@{/signup}" th:if="${email == null}">新規登録</a>
                <a th:href="@{/admin/dashboard}" th:if="${admin}">管理画面</a>
                <form th:action="@{/logout}" method="post" th:if="${email != null}">
                    <button class="cs-link-button" type="submit">ログアウト</button>
                </form>
            </nav>
        </div>
    </header>
    <footer class="cs-site-footer" th:fragment="siteFooter()">
        <div class="cs-container"><strong>CampShare</strong><span>借りて、もっと気軽に自然の中へ。</span></div>
    </footer>
    <div th:fragment="flash(type, message)" class="cs-alert" th:classappend="| cs-alert--${type}|" role="status">
        <span th:text="${message}">お知らせ</span>
    </div>
</th:block>
```

Implement `admin-layout.html` with `adminHeader(active)`, the CampShare Admin brand, links to `/admin/dashboard`, `/admin/gears`, and `/`, plus `aria-current="page"` when `active` matches `dashboard` or `gears`.

- [ ] **Step 5: Create the initial visual-system stylesheet**

Start `campshare.css` with the exact shared contract below, then include complete base rules for `body`, headings, links, images, `.cs-container`, header, navigation, footer, buttons, cards, form controls, tables, alerts, badges, focus states, the 767.98px mobile layout, and reduced motion:

```css
:root {
    --cs-forest: #31533b;
    --cs-forest-light: #58745a;
    --cs-canvas: #f8f3e8;
    --cs-surface: #fffdf8;
    --cs-accent: #e98b45;
    --cs-text: #26372a;
    --cs-muted: #718071;
    --cs-danger: #b23a35;
    --cs-border: #e3dccf;
    --cs-shadow: 0 12px 32px rgba(38, 55, 42, 0.10);
    --cs-radius: 16px;
}

body { margin: 0; color: var(--cs-text); background: var(--cs-canvas); font-family: system-ui, -apple-system, "Segoe UI", sans-serif; }
h1, h2, h3, p { margin-top: 0; }
a { color: var(--cs-forest); font-weight: 700; text-underline-offset: 3px; }
img { display: block; max-width: 100%; }
.cs-display { letter-spacing: -.025em; line-height: 1.18; }
.cs-container { width: min(1120px, calc(100% - 40px)); margin-inline: auto; }
.cs-brand, .cs-display { font-family: Georgia, "Yu Mincho", serif; }
.cs-site-header { position: sticky; top: 0; z-index: 20; background: rgba(255,253,248,.96); border-bottom: 1px solid var(--cs-border); }
.cs-site-header__inner { min-height: 72px; display: flex; align-items: center; justify-content: space-between; gap: 24px; }
.cs-site-nav { display: flex; align-items: center; gap: 20px; }
.cs-site-nav form { margin: 0; }
.cs-site-footer { margin-top: 72px; padding: 32px 0; color: #fff; background: var(--cs-forest); }
.cs-site-footer .cs-container { display: flex; justify-content: space-between; gap: 16px; }
.cs-button { min-height: 44px; display: inline-flex; align-items: center; justify-content: center; padding: 0 20px; border: 0; border-radius: 999px; color: #fff; background: var(--cs-forest); font-weight: 700; text-decoration: none; }
.cs-button--reservation { background: var(--cs-accent); }
.cs-button--danger { background: var(--cs-danger); }
.cs-button--small { min-height: 36px; padding-inline: 14px; }
.cs-link-button { border: 0; padding: 0; color: var(--cs-forest); background: transparent; font: inherit; font-weight: 700; }
.cs-card { border: 1px solid var(--cs-border); border-radius: var(--cs-radius); background: var(--cs-surface); box-shadow: var(--cs-shadow); }
.cs-card h2, .cs-card h3 { color: var(--cs-text); }
input, textarea, select { width: 100%; min-height: 46px; padding: 10px 12px; border: 1px solid #b9c1b7; border-radius: 10px; color: var(--cs-text); background: #fff; font: inherit; }
textarea { min-height: 120px; resize: vertical; }
table { width: 100%; border-collapse: collapse; background: var(--cs-surface); }
th, td { padding: 14px 16px; border-bottom: 1px solid var(--cs-border); text-align: left; vertical-align: middle; }
.cs-alert { padding: 14px 16px; border-radius: 12px; border: 1px solid var(--cs-border); background: var(--cs-surface); }
.cs-alert--success { border-color: #82a78b; background: #edf6ef; }
.cs-alert--danger { border-color: #d99a96; background: #fff0ef; color: #7d2925; }
:where(a, button, input, textarea):focus-visible { outline: 3px solid var(--cs-accent); outline-offset: 3px; }
@media (max-width: 767.98px) {
    .cs-container { width: min(100% - 28px, 1120px); }
    .cs-site-header__inner { min-height: 64px; align-items: flex-start; padding-block: 14px; }
    .cs-site-nav { justify-content: flex-end; flex-wrap: wrap; gap: 10px 14px; }
    .cs-site-footer .cs-container { flex-direction: column; }
}
@media (prefers-reduced-motion: reduce) {
    *, *::before, *::after { scroll-behavior: auto !important; transition-duration: .01ms !important; animation-duration: .01ms !important; }
}
```

- [ ] **Step 6: Run the contract test and full test suite**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest,NavigationModelAdviceTest test
.\mvnw.cmd test
```

Expected: `VisualTemplateContractTest` passes, followed by `BUILD SUCCESS` for the complete suite.

- [ ] **Step 7: Commit the foundation**

```powershell
git add src/main/java/com/example/campshare/web/NavigationModelAdvice.java src/main/resources/templates/fragments src/main/resources/static/css/campshare.css src/test/java/com/example/campshare/web/NavigationModelAdviceTest.java src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "feat: add shared CampShare visual system"
```

### Task 2: Build the branded top page and hero asset

**Files:**
- Create: `src/main/resources/static/images/campshare-hero.webp`
- Modify: `src/main/resources/templates/home.html`
- Modify: `src/main/resources/static/css/campshare.css`
- Modify: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: `siteHead(title)`, `siteHeader()`, `siteFooter()`, `.cs-container`, `.cs-button`, `.cs-button--reservation`, and model attributes `email` and `admin`.
- Produces: `.cs-hero`, `.cs-hero__copy`, `.cs-hero__media`, `.cs-home-grid`, and a local `/images/campshare-hero.webp` asset.

- [ ] **Step 1: Add the failing home-page contract test**

Add this method to `VisualTemplateContractTest`:

```java
@Test
void homeUsesSharedLayoutAndLocalHeroImage() throws IOException {
    String home = resource("templates/home.html");
    assertThat(home)
            .contains("siteHead('CampShare')")
            .contains("siteHeader()")
            .contains("cs-hero")
            .contains("/images/campshare-hero.webp")
            .contains("キャンプ用品を探す")
            .contains("siteFooter()");
    assertThat(new ClassPathResource("static/images/campshare-hero.webp").exists()).isTrue();
}
```

- [ ] **Step 2: Run the home contract test and verify it fails**

Run `.\mvnw.cmd -Dtest=VisualTemplateContractTest#homeUsesSharedLayoutAndLocalHeroImage test`.

Expected: FAIL because the home template has no shared layout and the image does not exist.

- [ ] **Step 3: Generate and optimize the hero image**

Use the `imagegen` skill to create one landscape image with this prompt:

```text
A warm editorial outdoor photograph for a Japanese camping gear rental website. A forest-edge campsite in soft golden morning light, with a cream family tent, neatly arranged wooden camping chairs, lantern, rolled sleeping bag, and compact cooking gear clearly visible. No prominent people, no logos, no lettering, no watermarks. Natural forest greens, warm sand and amber accents, realistic photography, calm welcoming mood, wide horizontal composition with the main gear slightly right of center, suitable for a website hero image.
```

Save the final optimized result as `src/main/resources/static/images/campshare-hero.webp`, approximately 1600×1000, under 350 KB. Verify with:

```powershell
Get-Item src\main\resources\static\images\campshare-hero.webp | Select-Object Name,Length
```

Expected: the file exists and `Length` is below `358400` bytes.

- [ ] **Step 4: Replace the top page with the approved responsive structure**

Use a normal HTML document with `<head th:replace="~{fragments/layout :: siteHead('CampShare')}">`, `<header th:replace="~{fragments/layout :: siteHeader()}">`, and `<footer th:replace="~{fragments/layout :: siteFooter()}">`. The main content must contain:

```html
<main>
    <section class="cs-hero">
        <div class="cs-container cs-hero__grid">
            <div class="cs-hero__copy">
                <p class="cs-eyebrow">GO LIGHT. CAMP MORE.</p>
                <h1 class="cs-display">借りて、もっと気軽に<br>自然の中へ。</h1>
                <p>必要なキャンプ用品を、使いたい日だけ。お気に入りのギアを見つけて、すぐに予約できます。</p>
                <div class="cs-action-row">
                    <a class="cs-button cs-button--reservation" th:href="@{/gears}">キャンプ用品を探す</a>
                    <a class="cs-button cs-button--ghost" href="#how-it-works">利用方法を見る</a>
                </div>
            </div>
            <div class="cs-hero__media" role="img" aria-label="森のキャンプサイトに並ぶテントとキャンプ用品"></div>
        </div>
    </section>
    <section class="cs-section" id="how-it-works">
        <div class="cs-container">
            <p class="cs-eyebrow">HOW IT WORKS</p>
            <h2 class="cs-display">かんたん3ステップ</h2>
            <div class="cs-home-grid">
                <article class="cs-card"><strong>01</strong><h3>ギアを選ぶ</h3><p>商品一覧から使いたいキャンプ用品を探します。</p></article>
                <article class="cs-card"><strong>02</strong><h3>日付を決める</h3><p>利用開始日と返却日を入力して予約します。</p></article>
                <article class="cs-card"><strong>03</strong><h3>自然を楽しむ</h3><p>道具の準備を軽くしてキャンプへ出かけます。</p></article>
            </div>
        </div>
    </section>
</main>
```

Do not leave the mockup text `CAMP GEAR` in the page or image.

- [ ] **Step 5: Add the complete hero and home-grid responsive rules**

Append rules that implement a two-column hero above 768px, vertical stacking below 768px, a `background-image: url('/images/campshare-hero.webp')` media panel with `background-size: cover`, a forest gradient copy panel, an orange reservation button, and a three/two/one-column `.cs-home-grid`. Give the hero media an aspect ratio on mobile so it cannot collapse.

- [ ] **Step 6: Run focused and full tests**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest#homeUsesSharedLayoutAndLocalHeroImage,HomeControllerTest test
.\mvnw.cmd test
```

Expected: both focused classes pass and the full suite ends with `BUILD SUCCESS`.

- [ ] **Step 7: Commit the top page**

```powershell
git add src/main/resources/templates/home.html src/main/resources/static/css/campshare.css src/main/resources/static/images/campshare-hero.webp src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "feat: redesign CampShare home page"
```

### Task 3: Redesign product discovery and detail pages

**Files:**
- Modify: `src/main/resources/templates/gears.html`
- Modify: `src/main/resources/templates/gear-detail.html`
- Modify: `src/main/resources/static/css/campshare.css`
- Modify: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: Shared public fragments and existing `gears` / `gear` model attributes with `id`, `name`, `category`, `description`, `dailyPrice`, `stockCount`, and `imageUrl`.
- Produces: `.cs-page-heading`, `.cs-gear-grid`, `.cs-gear-card`, `.cs-gear-detail`, `.cs-stock`, and a visible sold-out state.

- [ ] **Step 1: Add failing product-template contract tests**

```java
@Test
void gearPagesExposeProductAndReservationContracts() throws IOException {
    String list = resource("templates/gears.html");
    String detail = resource("templates/gear-detail.html");
    assertThat(list)
            .contains("siteHeader()")
            .contains("cs-gear-grid")
            .contains("th:each=\"gear : ${gears}\"")
            .contains("${gear.imageUrl}")
            .contains("${gear.stockCount}");
    assertThat(detail)
            .contains("cs-gear-detail")
            .contains("${gear.imageUrl}")
            .contains("/rentals/new")
            .contains("gear.stockCount > 0")
            .contains("在庫切れ");
}
```

- [ ] **Step 2: Run the focused contract and verify it fails**

Run `.\mvnw.cmd -Dtest=VisualTemplateContractTest#gearPagesExposeProductAndReservationContracts test`.

Expected: FAIL because the approved classes and sold-out condition are absent.

- [ ] **Step 3: Rebuild `gears.html` with semantic cards**

Keep `th:each="gear : ${gears}"`. Each `<article class="cs-card cs-gear-card">` must contain an image wrapper with the existing source and alt binding, category badge, `h2` name, description, formatted daily price, stock text, and link to `/gears/{id}`. Add a meaningful empty state guarded by `th:if="${#lists.isEmpty(gears)}"` and do not change the repository or controller.

- [ ] **Step 4: Rebuild `gear-detail.html` with conditional reservation state**

Use a two-column `<article class="cs-card cs-gear-detail">`. Preserve all existing displayed values. Render the orange reservation link only with `th:if="${gear.stockCount > 0}"`; render `<span class="cs-button cs-button--disabled" aria-disabled="true">在庫切れ</span>` with `th:unless="${gear.stockCount > 0}"`.

- [ ] **Step 5: Add product grid, card, and detail CSS**

Implement three columns at large widths, two columns between 768px and 991.98px, and one column below 768px. Use `aspect-ratio: 4 / 3` and `object-fit: cover` for card images. Align card actions using `display: flex`, `flex-direction: column`, and `margin-top: auto`. Switch `.cs-gear-detail` from two columns to one below 768px.

- [ ] **Step 6: Run product and regression tests**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest#gearPagesExposeProductAndReservationContracts,GearControllerTest test
.\mvnw.cmd test
```

Expected: focused tests pass; full suite reports `BUILD SUCCESS`.

- [ ] **Step 7: Commit product pages**

```powershell
git add src/main/resources/templates/gears.html src/main/resources/templates/gear-detail.html src/main/resources/static/css/campshare.css src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "feat: redesign CampShare gear pages"
```

### Task 4: Redesign reservation and account flows

**Files:**
- Modify: `src/main/resources/templates/rental-form.html`
- Modify: `src/main/resources/templates/rentals.html`
- Modify: `src/main/resources/templates/login.html`
- Modify: `src/main/resources/templates/signup.html`
- Modify: `src/main/resources/static/css/campshare.css`
- Modify: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: Shared public fragments; `gear`, `rentalForm`, `rentals`, `signupForm`, `errorMessage`, `param.reserved`, `param.error`, and `param.registered`; existing CSRF bindings and form actions.
- Produces: `.cs-form-shell`, `.cs-form-card`, `.cs-field`, `.cs-field-error`, `.cs-rental-summary`, `.cs-rental-list`, `.cs-empty-state`.

- [ ] **Step 1: Add failing reservation/account contract tests**

```java
@Test
void formsAndRentalHistoryKeepBindingsInsideSharedDesign() throws IOException {
    String rentalForm = resource("templates/rental-form.html");
    String rentals = resource("templates/rentals.html");
    String login = resource("templates/login.html");
    String signup = resource("templates/signup.html");
    assertThat(rentalForm)
            .contains("siteHeader()")
            .contains("th:object=\"${rentalForm}\"")
            .contains("*{startDate}")
            .contains("*{endDate}")
            .contains("${_csrf.parameterName}")
            .contains("cs-button--reservation");
    assertThat(rentals)
            .contains("th:each=\"rental : ${rentals}\"")
            .contains("cs-empty-state")
            .contains("param.reserved");
    assertThat(login).contains("name=\"email\"").contains("name=\"password\"").contains("param.error");
    assertThat(signup).contains("th:object=\"${signupForm}\"").contains("*{displayName}").contains("*{email}").contains("*{password}");
}
```

- [ ] **Step 2: Run the contract test and verify it fails**

Run `.\mvnw.cmd -Dtest=VisualTemplateContractTest#formsAndRentalHistoryKeepBindingsInsideSharedDesign test`.

Expected: FAIL because shared layout and new state classes are absent.

- [ ] **Step 3: Rebuild the reservation form without changing submission behavior**

Use a centered `.cs-form-shell`, keep the existing `/gears/{id}/rentals/new` POST action, `rentalForm` binding, CSRF hidden input, `startDate`, `endDate`, and `errorMessage`. Show the gear name and daily price in `.cs-rental-summary`. Put each label, input, and `th:errors` element inside `.cs-field`; use `aria-describedby` IDs for error containers. Make only “予約を確定する” orange.

- [ ] **Step 4: Rebuild rental history with desktop table and mobile cards**

Keep a semantic table for desktop and render the same `rentals` collection as `.cs-rental-list` cards below 768px. Both representations must show gear name, start/end dates, total price, and status. Preserve the `param.reserved` success message. When empty, show “まだ予約はありません” and a green link to `/gears` inside `.cs-empty-state`.

- [ ] **Step 5: Rebuild login and signup templates**

Use the same `.cs-form-shell` and `.cs-form-card`. Preserve login input names `email` and `password`, POST `/login`, `param.error`, signup POST `/signup`, `signupForm`, all field bindings/errors, and `param.registered`. Use Japanese labels and place the login/signup cross-link after the submit button.

- [ ] **Step 6: Add form, error, empty-state, and responsive history CSS**

Set the form shell maximum width to 640px; inputs to at least 46px tall; labels to semibold; field errors to danger color with reserved vertical space. Hide `.cs-rental-list` on desktop and the table on mobile using complementary media rules. Ensure error and success alerts remain readable without relying only on color.

- [ ] **Step 7: Run focused and full tests**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest#formsAndRentalHistoryKeepBindingsInsideSharedDesign,RentalControllerTest,LoginControllerTest,SignupControllerTest test
.\mvnw.cmd test
```

Expected: focused controller and contract tests pass; full suite reports `BUILD SUCCESS`.

- [ ] **Step 8: Commit reservation and account pages**

```powershell
git add src/main/resources/templates/rental-form.html src/main/resources/templates/rentals.html src/main/resources/templates/login.html src/main/resources/templates/signup.html src/main/resources/static/css/campshare.css src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "feat: redesign reservation and account flows"
```

### Task 5: Redesign all administration pages

**Files:**
- Modify: `src/main/resources/templates/admin/dashboard.html`
- Modify: `src/main/resources/templates/admin/gears.html`
- Modify: `src/main/resources/templates/admin/gear-form.html`
- Modify: `src/main/resources/static/css/campshare.css`
- Modify: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: `adminHeader(active)`, `summary`, `gears`, `gearForm`, `gearId`, existing query flags, form actions, and CSRF fields.
- Produces: `.cs-admin-shell`, `.cs-admin-grid`, `.cs-stat-card`, `.cs-admin-table`, `.cs-admin-form`.

- [ ] **Step 1: Add the failing admin-template contract test**

```java
@Test
void adminPagesUseSharedAdminLayoutAndKeepActions() throws IOException {
    String dashboard = resource("templates/admin/dashboard.html");
    String gears = resource("templates/admin/gears.html");
    String form = resource("templates/admin/gear-form.html");
    assertThat(dashboard)
            .contains("adminHeader('dashboard')")
            .contains("${summary.rentalCount}")
            .contains("${summary.totalSales}")
            .contains("${summary.outOfStockCount}");
    assertThat(gears)
            .contains("adminHeader('gears')")
            .contains("th:each=\"gear : ${gears}\"")
            .contains("/delete")
            .contains("${_csrf.parameterName}")
            .contains("deleteError");
    assertThat(form)
            .contains("th:object=\"${gearForm}\"")
            .contains("*{imageUrl}")
            .contains("${gearId == null}");
}
```

- [ ] **Step 2: Run the admin contract and verify it fails**

Run `.\mvnw.cmd -Dtest=VisualTemplateContractTest#adminPagesUseSharedAdminLayoutAndKeepActions test`.

Expected: FAIL because current admin pages do not use the shared admin fragment.

- [ ] **Step 3: Rebuild the dashboard around three stat cards**

Use `adminHeader('dashboard')`, a clear page heading, and three `.cs-stat-card` articles for total reservations, reservation revenue, and sold-out products. Preserve the exact `summary` property expressions. Use danger styling only for the sold-out count.

- [ ] **Step 4: Rebuild admin gear management without changing actions**

Use `adminHeader('gears')`, keep add, dashboard, edit, and delete destinations, preserve all query-flag alerts and the CSRF input. Use a responsive `.cs-admin-table`. Make edit a green outline action and delete a red outline action. Provide an empty table state when `gears` is empty.

- [ ] **Step 5: Replace the generated field loop with explicit Japanese form fields**

In `admin/gear-form.html`, keep the conditional POST action and `gearId`. Write six explicit `.cs-field` blocks for `name` (商品名), `category` (カテゴリ), `description` (説明, rendered as `<textarea>`), `dailyPrice` (1日料金), `stockCount` (在庫数), and `imageUrl` (画像URL), with matching `th:field` and `th:errors`. Preserve CSRF. Use a green “保存する” button.

- [ ] **Step 6: Add admin layout CSS**

Create a restrained admin surface using the same colors: a compact admin header, three-column stat grid collapsing to one column below 768px, readable table spacing, right-aligned action group, and the same form/error primitives as public pages. Do not use the orange reservation accent for admin save/edit actions.

- [ ] **Step 7: Run the contract and full regression suite**

Run:

```powershell
.\mvnw.cmd -Dtest=VisualTemplateContractTest#adminPagesUseSharedAdminLayoutAndKeepActions test
.\mvnw.cmd test
```

Expected: the admin contract passes and the complete suite ends with `BUILD SUCCESS`.

- [ ] **Step 8: Commit admin pages**

```powershell
git add src/main/resources/templates/admin src/main/resources/static/css/campshare.css src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "feat: redesign CampShare admin pages"
```

### Task 6: Perform accessibility, responsive, and final regression verification

**Files:**
- Modify if verification reveals defects: `src/main/resources/static/css/campshare.css`
- Modify if verification reveals defects: the affected file under `src/main/resources/templates/`
- Modify if a missing contract is found: `src/test/java/com/example/campshare/web/VisualTemplateContractTest.java`

**Interfaces:**
- Consumes: The completed visual system and every redesigned template.
- Produces: A verified desktop/mobile implementation with no functional regressions or known accessibility blockers in the primary flows.

- [ ] **Step 1: Run the complete automated suite from a clean application build**

Run:

```powershell
.\mvnw.cmd clean test
```

Expected: `BUILD SUCCESS`, with all existing and new tests passing.

- [ ] **Step 2: Start the application with the local profile**

Run the configured local application from the IDE, or use:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Expected: the application starts on `http://localhost:8080` without template parsing errors. If the local database is unavailable, use the existing project setup from `README.md`; do not change production configuration to bypass it.

- [ ] **Step 3: Verify the anonymous and customer flows at desktop width**

At approximately 1440×900, inspect `/`, `/gears`, one `/gears/{id}`, `/login`, and `/signup`. Sign in as a normal user and inspect `/gears/{id}/rentals/new` and `/mypage/rentals`. Confirm header visibility, readable hero crop, three-column product grid, clear orange reservation actions, inline errors, success alerts, empty state, image alternative text, and working navigation.

- [ ] **Step 4: Verify all public flows at smartphone width**

At approximately 390×844, repeat the public flow. Confirm no horizontal page overflow, hero copy precedes the image, gear cards are one column, buttons are at least 44px tall, rental history uses cards, forms fit the viewport, and the header remains usable.

- [ ] **Step 5: Verify administrator screens and role visibility**

Sign in as the existing admin user. Inspect `/admin/dashboard`, `/admin/gears`, `/admin/gears/new`, and an edit page at desktop and smartphone widths. Confirm only admins see management links, stat cards remain readable, table actions are distinct, all six form fields have Japanese labels, CSRF-backed actions submit, and public pages remain reachable.

- [ ] **Step 6: Verify keyboard and reduced-motion behavior**

Navigate the header, product cards, forms, and admin actions using Tab and Shift+Tab. Confirm visible focus rings, logical order, associated labels, and no keyboard trap. Enable reduced motion in browser emulation and confirm transitions become effectively instantaneous.

- [ ] **Step 7: Fix only defects found by the checks and rerun the relevant test first**

For each discovered defect, add or tighten one focused assertion in `VisualTemplateContractTest` when the requirement is statically testable, run that method to see it fail, make the smallest template/CSS correction, then rerun that method. Do not add new features during this pass.

- [ ] **Step 8: Run final verification and inspect the diff**

Run:

```powershell
.\mvnw.cmd clean test
git diff --check
git status --short
```

Expected: `BUILD SUCCESS`, no whitespace errors, and only the intended design files remain changed.

- [ ] **Step 9: Commit final verification fixes if any**

If Step 7 changed files:

```powershell
git add src/main/resources/templates src/main/resources/static/css/campshare.css src/test/java/com/example/campshare/web/VisualTemplateContractTest.java
git commit -m "fix: polish responsive CampShare design"
```

If Step 7 changed nothing, do not create an empty commit.
