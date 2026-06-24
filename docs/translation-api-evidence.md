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

## Translation Cache

`translation_cache` stores translated text by resource type, resource id, field, source language, target language, and SHA-256 source hash.
If the source text changes, the hash changes and the next request creates a new cache entry.

## Provider Behavior

The translation service uses the existing GMS OpenAI-compatible chat completions configuration.
If `GMS_KEY` is not configured, translation requests return `503` while the rest of the product continues to work.