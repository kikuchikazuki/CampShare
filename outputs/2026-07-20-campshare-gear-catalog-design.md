# CampShare Gear Catalog Design

## Goal

Store camp rental gear in PostgreSQL and show an attractive public gear catalog.

## Database

Flyway V3 creates `gears` with `id`, `name`, `category`, `description`, `daily_price`, `stock_count`, `image_url`, and `created_at`.

- `daily_price` and `stock_count` are non-negative.
- V3 inserts six demonstration items: tent, tarp, lantern, sleeping bag, table, and chair.
- Existing Flyway migrations remain unchanged.

## Application

- `Gear` is a JPA entity and `GearRepository` exposes ordered gear lookup.
- `GearController` serves `GET /gears` and supplies the gear list to Thymeleaf.
- The catalog is publicly accessible. Reservation actions are outside this task.

## UI

- `gears.html` uses Bootstrap CDN and responsive cards.
- Each card displays image, category, name, description, daily price, and stock count.
- The home page links to `/gears`.

## Verification

1. Verify V3 creates six gear records.
2. Verify the catalog controller returns the expected view and model.
3. Run all tests and inspect `/gears` in a browser.
