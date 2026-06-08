# API Field Naming Checklist

Use these names as the canonical Week 3 API contract.

## Canonical Fields

| Field | Use |
| --- | --- |
| `contentId` | String source ID for K-content, for example `C_DRAMA_001`. |
| `placeId` | String source ID for verified place data, for example `P_0001`. |
| `routeId` | String ID for user-created saved routes in the current implementation. |
| `postId` | Community post ID. |
| `commentId` | Community comment ID. |
| `thumbnailUrl` | Preferred image field for cards and route/place/content thumbnails. |
| `durationDays` | Preferred trip length field. |
| `visitStatus` | Place visit status, using `WANT_TO_GO` or `VISITED`. |
| `createdAt` | Creation timestamp. |
| `updatedAt` | Last update timestamp. |

## Compatibility Fields

| Compatibility Field | Preferred Field | Status |
| --- | --- | --- |
| `placeName` | `name` | Keep as compatibility alias for existing place/route UI. |
| `imageUrl` | `thumbnailUrl` | Keep only when a source image and thumbnail have distinct meanings; otherwise prefer `thumbnailUrl`. |
| `duration` | `durationDays` | Deprecated for API contracts; acceptable as frontend display compatibility. |
| `recommendedTime` | `arrivalTime` or schedule-specific field | Current backend route DTO still uses `recommendedTime`; avoid introducing a second field until route timing is redesigned. |
| `reason` | `recommendReason` | Current backend returns both in route stops for compatibility. Prefer `recommendReason` in new clients. |

## Current ID Policy

- `contentId` and `placeId` remain string IDs from seed/source data.
- `routeId` remains string in the current backend/frontend implementation because the route schema and DTOs already use string route IDs.
- Do not convert `contentId` or `placeId` to numeric IDs.

## Response Shape Notes

- Content DTOs should expose: `contentId`, `title`, `category`, `description`, `thumbnailUrl`, `releaseYear`, `likeCount`, `viewCount`, `relatedPlaceCount`, `isLiked`.
- Place DTOs should expose: `placeId`, `name`, `address`, `region`, `latitude`, `longitude`, `thumbnailUrl`, `likeCount`, `isLiked`, `visitStatus`, `relatedContents`.
- Route DTOs should expose: `routeId`, `title`, `summary`, `region`, `durationDays`, `sourceType`, `isPublic`, `places`, `createdAt`, `updatedAt`.
- AI route DTOs should preserve real `placeId` values and stable `dayNumber` plus `visitOrder` values.
