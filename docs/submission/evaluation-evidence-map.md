# 평가 증거 맵

생성일: 2026-06-08

이 문서는 평가자가 루브릭 항목별 구현 근거를 바로 찾을 수 있도록 backend, frontend, 문서 증거를 연결한 제출용 맵입니다.

| 루브릭 | 상태 | Backend 근거 | Frontend 근거 | 문서 근거 | 비고 |
| --- | --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 부분 완료 | `AdminController`, `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `schema.sql` | 콘텐츠/장소/코스/커뮤니티 화면 | `scenik-api-inventory.md`, `rubric-response.md` | admin UI는 없으며 backend-only 범위로 문서화했습니다. |
| #02 목록 조회·검색·정렬 | 부분 완료 | `ListQuerySupport`, `ContentController`, `PlaceController`, `CommunityController`, `RouteController`, `LikeVisitController` | `ContentListView.vue`, `PlaceListView.vue`, `CommunityView.vue`, `SimpleListView.vue` | `list-search-sort-pagination-evidence.md`, `list-search-sort-pagination.http` | 서버 query param과 HTTP 예시를 보강했습니다. |
| #03 상세 정보 조회 | 부분 완료 | detail controllers, `GlobalExceptionHandler` | `ContentDetailView.vue`, `PlaceDetailView.vue`, `CommunityView.vue`, `CourseDetailView.vue`, `EmptyState.vue` | `final-rubric-live-qa-checklist.md` | invalid ID live QA가 필요합니다. |
| #04 리뷰 작성·조회 | 구현 완료, live QA 필요 | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `ReviewDtos`, `schema.sql`의 `place_reviews`, `PlaceMapper.xml` | `reviewApi.js`, `PlaceDetailView.vue`, `detail-map-panels.css` | `review-feature-evidence.md`, `final-rubric-live-qa-checklist.md` | 중복 방지, owner check, 평균 평점/리뷰 수 포함. |
| #05 로그인·로그아웃 | 완료 | `AuthController`, `AuthService`, `AuthTokenMapper`, `GlobalExceptionHandler` | `authApi.js`, login/signup views, auth store | `rubric-response.md`, `final-rubric-live-qa-checklist.md` | token refresh 흐름 포함. |
| #06 회원가입 | 완료 | `AuthService.signup`, `UserMapper`, `schema.sql`의 `uq_users_email`, `uq_users_nickname` | signup view/store | `rubric-response.md`, `final-rubric-live-qa-checklist.md` | 이메일 인증은 future work입니다. |
| #07 프로필 관리 | 부분 완료 | `GET /api/users/me`, `PATCH /api/users/me`, `UserDto.preferences` | `MyProfileEditView.vue`, mypage views | `deferred-scope-decisions.md`, `rubric-response.md` | 프로필 이미지 업로드는 Deferred 안내로 처리했습니다. |
| #08 찜·즐겨찾기 | 완료에 가까움 | `LikeVisitController`, `LikeVisitService` | `engagementApi.js`, `uiStore`, mypage liked/visit lists | `scenik-api-status.md`, `final-rubric-live-qa-checklist.md` | 좋아요/방문 상태는 backend-backed입니다. |
| #09 팔로우·팔로잉 | 보류 | 없음 | 없음 | `deferred-scope-decisions.md`, `rubric-response.md` | Week 3 핵심 MVP 밖으로 결정했습니다. |
| #10 게시판 CRUD | 완료에 가까움 | `CommunityController`, `CommunityService`, `CommunityMapper` | `CommunityView.vue`, `CommunityWriteView.vue`, `communityApi.js` | `community-comment-evidence.md`, `list-search-sort-pagination-evidence.md` | pagination/sort 증거 포함. |
| #11 댓글 CRUD | 부분 완료 | `CommunityController`, `CommunityService`, `CommunityDtos`, owner check | `CommunityView.vue`, `uiStore.editComment`, `uiStore.removeComment`, `community.css` | `community-comment-evidence.md`, `final-rubric-live-qa-checklist.md` | 댓글 수정/삭제 UI는 구현, live QA 필요. 대댓글 UI는 보류. |
| #12 계획·챌린지 관리 | 완료에 가까움 | `RouteController`, `RouteService`, `RoutePlaceMapper` | `AiResultView.vue`, `CourseDetailView.vue`, `CourseEditView.vue`, `routeApi.js` | `in-progress-live-qa-checklist.md`, `final-rubric-live-qa-checklist.md` | route place 편집 live QA 필요. |
| #13 생성형 AI 활용 기능 | 완료에 가까움 | `AiRouteController`, `AiRouteService` | `AiLoadingView.vue`, `AiResultView.vue`, `aiApi.js` | `ai-prompt-context-design.md`, `backend-fastapi-contract.md`, `week4-task-plan.md` | Week 3 deterministic, Week 4 RAG/OpenAI 계획. |
| #14 설계·기획 문서 | 부분 완료 | API/status docs | 화면/flow 관련 문서 | `PRD.md`, `presentation-outline.md`, `final-report-outline.md`, `evaluation-evidence-map.md` | 제출 전 ERD 이미지/원본 위치 확인 필요. |
| #15 제출물 완성도 | 부분 완료 | 전체 backend source/docs | 전체 frontend source | `submission-checklist.md`, `evaluation-evidence-map.md`, `dataset-summary.md`, `external-api-ai-usage.md`, `demo-scenario.md`, `rubric-response.md` | 실제 최종 보고서/PPT 파일 작성은 별도 필요. |

## 빠른 확인 경로

- 리뷰 기능: `backend/docs/evidence/review-feature-evidence.md`
- 목록/검색/정렬/페이징: `backend/docs/evidence/list-search-sort-pagination-evidence.md`
- 커뮤니티/댓글: `backend/docs/evidence/community-comment-evidence.md`
- 최종 QA: `backend/docs/qa/final-rubric-live-qa-checklist.md`
- 발표 개요: `backend/docs/submission/presentation-outline.md`
- 최종 보고서 개요: `backend/docs/submission/final-report-outline.md`
- 루브릭 응답: `backend/docs/submission/rubric-response.md`
