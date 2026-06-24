# Place image URL update evidence

## Date

2026-06-24

## Source and target

- Source CSV: `data/seed/places.csv`
- Source identifier: `place_id`
- Source image column: `image_url`
- Target table: `places`
- Target column: `image_url`
- Runtime seed file: `src/main/resources/data.sql`
- Reproducible update script: `scripts/syncPlaceImages.cjs`
- Generated SQL: `build/place-image-update.sql`

## Safety rules

- Match only by `place_id`.
- Do not match by place name because duplicate Korean names exist.
- Do not overwrite DB values with blank CSV values.
- Treat empty string, `-`, `null`, `NULL`, and `N/A` as empty image values.
- Preserve all non-image columns.

## CSV dry run

| Metric | Count |
| --- | ---: |
| CSV rows | 281 |
| CSV rows with non-empty `image_url` | 281 |
| CSV rows skipped because image URL was empty | 0 |
| Duplicate `place_id` values | 0 |
| Duplicate place names | 3 |

Duplicate place names do not affect the update because `place_id` is unique and used for matching.

## Seed SQL update

`node scripts/syncPlaceImages.cjs` updated `src/main/resources/data.sql` and generated `build/place-image-update.sql`.

| Metric | Count |
| --- | ---: |
| CSV rows with image URL | 281 |
| Matched `data.sql` place rows | 281 |
| `data.sql` rows changed on first run | 193 |
| Missing `data.sql` place matches | 0 |

A second run is idempotent and reports 0 additional `data.sql` row changes.

## Local DB update verification

A temporary Java JDBC runner applied `build/place-image-update.sql` to the local MySQL database using the existing `.env` database configuration. No database secrets are recorded here.

| Metric | Count |
| --- | ---: |
| DB places before update | 281 |
| DB rows with image URL before update | 88 |
| CSV image rows matched in DB | 281 |
| JDBC update statements executed against matched rows | 281 |
| DB rows skipped because no matching place existed | 0 |
| DB rows with image URL after update | 281 |

Sample API checks after the update:

- `GET /api/places/P_0003` returned the CSV image URL for Geumseongru.
- `GET /api/places/P_0281` returned the CSV image URL for Busan Citizens Park.

## Re-run commands

```powershell
cd C:\Users\SSAFY\Desktop\sceniK\sceniK-back
node scripts\syncPlaceImages.cjs
```

If `mysql` CLI is available, apply the generated SQL manually:

```powershell
mysql --default-character-set=utf8mb4 -u <user> -p <database> < build\place-image-update.sql
```
