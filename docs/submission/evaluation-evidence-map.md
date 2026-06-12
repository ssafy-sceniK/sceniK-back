# 평가 증거 맵

생성일: 2026-06-09

이 문서는 평가자가 루브릭 항목별 구현 근거를 바로 찾을 수 있도록 backend, frontend, 문서 증거를 연결한 제출용 맵입니다.

| 루브릭 | 상태 | Backend 근거 | Frontend 근거 | 문서 근거 | 비고 |
| --- | --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 보강 완료 | `AdminController`, `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `PlaceReviewController`, `schema.sql` | 콘텐츠/장소 관리자 UI, 코스/커뮤니티/리뷰 화면, `CourseCrudPanel.vue`, `CourseSaveStatusPanel.vue`, `CourseDeleteActionSheet.vue`, `FormStatusNotice` | `core-crud-evidence.md`, `scenik-api-inventory.md`, `rubric-response.md` | 관리자 콘텐츠/장소 CUD, REST HTTP semantics, 코스 저장/수정/삭제 상태를 명시했습니다. |
| #02 목록 조회·검색·정렬 | 완료에 가까움 | `ListQuerySupport`, `ContentController`, `PlaceController`, `CommunityController`, `RouteController`, `LikeVisitController`, `schema.sql` indexes | `ContentListView.vue`, `PlaceListView.vue`, `CommunityView.vue`, `SimpleListView.vue` | `list-search-sort-pagination-evidence.md`, `list-search-sort-pagination.http` | query 조합, boundary QA, 동적 SQL, 인덱스 근거를 보강했습니다. |
| #03 상세 정보 조회 | 완료에 가까움 | detail controllers, `GlobalExceptionHandler`, DTO null/empty 처리 | `ContentDetailView.vue`, `PlaceDetailView.vue`, `CommunityView.vue`, `CourseDetailView.vue`, `EmptyState.vue` | `detail-retrieval-null-empty-evidence.md`, `final-rubric-live-qa-checklist.md`, `browser-live-qa-evidence.md` | 관련 데이터 빈 배열/빈 상태 정책을 명시했습니다. |
| #04 리뷰 작성·조회 | 구현 완료, live QA 필요 | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `ReviewDtos`, `schema.sql`의 `place_reviews`, `PlaceMapper.xml` | `reviewApi.js`, `PlaceDetailView.vue`, `ContentDetailView.vue`, `MyPageView.vue`, `SimpleListView.vue`, `detail-map-panels.css` | `review-feature-evidence.md`, `browser-live-qa-evidence.md`, `final-rubric-live-qa-checklist.md` | 방문 리뷰 요약, 평점 분포, 중복 리뷰 안내, 콘텐츠 상세의 관련 장소 리뷰 안내 포함. |
| #05 로그인·로그아웃 | 완료 | `AuthController`, `AuthService`, `AuthTokenMapper`, `GlobalExceptionHandler` | `authApi.js`, login/signup views, auth store, `MyPageView.vue`, `SettingsView.vue` | `auth-profile-evidence.md`, `rubric-response.md`, `final-rubric-live-qa-checklist.md` | Settings 세션 관리와 token revoke 흐름 포함. |
| #06 회원가입 | 완료 | `AuthService.signup`, `AuthService.checkEmail`, `UserMapper`, `schema.sql`의 `uq_users_email`, `uq_users_nickname` | signup view/store, `authApi.checkEmailAvailability` | `auth-profile-evidence.md`, `rubric-response.md`, `final-rubric-live-qa-checklist.md` | 이메일 중복 확인과 backend 정책 기반 비밀번호 안내 포함. 이메일 인증은 future work입니다. |
| #07 프로필 관리 | 보강 완료, 이미지 업로드 Deferred | `GET /api/users/me`, `PATCH /api/users/me`, `UserDto.preferences` | `MyProfileEditView.vue`, `MyPageView.vue` | `auth-profile-evidence.md`, `deferred-scope-decisions.md`, `rubric-response.md` | 로컬 이미지 미리보기, 업로드 deferred 안내, 이메일 read-only, preference chips, 저장 feedback, MyPage 취향 요약을 보강했습니다. |
| #08 찜·즐겨찾기 | 완료에 가까움 | `LikeVisitController`, `LikeVisitService` | `engagementApi.js`, `uiStore`, detail like buttons, mypage liked/visit lists | `scenik-api-status.md`, `final-rubric-live-qa-checklist.md` | pending guard와 `aria-pressed`를 추가했습니다. |
| #09 팔로우·팔로잉 | 구현 완료, live QA 필요 | `FollowController`, `FollowService`, `FollowMapper.xml`, `schema.sql`의 `follows` | `followApi.js`, `uiStore`, `CommunityView.vue`, `MyPageView.vue`, `SimpleListView.vue` | `follow-feature-evidence.md`, `follow-live-qa-evidence.md`, `final-rubric-live-qa-checklist.md` | 맞팔 badge와 목록 내 follow/unfollow 상태 표시 포함. Account A/B 캡처 계획과 저장 경로를 명시했습니다. |
| #10 게시판 CRUD | 완료에 가까움 | `CommunityController`, `CommunityService`, `CommunityMapper` | `CommunityView.vue`, `CommunityWriteView.vue`, `communityApi.js` | `community-comment-evidence.md`, `list-search-sort-pagination-evidence.md` | pagination/sort 증거 포함. |
| #11 댓글 CRUD | 부분 완료 | `CommunityController`, `CommunityService`, `CommunityDtos`, owner check, `parentCommentId`, `comment_likes` | `CommunityView.vue`, `uiStore.addComment`, `uiStore.editComment`, `uiStore.removeComment`, `uiStore.toggleCommentLike`, `community.css` | `community-comment-evidence.md`, `browser-live-qa-evidence.md`, `final-rubric-live-qa-checklist.md` | 댓글 action sheet, 1단계 답글, 답글 대상 입력, 댓글 좋아요 UI/API 구현, live QA 필요. |
| #12 계획·챌린지 관리 | 완료에 가까움 | `RouteController`, `RouteService`, `RoutePlaceMapper` | `AiResultView.vue`, `CourseDetailView.vue`, `CourseEditView.vue`, `routeApi.js` | `in-progress-live-qa-checklist.md`, `final-rubric-live-qa-checklist.md` | route place 편집 live QA 필요. |
| #13 생성형 AI 활용 기능 | 완료에 가까움 | `AiRouteController`, `AiRouteService` | `CourseConditionsView.vue`, `AiLoadingView.vue`, `AiResultView.vue`, `aiApi.js` | `ai-prompt-context-design.md`, `backend-fastapi-contract.md`, `week4-task-plan.md` | 조건/로딩/결과 화면에 AI 참고 조건과 검증 장소 기반 추천 패널 추가. |
| #14 설계·기획 문서 | 부분 완료 | API/status docs | 화면/flow 관련 문서 | `document-index.md`, `PRD.md`, `presentation-outline.md`, `final-report-outline.md`, `evaluation-evidence-map.md` | 문서 위치를 index로 연결했습니다. |
| #15 제출물 완성도 | 보강 완료, export 필요 | 전체 backend source/docs | 전체 frontend source | `final-report.md`, `presentation.md`, `submission-artifact-index.md`, `submission-checklist.md`, `evaluation-evidence-map.md`, `dataset-summary.md`, `external-api-ai-usage.md`, `demo-scenario.md`, `rubric-response.md` | final Markdown 본문은 준비되었습니다. PDF/PPT export와 live screenshot은 제출 전 수동 작업입니다. |

## 빠른 확인 경로

- 리뷰 기능: `backend/docs/evidence/review-feature-evidence.md`
- 팔로우 기능: `backend/docs/evidence/follow-feature-evidence.md`, `backend/docs/evidence/follow-live-qa-evidence.md`
- 브라우저 live QA: `backend/docs/evidence/browser-live-qa-evidence.md`
- 인증/프로필: `backend/docs/evidence/auth-profile-evidence.md`
- 핵심 CRUD: `backend/docs/evidence/core-crud-evidence.md`
- 목록/검색/정렬/페이징: `backend/docs/evidence/list-search-sort-pagination-evidence.md`
- 커뮤니티/댓글: `backend/docs/evidence/community-comment-evidence.md`
- 최종 QA: `backend/docs/qa/final-rubric-live-qa-checklist.md`
- 발표자료 final본: `backend/docs/submission/presentation.md`
- 최종 보고서 final본: `backend/docs/submission/final-report.md`
- 발표 개요: `backend/docs/submission/presentation-outline.md`
- 최종 보고서 개요: `backend/docs/submission/final-report-outline.md`
- 루브릭 응답: `backend/docs/submission/rubric-response.md`
- 제출 산출물 인덱스: `backend/docs/submission/submission-artifact-index.md`

## 루브릭별 직접 확인 경로

아래 경로는 analyzer/evaluator가 항목별 구현 위치를 바로 확인할 수 있도록 controller/service/mapper, frontend 화면, API, QA 문서를 함께 묶은 보강 목록입니다. 줄 번호는 코드 변경에 따라 흔들릴 수 있으므로 파일 경로와 문서 섹션명을 기준으로 확인합니다.

### #01 핵심 데이터 관리 CRUD

- Backend:
  - `src/main/java/com/scenik/controller/RouteController.java`
  - `src/main/java/com/scenik/controller/CommunityController.java`
  - `src/main/java/com/scenik/controller/PlaceReviewController.java`
  - `src/main/java/com/scenik/controller/AdminController.java`
  - `src/main/resources/schema.sql`
- Frontend:
  - `frontend/src/views/CourseDetailView.vue`
  - `frontend/src/views/CourseEditView.vue`
  - `frontend/src/components/course/CourseCrudPanel.vue`
  - `frontend/src/views/CommunityView.vue`
  - `frontend/src/views/PlaceDetailView.vue`
- API:
  - `POST /api/routes`, `PATCH /api/routes/{routeId}`, `DELETE /api/routes/{routeId}`
  - `POST /api/posts`, `PATCH /api/posts/{postId}`, `DELETE /api/posts/{postId}`
  - `POST /api/places/{placeId}/reviews`, `PATCH /api/places/{placeId}/reviews/{reviewId}`, `DELETE /api/places/{placeId}/reviews/{reviewId}`
- QA:
  - `docs/evidence/core-crud-evidence.md`
  - `docs/qa/final-rubric-live-qa-checklist.md`의 `최종 회귀 QA — 경계값/빈 상태/상태 전환`

### #02 목록 조회·검색·정렬

- Backend:
  - `src/main/java/com/scenik/util/ListQuerySupport.java`
  - `src/main/java/com/scenik/controller/ContentController.java`
  - `src/main/java/com/scenik/controller/PlaceController.java`
  - `src/main/java/com/scenik/controller/CommunityController.java`
  - `src/main/java/com/scenik/controller/RouteController.java`
- Frontend:
  - `frontend/src/views/ContentListView.vue`
  - `frontend/src/views/PlaceListView.vue`
  - `frontend/src/views/CommunityView.vue`
  - `frontend/src/views/SimpleListView.vue`
  - `frontend/src/components/common/SortSegmentedControl.vue`
- API:
  - `GET /api/contents?page=&size=&sort=&keyword=`
  - `GET /api/places?page=&size=&sort=&region=&category=&keyword=`
  - `GET /api/posts?page=&size=&sort=&keyword=&region=`
  - `GET /api/users/me/routes?page=&size=&sort=`
- QA:
  - `docs/evidence/list-search-sort-pagination-evidence.md`
  - `api-tests/list-search-sort-pagination.http`

### #03 상세 정보 조회

- Backend:
  - `src/main/java/com/scenik/controller/ContentController.java`
  - `src/main/java/com/scenik/controller/PlaceController.java`
  - `src/main/java/com/scenik/controller/RouteController.java`
  - `src/main/java/com/scenik/exception/GlobalExceptionHandler.java`
- Frontend:
  - `frontend/src/views/ContentDetailView.vue`
  - `frontend/src/views/PlaceDetailView.vue`
  - `frontend/src/views/CourseDetailView.vue`
  - `frontend/src/components/common/EmptyState.vue`
- API:
  - `GET /api/contents/{contentId}`
  - `GET /api/contents/{contentId}/places`
  - `GET /api/places/{placeId}`
  - `GET /api/routes/{routeId}`
- QA:
  - `docs/evidence/detail-retrieval-null-empty-evidence.md`
  - `docs/evidence/browser-live-qa-evidence.md`
  - invalid ID, related data empty, fallback image, back/retry recovery 확인

### #04 리뷰 작성·조회

- Backend:
  - `src/main/java/com/scenik/controller/PlaceReviewController.java`
  - `src/main/java/com/scenik/service/PlaceReviewService.java`
  - `src/main/resources/mapper/PlaceReviewMapper.xml`
  - `src/main/resources/mapper/PlaceMapper.xml`
- Frontend:
  - `frontend/src/api/reviewApi.js`
  - `frontend/src/components/review/ReviewPanel.vue`
  - `frontend/src/views/PlaceDetailView.vue`
  - `frontend/src/views/SimpleListView.vue`
- API:
  - `GET /api/places/{placeId}/reviews`
  - `POST /api/places/{placeId}/reviews`
  - `PATCH /api/places/{placeId}/reviews/{reviewId}`
  - `DELETE /api/places/{placeId}/reviews/{reviewId}`
  - `GET /api/users/me/reviews`
- QA:
  - `docs/evidence/review-feature-evidence.md`
  - duplicate review prevention, owner edit/delete, average rating update

### #05 로그인·로그아웃

- Backend:
  - `src/main/java/com/scenik/controller/AuthController.java`
  - `src/main/java/com/scenik/service/AuthService.java`
  - `src/main/resources/mapper/AuthTokenMapper.xml`
  - `src/main/java/com/scenik/config/SecurityConfig.java`
- Frontend:
  - `frontend/src/api/authApi.js`
  - `frontend/src/api/http.js`
  - `frontend/src/stores/uiStore.js`
  - `frontend/src/views/LoginView.vue`
  - `frontend/src/views/SettingsView.vue`
- API:
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/users/me`
- QA:
  - `docs/evidence/auth-profile-evidence.md`
  - missing token, invalid token, logout token reuse, expired token if supported

