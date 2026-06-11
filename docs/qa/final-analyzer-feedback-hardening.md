# 최종 Analyzer 피드백 보강 감사

생성일: 2026-06-11

이 문서는 최신 analyzer 피드백에서 남은 요청을 기능 추가가 아니라 검증 근거, 제출 산출물 경로, 최종 QA 완성도 관점으로 정리한 제출용 감사 문서입니다. 이번 보강의 목적은 평가자가 루브릭별 구현 근거와 제출 산출물 위치를 더 빠르게 확인할 수 있도록 문서 연결성과 최종 QA 항목을 보강하는 것입니다.

## 보강 원칙

- 새 기능을 넓히지 않고 기존 구현의 증거와 검증 경로를 명확히 정리합니다.
- 실제 실행하지 않은 live QA, screenshot, PDF/PPT export는 완료로 표기하지 않습니다.
- backend, frontend, API endpoint, QA 문서, 남은 수동 확인 항목을 분리해 평가자가 추적할 수 있게 합니다.
- `sceniK-ai`는 이번 pass에서 수정하지 않습니다.

## 루브릭별 최종 보강 감사

| Rubric | Current analyzer status | Remaining feedback | Final hardening action | Evidence file | Code evidence | QA check | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 구현 확인, admin UI 범위 보수 표기 | 입력 검증과 응답 상태 코드 일관성 최종 확인 필요 | 사용자-facing CRUD와 backend-only admin CRUD를 분리하고 invalid/create/update/delete/owner 권한 QA를 최종 회귀 항목에 추가 | `docs/evidence/core-crud-evidence.md`, `docs/submission/evaluation-evidence-map.md` | `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `PlaceReviewController`, `AdminController`, `schema.sql` | `final-rubric-live-qa-checklist.md`의 #01 CRUD 경계값 항목 | 문서 보강 완료, live QA 필요 |
| #02 목록 조회·검색·정렬 | 구현 확인, 일부 pagination UI 보수 표기 | 검색/정렬/페이지 조합과 query 동작 확인 필요 | keyword/category/sort/pagination/more/URL query restore를 최종 회귀 QA에 추가 | `docs/evidence/list-search-sort-pagination-evidence.md`, `api-tests/list-search-sort-pagination.http` | `ListQuerySupport`, `ContentController`, `PlaceController`, `CommunityController`, `RouteController`, `LikeVisitController` | empty keyword, no result, latest/popular, category+keyword, pagination | 문서 보강 완료, live QA 필요 |
| #03 상세 정보 조회 | 구현 확인, invalid ID 증거 필요 | 404/empty state와 관련 데이터 fallback 확인 필요 | invalid contentId/placeId/routeId, related data empty, fallback image, back/retry recovery를 QA에 추가 | `docs/evidence/browser-live-qa-evidence.md`, `docs/qa/final-rubric-live-qa-checklist.md` | `ContentController`, `PlaceController`, `RouteController`, `GlobalExceptionHandler` | invalid detail route browser QA | 문서 보강 완료, live QA 필요 |
| #04 리뷰 작성·조회 | 구현 확인, live QA 증거 필요 | 중복 리뷰 방지와 평균 평점 집계 경계값 확인 필요 | rating min/max, empty content, duplicate review, aggregate update, owner-only edit/delete를 회귀 QA에 추가 | `docs/evidence/review-feature-evidence.md`, `docs/evidence/browser-live-qa-evidence.md` | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `PlaceMapper.xml`, `ReviewDtos` | 리뷰 create/update/delete 후 평균 평점/리뷰 수 확인 | 문서 보강 완료, live QA 필요 |
| #05 로그인·로그아웃 | 구현 확인 | JWT/session invalidation, logout token reuse, expired token 확인 필요 | missing/invalid token, logout token reuse, expired token if supported, frontend login guidance를 회귀 QA에 추가 | `docs/evidence/auth-profile-evidence.md`, `docs/qa/final-rubric-live-qa-checklist.md` | `AuthController`, `AuthService`, `AuthTokenMapper.xml`, `SecurityConfig` | logout 후 재사용 차단 여부, 401 안내 | 문서 보강 완료, live QA 필요 |
| #06 회원가입 | 구현 확인 | 비밀번호 정책, 이메일 중복, frontend/server 메시지 일관성 확인 필요 | invalid email, duplicate email, short password, password confirmation mismatch, signup success route를 QA에 추가 | `docs/evidence/auth-profile-evidence.md` | `AuthService.signup`, `AuthService.checkEmail`, `UserMapper.xml`, `GlobalExceptionHandler` | 회원가입 form validation과 backend validation 비교 | 문서 보강 완료, live QA 필요 |
| #07 프로필 관리 | 구현 확인, 이미지 업로드 Deferred | 프로필 이미지/preference partial update와 validation 확인 필요 | nickname too short, preference chips partial update, image preview/deferred upload copy, save success/failure를 QA에 추가 | `docs/evidence/auth-profile-evidence.md`, `docs/project/deferred-scope-decisions.md` | `AuthController`, `AuthService.updateMe`, `UserMapper.xml`, `UserDto.preferences` | profile edit browser QA | 문서 보강 완료, live QA 필요 |
| #08 찜·즐겨찾기 | 구현 확인 | 사용자 격리와 toggle idempotency 확인 필요 | double-click guard, duplicate like, unlike missing, list refresh, user isolation을 QA에 추가 | `docs/qa/final-rubric-live-qa-checklist.md`, `docs/api/scenik-api-status.md` | `LikeVisitController`, `LikeVisitService`, `LikeVisitMapper.xml` | content/place like toggle 및 mypage list refresh | 문서 보강 완료, live QA 필요 |
| #09 팔로우·팔로잉 | 구현 확인, two-account live QA 필요 | follower/following count와 mutual state 일관성 확인 필요 | self follow, duplicate follow, unfollow missing, count, mutual badge, two-account QA를 명시 | `docs/evidence/follow-feature-evidence.md`, `docs/evidence/follow-live-qa-evidence.md` | `FollowController`, `FollowService`, `FollowMapper.xml`, `FollowDtos` | Account A/B follow/unfollow/mutual badge | 문서 보강 완료, 수동 캡처 필요 |
| #10 게시판 CRUD | 구현 확인 | post create/edit/delete 권한과 sort flow 확인 필요 | owner edit/delete, non-owner blocked, search/sort, empty board state를 QA에 추가 | `docs/evidence/community-comment-evidence.md` | `CommunityController`, `CommunityService`, `CommunityMapper.xml` | post CRUD 및 permission browser QA | 문서 보강 완료, live QA 필요 |
| #11 댓글 CRUD | 구현 확인, live QA 필요 | comment/reply CRUD와 owner validation 동시 확인 필요 | empty comment, reply, reply-to-reply one-level mapping, comment like, owner/non-owner validation을 QA에 추가 | `docs/evidence/community-comment-evidence.md`, `docs/evidence/browser-live-qa-evidence.md` | `CommunityController`, `CommunityService`, `CommunityDtos`, `CommunityMapper.xml` | comment action sheet와 reply/like 검증 | 문서 보강 완료, live QA 필요 |
| #12 계획·챌린지 관리 | 구현 확인 | progress calculation, duration/period, route-place reorder 확인 필요 | save route, edit title, reorder place, remove all places validation, delete route, progress summary를 QA에 추가 | `docs/evidence/core-crud-evidence.md`, `docs/qa/final-rubric-live-qa-checklist.md` | `RouteController`, `RouteService`, `RouteMapper.xml`, `RoutePlaceMapper.xml` | route save/edit/detail/delete browser QA | 문서 보강 완료, live QA 필요 |
| #13 생성형 AI 활용 기능 | 구현 확인, Week 4 AI 품질 개선 분리 | prompt context와 fallback policy가 사용자 조건과 연결되는지 확인 필요 | missing conditions, fallback route generation, prompt context panel, result reason panel, backend-only AI call을 QA에 추가 | `docs/ai/ai-prompt-context-design.md`, `docs/ai/backend-fastapi-contract.md` | `AiRouteController`, `AiRouteService`, `AiRouteMapper.xml` | AI 조건/로딩/결과 패널 및 frontend 직접 AI 호출 금지 확인 | 문서 보강 완료, live QA 필요 |
| #14 설계·기획 문서 | 구현 확인, 직접 연결성 보강 필요 | README, PRD, 구현 명세, QA docs 간 링크 강화 필요 | document index에 최종 analyzer hardening 문서를 추가하고 final evidence map에서 직접 확인 경로를 확장 | `docs/project/document-index.md`, `docs/submission/evaluation-evidence-map.md` | 문서 산출물 중심 | 문서 링크/경로 정합성 확인 | 문서 보강 완료 |
| #15 제출물 완성도 | 구현 확인, export path 명확화 필요 | 최종 보고서/발표자료 실제 포맷과 경로 명확화 필요 | Markdown final본, PDF/PPT planned export path, outline/reference, live screenshot 상태를 분리 | `docs/submission/submission-artifact-index.md`, `docs/submission/submission-checklist.md` | 제출 산출물 중심 | PDF/PPT export pending, screenshot pending, analyzer rerun | 문서 보강 완료, export 필요 |

## 최종 판단

- 기능 구현 자체를 새로 넓히는 작업은 수행하지 않았습니다.
- 제출 전 남은 작업은 실제 브라우저 캡처, PDF/PPT export, analyzer 재실행입니다.
- 이번 pass에서 생성한 항목은 평가자가 구현 근거와 수동 검증 필요 상태를 혼동하지 않도록 돕는 제출 안정화 문서입니다.
