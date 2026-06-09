# 핵심 데이터 CRUD 증거

생성일: 2026-06-09

루브릭 #01 핵심 데이터 관리 CRUD 항목은 사용자 화면 CRUD와 backend-only 관리 CRUD를 분리해 판단해야 합니다.

## 사용자 화면에서 보이는 CRUD

| 도메인 | Create | Read | Update | Delete | 근거 |
| --- | --- | --- | --- | --- | --- |
| 여행 경로 | `POST /api/routes` | `GET /api/users/me/routes`, `GET /api/routes/{routeId}` | `PATCH /api/routes/{routeId}` | `DELETE /api/routes/{routeId}` | `RouteController`, `AiResultView`, `CourseEditView` |
| 코스 장소 | `POST /api/routes/{routeId}/places` | route detail | `PATCH /api/routes/{routeId}/places/{routePlaceId}`, reorder | `DELETE /api/routes/{routeId}/places/{routePlaceId}` | `RouteController`, `routeApi.js` |
| 커뮤니티 게시글 | `POST /api/posts` | `GET /api/posts`, `GET /api/posts/{postId}` | `PATCH /api/posts/{postId}` | `DELETE /api/posts/{postId}` | `CommunityController`, community screens |
| 댓글 | `POST /api/posts/{postId}/comments` | `GET /api/posts/{postId}/comments` | `PATCH /api/posts/{postId}/comments/{commentId}` | `DELETE /api/posts/{postId}/comments/{commentId}` | `CommunityController`, `CommunityView` |
| 장소 리뷰 | `POST /api/places/{placeId}/reviews` | `GET /api/places/{placeId}/reviews` | `PATCH /api/places/{placeId}/reviews/{reviewId}` | `DELETE /api/places/{placeId}/reviews/{reviewId}` | `PlaceReviewController`, `PlaceDetailView` |
| 사용자 프로필 | signup | `GET /api/users/me` | `PATCH /api/users/me` | Deferred | `AuthController`, profile screens |

## Backend-only 관리 CRUD

| 도메인 | Endpoint | 상태 | 비고 |
| --- | --- | --- | --- |
| 콘텐츠 생성 | `POST /api/admin/contents` | In progress | admin UI 없음 |
| 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | In progress | admin UI 없음 |
| 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | In progress | admin UI 없음 |
| 장소 생성 | `POST /api/admin/places` | In progress | admin UI 없음 |
| 장소 수정 | `PATCH /api/admin/places/{placeId}` | In progress | admin UI 없음 |
| 장소 삭제 | `DELETE /api/admin/places/{placeId}` | In progress | admin UI 없음 |

## 판단

일반 사용자 데모 흐름에서 필요한 CRUD는 경로, 게시글, 댓글, 리뷰, 프로필 중심으로 구현되어 있습니다. 콘텐츠/장소 원본 데이터 관리 화면은 관리자 권한 정책과 운영 UI가 필요하므로 Week 3에서는 backend-only `In progress`로 유지합니다.
