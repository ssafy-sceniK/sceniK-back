# 루브릭 응답 정리

생성일: 2026-06-09

이 문서는 최신 프론트엔드/루브릭 피드백 이후 보강한 구현, 문서, QA 근거를 #01~#15 루브릭별로 정리한 제출용 답변입니다. 상태는 실제 구현과 검증 근거를 기준으로 보수적으로 표기합니다.

최신 analyzer 결과는 89/100, Complete 14/15, Partial 1/15, Missing 0/15입니다. 이번 최종 pass는 새 기능을 늘리기보다 AI 코스 선택 화면의 시각 polish, follow live QA 절차, review/detail/notification/community 브라우저 QA 증거 계획, 제출 산출물 index를 보강했습니다.

## 전체 요약

| 구분 | 완료율 | 설명 |
| --- | ---: | --- |
| Week 2 | 95% | 콘텐츠/장소 탐색, 상세, 검색, 좋아요, 방문 상태 등 핵심 사용자 흐름이 backend와 frontend에 연결되어 있습니다. |
| Week 3 | 95% | 인증, 이메일 중복 확인, 마이페이지, AI route generation, route save, community, notifications, review, follow 최소 흐름이 구현되었고 정적 QA를 통과했습니다. |
| Week 4 준비도 | 58% | FastAPI skeleton, backend-FastAPI 계약 문서, RAG/OpenAI 계획은 준비되었지만 ChromaDB/embedding/OpenAI production 연동은 아직 전입니다. |

## 루브릭별 응답

