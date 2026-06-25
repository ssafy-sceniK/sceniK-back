# Place Image URL Sync

This note documents the production-safe image URL sync for seeded places.

## Why

Production runs with `spring.sql.init.mode=never`, so deploying a new backend JAR does not automatically re-run `data.sql` against existing rows. If seeded `places.csv` gains `image_url` values after rows already exist in production, the deployed API can still return blank image fields.

## Dry Run And SQL Generation

From the backend repository:

```powershell
node scripts/syncPlaceImages.cjs
```

The script:

- reads `data/seed/places.csv`
- validates non-empty image URLs
- checks duplicate `place_id` values
- compares rows against `src/main/resources/data.sql`
- generates `build/place-image-update.sql`
- does not connect to any DB
- does not update `data.sql` unless `--write-data-sql` is passed

`build/place-image-update.sql` is generated output and is not committed. Regenerate it on the deployment machine or from a clean checkout before executing the SQL.

## Live API Audit

Before running production SQL, compare the deployed API contract against the authoritative local CSV:

```powershell
node scripts/auditPlaceImageApi.cjs --api-base-url https://api.scenik.p-e.kr/api --format json
```

For a single known failing place:

```powershell
node scripts/auditPlaceImageApi.cjs --api-base-url https://api.scenik.p-e.kr/api --place-id P_0262 --format json
```

The audit reports rows where `places.csv` has a valid `image_url` but the API response has no usable `imageUrl`, `image_url`, `thumbnailUrl`, or `thumbnail_url`. Those rows are expected to trigger the frontend initial/avatar fallback until production `places.image_url` is filled.

## Generated SQL Behavior

The generated SQL only fills missing/empty image values:

```sql
UPDATE places
SET image_url = '...'
WHERE place_id = 'P_0000'
  AND (image_url IS NULL OR TRIM(image_url) = '' OR image_url IN ('-', 'null', 'NULL', 'N/A', 'n/a'));
```

It does not overwrite existing non-empty production values.

## Image URL Validation Notes

The sync script validates URL shape and rejects empty, local, relative, `file:`, and `data:` values. It intentionally does not use `HEAD` as a hard validity check because some image hosts reject `HEAD` while serving browser `GET` requests normally.

Example: `P_0169` Mungyeong Eco World uses the official Mungyeong tourism image URL. A `HEAD` request returns `403`, but a browser-style GET returns `200 image/jpeg`, so it is treated as loadable rather than broken.

## Production Execution

Run the generated SQL only on the intended production database using the deployment server's existing DB credentials or an approved admin channel. Do not paste or commit credentials.

After execution, run the verification queries appended at the bottom of `build/place-image-update.sql`, especially the sample IDs:

- `P_0262` BTS Bus Stop
- `P_0150` Korean Stone Art Museum
- `P_0167` Mungyeong Yongduri Super Set
- `P_0168` Mungyeong Rail Bike Gurang-ri Station
- `P_0169` Mungyeong Eco World
- `P_0170` Grand Walkerhill Seoul Aston House
- `P_0171` Aqua Planet Ilsan
- `P_0172` Sayuwon
- `P_0173` Chungju Geonji Village
- `P_0262` BTS Bus Stop

Do not report production DB completion unless the SQL was executed on production and the verification queries passed.
