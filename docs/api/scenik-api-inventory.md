# sceniK API 인벤토리

생성일: 2026-06-08

이 문서는 Week 3 마감 기준 Spring Boot backend, Vue frontend API adapter, 주요 화면 연결 상태를 기준으로 정리한 API 인벤토리입니다. Week 4의 실제 RAG/OpenAI/ChromaDB 구현은 아직 완료된 기능으로 기록하지 않습니다.

## 상태 정의

| 상태 | 의미 |
| --- | --- |
| 완료 | 백엔드 endpoint가 있고 프론트 연결 또는 백엔드 전용 설계가 확인되었습니다. |
| 부분 완료 | 백엔드나 프론트 일부는 있으나 UI 연결, live QA, 또는 제품 플로우가 남아 있습니다. |
| 미구현 | Week 3 필수로 기대되었지만 구현이 확인되지 않은 항목입니다. |
| 보류 | Week 3 MVP 범위 밖으로 명시적으로 미룬 항목입니다. |
| 폐기 | 구버전/spec 경로이며 새 클라이언트에서 사용하면 안 됩니다. |

## Canonical API 목록

| 영역 | Method | Endpoint | 상태 | Backend | Frontend | 메모 |
| --- | --- | --- | --- | --- | --- | --- |
| Auth | POST | `/api/auth/signup` | 완료 | `AuthController` | signup view/store | 회원가입과 token response가 구현되어 있습니다. |
| Auth | POST | `/api/auth/login` | 완료 | `AuthController` | login view/store | 이메일/비밀번호 로그인이 구현되어 있습니다. |
| Auth | POST | `/api/auth/logout` | 완료 | `AuthController` | auth store | Bearer token 기반 로그아웃입니다. |
| Auth | POST | `/api/auth/token/refresh` | 완료 | `AuthController` | auth API | refresh token 재발급입니다. |
| User | GET | `/api/users/me` | 완료 | `AuthController` | auth/mypage | 현재 사용자 hydration입니다. |
| User | PATCH | `/api/users/me` | 완료 | `AuthController` | profile edit | 사용자 프로필 수정입니다. |
| User | GET | `/api/users/me/history` | 완료 | `UserActivityController` | mypage | 최근 조회/활동 이력입니다. |
| User | GET | `/api/users/me/activity-summary` | 완료 | `UserActivityController` | mypage | 마이페이지 요약 카운트입니다. |
| Content | GET | `/api/contents` | 완료 | `ContentController` | content/explore stores | `category`, `keyword`, `popular`, `sort`, `page`, `size`를 지원합니다. |
| Content | GET | `/api/contents/categories` | 완료 | `ContentController` | explore/content stores | 카테고리 chip 조회입니다. |
| Content | GET | `/api/contents/popular` | 완료 | `ContentController` | home/explore | 인기 콘텐츠 shortcut입니다. |
| Content | GET | `/api/contents/{contentId}` | 완료 | `ContentController` | content detail | 문자열 `contentId` 기반 상세 조회입니다. |
| Content | GET | `/api/contents/{contentId}/places` | 완료 | `ContentController` | content detail | 콘텐츠 관련 검증 장소 조회입니다. |
| Content Like | POST | `/api/contents/{contentId}/likes` | 완료 | `LikeVisitController` | UI store/detail cards | 인증 필요, 콘텐츠 좋아요입니다. |
| Content Like | DELETE | `/api/contents/{contentId}/likes` | 완료 | `LikeVisitController` | UI store/detail cards | 인증 필요, 콘텐츠 좋아요 취소입니다. |
| Content Like | GET | `/api/users/me/liked-contents` | 완료 | `LikeVisitController` | mypage/store | `page`, `size` 지원, content ID 목록 반환입니다. |
| Place | GET | `/api/places` | 완료 | `PlaceController` | explore/place store | `region`, `district`, `category`, `theme`, `keyword`, `sort`, `page`, `size`를 지원합니다. |
| Place | GET | `/api/places/nearby` | 완료 | `PlaceController` | map/explore | 좌표/반경 기반 주변 장소 조회입니다. |
| Place | GET | `/api/places/{placeId}` | 완료 | `PlaceController` | place detail | 문자열 `placeId` 기반 상세 조회입니다. |
| Place Like | POST | `/api/places/{placeId}/likes` | 완료 | `LikeVisitController` | place detail/cards | 인증 필요, 장소 좋아요입니다. |
| Place Like | DELETE | `/api/places/{placeId}/likes` | 완료 | `LikeVisitController` | place detail/cards | 인증 필요, 장소 좋아요 취소입니다. |
| Place Like | GET | `/api/users/me/liked-places` | 완료 | `LikeVisitController` | mypage/store | `page`, `size` 지원, place ID 목록 반환입니다. |
| Visit Status | POST | `/api/places/{placeId}/visit-status` | 완료 | `LikeVisitController` | place detail/mypage | `WANT_TO_GO`, `VISITED`를 받습니다. |
| Visit Status | PUT | `/api/places/{placeId}/visit-status` | 완료 | `LikeVisitController` | compatibility | POST와 동일한 갱신 동작입니다. |
| Visit Status | DELETE | `/api/places/{placeId}/visit-status` | 완료 | `LikeVisitController` | place detail/mypage | 방문 상태 삭제입니다. |
| Visit Status | GET | `/api/users/me/visit-status` | 완료 | `LikeVisitController` | mypage/store | `page`, `size`를 지원합니다. |
| Review | GET | `/api/places/{placeId}/reviews` | 부분 완료 | `PlaceReviewController` | place detail | 목록 UI는 있으나 live QA가 필요합니다. |
| Review | POST | `/api/places/{placeId}/reviews` | 부분 완료 | `PlaceReviewController` | place detail | 작성 UI는 있으나 live QA가 필요합니다. |
| Review | PATCH | `/api/places/{placeId}/reviews/{reviewId}` | 부분 완료 | `PlaceReviewController` | place detail | 수정 UI는 연결되었으나 live QA가 필요합니다. |
| Review | DELETE | `/api/places/{placeId}/reviews/{reviewId}` | 부분 완료 | `PlaceReviewController` | place detail | 삭제 UI는 연결되었으나 live QA가 필요합니다. |
| Review | GET | `/api/users/me/reviews` | 부분 완료 | `PlaceReviewController` | API adapter only | 백엔드는 있으나 마이리뷰 화면이 없습니다. |
| AI Route | POST | `/api/ai/routes` | 완료 | `AiRouteController` | AI condition/loading flow | 검증 장소 기반 deterministic 경로 생성입니다. |
| AI Route | POST | `/api/ai/routes/{generationId}/regenerate` | 완료 | `AiRouteController` | AI loading/result | 기존 요청 기반 재생성입니다. |
| Route | GET | `/api/users/me/routes` | 완료 | `RouteController` | saved route screens | `page`, `size`, `sort=latest|updated`를 지원합니다. |
| Route | POST | `/api/routes` | 완료 | `RouteController` | route save flow | AI/user route 저장입니다. |
| Route | GET | `/api/routes/{routeId}` | 완료 | `RouteController` | route detail/edit | 소유자 검증이 있습니다. |
| Route | PATCH | `/api/routes/{routeId}` | 완료 | `RouteController` | route edit | 저장 경로 수정입니다. |
| Route | DELETE | `/api/routes/{routeId}` | 완료 | `RouteController` | route management | 저장 경로 삭제입니다. |
| Route Places | POST | `/api/routes/{routeId}/places` | 부분 완료 | `RouteController` | edit flow partial | 백엔드는 장소 추가를 지원합니다. |
| Route Places | PATCH | `/api/routes/{routeId}/places/{routePlaceId}` | 부분 완료 | `RouteController` | edit flow partial | 백엔드는 장소 수정을 지원합니다. |
| Route Places | DELETE | `/api/routes/{routeId}/places/{routePlaceId}` | 부분 완료 | `RouteController` | edit flow partial | 백엔드는 장소 삭제를 지원합니다. |
| Route Places | PATCH | `/api/routes/{routeId}/places/reorder` | 부분 완료 | `RouteController` | edit flow partial | 백엔드는 순서 변경을 지원합니다. |
| Community | GET | `/api/posts` | 완료 | `CommunityController` | community list | `page`, `size`, `sort`, `keyword`, `region`을 지원합니다. |
| Community | GET | `/api/posts/{postId}` | 완료 | `CommunityController` | community detail | 게시글 상세 조회입니다. |
| Community | POST | `/api/posts` | 완료 | `CommunityController` | write view | 인증 필요, 게시글 작성입니다. |
| Community | PATCH | `/api/posts/{postId}` | 완료 | `CommunityController` | edit flow | 소유자 검증 후 수정합니다. |
| Community | DELETE | `/api/posts/{postId}` | 완료 | `CommunityController` | edit flow | 소유자 검증 후 삭제합니다. |
| Comments | GET | `/api/posts/{postId}/comments` | 완료 | `CommunityController` | community detail | `page`, `size`를 지원합니다. |
| Comments | POST | `/api/posts/{postId}/comments` | 완료 | `CommunityController` | community detail | 인증 필요, 댓글 작성입니다. |
| Comments | PATCH | `/api/posts/{postId}/comments/{commentId}` | 부분 완료 | `CommunityController` | community detail | 댓글 작성자 수정 UI는 연결되었으나 live QA가 필요합니다. |
| Comments | DELETE | `/api/posts/{postId}/comments/{commentId}` | 부분 완료 | `CommunityController` | community detail | 댓글 작성자 삭제 UI는 연결되었으나 live QA가 필요합니다. |
| Community Likes | POST | `/api/posts/{postId}/likes` | 완료 | `CommunityController` | community UI | 인증 필요, 게시글 좋아요입니다. |
| Community Likes | DELETE | `/api/posts/{postId}/likes` | 완료 | `CommunityController` | community UI | 인증 필요, 게시글 좋아요 취소입니다. |
| Reports | POST | `/api/posts/{postId}/reports` | 완료 | `CommunityController` | community UI | 게시글 신고이며 중복 신고를 방지합니다. |
| Notifications | GET | `/api/notifications` | 완료 | `CommunityController` | notifications UI | `page`, `size`, `sort=latest`와 unread header를 지원합니다. |
| Notifications | PATCH | `/api/notifications/read` | 완료 | `CommunityController` | notifications UI | 전체 알림 읽음 canonical endpoint입니다. |
| Notifications | PATCH | `/api/notifications/{notificationId}/read` | 완료 | `CommunityController` | notifications UI | 단일 알림 읽음 처리입니다. |
| User Posts | GET | `/api/users/me/posts` | 완료 | `CommunityController` | mypage/community | `page`, `size`를 지원합니다. |
| Map | GET | `/api/maps/geocode` | 부분 완료 | `MapController` | map/search | backend fallback은 있으나 외부 provider live QA가 필요합니다. |
| Map | GET | `/api/maps/reverse-geocode` | 부분 완료 | `MapController` | map/search | backend fallback은 있으나 외부 provider live QA가 필요합니다. |
| Map | GET | `/api/maps/local-search` | 부분 완료 | `MapController` | map/search | backend fallback은 있으나 외부 provider live QA가 필요합니다. |
| Search | GET | `/api/search` | 완료 | `SearchController` | search view/store | 통합 콘텐츠/장소 검색입니다. |
| Admin | POST | `/api/admin/contents` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |
| Admin | PATCH | `/api/admin/contents/{contentId}` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |
| Admin | DELETE | `/api/admin/contents/{contentId}` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |
| Admin | POST | `/api/admin/places` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |
| Admin | PATCH | `/api/admin/places/{placeId}` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |
| Admin | DELETE | `/api/admin/places/{placeId}` | 부분 완료 | `AdminController` | none | 백엔드 전용 admin utility입니다. |

