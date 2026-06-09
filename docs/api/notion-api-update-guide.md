# Notion API 테이블 업데이트 가이드

생성일: 2026-06-09

이 문서는 현재 백엔드/프론트 구현과 [scenik-api-inventory.md](./scenik-api-inventory.md)를 기준으로 Notion API 테이블을 최종 정리하기 위한 안내입니다. Notion 테이블의 대상 카테고리는 `Auth`, `User`, `Content`, `Place`, `AI`, `Route`, `Community`, `Notification`, `Admin`입니다.

## 최종 상태 요약

| 상태 | 개수 | 의미 |
| --- | ---: | --- |
| Done | 49 | 백엔드 구현이 있고 프론트 연결 또는 백엔드 전용 설계가 확인되었으며 build/smoke/static QA 근거가 있습니다. |
| In progress | 24 | 백엔드는 있으나 UI가 불완전하거나 live QA가 필요합니다. Admin 백엔드 전용 endpoint와 follow live QA 전 항목도 여기에 둡니다. |
| Deferred | 11 | Week 3 MVP 범위 밖으로 명시적으로 보류한 항목입니다. |
| Deprecated | 14 | 구버전 경로이며 현재 canonical endpoint로 교체해야 합니다. |
| Not progress | 0 | 현재 확인된 Notion 대상 행 중 Not progress로 유지할 항목은 없습니다. |

## Done으로 변경할 행

| 카테고리 | 행 |
| --- | --- |
| Auth | 회원가입, 로그인, 로그아웃, 토큰 재발급 |
| User | 내 정보 조회, 내 정보 수정, 내 활동 이력, 내 활동 요약, 내 게시글 목록 |
| Content | 콘텐츠 목록, 콘텐츠 카테고리, 인기 콘텐츠, 콘텐츠 상세, 콘텐츠 관련 장소, 콘텐츠 좋아요, 콘텐츠 좋아요 취소, 내가 좋아요한 콘텐츠 |
| Place | 장소 목록, 주변 장소, 장소 상세, 장소 좋아요, 장소 좋아요 취소, 내가 좋아요한 장소, 가고 싶어요 상태, 다녀왔어요 상태, 방문 상태 삭제, 내 방문 상태 목록 |
| AI | AI 경로 생성, AI 경로 재생성 |
| Route | 내 저장 코스 목록, 경로 저장, 경로 상세, 경로 수정, 경로 삭제 |
| Community | 게시글 목록, 게시글 상세, 게시글 작성, 게시글 수정, 게시글 삭제, 댓글 목록, 댓글 작성, 게시글 좋아요, 게시글 좋아요 취소, 게시글 신고 |
| Notification | 알림 목록, 전체 알림 읽음, 단일 알림 읽음 |

## In progress로 변경할 행

| 카테고리 | 행 | 사유 |
| --- | --- | --- |
| User | 내 리뷰 목록 | 백엔드와 마이페이지 리뷰 목록 UI는 연결되었으나 live QA가 필요합니다. |
| User | 팔로우, 언팔로우, 팔로워 목록, 팔로잉 목록 | 백엔드와 프론트 최소 UI는 연결되었으나 두 사용자 계정 기반 live QA가 필요합니다. |
| Place | 장소 리뷰 목록, 장소 리뷰 작성 | 장소 상세 UI 연결은 있으나 live QA가 필요합니다. |
| Place | 장소 리뷰 수정, 장소 리뷰 삭제 | 장소 상세 수정/삭제 UI는 연결되었으나 live QA가 필요합니다. |
| Place | 지도 주소 검색, 지도 역지오코딩, 지도 로컬 검색 | 백엔드 fallback은 있으나 외부 map provider live QA가 필요합니다. |
| Route | 경로 장소 추가, 경로 장소 수정, 경로 장소 삭제, 경로 장소 순서 변경 | 백엔드는 있으나 편집 UI live QA가 필요합니다. |
| Community | 댓글 수정, 댓글 삭제 | 댓글 작성자 수정/삭제 UI는 연결되었으나 live QA가 필요합니다. |
| Admin | 관리자 콘텐츠 생성, 관리자 콘텐츠 수정, 관리자 콘텐츠 삭제, 관리자 장소 생성, 관리자 장소 수정, 관리자 장소 삭제 | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. |

## Deferred로 변경할 행

| 카테고리 | 행 | 사유 |
| --- | --- | --- |
| Auth | 소셜 로그인 | Week 3 MVP는 이메일/비밀번호 인증 기준입니다. |
| AI | OpenAI/RAG 경로 품질 개선, 임베딩 전체 재생성, 장소 임베딩 갱신 | 실제 OpenAI/RAG/ChromaDB production 통합은 Week 4 범위입니다. |
| Route | 경로 공개 공유 | 공개 공유 제품 플로우가 아직 완성되지 않았습니다. |
| Community | 댓글 신고 | 현재 backend endpoint가 없으며 Week 3 MVP 범위 밖입니다. |
| Notification | 알림 설정 | Week 3 알림 범위는 목록/읽음 처리입니다. |

## Deprecated로 변경할 행

