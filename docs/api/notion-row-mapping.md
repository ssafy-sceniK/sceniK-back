# Notion API 행별 매핑

생성일: 2026-06-09

이 문서는 기존 Notion API 테이블을 바로 수정할 수 있도록 Notion 행명을 기준으로 현재 canonical API와 구현 상태를 1:1로 정리한 문서입니다. 상태 판단 기준은 [scenik-api-inventory.md](./scenik-api-inventory.md), [scenik-api-status.md](./scenik-api-status.md), [notion-api-status-update.csv](./notion-api-status-update.csv), [notion-api-update-guide.md](./notion-api-update-guide.md)를 기준으로 했습니다.

| Notion Row | Actual Endpoint | Status | Reason | Backend Evidence | Frontend Evidence |
|------------|----------------|---------|---------|------------------|-------------------|
| 회원가입 | `POST /api/auth/signup`, `GET /api/auth/check-email?email=` | Done | 백엔드 회원가입과 이메일 중복 확인 API가 구현되어 있고, 프론트 회원가입 화면에서 중복 확인과 비밀번호 정책 안내를 제공합니다. | `AuthController`, `AuthService.checkEmail`, `AuthService.signup` | `SignupView`, `authApi.checkEmailAvailability`, auth store |
| 로그인 | `POST /api/auth/login` | Done | 이메일/비밀번호 로그인과 token 저장 흐름이 구현되어 있습니다. | `AuthController`, `AuthService` | login view, auth store |
| 로그아웃 | `POST /api/auth/logout` | Done | Bearer token 기반 로그아웃 API가 구현되어 있고, 설정 화면에서 세션 종료 CTA와 결과 안내를 제공합니다. | `AuthController`, `AuthService` | `SettingsView`, auth store |
| 토큰 재발급 | `POST /api/auth/token/refresh` | Done | refresh token 재발급 API가 구현되어 있습니다. | `AuthController`, `AuthService` | `authApi.refreshToken` |
| 카카오 소셜 로그인 | `GET/POST /api/auth/social/kakao` | Deferred | Week 3 MVP는 이메일/비밀번호 인증 기준이며 소셜 로그인은 보류되었습니다. | 없음 | 없음 |
| 구글 소셜 로그인 | `GET/POST /api/auth/social/google` | Deferred | Week 3 MVP는 이메일/비밀번호 인증 기준이며 소셜 로그인은 보류되었습니다. | 없음 | 없음 |
| 프로필 및 기본 정보 조회 | `GET /api/users/me` | Done | 현재 사용자 기본 정보와 preferences hydration이 구현되어 있습니다. | `AuthController`, `UserDto` | `authApi.fetchMe`, `MyPageView` |
| 회원 정보 수정 | `PATCH /api/users/me` | Done | 닉네임과 preferences 수정 API가 구현되어 있고 프로필 편집 화면에서 호출합니다. | `AuthController`, `AuthService.updateMe` | `MyProfileEditView`, `uiStore.updateProfile` |
| 회원 탈퇴 | `DELETE /api/users/me` | Deferred | 회원 탈퇴는 Week 3 MVP 범위 밖이며 현재 endpoint가 없습니다. | 없음 | 없음 |
| 내 프로필 이미지 업로드 | `POST /api/users/me/profile-image` | Deferred | 현재 MVP는 기본 아바타를 사용하며 이미지 업로드 endpoint는 없습니다. | 없음 | `MyProfileEditView` 안내 메시지 |
| 내 콘텐츠 취향 설정 조회 | `GET /api/users/me` | Done | 별도 취향 조회 endpoint 대신 현재 사용자 응답의 `preferences`로 조회합니다. | `AuthController`, `UserDto.preferences` | `authApi.fetchMe`, `MyProfileEditView` |
| 내 취향 및 선호 지역/테마 저장 | `PATCH /api/users/me` | Done | `preferences` 배열 저장이 백엔드와 프론트에 연결되어 있습니다. | `AuthController`, `AuthService.updateMe` | `uiStore.savePreferences`, `MyProfileEditView` |
| 내가 좋아요한 콘텐츠 목록 조회 | `GET /api/users/me/liked-contents` | Done | 마이페이지용 좋아요 콘텐츠 목록 API와 프론트 목록이 연결되어 있습니다. | `LikeVisitController` | `engagementApi.fetchEngagementState`, `SimpleListView` |
| 내가 좋아요한 장소 목록 조회 | `GET /api/users/me/liked-places` | Done | 마이페이지용 좋아요 장소 목록 API와 프론트 목록이 연결되어 있습니다. | `LikeVisitController` | `engagementApi.fetchEngagementState`, `SimpleListView` |
| 내 방문 상태 목록 조회 | `GET /api/users/me/visit-status` | Done | 방문 예정/방문 완료 상태 목록을 한 endpoint에서 조회하고 프론트에서 분리 표시합니다. | `LikeVisitController` | `engagementApi.fetchVisitStatuses`, `MyPageView`, `SimpleListView` |
| 작성한 커뮤니티 게시글 목록 조회 | `GET /api/users/me/posts` | Done | 내가 작성한 게시글 목록 API가 구현되어 있으며 마이페이지에서 사용합니다. | `CommunityController` | `SimpleListView`, community store |
| 여행 활동 히스토리 조회 | `GET /api/users/me/history` | Done | 최근 조회/활동 이력 API가 구현되어 있고 마이페이지에서 사용합니다. | `UserActivityController` | `MyPageView` |
| 내 활동 요약 조회 | `GET /api/users/me/activity-summary` | Done | 좋아요, 방문 상태, 저장 경로 등 마이페이지 요약 카운트가 구현되어 있습니다. | `UserActivityController` | `MyPageView` |
| 내가 작성한 리뷰 목록 조회 | `GET /api/users/me/reviews` | In progress | 백엔드와 마이페이지 리뷰 목록 UI는 연결되었으나 live QA가 아직 필요합니다. | `PlaceReviewController` | `reviewApi.fetchMyReviews`, `SimpleListView` |
| 팔로우 | `POST /api/users/{userId}/follow` | In progress | 백엔드와 커뮤니티 작성자/팔로워 목록의 팔로우 버튼은 연결되었으나 두 계정 기반 live QA가 아직 필요합니다. | `FollowController`, `FollowService` | `followApi.followUser`, `CommunityView`, `SimpleListView` |
| 언팔로우 | `DELETE /api/users/{userId}/follow` | In progress | 백엔드와 커뮤니티 작성자/팔로잉 목록의 언팔로우 UI는 연결되었으나 두 계정 기반 live QA가 아직 필요합니다. | `FollowController`, `FollowService` | `followApi.unfollowUser`, `CommunityView`, `SimpleListView` |
| 팔로워 목록 조회 | `GET /api/users/{userId}/followers` | In progress | current user id로 팔로워 목록을 조회하고 맞팔 배지를 표시하는 UI가 연결되었으나 live QA가 아직 필요합니다. | `FollowController`, `FollowMapper.xml` | `followApi.fetchFollowers`, `SimpleListView` |
| 팔로잉 목록 조회 | `GET /api/users/{userId}/following`, `GET /api/users/me/following` | In progress | 백엔드와 마이페이지 팔로잉 목록, 언팔로우 버튼, 맞팔 상태 계산 UI는 연결되었으나 live QA가 아직 필요합니다. | `FollowController`, `FollowMapper.xml` | `followApi.fetchMyFollowing`, `SimpleListView` |
| 콘텐츠 목록 조회 | `GET /api/contents` | Done | 목록 조회와 pagination이 구현되어 있습니다. | `ContentController` | `contentApi.fetchContents`, content/explore stores |
| 콘텐츠 카테고리 목록 조회 | `GET /api/contents/categories` | Done | 현재 canonical 카테고리 endpoint가 구현되어 있습니다. | `ContentController` | content/explore stores |
| 인기 콘텐츠 목록 조회 | `GET /api/contents/popular` | Done | 인기 콘텐츠 단축 조회 API가 구현되어 있습니다. | `ContentController` | `HomeView`, explore store |
| 콘텐츠 상세 조회 | `GET /api/contents/{contentId}` | Done | 문자열 `contentId` 기반 상세 조회가 구현되어 있습니다. | `ContentController` | `ContentDetailView` |
| 콘텐츠 관련 장소 목록 조회 | `GET /api/contents/{contentId}/places` | Done | 콘텐츠와 연결된 검증 장소 목록 조회가 구현되어 있습니다. | `ContentController` | `ContentDetailView`, AI loading flow |
| 콘텐츠 좋아요 | `POST /api/contents/{contentId}/likes` | Done | singular `/like`가 아니라 plural `/likes`가 현재 canonical endpoint입니다. | `LikeVisitController` | `engagementApi.likeContent`, UI store |
| 콘텐츠 좋아요 취소 | `DELETE /api/contents/{contentId}/likes` | Done | 콘텐츠 좋아요 취소 API가 구현되어 있습니다. | `LikeVisitController` | `engagementApi.unlikeContent`, UI store |
| `/api/contents/{contentId}/like` | `POST /api/contents/{contentId}/likes` | Deprecated | 구버전 singular path이며 현재는 plural `/likes`로 교체되었습니다. | canonical: `LikeVisitController` | canonical: `engagementApi.likeContent` |
| `/api/content` | `GET /api/contents` | Deprecated | 구버전 단수 content path이며 현재는 plural `/contents`를 사용합니다. | canonical: `ContentController` | canonical: `contentApi.fetchContents` |
| `/api/content/{id}` | `GET /api/contents/{contentId}` | Deprecated | 구버전 단수 content detail path이며 현재는 문자열 `contentId` 기반 plural path를 사용합니다. | canonical: `ContentController` | canonical: `ContentDetailView` |
| 장소 목록 조회 | `GET /api/places` | Done | 지역, 카테고리, 키워드, 정렬, pagination 조회가 구현되어 있습니다. | `PlaceController` | `placeApi.fetchPlaces`, place/explore stores |
| 주변 장소 목록 조회 | `GET /api/places/nearby` | Done | 좌표 기반 주변 장소 조회 API가 구현되어 있습니다. | `PlaceController` | map/explore flow |
| 장소 상세 조회 | `GET /api/places/{placeId}` | Done | 문자열 `placeId` 기반 상세 조회가 구현되어 있습니다. | `PlaceController` | `PlaceDetailView` |
| 장소 좋아요 | `POST /api/places/{placeId}/likes` | Done | singular `/like`가 아니라 plural `/likes`가 현재 canonical endpoint입니다. | `LikeVisitController` | `engagementApi.likePlace`, place detail/cards |
| 장소 좋아요 취소 | `DELETE /api/places/{placeId}/likes` | Done | 장소 좋아요 취소 API가 구현되어 있습니다. | `LikeVisitController` | `engagementApi.unlikePlace`, place detail/cards |
| 방문예정 등록 | `POST/PUT /api/places/{placeId}/visit-status` + `WANT_TO_GO` | Done | 기존 방문예정 행은 `visit-status`에 `WANT_TO_GO` 값을 보내는 방식으로 교체되었습니다. | `LikeVisitController` | `engagementApi.setVisitStatus`, place detail/mypage |
| 방문완료 등록 | `POST/PUT /api/places/{placeId}/visit-status` + `VISITED` | Done | 기존 방문완료 행은 `visit-status`에 `VISITED` 값을 보내는 방식으로 교체되었습니다. | `LikeVisitController` | `engagementApi.setVisitStatus`, place detail/mypage |
| 방문 상태 삭제 | `DELETE /api/places/{placeId}/visit-status` | Done | 장소 방문 상태 삭제 API가 구현되어 있습니다. | `LikeVisitController` | `engagementApi.clearVisitStatus`, place detail/mypage |
| 장소 리뷰 목록 조회 | `GET /api/places/{placeId}/reviews` | In progress | 백엔드와 장소 상세 UI는 있으나 live QA가 아직 필요합니다. | `PlaceReviewController` | `PlaceDetailView`, `reviewApi.fetchPlaceReviews` |
| 장소 리뷰 작성 | `POST /api/places/{placeId}/reviews` | In progress | 백엔드와 작성 UI는 있으나 live QA가 아직 필요합니다. | `PlaceReviewController` | `PlaceDetailView`, `reviewApi.createPlaceReview` |
| 장소 리뷰 수정 | `PATCH /api/places/{placeId}/reviews/{reviewId}` | In progress | 백엔드와 장소 상세 수정 UI는 연결되었으나 live QA가 아직 필요합니다. | `PlaceReviewController` | `PlaceDetailView`, `reviewApi.updatePlaceReview` |
| 장소 리뷰 삭제 | `DELETE /api/places/{placeId}/reviews/{reviewId}` | In progress | 백엔드와 장소 상세 삭제 UI는 연결되었으나 live QA가 아직 필요합니다. | `PlaceReviewController` | `PlaceDetailView`, `reviewApi.deletePlaceReview` |
| `/api/places/{placeId}/like` | `POST /api/places/{placeId}/likes` | Deprecated | 구버전 singular path이며 현재는 plural `/likes`로 교체되었습니다. | canonical: `LikeVisitController` | canonical: `engagementApi.likePlace` |
| `/api/places/{placeId}/visit-plan` | `POST/PUT /api/places/{placeId}/visit-status` + `WANT_TO_GO` | Deprecated | 방문예정 전용 구버전 path이며 현재는 status 값으로 구분합니다. | canonical: `LikeVisitController` | canonical: `engagementApi.setVisitStatus` |
| `/api/places/{placeId}/visit-complete` | `POST/PUT /api/places/{placeId}/visit-status` + `VISITED` | Deprecated | 방문완료 전용 구버전 path이며 현재는 status 값으로 구분합니다. | canonical: `LikeVisitController` | canonical: `engagementApi.setVisitStatus` |
| 지도 주소 검색 | `GET /api/maps/geocode` | In progress | 백엔드 fallback은 있으나 외부 map provider live QA가 필요합니다. | `MapController`, `MapService` | map/search UI |
| 지도 역지오코딩 | `GET /api/maps/reverse-geocode` | In progress | 백엔드 fallback은 있으나 외부 map provider live QA가 필요합니다. | `MapController`, `MapService` | map/search UI |
| 지도 로컬 검색 | `GET /api/maps/local-search` | In progress | 백엔드 fallback은 있으나 외부 map provider live QA가 필요합니다. | `MapController`, `MapService` | map/search UI |
| AI 여행 동선 생성 | `POST /api/ai/routes` | Done | Spring Boot backend의 검증 장소 기반 AI route generation이 구현되어 있습니다. | `AiRouteController`, `AiRouteService` | `AiLoadingView`, `aiApi.generateAiRoute` |
| AI 여행 동선 재생성 | `POST /api/ai/routes/{generationId}/regenerate` | Done | 기존 generationId 기반 재생성 API가 구현되어 있습니다. | `AiRouteController` | AI loading/result flow |
| `/api/ai-routes` | `POST /api/ai/routes` 또는 `POST /api/routes` | Deprecated | 구버전 AI route path이며 생성은 `/api/ai/routes`, 저장은 `/api/routes`로 분리되었습니다. | canonical: `AiRouteController`, `RouteController` | canonical: `aiApi`, `routeApi` |
| `/api/ai/courses` | `POST /api/ai/routes` | Deprecated | 구버전 AI course path이며 현재 canonical 생성 endpoint는 `/api/ai/routes`입니다. | canonical: `AiRouteController` | canonical: `aiApi.generateAiRoute` |
| OpenAI/RAG 경로 품질 개선 | `POST /api/ai/routes` | Deferred | 실제 OpenAI/RAG/ChromaDB 품질 개선은 Week 4 범위입니다. | deterministic fallback만 구현 | AI condition/loading flow |
| 임베딩 전체 재생성 | `(internal) POST /ai/embeddings/rebuild` | Deferred | FastAPI 내부 stub은 있으나 production ChromaDB/RAG 관리 API는 아직 아닙니다. | `sceniK-ai` stub | 없음 |
| 장소 임베딩 갱신 | `(internal) PUT /ai/embeddings/{placeId}` | Deferred | FastAPI 내부 stub은 있으나 production embedding pipeline은 아직 아닙니다. | `sceniK-ai` stub | 없음 |
| 저장한 AI 여행 동선 목록 조회 | `GET /api/users/me/routes` | Done | 마이페이지 저장 경로 목록 조회가 구현되어 있습니다. | `RouteController` | `routeApi.fetchMyRoutes`, saved route screens |
| AI 여행 동선 저장 | `POST /api/routes` | Done | AI 결과 또는 사용자 편집 경로 저장 API가 구현되어 있습니다. | `RouteController` | `routeApi.saveRoute`, AI result save flow |
| AI 여행 동선 상세 조회 | `GET /api/routes/{routeId}` | Done | 저장 경로 상세 조회가 구현되어 있습니다. | `RouteController` | `CourseDetailView`, `routeApi.fetchRouteDetail` |
| AI 여행 동선 수정 | `PATCH /api/routes/{routeId}` | Done | 저장 경로 기본 정보 수정 API가 구현되어 있습니다. | `RouteController` | `CourseEditView`, `routeApi.updateRoute` |
| AI 여행 동선 삭제 | `DELETE /api/routes/{routeId}` | Done | 저장 경로 삭제 API가 구현되어 있습니다. | `RouteController` | route management flow |
| 코스 장소 추가 | `POST /api/routes/{routeId}/places` | In progress | 백엔드는 구현되어 있으나 편집 UI live QA가 필요합니다. | `RouteController` | `routeApi.addRoutePlace`, edit flow partial |
| 코스 장소 수정 | `PATCH /api/routes/{routeId}/places/{routePlaceId}` | In progress | 백엔드는 구현되어 있으나 편집 UI live QA가 필요합니다. | `RouteController` | `routeApi.updateRoutePlace`, edit flow partial |
| 코스 장소 삭제 | `DELETE /api/routes/{routeId}/places/{routePlaceId}` | In progress | 백엔드는 구현되어 있으나 편집 UI live QA가 필요합니다. | `RouteController` | `routeApi.deleteRoutePlace`, edit flow partial |
| 코스 장소 순서 변경 | `PATCH /api/routes/{routeId}/places/reorder` | In progress | 백엔드는 구현되어 있으나 편집 UI live QA가 필요합니다. | `RouteController` | `routeApi.reorderRoutePlaces`, edit flow partial |
| AI 여행 동선 공개 공유 | `PATCH /api/routes/{routeId}/visibility` | Deferred | 공개 공유 제품 플로우는 Week 3 MVP 범위 밖입니다. | 없음 | 없음 |
| `/api/courses` | `POST /api/routes`, `GET /api/users/me/routes` | Deprecated | 구버전 courses path이며 저장은 `/api/routes`, 목록은 `/api/users/me/routes`를 사용합니다. | canonical: `RouteController` | canonical: `routeApi` |
| `/api/users/me/courses` | `GET /api/users/me/routes` | Deprecated | 구버전 내 코스 목록 path이며 현재는 `/api/users/me/routes`를 사용합니다. | canonical: `RouteController` | canonical: `routeApi.fetchMyRoutes` |
| 커뮤니티 게시글 목록 조회 | `GET /api/posts` | Done | 게시글 목록, 검색, 지역 필터, 정렬, pagination이 구현되어 있습니다. | `CommunityController` | `CommunityView`, `communityApi.fetchPosts` |
| 커뮤니티 게시글 상세 조회 | `GET /api/posts/{postId}` | Done | 게시글 상세 조회 API가 구현되어 있습니다. | `CommunityController` | community detail flow |
| 커뮤니티 게시글 작성 | `POST /api/posts` | Done | 게시글 작성 API와 프론트 작성 플로우, 저장 상태 안내가 구현되어 있습니다. | `CommunityController` | `CommunityWriteView`, `communityApi.createPost`, `FormStatusNotice` |
| 커뮤니티 게시글 수정 | `PATCH /api/posts/{postId}` | Done | 게시글 소유자 기반 수정 API와 상세 화면 수정 UI가 구현되어 있습니다. | `CommunityController` | `CommunityView`, `uiStore.editPost`, `communityApi.updatePost` |
| 커뮤니티 게시글 삭제 | `DELETE /api/posts/{postId}` | Done | 게시글 소유자 기반 삭제 API와 상세 화면 삭제 확인 UI가 구현되어 있습니다. | `CommunityController` | `CommunityView`, `uiStore.removePost`, `communityApi.deletePost` |
| 게시글 댓글 목록 조회 | `GET /api/posts/{postId}/comments` | Done | 댓글 목록 API와 pagination이 구현되어 있습니다. | `CommunityController` | community detail, `communityApi.fetchComments` |
| 게시글 댓글 작성 | `POST /api/posts/{postId}/comments` | Done | 댓글 작성 API와 프론트 상세 플로우, 작성 상태 안내가 구현되어 있습니다. | `CommunityController` | `CommunityView`, `communityApi.createComment`, `FormStatusNotice` |
| 게시글 댓글 수정 | `PATCH /api/posts/{postId}/comments/{commentId}` | In progress | 백엔드와 댓글 작성자 수정 action sheet UI는 연결되었으나 live QA가 아직 필요합니다. | `CommunityController` | `CommunityView`, `uiStore.editComment`, `communityApi.updateComment` |
| 게시글 댓글 삭제 | `DELETE /api/posts/{postId}/comments/{commentId}` | In progress | 백엔드와 댓글 작성자 삭제 action sheet UI는 연결되었으나 live QA가 아직 필요합니다. | `CommunityController` | `CommunityView`, `uiStore.removeComment`, `communityApi.deleteComment` |
| 게시글 좋아요 | `POST /api/posts/{postId}/likes` | Done | singular `/like`가 아니라 plural `/likes`가 현재 canonical endpoint입니다. | `CommunityController` | community UI, `communityApi.likePost` |
| 게시글 좋아요 취소 | `DELETE /api/posts/{postId}/likes` | Done | 게시글 좋아요 취소 API가 구현되어 있습니다. | `CommunityController` | community UI, `communityApi.unlikePost` |
| 게시글 신고 | `POST /api/posts/{postId}/reports` | Done | singular `/report`가 아니라 plural `/reports`가 현재 canonical endpoint입니다. | `CommunityController` | community UI, `communityApi.reportPost` |
| `/api/posts/{postId}/like` | `POST /api/posts/{postId}/likes` | Deprecated | 구버전 singular path이며 현재는 plural `/likes`로 교체되었습니다. | canonical: `CommunityController` | canonical: `communityApi.likePost` |
| `/api/posts/{postId}/report` | `POST /api/posts/{postId}/reports` | Deprecated | 구버전 singular path이며 현재는 plural `/reports`로 교체되었습니다. | canonical: `CommunityController` | canonical: `communityApi.reportPost` |
| 댓글 신고 | `POST /api/posts/{postId}/comments/{commentId}/reports` | Deferred | 댓글 신고 canonical path는 정했지만 현재 backend endpoint는 없고 Week 3 MVP 범위 밖입니다. | 없음 | 없음 |
| `/api/posts/{postId}/comments/{commentId}/report` | `POST /api/posts/{postId}/comments/{commentId}/reports` | Deprecated | 구버전 singular path입니다. canonical 댓글 신고 기능 자체는 현재 Deferred입니다. | 없음 | 없음 |
| 알림 목록 조회 | `GET /api/notifications` | Done | 알림 목록과 unread count header가 구현되어 있습니다. | `CommunityController` | `NotificationsView`, `communityApi.getNotifications` |
| 전체 알림 읽음 처리 | `PATCH /api/notifications/read` | Done | 현재 mark-all-read canonical endpoint입니다. | `CommunityController` | `NotificationsView`, `communityApi.markNotificationsRead` |
| 단일 알림 읽음 처리 | `PATCH /api/notifications/{notificationId}/read` | Done | 단일 알림 읽음 처리 API가 구현되어 있습니다. | `CommunityController` | notifications UI |
| `/api/notifications/read-all` | `PATCH /api/notifications/read` | Deprecated | 구버전 read-all path이며 현재는 `/api/notifications/read`를 사용합니다. | canonical: `CommunityController` | canonical: `NotificationsView` |
| 알림 설정 조회 | `GET /api/users/me/notification-settings` | Deferred | 알림 설정은 Week 3 MVP 범위 밖이며 현재 endpoint가 없습니다. | 없음 | 없음 |
| 알림 설정 수정 | `PATCH /api/users/me/notification-settings` | Deferred | 알림 설정은 Week 3 MVP 범위 밖이며 현재 endpoint가 없습니다. | 없음 | 없음 |
| 관리자 콘텐츠 생성 | `POST /api/admin/contents` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |
| 관리자 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |
| 관리자 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |
| 관리자 장소 생성 | `POST /api/admin/places` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |
| 관리자 장소 수정 | `PATCH /api/admin/places/{placeId}` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |
| 관리자 장소 삭제 | `DELETE /api/admin/places/{placeId}` | In progress | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. | `AdminController` | 없음 |

## 상태 카운트

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 24 |
| Deferred | 11 |
| Deprecated | 14 |
| Not progress | 0 |

## ID 타입 메모

| ID | 현재 타입 | 메모 |
| --- | --- | --- |
| `contentId` | String | 예: `C_DRAMA_001` |
| `placeId` | String | 예: `P_0001` |
| `routeId` | String | 현재 route schema와 DTO 기준 문자열입니다. |
| `userId` | Long / BIGINT | 사용자 PK는 숫자입니다. |
| `postId` | String | 현재 code/schema 기준 문자열입니다. |
| `commentId` | String | 현재 code/schema 기준 문자열입니다. |
| `notificationId` | String | 현재 code/schema 기준 문자열입니다. |
| `reportId` | String | 현재 code/schema 기준 문자열입니다. |
| `reviewId` | Long / BIGINT | 장소 리뷰 PK는 숫자입니다. |
