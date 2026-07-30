# Home Page Removal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the standalone home page, make the gear catalog the application entry point, and expose the former home-page actions through a reusable authenticated header.

**Architecture:** `HomeController` remains as the single owner of `GET /`, but returns a redirect to `/gears` instead of rendering a template. A `@ControllerAdvice` publishes authentication-derived navigation attributes to every MVC model, while a Thymeleaf fragment renders one shared header in all site templates. Existing CSS variables and Bootstrap utilities provide the layout without JavaScript or a new dependency.

**Tech Stack:** Java 17, Spring Boot 3.5, Spring MVC, Spring Security, Thymeleaf, Bootstrap 5.3, JUnit 5, MockMvc

## Global Constraints

- Keep `/gears` as the canonical gear-list URL.
- Keep logout as a CSRF-protected POST request.
- Show the admin link only to users with `ROLE_ADMIN`.
- Do not add JavaScript or a new Thymeleaf/Spring Security integration dependency.
- Preserve the existing gear, rental, signup, and admin behavior.
- Reuse the existing color variables from `site.css`.

---

## File Structure

- `src/main/java/com/example/campshare/web/HomeController.java`: redirect the root URL only.
- `src/main/java/com/example/campshare/web/NavigationModelAdvice.java`: publish `navEmail`, `navAuthenticated`, and `navAdmin` for every rendered view.
- `src/main/java/com/example/campshare/config/SecurityConfig.java`: send successful login and logout directly to `/gears`.
- `src/main/resources/templates/fragments/site-header.html`: own all shared navigation markup and conditional rendering.
- `src/main/resources/static/css/site.css`: own desktop and narrow-screen header presentation.
- Existing templates: insert the header fragment and remove redundant top/home links.
- `src/test/java/com/example/campshare/web/HomeControllerTest.java`: verify the root redirect.
- `src/test/java/com/example/campshare/web/NavigationModelAdviceTest.java`: verify anonymous, user, and admin navigation attributes.
- `src/test/java/com/example/campshare/web/SiteHeaderTemplateTest.java`: verify fragment structure and its inclusion by every site template.
- `src/test/java/com/example/campshare/web/SiteStylesheetTemplateTest.java`: remove the deleted home template from the stylesheet coverage list.

### Task 1: Make the Gear Catalog the Entry Point

**Files:**
- Modify: `src/test/java/com/example/campshare/web/HomeControllerTest.java`
- Modify: `src/main/java/com/example/campshare/web/HomeController.java`
- Modify: `src/main/java/com/example/campshare/config/SecurityConfig.java`

**Interfaces:**
- Consumes: Spring MVC `GET /` routing and Spring Security login/logout configuration.
- Produces: `HomeController.topPage(): String`, returning `"redirect:/gears"`.

- [ ] **Step 1: Replace the home-view test with a redirect test**

```java
@Test
void topPageRedirectsToGearList() throws Exception {
    mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/gears"))
            .andExpect(redirectedUrl("/gears"));
}
```

Add this import:

```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```powershell
.\mvnw.cmd -Dtest=HomeControllerTest test
```

Expected: FAIL because the current response is HTTP 200 with view name `home`.

- [ ] **Step 3: Reduce `HomeController` to the redirect**

Replace the controller body with:

```java
package com.example.campshare.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String topPage() {
        return "redirect:/gears";
    }
}
```

In `SecurityConfig`, replace:

```java
.defaultSuccessUrl("/", true)
```

with:

```java
.defaultSuccessUrl("/gears", true)
```

and replace:

```java
.logoutSuccessUrl("/")
```

with:

```java
.logoutSuccessUrl("/gears")
```

- [ ] **Step 4: Run the focused test and verify it passes**

Run:

```powershell
.\mvnw.cmd -Dtest=HomeControllerTest test
```

Expected: BUILD SUCCESS with one passing test.

- [ ] **Step 5: Commit the entry-point change**

```powershell
git add src/main/java/com/example/campshare/web/HomeController.java src/main/java/com/example/campshare/config/SecurityConfig.java src/test/java/com/example/campshare/web/HomeControllerTest.java
git commit -m "feat: redirect home to gear catalog"
```

### Task 2: Provide Shared Navigation State

**Files:**
- Create: `src/test/java/com/example/campshare/web/NavigationModelAdviceTest.java`
- Create: `src/main/java/com/example/campshare/web/NavigationModelAdvice.java`

**Interfaces:**
- Consumes: nullable `org.springframework.security.core.Authentication`.
- Produces: model attributes `navAuthenticated: boolean`, `navEmail: String|null`, and `navAdmin: boolean`.

- [ ] **Step 1: Write unit tests for anonymous, user, and admin state**

```java
package com.example.campshare.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationModelAdviceTest {

    private final NavigationModelAdvice advice = new NavigationModelAdvice();

    @Test
    void anonymousNavigationHasNoAccountActions() {
        ConcurrentModel model = new ConcurrentModel();

        advice.addNavigationState(null, model);

        assertFalse((boolean) model.getAttribute("navAuthenticated"));
        assertNull(model.getAttribute("navEmail"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void userNavigationShowsIdentityWithoutAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("camper@example.com", "password", "ROLE_USER");
        authentication.setAuthenticated(true);

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("camper@example.com", model.getAttribute("navEmail"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void adminNavigationIncludesAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@example.com", "password", "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("admin@example.com", model.getAttribute("navEmail"));
        assertTrue((boolean) model.getAttribute("navAdmin"));
    }
}
```

- [ ] **Step 2: Run the focused test and verify compilation fails**

Run:

```powershell
.\mvnw.cmd -Dtest=NavigationModelAdviceTest test
```

Expected: compilation failure because `NavigationModelAdvice` does not exist.

- [ ] **Step 3: Implement the global navigation model advice**

```java
package com.example.campshare.web;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {

    @ModelAttribute
    void addNavigationState(Authentication authentication, Model model) {
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        boolean admin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("navAuthenticated", authenticated);
        model.addAttribute("navEmail", authenticated ? authentication.getName() : null);
        model.addAttribute("navAdmin", admin);
    }
}
```

- [ ] **Step 4: Run the focused test and verify it passes**

Run:

```powershell
.\mvnw.cmd -Dtest=NavigationModelAdviceTest test
```

Expected: BUILD SUCCESS with three passing tests.

- [ ] **Step 5: Commit the shared model state**

```powershell
git add src/main/java/com/example/campshare/web/NavigationModelAdvice.java src/test/java/com/example/campshare/web/NavigationModelAdviceTest.java
git commit -m "feat: expose shared navigation state"
```

### Task 3: Add and Apply the Shared Header

**Files:**
- Create: `src/test/java/com/example/campshare/web/SiteHeaderTemplateTest.java`
- Create: `src/main/resources/templates/fragments/site-header.html`
- Modify: `src/main/resources/static/css/site.css`
- Modify: `src/main/resources/templates/gears.html`
- Modify: `src/main/resources/templates/gear-detail.html`
- Modify: `src/main/resources/templates/rental-form.html`
- Modify: `src/main/resources/templates/rentals.html`
- Modify: `src/main/resources/templates/admin/dashboard.html`
- Modify: `src/main/resources/templates/admin/gears.html`
- Modify: `src/main/resources/templates/admin/gear-form.html`
- Modify: `src/test/java/com/example/campshare/web/SiteStylesheetTemplateTest.java`
- Delete: `src/main/resources/templates/home.html`

**Interfaces:**
- Consumes: `navAuthenticated`, `navEmail`, and `navAdmin` model attributes from `NavigationModelAdvice`.
- Produces: Thymeleaf fragment `siteHeader`, included with `th:replace="~{fragments/site-header :: siteHeader}"`.

- [ ] **Step 1: Write static template contract tests**

Create:

```java
package com.example.campshare.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteHeaderTemplateTest {

    private static final Path TEMPLATES = Path.of("src/main/resources/templates");
    private static final List<String> SITE_TEMPLATES = List.of(
            "gears.html", "gear-detail.html", "rental-form.html", "rentals.html",
            "admin/dashboard.html", "admin/gears.html", "admin/gear-form.html");

    @Test
    void sharedHeaderContainsAllConditionalActions() throws IOException {
        String header = Files.readString(TEMPLATES.resolve("fragments/site-header.html"));

        assertTrue(header.contains("th:fragment=\"siteHeader\""));
        assertTrue(header.contains("@{/gears}"));
        assertTrue(header.contains("@{/login}"));
        assertTrue(header.contains("@{/signup}"));
        assertTrue(header.contains("@{/mypage/rentals}"));
        assertTrue(header.contains("@{/admin/gears}"));
        assertTrue(header.contains("@{/logout}"));
        assertTrue(header.contains("method=\"post\""));
        assertTrue(header.contains("th:if=\"${navAuthenticated}\""));
        assertTrue(header.contains("th:if=\"${navAdmin}\""));
    }

    @Test
    void everySiteTemplateIncludesTheSharedHeader() throws IOException {
        for (String template : SITE_TEMPLATES) {
            String html = Files.readString(TEMPLATES.resolve(template));
            assertTrue(
                    html.contains("th:replace=\"~{fragments/site-header :: siteHeader}\""),
                    template + " must include the shared header");
        }
    }

    @Test
    void standaloneHomeTemplateIsRemoved() {
        assertFalse(Files.exists(TEMPLATES.resolve("home.html")));
    }
}
```

In `SiteStylesheetTemplateTest`, change the template list to:

```java
List<String> templates = List.of(
        "gears.html", "gear-detail.html", "rental-form.html", "rentals.html",
        "admin/dashboard.html", "admin/gears.html", "admin/gear-form.html");
```

- [ ] **Step 2: Run template tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -Dtest=SiteHeaderTemplateTest,SiteStylesheetTemplateTest test
```

Expected: `SiteHeaderTemplateTest` fails because the fragment is absent, templates do not include it, and `home.html` still exists.

- [ ] **Step 3: Create the header fragment**

Create `fragments/site-header.html`:

```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<body>
<header class="site-header" th:fragment="siteHeader">
    <div class="site-header__inner">
        <a class="site-header__brand" th:href="@{/gears}">CampShare</a>
        <nav class="site-header__nav" aria-label="メインナビゲーション">
            <a th:href="@{/gears}">用品一覧</a>
            <a th:if="${navAuthenticated}" th:href="@{/mypage/rentals}">予約履歴</a>
            <a th:if="${navAdmin}" th:href="@{/admin/gears}">商品管理</a>
        </nav>
        <div class="site-header__account">
            <th:block th:unless="${navAuthenticated}">
                <a th:href="@{/login}">ログイン</a>
                <a class="site-header__signup" th:href="@{/signup}">新規登録</a>
            </th:block>
            <th:block th:if="${navAuthenticated}">
                <span class="site-header__email" th:text="${navEmail}">user@example.com</span>
                <form th:action="@{/logout}" method="post">
                    <button type="submit">ログアウト</button>
                </form>
            </th:block>
        </div>
    </div>
</header>
</body>
</html>
```

- [ ] **Step 4: Add responsive header styles**

Append readable, multiline rules to `site.css`:

```css
.site-header {
    background: var(--forest);
    color: var(--surface);
}

.site-header__inner {
    width: min(1120px, calc(100% - 32px));
    margin: 0 auto;
    padding: 14px 0;
    display: flex;
    align-items: center;
    gap: 24px;
}

.site-header a {
    color: inherit;
    text-decoration: none;
}

.site-header__brand {
    font-size: 1.25rem;
    font-weight: 700;
}

.site-header__nav,
.site-header__account {
    display: flex;
    align-items: center;
    gap: 16px;
}

.site-header__account {
    margin-left: auto;
}

.site-header__signup,
.site-header button {
    border: 1px solid var(--surface);
    border-radius: 999px;
    padding: 6px 12px;
}

.site-header form {
    margin: 0;
}

.site-header button {
    background: transparent;
    color: inherit;
    cursor: pointer;
}

.site-header__email {
    overflow-wrap: anywhere;
}

@media (max-width: 768px) {
    .site-header__inner {
        width: min(100% - 24px, 1120px);
        flex-wrap: wrap;
        gap: 10px 16px;
    }

    .site-header__nav {
        order: 3;
        width: 100%;
        flex-wrap: wrap;
    }

    .site-header__account {
        flex-wrap: wrap;
        justify-content: flex-end;
    }
}
```

Preserve the existing `site.css` variables and component rules while reformatting the current one-line file as needed.

- [ ] **Step 5: Include the fragment in all site templates**

Immediately after each `<body class="site-page">`, add:

```html
<div th:replace="~{fragments/site-header :: siteHeader}"></div>
```

For `rental-form.html`, first change:

```html
<body>
```

or its existing body declaration to:

```html
<body class="site-page">
```

and use `site-container` for the outer `<main>` while retaining its `max-width: 720px` constraint.

Remove only these now-redundant links:

- `gears.html`: the link back to the CampShare top page.
- `admin/gears.html`: the link back to the top page.

Keep contextual back links such as “用品一覧へ戻る”, “商品詳細へ戻る”, and “商品管理へ戻る”, because they express local hierarchy rather than global navigation.

Delete:

```text
src/main/resources/templates/home.html
```

- [ ] **Step 6: Run template tests and verify they pass**

Run:

```powershell
.\mvnw.cmd -Dtest=SiteHeaderTemplateTest,SiteStylesheetTemplateTest test
```

Expected: BUILD SUCCESS with all header and stylesheet contract tests passing.

- [ ] **Step 7: Run the full test suite**

Run:

```powershell
.\mvnw.cmd test
```

Expected: BUILD SUCCESS with no test failures or errors.

- [ ] **Step 8: Commit the shared header**

```powershell
git add src/main/resources/templates src/main/resources/static/css/site.css src/test/java/com/example/campshare/web/SiteHeaderTemplateTest.java src/test/java/com/example/campshare/web/SiteStylesheetTemplateTest.java
git commit -m "feat: replace home page with shared header"
```

### Task 4: Verify the Integrated Navigation

**Files:**
- Modify only if verification exposes a defect in files already listed above.

**Interfaces:**
- Consumes: the completed root redirect, global navigation model, fragment, templates, and CSS.
- Produces: verified desktop/mobile navigation behavior with no new public interface.

- [ ] **Step 1: Start the application**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: the application starts successfully on its configured local port.

- [ ] **Step 2: Verify anonymous navigation**

Open `/` and confirm:

- The browser lands on `/gears`.
- The header shows CampShare, 用品一覧, ログイン, and 新規登録.
- The header does not show 予約履歴, 商品管理, an email address, or ログアウト.

- [ ] **Step 3: Verify authenticated navigation**

Log in as a general user and confirm:

- Successful login lands on `/gears`.
- The header shows the email address, 予約履歴, and ログアウト.
- 商品管理 is absent.
- Logging out uses the form button and lands on `/gears`.

Log in as an administrator and confirm 商品管理 appears and links to `/admin/gears`.

- [ ] **Step 4: Verify narrow-screen layout**

At a viewport width of 375 pixels, confirm:

- Header rows wrap without horizontal scrolling.
- The email address wraps instead of pushing actions outside the viewport.
- Every link and the logout button remains visible and clickable.

- [ ] **Step 5: Re-run automated verification**

Run:

```powershell
.\mvnw.cmd test
```

Expected: BUILD SUCCESS with no test failures or errors.