| 구버전 행 | 현재 canonical endpoint | 조치 |
| --- | --- | --- |
| `/api/contents/{contentId}/like` | `/api/contents/{contentId}/likes` | Deprecated로 변경하고 URL을 canonical로 교체합니다. |
| `/api/places/{placeId}/like` | `/api/places/{placeId}/likes` | Deprecated로 변경하고 URL을 canonical로 교체합니다. |
| `/api/posts/{postId}/like` | `/api/posts/{postId}/likes` | Deprecated로 변경하고 URL을 canonical로 교체합니다. |
| `/api/posts/{postId}/report` | `/api/posts/{postId}/reports` | Deprecated로 변경하고 URL을 canonical로 교체합니다. |
| `/api/posts/{postId}/comments/{commentId}/report` | `/api/posts/{postId}/comments/{commentId}/reports` | singular 경로는 Deprecated입니다. canonical 댓글 신고 API 자체는 현재 Deferred입니다. |
| `/api/places/{placeId}/visit-plan` | `/api/places/{placeId}/visit-status` + `WANT_TO_GO` | Deprecated로 변경합니다. |
| `/api/places/{placeId}/visit-complete` | `/api/places/{placeId}/visit-status` + `VISITED` | Deprecated로 변경합니다. |
| `/api/ai-routes` | `/api/ai/routes` | Deprecated로 변경합니다. |
| `/api/ai/courses` | `/api/ai/routes` | Deprecated로 변경합니다. |
| `/api/courses` | `/api/routes`, `/api/users/me/routes` | Deprecated로 변경합니다. |
| `/api/users/me/courses` | `/api/users/me/routes` | Deprecated로 변경합니다. |
| `/api/notifications/read-all` | `/api/notifications/read` | Deprecated로 변경합니다. |
| `/api/content` | `/api/contents` | Deprecated로 변경합니다. |
| `/api/content/{id}` | `/api/contents/{contentId}` | Deprecated로 변경합니다. |

## Not progress로 유지할 행

현재 확인된 Notion 대상 API 중 `Not progress`로 유지할 항목은 없습니다. 기능명이 불명확한 Notion 행이 추가로 발견되면 다음 기준으로 분류합니다.

- Week 3 MVP 필수인데 구현이 전혀 없으면 `Not progress`
- Week 3 범위 밖으로 합의된 기능이면 `Deferred`
- 구버전 경로이면 `Deprecated`
- 백엔드만 있거나 live QA가 없으면 `In progress`

## ID 타입 메모

| ID | 현재 타입 | 근거 |
| --- | --- | --- |
| `contentId` | String | 예: `C_DRAMA_001` |
| `placeId` | String | 예: `P_0001` |
| `routeId` | String | 현재 `travel_routes.route_id`와 DTO가 문자열입니다. |
| `userId` | Long / BIGINT | `users.user_id` 기준 숫자 ID입니다. |
| `postId` | String | 현재 `community_posts.post_id`가 `VARCHAR(64)`입니다. |
| `commentId` | String | 현재 `comments.comment_id`가 `VARCHAR(64)`입니다. |
| `notificationId` | String | 현재 `notifications.notification_id`가 `VARCHAR(64)`입니다. |
| `reportId` | String | 현재 `reports.report_id`가 `VARCHAR(64)`입니다. |
| `reviewId` | Long / BIGINT | 현재 `place_reviews.review_id`가 `BIGINT AUTO_INCREMENT`입니다. |

## Notion 수정 순서

1. [notion-api-status-update.csv](./notion-api-status-update.csv)를 Notion API 테이블에 import하거나 기존 테이블과 대조합니다.
2. `URL`이 singular `like` 또는 `report`인 행은 plural canonical endpoint로 교체합니다.
3. `/api/courses`, `/api/users/me/courses`, `/api/ai-routes`, `/api/notifications/read-all` 행은 `Deprecated`로 변경합니다.
4. review edit/delete, comment edit/delete/reply, follow/following, map provider, route place edit, admin endpoint는 live QA 또는 admin UI가 완료될 때까지 `In progress`로 둡니다.
5. social login, notification settings, public route sharing, real RAG/OpenAI/ChromaDB 항목은 `Deferred`로 둡니다.
6. 완료된 핵심 Week 3 행은 `Done`으로 변경합니다.

## QA 확인 결과

| 확인 항목 | 결과 |
| --- | --- |
| Markdown 링크 | 이 문서의 로컬 상대 링크는 모두 `backend/docs/api` 기준 파일을 가리킵니다. |
| 구버전 경로 상태 | 구버전 경로는 `Done`으로 표시하지 않고 `Deprecated`로 분리했습니다. |
| `/api` base URL | public Spring Boot API는 `/api` prefix를 사용합니다. FastAPI 내부 stub은 `(internal)`로 구분했습니다. |
| ID 타입 | 현재 schema/code 기준으로 문자열/숫자 타입을 분리했습니다. |
| CSV import | CSV는 요청한 8개 컬럼을 사용하며 모든 필드를 quote 처리해 Excel/Notion import가 쉽게 되도록 정리했습니다. |