## 폐기 또는 보류된 API 이름

| 영역 | Method | Endpoint | 상태 | 교체/결정 |
| --- | --- | --- | --- | --- |
| Content | GET | `/api/content` | 폐기 | `/api/contents`를 사용합니다. |
| Content | GET | `/api/content/{id}` | 폐기 | `/api/contents/{contentId}`를 사용합니다. |
| Content Like | POST | `/api/contents/{contentId}/like` | 폐기 | `/api/contents/{contentId}/likes`를 사용합니다. |
| Place Like | POST | `/api/places/{placeId}/like` | 폐기 | `/api/places/{placeId}/likes`를 사용합니다. |
| Place | POST | `/api/places/{placeId}/visit-plan` | 폐기 | `/api/places/{placeId}/visit-status` + `WANT_TO_GO`를 사용합니다. |
| Place | POST | `/api/places/{placeId}/visit-complete` | 폐기 | `/api/places/{placeId}/visit-status` + `VISITED`를 사용합니다. |
| Community Like | POST | `/api/posts/{postId}/like` | 폐기 | `/api/posts/{postId}/likes`를 사용합니다. |
| Community Report | POST | `/api/posts/{postId}/report` | 폐기 | `/api/posts/{postId}/reports`를 사용합니다. |
| Comment Report | POST | `/api/posts/{postId}/comments/{commentId}/report` | 폐기 | plural reports 경로가 canonical이지만 댓글 신고 자체는 현재 보류입니다. |
| Route | GET/POST | `/api/courses` | 폐기 | `/api/routes`, `/api/users/me/routes`를 사용합니다. |
| Route | GET | `/api/users/me/courses` | 폐기 | `/api/users/me/routes`를 사용합니다. |
| AI Route | POST | `/api/ai-routes` | 폐기 | `/api/ai/routes`를 사용합니다. |
| AI Route | POST | `/api/ai/courses` | 폐기 | `/api/ai/routes`를 사용합니다. |
| Notifications | PATCH | `/api/notifications/read-all` | 폐기 | `/api/notifications/read`를 사용합니다. |
| Follow | POST/DELETE/GET | `/api/users/{userId}/follow*` | 보류 | Week 3 이후 social layer로 분리합니다. |
| Notification Settings | GET/PATCH | `/api/users/me/notification-settings` | 보류 | Week 3 MVP 범위가 아닙니다. |
| Social Login | GET/POST | provider-specific OAuth endpoints | 보류 | Week 3 canonical auth는 이메일/비밀번호입니다. |
| Public Route Sharing | PATCH | `/api/routes/{routeId}/visibility` | 보류 | route visibility field는 있으나 공개 공유 제품 플로우는 미완성입니다. |
| Real RAG/OpenAI/ChromaDB | Internal | `(internal) /ai/embeddings/*` | 보류 | FastAPI stub은 있으나 production AI 품질 개선은 Week 4 범위입니다. |

## ID 타입 기준

| ID | 현재 타입 | 설명 |
| --- | --- | --- |
| `contentId` | String | 예: `C_DRAMA_001` |
| `placeId` | String | 예: `P_0001` |
| `routeId` | String | 현재 구현에서 route ID는 문자열입니다. |
| `userId` | Long / BIGINT | 사용자 PK는 숫자입니다. |
| `postId` | String | 현재 code/schema 기준 문자열입니다. |
| `commentId` | String | 현재 code/schema 기준 문자열입니다. |
| `notificationId` | String | 현재 code/schema 기준 문자열입니다. |
| `reportId` | String | 현재 code/schema 기준 문자열입니다. |
| `reviewId` | Long / BIGINT | 장소 리뷰 PK는 숫자입니다. |
