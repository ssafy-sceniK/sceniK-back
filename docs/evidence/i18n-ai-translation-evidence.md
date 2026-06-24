# i18n AI Translation Evidence

## Scope

This evidence covers the 2026-06-24 multilingual support pass.

## Confirmed Changes

- Frontend sends the selected UI locale as `Accept-Language` on API requests.
- Backend content/place APIs read `Accept-Language` and localize known system-provided display fields.
- User-generated community text, comments, replies, reviews, and profile text remain original by default and continue to use the explicit translation endpoint when the user requests translation.
- Translation cache behavior remains unchanged: cache keys include resource type, resource id, field, source language, target language, and source hash.

## Verification Commands

- `./gradlew.bat test --tests com.scenik.service.SystemLocalizationServiceTest`: passed after implementation.
- Full frontend/backend verification should be checked in the final task report.

## Remaining Evidence Gaps

- No browser screenshot QA has been captured for this pass.
- No runtime API smoke call has been captured against a running backend for localized content/place endpoints.
- No manual verification has been captured for all 281 place records.
