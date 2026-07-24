# CampShare Site-wide Styles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply one consistent CampShare visual system to all public and admin templates except the existing auth pages.

**Architecture:** Add `site.css` for shared tokens and reusable layout components, then link it from the five public and three admin templates. Keep Bootstrap as the grid/table foundation where it already exists, but replace inline styles and Bootstrap color-specific classes with CampShare component classes.

**Tech Stack:** Spring Boot 3.5.16, Thymeleaf, Spring MVC Test, Bootstrap 5, CSS.

## Global Constraints

- Create `src/main/resources/static/css/site.css`; do not place style attributes or style elements in templates.
- Preserve every existing URL, form action, method, CSRF token, Thymeleaf expression, and authorization rule.
- Keep `auth.css`, `login.html`, and `signup.html` out of scope.
- Use `#31533B`, `#F8F3E8`, `#FFFDF8`, and `#E98B45` as the shared color roles.

---

### Task 1: Add regression coverage for the shared stylesheet

**Files:**
- Modify: `src/test/java/com/example/campshare/web/HomeControllerTest.java`
- Modify: `src/test/java/com/example/campshare/web/GearControllerTest.java`
- Modify: `src/test/java/com/example/campshare/web/RentalControllerTest.java`
- Modify: `src/test/java/com/example/campshare/web/AdminGearControllerTest.java` (or create it if absent)

**Interfaces:**
- Consumes: existing GET routes for top page, gear list/detail, rental form/history, and admin pages.
- Produces: rendered-response assertions that contain `/css/site.css`.

- [ ] **Step 1: Add a content assertion for each page-returning controller test**

```java
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

.andExpect(content().string(containsString("/css/site.css")));
```

- [ ] **Step 2: Run the affected tests and confirm red**

Run: `./mvnw test "-Dtest=HomeControllerTest,GearControllerTest,RentalControllerTest,AdminGearControllerTest"`

Expected: failures because the templates do not yet load the stylesheet.

- [ ] **Step 3: Commit the failing contract tests**

Run: `git add src/test/java/com/example/campshare && git commit -m "test: require shared site stylesheet"`

### Task 2: Create the shared CSS component layer

**Files:**
- Create: `src/main/resources/static/css/site.css`

**Interfaces:**
- Consumes: `site-page`, `site-header`, `site-container`, `site-card`, `primary-action`, `secondary-action`, `danger-action`, `notice-*`, `gear-card`, `status-*`, `admin-stat`, `site-table`, and `site-form` classes.
- Produces: desktop and mobile styles usable by every non-auth template.

- [ ] **Step 1: Define CSS variables and global page rules**

```css
:root { --forest: #31533B; --canvas: #F8F3E8; --surface: #FFFDF8; --accent: #E98B45; --text: #26372A; --muted: #718071; --danger: #B42318; }
.site-page { margin: 0; min-height: 100vh; background: var(--canvas); color: var(--text); font-family: system-ui, sans-serif; }
.site-container { width: min(1120px, calc(100% - 32px)); margin-inline: auto; padding-block: 32px; }
.site-card { border: 0; border-radius: 16px; background: var(--surface); box-shadow: 0 10px 24px rgb(38 55 42 / 12%); }
```

- [ ] **Step 2: Add shared buttons, forms, notices, cards, tables, and responsive rules**

```css
.primary-action { background: var(--accent); color: #fff; }
.secondary-action { border: 1px solid var(--forest); color: var(--forest); }
.danger-action { background: var(--danger); color: #fff; }
.site-form input, .site-form textarea { min-height: 44px; width: 100%; border-radius: 10px; }
.site-form input:focus, .site-form textarea:focus { outline: 3px solid rgb(88 116 90 / 35%); }
@media (max-width: 768px) { .site-container { width: min(100% - 24px, 1120px); padding-block: 20px; } .site-table thead { display: none; } .site-table tr { display: grid; gap: 8px; padding: 16px; } }
```

- [ ] **Step 3: Commit the stylesheet**

Run: `git add src/main/resources/static/css/site.css && git commit -m "feat: add shared site stylesheet"`

### Task 3: Apply shared CSS to public templates

**Files:**
- Modify: `src/main/resources/templates/home.html`
- Modify: `src/main/resources/templates/gears.html`
- Modify: `src/main/resources/templates/gear-detail.html`
- Modify: `src/main/resources/templates/rental-form.html`
- Modify: `src/main/resources/templates/rentals.html`

**Interfaces:**
- Consumes: Task 2 CSS classes and all existing models (`email`, `admin`, `gears`, `gear`, `rentalForm`, `rentals`).
- Produces: a consistent public journey without changing links or submits.

- [ ] **Step 1: Add this link to each head**

```html
<link rel="stylesheet" th:href="@{/css/site.css}">
```

- [ ] **Step 2: Add `site-page` to body and `site-container` to main**

```html
<body class="site-page">
<main class="site-container">
```

- [ ] **Step 3: Replace inline image/container sizing with CSS classes and apply component classes**

Use `gear-card` for each gear article, `site-card` for detail/form/history panels, `primary-action` only for booking confirmation and `secondary-action` for non-destructive navigation. Keep every `th:*` attribute unchanged.

- [ ] **Step 4: Commit public templates**

Run: `git add src/main/resources/templates/home.html src/main/resources/templates/gears.html src/main/resources/templates/gear-detail.html src/main/resources/templates/rental-form.html src/main/resources/templates/rentals.html && git commit -m "feat: style public CampShare pages"`

### Task 4: Apply shared CSS to admin templates

**Files:**
- Modify: `src/main/resources/templates/admin/dashboard.html`
- Modify: `src/main/resources/templates/admin/gears.html`
- Modify: `src/main/resources/templates/admin/gear-form.html`

**Interfaces:**
- Consumes: Task 2 CSS classes and existing `summary`, `gears`, `gearForm`, `gearId`, flash parameters, and CSRF values.
- Produces: visually consistent admin cards, table, alerts, and form controls.

- [ ] **Step 1: Link `site.css` and apply `site-page`/`site-container`**

```html
<link rel="stylesheet" th:href="@{/css/site.css}">
<body class="site-page"><main class="site-container">
```

- [ ] **Step 2: Style dashboard metrics, admin table, action buttons, and form fields**

Use `admin-stat site-card` for each summary metric, `site-table` for the gear table, `primary-action` for create/save, `secondary-action` for dashboard/edit, `danger-action` for delete, `notice-success`/`notice-danger` for alerts, and `site-form site-card` for the product form. Preserve all links, forms, and CSRF inputs.

- [ ] **Step 3: Run the full suite**

Run: `./mvnw test`

Expected: `BUILD SUCCESS` with all tests passing.

- [ ] **Step 4: Manually verify**

Open `/`, `/gears`, a gear detail, a rental form, `/mypage/rentals`, `/admin/dashboard`, `/admin/gears`, and the gear form on desktop and narrow widths. Verify links, forms, alerts, images, tables, and focus states.

- [ ] **Step 5: Commit admin templates**

Run: `git add src/main/resources/templates/admin && git commit -m "feat: style CampShare admin pages"`