### #06 회원가입

- Backend:
  - `src/main/java/com/scenik/service/AuthService.java`
  - `src/main/resources/mapper/UserMapper.xml`
  - `src/main/resources/schema.sql`
- Frontend:
  - `frontend/src/views/SignupView.vue`
  - `frontend/src/components/auth/SignupValidationPanel.vue`
  - `frontend/src/api/authApi.js`
- API:
  - `POST /api/auth/signup`
  - `GET /api/auth/check-email?email=`
- QA:
  - invalid email, duplicate email, password too short, password confirmation mismatch

### #07 프로필 관리

- Backend:
  - `src/main/java/com/scenik/controller/AuthController.java`
  - `src/main/java/com/scenik/service/AuthService.java`
  - `src/main/resources/mapper/UserMapper.xml`
- Frontend:
  - `frontend/src/views/MyProfileEditView.vue`
  - `frontend/src/views/MyPageView.vue`
  - `frontend/src/stores/uiStore.js`
- API:
  - `GET /api/users/me`
  - `PATCH /api/users/me`
- QA:
  - `docs/evidence/auth-profile-evidence.md`
  - profile image preview는 로컬 미리보기이며 서버 업로드는 Deferred

### #08 찜·즐겨찾기

- Backend:
  - `src/main/java/com/scenik/controller/LikeVisitController.java`
  - `src/main/java/com/scenik/service/LikeVisitService.java`
  - `src/main/resources/mapper/LikeVisitMapper.xml`