| 항목 | 상태 | 구현/문서 근거 | 남은 한계 | 이번 보강 |
| --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 부분 완료 | `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `PlaceReviewController`, `AdminController`, `schema.sql`, `CommunityView.vue`, `CourseEditView.vue`, `CourseDetailView.vue` | admin UI는 없습니다. 관리자 기능은 backend-only utility에 가깝고 live E2E 확인이 남아 있습니다. | 커뮤니티 게시글 수정/삭제 UI, 코스 저장/수정/삭제 상태 안내, 사용자 CRUD와 admin backend-only 범위를 `core-crud-evidence.md`에 보강했습니다. |
| #02 목록 조회·검색·정렬 | 부분 완료 | `ListQuerySupport`, `GET /api/contents`, `GET /api/places`, `GET /api/posts`, `GET /api/users/me/routes`, `list-search-sort-pagination-evidence.md` | 모든 모바일 화면에 명시적 페이지 번호 UI가 드러나지는 않습니다. | 콘텐츠/장소/커뮤니티에 sort select, 결과 수, “더 보기” UI를 추가했습니다. |
| #03 상세 정보 조회 | 부분 완료 | `ContentDetailView.vue`, `PlaceDetailView.vue`, `CommunityView.vue`, `CourseDetailView.vue`, `GlobalExceptionHandler` | live server 기반 404/재시도 시나리오는 수동 QA가 필요합니다. | 상세 not-found/error UX를 최종 live QA 체크리스트에 추가했습니다. |
| #04 리뷰 작성·조회 | 구현 완료, live QA 필요 | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `schema.sql`의 `place_reviews`, `reviewApi.js`, `PlaceDetailView.vue`, `SimpleListView.vue`, `ContentDetailView.vue` | 실제 실행 환경에서 create/edit/delete live QA가 남아 있습니다. | 리뷰 작성/수정/삭제 상태 안내, 평점 분포, 콘텐츠 상세의 관련 장소 리뷰 안내, 중복 방지, owner check 증거를 추가했습니다. |
| #05 로그인·로그아웃 | 완료 | `AuthController`, `AuthService`, `authApi.js`, auth store, login/logout flow, `MyPageView.vue`, `SettingsView.vue` | 운영 환경 token 만료 시나리오 live QA는 별도 확인이 필요합니다. | 설정 화면 세션 관리 카드와 로그아웃 결과 안내를 추가하고 `auth-profile-evidence.md`를 갱신했습니다. |
| #06 회원가입 | 완료 | `AuthService.signup`, `AuthService.checkEmail`, `UserMapper`, `GlobalExceptionHandler`, `SignupView.vue`, `authApi.checkEmailAvailability` | 이메일 인증은 Week 3 MVP 범위 밖입니다. | `GET /api/auth/check-email?email=` 이메일 중복 확인, 비밀번호 정책 안내, 중복 이메일/닉네임 오류 매핑 근거를 명시했습니다. |
| #07 프로필 관리 | 부분 완료 | `GET /api/users/me`, `PATCH /api/users/me`, `MyProfileEditView.vue`, `UserDto.preferences` | 프로필 이미지 업로드는 아직 구현하지 않았습니다. | 화면에 “프로필 이미지는 추후 지원 예정입니다.” 안내를 명확히 했습니다. |
| #08 찜·즐겨찾기 | 완료에 가까움 | `LikeVisitController`, `LikeVisitService`, `engagementApi.js`, `uiStore`, 마이페이지 좋아요/방문 상태 목록 | 동시 클릭 같은 live UX edge case는 수동 QA가 필요합니다. | 좋아요 pending guard, disabled state, `aria-pressed`를 추가했습니다. |
| #09 팔로우·팔로잉 | 구현 완료, live QA 필요 | `FollowController`, `FollowService`, `FollowMapper.xml`, `followApi.js`, `CommunityView.vue`, `MyPageView.vue`, `SimpleListView.vue` | 두 계정 기반 live E2E 검증이 아직 필요합니다. | 커뮤니티 작성자 팔로우/언팔로우, 마이페이지 팔로워/팔로잉 목록, 맞팔 배지와 목록 내 언팔로우 동작을 보강했습니다. |
| #10 게시판 CRUD | 완료에 가까움 | `CommunityController`, `CommunityService`, `CommunityMapper`, `communityApi.js`, `CommunityView.vue`, `CommunityWriteView.vue` | live server에서 pagination/sort/detail error QA는 수동 확인이 필요합니다. | 게시글 CRUD, 검색/정렬/페이징 증거 문서와 manual QA steps를 보강했습니다. |
| #11 댓글 CRUD | 부분 완료 | `CommunityController`, `CommunityService`, `communityApi.updateComment/deleteComment`, `uiStore.editComment/removeComment/addComment`, `CommunityView.vue` | 댓글 수정/삭제/답글 live QA가 필요합니다. | 댓글 작성/수정/삭제 상태 안내, 댓글 관리 action sheet, 1단계 답글과 답글에서 답글 작성 시 상위 댓글로 매핑되는 동작을 문서화했습니다. |
| #12 계획·챌린지 관리 | 완료에 가까움 | `RouteController`, `RouteService`, `RoutePlaceMapper`, `routeApi.js`, `AiResultView.vue`, `CourseDetailView.vue`, `CourseEditView.vue` | route place add/update/delete/reorder live QA가 남아 있습니다. | route save/edit 오류 복구 항목을 최종 QA 체크리스트에 포함했습니다. |
| #13 생성형 AI 활용 기능 | 완료에 가까움 | `AiRouteController`, `AiRouteService`, `aiApi.js`, `CourseConditionsView.vue`, `AiLoadingView.vue`, `AiResultView.vue` | 실제 RAG/OpenAI/ChromaDB 기반 품질 개선은 Week 4 범위입니다. | AI 참고 조건과 검증 장소 기반 추천 패널을 화면에 추가하고 관련 문서를 정리했습니다. |
| #14 설계·기획 문서 | 부분 완료 | `PRD.md`, API docs, submission docs, `evaluation-evidence-map.md`, `presentation-outline.md`, `document-index.md` | 외부 설계 산출물의 최종 위치는 제출 전 한 번 더 확인해야 합니다. | 기능별 증거 맵, 발표 개요, 제출 체크리스트, 문서 index를 보강했습니다. |
| #15 제출물 완성도 | 부분 완료 | `final-report-outline.md`, `presentation-outline.md`, `dataset-summary.md`, `external-api-ai-usage.md`, `demo-scenario.md`, `submission-checklist.md` | 실제 발표 PPT와 최종 보고서 파일 자체는 별도 작성이 필요합니다. | 제출 체크리스트와 평가 증거 맵을 추가하고 API 상태 수치를 최신화했습니다. |

## Notion API 상태 요약

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 24 |
| Deferred | 11 |
| Deprecated | 14 |
| Not progress | 0 |

상세 근거:
- `backend/docs/api/scenik-api-inventory.md`
- `backend/docs/api/scenik-api-status.md`
- `backend/docs/api/notion-row-mapping.md`
- `backend/docs/api/notion-api-status-update.csv`
- `backend/docs/api/notion-api-update-guide.md`
- `backend/docs/evidence/follow-live-qa-evidence.md`
- `backend/docs/evidence/browser-live-qa-evidence.md`
- `backend/docs/submission/submission-artifact-index.md`

## QA 결과

| 항목 | 결과 | 비고 |
| --- | --- | --- |
| Frontend lint | PASS | `npm run lint` 통과 |
| Backend build | PASS | `.\gradlew.bat clean build` 통과 |
| Frontend build | PASS | `npm run build` 통과, Vite chunk size warning만 발생 |
| Course smoke | PASS | `npm run smoke:course` 통과 |
| Error smoke | PASS | `npm run smoke:errors` 통과 |
| AI pytest | 해당 없음 | 이번 보강은 frontend/backend 범위이며 `sceniK-ai`는 변경하지 않았습니다. |
| Deprecated API source scan | PASS | source code에서 구버전 API path 사용 없음 |
| Frontend direct AI call scan | PASS | frontend에서 `sceniK-ai`, `localhost:8000`, `127.0.0.1:8000` 직접 호출 없음 |
| Secret pattern scan | PASS | 실제 API key/token/private key 패턴 없음 |

## 제출 코멘트

Week 3 기준 핵심 사용자 플로우는 대부분 구현되어 있으며, 평가에서 누락되기 쉬운 이메일 중복 확인, 설정 화면 로그아웃, 리뷰 상태 안내, 팔로우/맞팔, 댓글 관리 action sheet, 게시글 수정/삭제, AI 컨텍스트 표시를 화면과 증거 문서로 보강했습니다. 최종 pass에서는 팔로우 live QA 증거와 제출 산출물 연결성을 보강해 analyzer/evaluator가 확인하기 쉬워졌습니다. 남은 항목은 실제 브라우저 캡처, 외부 map provider 검증, Week 4 AI 품질 개선 범위로 분리했습니다.
