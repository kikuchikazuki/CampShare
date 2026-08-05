# 商品一覧ページネーション Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 商品一覧と検索結果を1ページ15件にし、正しいページ数だけを上下2か所のナビゲーションで選べるようにする。

**Architecture:** Spring Data `Page<Gear>` をControllerへ渡し、DB側でページングする。Thymeleafは同じナビゲーションを商品グリッド前後に描画し、`q`をリンクへ引き継ぐ。ページ番号は`Page`の実際の総ページ数を上限とする。

**Tech Stack:** Spring Boot MVC, Spring Data JPA, Thymeleaf, Bootstrap-compatible CSS

## Global Constraints

- 1ページのサイズは常に15件。
- 20件ならページ1・2だけを表示し、3ページ目を生成しない。
- 検索結果にもページングを適用し、`q`をページリンクへ保持する。
- ナビゲーションは商品グリッド直前とページ最下部の2か所に表示する。
- 現在ページは淡いグレー、通常ページは白背景、次へボタンは濃いチャコールとし、CampShareの既存色・角丸・境界線に合わせる。
- 既存の未関連変更（`.gitignore`、repository guidelines、V6/V7 SQL、親ディレクトリの未追跡物）は変更・ステージしない。

---

### Task 1: DBページングとControllerモデル

**Files:**

- Modify: `src/main/java/com/example/campshare/gear/GearRepository.java`
- Modify: `src/main/java/com/example/campshare/web/GearController.java`
- Test: `src/test/java/com/example/campshare/web/GearControllerTest.java`

**Interfaces:** `GearRepository.findAllByOrderByIdAsc(Pageable)` と `GearRepository.search(String, Pageable)` は `Page<Gear>` を返す。Controllerはモデルへ `gears`（現在ページの`List<Gear>`）、`pageNumber`（0始まり）、`totalPages`、`query` を追加する。

- [ ] **Step 1: ページングを要求するControllerテストを先に追加する**

```java
@Test
void gearListRequestsFirstPageWithFifteenItems() throws Exception {
    Page<Gear> page = new PageImpl<>(List.of(), PageRequest.of(0, 15), 20);
    when(gearRepository.findAllByOrderByIdAsc(any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/gears"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("pageNumber", 0))
            .andExpect(model().attribute("totalPages", 2));
}
```

- [ ] **Step 2: 失敗を確認する**

Run the focused test with the project Maven launcher. Expected: compilation/test failure because repository and Controller still return `List<Gear>` and do not expose page metadata.

- [ ] **Step 3: RepositoryをPage対応にする**

Add `org.springframework.data.domain.Page` and `Pageable`, then use:

```java
Page<Gear> findAllByOrderByIdAsc(Pageable pageable);
@Query("select g from Gear g where lower(concat(g.name, ' ', g.category, ' ', g.description)) like lower(concat('%', :query, '%')) order by g.id asc")
Page<Gear> search(@Param("query") String query, Pageable pageable);
```

- [ ] **Step 4: Controllerでページを作成する**

Use `PageRequest.of(Math.max(page, 0), 15)` and clamp a requested page beyond `page.getTotalPages()` to the last valid page before adding the model. Use the selected repository query for empty/non-empty `q`, then add `page.getContent()`, `page.getNumber()`, `page.getTotalPages()`, and the trimmed query.

- [ ] **Step 5: focused testsを通す**

Add tests for page 2, search query preservation, and a request beyond the last page. Expected: for a 20-item Page, `totalPages` is 2 and no model state represents page index 2.

- [ ] **Step 6: Commit backend changes**

Run: `git add src/main/java/com/example/campshare/gear/GearRepository.java src/main/java/com/example/campshare/web/GearController.java src/test/java/com/example/campshare/web/GearControllerTest.java; git commit -m "feat: page gear results"`

### Task 2: Dual pagination navigation and CampShare styling

**Files:**

- Modify: `src/main/resources/templates/gears.html`
- Modify: `src/main/resources/static/css/site.css`
- Test: `src/test/java/com/example/campshare/web/SiteStylesheetTemplateTest.java`

**Interfaces:** Template consumes `gears`, `pageNumber`, `totalPages`, and `query`; it renders two identical navigation blocks whose links use `/gears?page=<zero-based>&q=<query>`.

- [ ] **Step 1: Add template assertions before markup changes**

Add assertions that `gears.html` contains two pagination containers, a `page` query parameter, and the `totalPages` model reference. Expected: failure before the new markup exists.

- [ ] **Step 2: Add a reusable Thymeleaf pagination block in both locations**

Render the block before and after the card grid. Use `th:if="${totalPages > 1}"`. Generate page links from `#numbers.sequence(0, totalPages - 1)` so no link can exceed the actual total. Include `aria-label="ページネーション"`, `aria-current="page"` on the active page, and `aria-label="次のページ"` for the next control. Preserve `query` in every link.

- [ ] **Step 3: Add pagination CSS**

Create `.gear-pagination`, `.gear-pagination__link`, `.gear-pagination__link--current`, `.gear-pagination__link--next`, and disabled/ellipsis styles. Use `--surface`, `--canvas`, `--text`, and a dark charcoal next control; keep focus outlines visible and allow wrapping on narrow screens.

- [ ] **Step 4: Run template tests and inspect rendered HTML**

Run the focused template tests and verify a 20-item model renders only page links 1 and 2. Expected: both navigation blocks contain the same two page links.

- [ ] **Step 5: Commit frontend changes**

Run: `git add src/main/resources/templates/gears.html src/main/resources/static/css/site.css src/test/java/com/example/campshare/web/SiteStylesheetTemplateTest.java; git commit -m "feat: add dual gear pagination controls"`

### Task 3: Full verification and handoff

**Files:**

- Verify: `src/main/java/com/example/campshare/gear/GearRepository.java`
- Verify: `src/main/java/com/example/campshare/web/GearController.java`
- Verify: `src/main/resources/templates/gears.html`
- Verify: `src/main/resources/static/css/site.css`

- [ ] **Step 1: Run the full test suite**

Run the project Maven test command. Expected: all tests pass with zero failures or errors.

- [ ] **Step 2: Check the diff and working tree**

Run: `git diff --check; git status --short`

Expected: no whitespace errors, and only the pagination commits plus pre-existing unrelated changes remain visible.

- [ ] **Step 3: Manually verify boundary cases**

Open `/gears` with 50 items and `/gears?q=<term>` with 20 matching items. Confirm 15 cards on page 1, 5 cards on page 2 for the 20-item case, no page 3 link, two navigation blocks, and preserved search text after navigation.