- Frontend:
  - `frontend/src/api/engagementApi.js`
  - `frontend/src/stores/uiStore.js`
  - `frontend/src/views/ContentDetailView.vue`
  - `frontend/src/views/PlaceDetailView.vue`
  - `frontend/src/views/SimpleListView.vue`
- API:
  - `POST /api/contents/{contentId}/likes`
  - `DELETE /api/contents/{contentId}/likes`
  - `POST /api/places/{placeId}/likes`
  - `DELETE /api/places/{placeId}/likes`
  - `GET /api/users/me/liked-contents`
  - `GET /api/users/me/liked-places`
- QA:
  - toggle idempotency, pending guard, list refresh, user isolation

### #09 팔로우·팔로잉

- Backend:
  - `src/main/java/com/scenik/controller/FollowController.java`
  - `src/main/java/com/scenik/service/FollowService.java`
  - `src/main/resources/mapper/FollowMapper.xml`
- Frontend:
  - `frontend/src/api/followApi.js`
  - `frontend/src/components/follow/AuthorFollowCard.vue`
  - `frontend/src/components/follow/FollowUserListItem.vue`
  - `frontend/src/views/CommunityView.vue`
  - `frontend/src/views/SimpleListView.vue`
- API:
  - `POST /api/users/{userId}/follow`
  - `DELETE /api/users/{userId}/follow`
  - `GET /api/users/me/following`
  - `GET /api/users/{userId}/followers`
