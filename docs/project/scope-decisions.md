# sceniK Scope Decisions

Generated: 2026-06-08

This document records Week 3 close-out decisions where a feature was intentionally deferred instead of added late and riskily.

## Deferred: Follow / Following

Status: DEFERRED

Reason:
- No existing user-follow schema, mapper, service, store, or UI flow was found.
- Adding follow/following would touch auth ownership rules, notification generation, profile/mypage surfaces, and community author interactions.
- That is not a safe close-out patch for Week 3 because it would create new behavior without enough time for live end-to-end QA.

Current decision:
- Do not expose follow endpoints in Week 3.
- Do not add placeholder frontend buttons that imply working social relationships.
- Track follow/following as a later social/community enhancement.

Candidate future API shape:
- `POST /api/users/{userId}/follow`
- `DELETE /api/users/{userId}/follow`
- `GET /api/users/me/following`
- `GET /api/users/me/followers`

## Deferred: Notification Settings

Status: DEFERRED

Reason:
- Week 3 notification scope is list/read/read-all.
- Per-user channel/preferences settings are not required for the current MVP.

Current decision:
- Keep `GET /api/notifications`, `PATCH /api/notifications/read`, and `PATCH /api/notifications/{notificationId}/read` as canonical.
- Treat `/api/users/me/notification-settings` as a future settings feature.

## Deprecated: Legacy Endpoint Names

Status: DEPRECATED

Canonical replacements:
- `/api/content` -> `/api/contents`
- `/api/content/{id}` -> `/api/contents/{contentId}`
- `/api/places/{placeId}/visit-plan` -> `/api/places/{placeId}/visit-status` with `WANT_TO_GO`
- `/api/places/{placeId}/visit-complete` -> `/api/places/{placeId}/visit-status` with `VISITED`
- `/api/courses` -> `/api/routes` and `/api/users/me/routes`
- `/api/ai-routes` -> `/api/ai/routes`
- `/api/notifications/read-all` -> `/api/notifications/read`

## Partial: Map Provider Live Verification

Status: PARTIAL

Reason:
- Backend map endpoints have fallback-safe behavior.
- External map/geocode provider live QA was not run during close-out because servers and external credentials should not be assumed.

Current decision:
- Keep map endpoints documented as implemented but live-provider QA blocked until credentials/environment are confirmed.
