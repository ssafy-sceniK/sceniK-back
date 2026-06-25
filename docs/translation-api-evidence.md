# Translation API Evidence

## Endpoint

`POST /api/translations`

The endpoint requires an authenticated user and accepts:

- `resourceType`: `POST`, `COMMENT`, `REVIEW`, `PROFILE`, or `USER`
- `resourceId`: source resource id
- `field`: `content`, `title`, or `bio` depending on resource type
- `targetLanguage`: optional, defaults to the current user's saved language

## Language Storage

- `users.language` stores the user's UI language.
- `community_posts.language`, `comments.language`, and `place_reviews.language` store detected source language for user-generated text.
- Language detection supports `ko`, `en`, `ja`, and `zh-CN` with deterministic Unicode-script heuristics.

## API Locale Integration

- Frontend requests now send the selected locale through `Accept-Language`.
- `ContentController` and `PlaceController` read `Accept-Language` and pass it into service methods.
- `SystemLocalizationService` normalizes browser headers such as `en-US,en;q=0.8` to `en`, `ja-JP` to `ja`, and `zh-CN`/`zh` to `zh-CN`.
- Existing response fields remain backward compatible: `title`, `category`, `tags`, `placeName`, `region`, and `category` contain localized display values when available.
- Original source values are preserved in added fields such as `originalTitle`, `originalCategory`, `originalTags`, `originalPlaceName`, `originalRegion`, and `locale`.
- Korean remains the fallback when the header is missing, unsupported, or a specific translation is unavailable.

## System Data Coverage

- All 22 seeded content IDs have localized title mappings for `en`, `ja`, and `zh-CN` in `SystemLocalizationService`.
- High-impact category, region, theme, travel-style, budget, weather, companion, and tag labels are mapped in the same service.
- Place names use explicit official/common mappings for high-priority visible places, and otherwise fall back safely to the Korean source name.
- The current implementation uses a static server-side translation catalog rather than DB translation columns or a `system_translations` table, because this is the lowest-risk extension for the current schema and existing admin CRUD APIs.

## Translation Cache

`translation_cache` stores translated text by resource type, resource id, field, source language, target language, and SHA-256 source hash.
If the source text changes, the hash changes and the next request creates a new cache entry.

## Provider Behavior

The translation service uses the GMS Gemini-compatible `generateContent` endpoint.
User-generated content translation uses `scenik.ai.gms.translation-model`, which defaults to `gemini-2.5-flash-lite` and can be overridden with `GMS_TRANSLATION_MODEL`.
If a fallback model is configured through `scenik.ai.gms.translation-fallback-model`, translation retries once after the primary Gemini request fails.
If `GMS_KEY` is not configured, user-generated translation requests return `503` while the rest of the product continues to work.

## Remaining Gaps

- DB/source seed data still does not contain per-record translated place names or descriptions for all 281 places.
- Content descriptions and cast metadata are not fully translated.
- Browser QA for locale switching and dark mode fit has not been completed in this pass.

## Place System Localization Update on 2026-06-24

SystemLocalizationService now localizes place system fields before API DTOs are returned.

| Item | Result |
| --- | ---: |
| Seeded places checked | 281 |
| Place categories covered | 40 |
| Place regions covered | 15 |
| Place districts covered | 89 |

### Behavior

- Place DTO display fields keep backward-compatible names: placeName, name, category, region, district, and description contain display-ready values for the requested locale.
- originalPlaceName, originalRegion, originalDistrict, originalCategory, and originalDescription preserve the Korean source values.
- Place names fall back to the Korean source name unless an explicit safe mapping exists.
- High-priority place-name mappings now cover official/common names such as Lotte World Tower Seoul Sky, Bukchon Asian Cultural Art Museum, Korean Stone Art Museum, BaekInje''s House Museum, Choi Sunu House, Korea Furniture Museum, major palaces, parks, museums, beaches, and visible AI-course/home/detail route places.
- Full concatenated romanization is avoided for common facility suffixes such as museum, park, palace, beach, station, school, village, observatory, and cultural space.
- Place descriptions use a generated localized fallback for en, ja, and zh-CN when no per-place official localized description exists.
- Generated descriptions use localized category, district, region, and related system content title parsed from the source bracket prefix.
- Korean remains the fallback locale for missing or unsupported Accept-Language values.

### Test evidence

- SystemLocalizationServiceTest covers English generated place descriptions, district localization, category localization, and original field preservation.
- Existing content localization tests still cover title/category/tag localization and original values.