- QA:
  - `docs/evidence/follow-feature-evidence.md`
  - `docs/evidence/follow-live-qa-evidence.md`

### #10 게시판 CRUD

- Backend:
  - `src/main/java/com/scenik/controller/CommunityController.java`
  - `src/main/java/com/scenik/service/CommunityService.java`
  - `src/main/resources/mapper/CommunityMapper.xml`
- Frontend:
  - `frontend/src/api/communityApi.js`
  - `frontend/src/views/CommunityView.vue`
  - `frontend/src/views/CommunityWriteView.vue`
- API:
  - `GET /api/posts`
  - `GET /api/posts/{postId}`
  - `POST /api/posts`
  - `PATCH /api/posts/{postId}`
  - `DELETE /api/posts/{postId}`
- QA:
  - owner edit/delete, non-owner blocked, search/sort, empty board state

### #11 댓글 CRUD

- Backend:
  - `src/main/java/com/scenik/controller/CommunityController.java`
  - `src/main/java/com/scenik/service/CommunityService.java`
  - `src/main/resources/mapper/CommunityMapper.xml`
- Frontend:
  - `frontend/src/api/communityApi.js`
  - `frontend/src/stores/uiStore.js`
  - `frontend/src/views/CommunityView.vue`
- API:
  - `GET /api/posts/{postId}/comments`
  - `POST /api/posts/{postId}/comments`
  - `PATCH /api/posts/{postId}/comments/{commentId}`
  - `DELETE /api/posts/{postId}/comments/{commentId}`
  - `POST /api/posts/comments/{commentId}/likes`
  - `DELETE /api/posts/comments/{commentId}/likes`
