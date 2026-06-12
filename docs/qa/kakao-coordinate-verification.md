# Kakao place coordinate verification

## Data flow

1. `data/seed/places.csv` is the canonical seed source for place address, region, district, latitude, and longitude.
2. `scripts/geocodePlaces.cjs` resolves the source rows with Kakao Local REST API and records the coordinate source.
3. `scripts/syncPlaceCoordinates.cjs` updates runtime `data.sql`, frontend `mockData.js`, and emits a targeted DB update SQL file.
4. MySQL `places` stores `DECIMAL(10, 7)` latitude and longitude values.
5. `PlaceMapper` maps the DB columns to `Place`, and `PlaceDto` exposes them without coordinate conversion.
6. The frontend `placeApi` preserves the API fields, and `MapPreview` creates `kakao.maps.LatLng(latitude, longitude)` for each marker.

## Root cause

The previous CSV import script ignored place addresses when producing coordinates. It generated synthetic positions by adding row and column offsets to a region center. Those values were copied to `data.sql`, MySQL, the place API, frontend data, and Kakao markers.

## Correction summary

- Total places: 281
- Kakao keyword matches: 242
- Kakao address matches: 37
- Unresolved and intentionally excluded from the map: 2
- DB rows with resolved coordinates: 279

Unresolved places:

- `P_0022` 신촌그래피티벽화터널: the old parcel address and place name no longer resolve through Kakao Local API.
- `P_0229` 부산 남부민동: the source identifies a broad neighborhood rather than a specific building or filming point.

The frontend coordinate validator was also corrected so `null` coordinates are not converted to JavaScript numeric zero and rendered as a marker at `(0, 0)`. Searching an unresolved place now keeps the place card visible while showing the no-coordinate map state without a marker.

## Five-place verification

The CSV, fresh `data.sql` import, current MySQL row, and backend API returned the same values below. The map page was filtered to each place individually; every case displayed one card and one Kakao marker with the matching place title and no console error.

| Place | Canonical address | Latitude | Longitude | CSV | DB | API | Kakao marker |
| --- | --- | ---: | ---: | --- | --- | --- | --- |
| P_0001 킨텍스 | 경기 고양시 일산서구 킨텍스로 217-60 | 37.6689333 | 126.7458026 | Match | Match | Match | Match |
| P_0002 서울랜드 | 경기 과천시 광명로 181 | 37.4339028 | 127.0187021 | Match | Match | Match | Match |
| P_0003 금성루 | 서울 중구 충무로2길 25 | 37.5625391 | 126.9945989 | Match | Match | Match | Match |
| P_0004 팔도건어물 | 서울 도봉구 삼양로154길 36 | 37.6555996 | 127.0152309 | Match | Match | Match | Match |
| P_0005 백운시장 | 서울 도봉구 삼양로154길 42 | 37.6557113 | 127.0155981 | Match | Match | Match | Match |

Additional consistency correction:

- `P_0170` 그랜드 워커힐 서울 애스톤하우스 was reclassified from `서울/광진구` to `경기/구리시` based on its canonical Kakao address, `경기 구리시 아차산로 1`.

## Validation commands

- Backend: `bash gradlew test`
- Frontend: `npm run lint`
- Frontend production build: `vite build --configLoader runner`
- Fresh DB import: applied `schema.sql` and `data.sql` to a temporary database, verified 281 rows and 279 resolved coordinate pairs, then removed the temporary database.
