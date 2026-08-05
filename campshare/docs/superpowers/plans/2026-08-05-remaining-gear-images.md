# Remaining Gear Images Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add individual catalog images for the 44 non-lighting gears, then update their URLs in Flyway V11.

**Architecture:** Add each PNG under Spring Boot static resources. Add, but never alter, a Flyway V11 migration containing one URL update per target gear; V1–V10 are immutable.

**Tech Stack:** Spring Boot static resources, Flyway SQL, built-in image generation

## Global Constraints

- The 44 targets are the V8 gears excluding the six V10 lighting gears.
- Use a white-to-pale-gray studio background, one product, an almost invisible contact shadow, and no people, text, watermark, logo, or props.
- Store each asset as `src/main/resources/static/images/gear-<english-kebab-name>.png`.
- Create `src/main/resources/db/migration/V11__set_remaining_gear_images.sql`; do not modify V1–V10.

---

### Task 1: Define the V11 mapping

**Files:**

- Read: `src/main/resources/db/migration/V8__seed_additional_gears.sql`
- Read: `src/main/resources/db/migration/V10__set_lighting_gear_images.sql`
- Create: `src/main/resources/db/migration/V11__set_remaining_gear_images.sql`

**Interfaces:** Consumes V8 gear names and V10 exclusions. Produces 44 statements in this exact form: `UPDATE gears SET image_url = '/images/gear-<english-kebab-name>.png' WHERE name = '<exact V8 name>';`.

- [ ] **Step 1: List the V8 and V10 names.**

Run: `rg "^\\('" src/main/resources/db/migration/V8__seed_additional_gears.sql; rg "^UPDATE gears" src/main/resources/db/migration/V10__set_lighting_gear_images.sql`

Expected: 50 V8 insertions and six lighting exclusions.

- [ ] **Step 2: Write V11 with exactly 44 mappings.**

Use the exact Japanese item name from V8 and one distinct lowercase English kebab-case filename for every non-lighting item. Example:

```sql
UPDATE gears SET image_url = '/images/gear-solo-tent.png' WHERE name = 'ソロテント';
```

- [ ] **Step 3: Verify the update count.**

Run: `rg -c "^UPDATE gears SET image_url" src/main/resources/db/migration/V11__set_remaining_gear_images.sql`

Expected: `44`.

- [ ] **Step 4: Commit.**

Run: `git add src/main/resources/db/migration/V11__set_remaining_gear_images.sql; git commit -m "feat: map remaining gear images"`

### Task 2: Generate and place the catalog PNGs

**Files:**

- Create: `src/main/resources/static/images/gear-*.png` (44 files)
- Read: `src/main/resources/db/migration/V11__set_remaining_gear_images.sql`

**Interfaces:** Consumes each V11 item name and filename. Produces a PNG for every V11 URL.

- [ ] **Step 1: Extract the 44 target paths.**

Run: `rg -o "/images/gear-[a-z0-9-]+\\.png" src/main/resources/db/migration/V11__set_remaining_gear_images.sql | Sort-Object -Unique`

Expected: 44 unique paths.

- [ ] **Step 2: Generate one image per V11 gear using this exact prompt, replacing only `<商品名>`.**

```text
Use case: product-mockup
Asset type: CampShare rental catalog product image
Primary request: A single <商品名> for a Japanese camping-equipment rental catalog.
Scene/backdrop: seamless clean white to very pale gray studio backdrop.
Style/medium: photorealistic product photography.
Composition/framing: one complete product, centered, front three-quarter view, generous padding; no cropped edges.
Lighting/mood: soft even studio light with an extremely subtle contact shadow.
Constraints: no people, no other products, no outdoor scene, no text, no watermark, no brand logo.
```

Copy each selected result to `src/main/resources/static/images/` with its exact V11 filename. Do not overwrite any of the six existing lighting PNGs.

- [ ] **Step 3: Verify every V11 URL resolves to a file.**

Run: `$urls = rg -o "/images/(gear-[a-z0-9-]+\\.png)" src/main/resources/db/migration/V11__set_remaining_gear_images.sql | ForEach-Object { $_ -replace '^.*/','' }; $missing = $urls | Where-Object { -not (Test-Path (Join-Path 'src/main/resources/static/images' $_)) }; "urls=$($urls.Count) missing=$($missing.Count)"; $missing`

Expected: `urls=44 missing=0`.

- [ ] **Step 4: Commit.**

Run: `git add src/main/resources/static/images/gear-*.png; git commit -m "feat: add remaining gear catalog images"`

### Task 3: Verify the migration and application

**Files:**

- Verify: `src/main/resources/db/migration/V11__set_remaining_gear_images.sql`
- Verify: `src/main/resources/static/images/gear-*.png`

**Interfaces:** Consumes the V11 mappings and 44 PNGs. Produces a complete, deployable local image catalog.

- [ ] **Step 1: Re-run the image reference check.**

Run: `$urls = rg -o "/images/(gear-[a-z0-9-]+\\.png)" src/main/resources/db/migration/V11__set_remaining_gear_images.sql | ForEach-Object { $_ -replace '^.*/','' }; $urls | ForEach-Object { if (-not (Test-Path (Join-Path 'src/main/resources/static/images' $_))) { throw "Missing image: $_" } }; 'all image references resolve'`

Expected: `all image references resolve`.

- [ ] **Step 2: Run the test suite.**

Run: `./gradlew test`

Expected: exit code 0 and `BUILD SUCCESSFUL`.

- [ ] **Step 3: Confirm the migration sequence and inspect the worktree.**

Run: `Get-ChildItem src/main/resources/db/migration -Filter 'V*__*.sql' | Sort-Object Name | Select-Object -ExpandProperty Name; git status --short`

Expected: V11 follows V10; only the intended V11 and 44 image files are part of this task.