- QA:
  - `docs/evidence/community-comment-evidence.md`
  - reply-to-reply one-level mapping, owner/non-owner validation

### #12 계획·챌린지 관리

- Backend:
  - `src/main/java/com/scenik/controller/RouteController.java`
  - `src/main/java/com/scenik/service/RouteService.java`
  - `src/main/resources/mapper/RouteMapper.xml`
  - `src/main/resources/mapper/RoutePlaceMapper.xml`
- Frontend:
  - `frontend/src/api/routeApi.js`
  - `frontend/src/views/AiResultView.vue`
  - `frontend/src/views/CourseDetailView.vue`
  - `frontend/src/views/CourseEditView.vue`
  - `frontend/src/components/course/RouteProgressSummary.vue`
- API:
  - `POST /api/routes`
  - `GET /api/users/me/routes`
  - `GET /api/routes/{routeId}`
  - `PATCH /api/routes/{routeId}`
  - `DELETE /api/routes/{routeId}`
  - route place add/update/delete/reorder endpoints
- QA:
  - route save, edit title, reorder place, remove all places validation, delete route

### #13 생성형 AI 활용 기능

- Backend:
  - `src/main/java/com/scenik/controller/AiRouteController.java`
  - `src/main/java/com/scenik/service/AiRouteService.java`
  - `src/main/resources/mapper/AiRouteMapper.xml`
- Frontend:
  - `frontend/src/api/aiApi.js`
  - `frontend/src/views/CourseConditionsView.vue`
  - `frontend/src/views/AiLoadingView.vue`
  - `frontend/src/views/AiResultView.vue`
  - `frontend/src/components/ai/AiPromptContextSummary.vue`
- API:
  - `POST /api/ai/routes`
- QA:
  - prompt context panel, result reason panel, fallback route generation, backend-only AI call

### #14 설계·기획 문서

- 문서:
  - `docs/project/PRD.md`
  - `docs/project/scenik-implementation-spec.md`
  - `docs/project/document-index.md`
  - `docs/submission/evaluation-evidence-map.md`
  - `docs/qa/final-analyzer-feedback-hardening.md`
- QA:
  - README, PRD, 구현 명세, QA 문서, 제출 산출물 index 간 링크 정합성 확인

### #15 제출물 완성도

- 제출 산출물:
  - `docs/submission/final-report.md`
  - `docs/submission/presentation.md`
  - `docs/submission/submission-artifact-index.md`
  - `docs/submission/submission-checklist.md`
  - `docs/submission/rubric-response.md`
- Planned export:
  - `backend/docs/submission/final-report.pdf`
  - `backend/docs/submission/presentation.pdf`
  - `backend/docs/submission/presentation.pptx`
- QA:
  - PDF/PPT export 상태는 아직 `export 필요`
  - live screenshot은 아직 `수동 캡처 필요`
