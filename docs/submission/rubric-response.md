# 루브릭 응답 정리

생성일: 2026-06-08

이 문서는 73/100 피드백 이후 보강한 구현·문서·QA 근거를 #01~#15 루브릭별로 정리한 제출용 답변입니다. 상태는 실제 구현과 검증 근거를 기준으로 보수적으로 표기합니다.

## 전체 요약

| 구분 | 완료율 | 설명 |
| --- | ---: | --- |
| Week 2 | 95% | 콘텐츠/장소 탐색, 상세, 검색, 좋아요, 방문 상태 등 핵심 사용자 흐름이 backend와 frontend에 연결되어 있습니다. |
| Week 3 | 90% | 인증, 마이페이지, AI route generation, route save, community, notifications, review 기능이 구현되었고 build/smoke/pytest가 통과했습니다. |
| Week 4 준비도 | 55% | FastAPI skeleton, backend-FastAPI 계약 문서, RAG/OpenAI 계획은 준비되었지만 ChromaDB/embedding/OpenAI production 연동은 아직 전입니다. |

## 루브릭별 응답

| 항목 | 상태 | 구현/문서 근거 | 남은 한계 | 이번 보강 |
| --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 부분 완료 | `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `AdminController`, `schema.sql` | admin UI는 없습니다. 관리자 기능은 backend-only utility에 가깝습니다. | 핵심 사용자 CRUD와 admin backend-only 범위를 `evaluation-evidence-map.md`에 분리했습니다. |
| #02 목록 조회·검색·정렬 | 부분 완료 | `ListQuerySupport`, `GET /api/contents`, `GET /api/places`, `GET /api/posts`, `GET /api/users/me/routes`, `list-search-sort-pagination-evidence.md` | 모든 모바일 화면에 명시적 페이지 번호 UI가 드러나지는 않습니다. | endpoint별 `page`, `size`, `sort`, `keyword`, `region` 지원 여부와 HTTP 테스트를 추가했습니다. |
| #03 상세 정보 조회 | 부분 완료 | `ContentDetailView.vue`, `PlaceDetailView.vue`, `CommunityView.vue`, `CourseDetailView.vue`, `GlobalExceptionHandler` | live server 기반 404/재시도 시나리오는 수동 QA가 필요합니다. | 최종 live QA 체크리스트에 상세 not-found/error UX 항목을 추가했습니다. |
| #04 리뷰 작성·조회 | 구현 완료, live QA 필요 | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `schema.sql`의 `place_reviews`, `reviewApi.js`, `PlaceDetailView.vue` | 실제 실행 환경에서 create/edit/delete live QA가 남아 있습니다. | 리뷰 목록/작성/수정/삭제, 중복 방지, owner check, 평균 평점/리뷰 수, 검증 메시지 증거 문서를 추가했습니다. |
| #05 로그인·로그아웃 | 완료 | `AuthController`, `AuthService`, `authApi.js`, auth store, login/logout flow | 운영 환경 token 만료 시나리오 live QA는 별도 확인이 필요합니다. | 인증 흐름을 evaluation evidence map과 QA 체크리스트에 연결했습니다. |
| #06 회원가입 | 완료 | `AuthService.signup`, `UserMapper`, `GlobalExceptionHandler`, signup view/store | 이메일 인증은 Week 3 MVP 범위 밖입니다. | 중복 이메일/닉네임, 비밀번호 길이 한국어 오류 매핑과 `uq_users_nickname` 근거를 명시했습니다. |
| #07 프로필 관리 | 부분 완료 | `GET /api/users/me`, `PATCH /api/users/me`, `MyProfileEditView.vue`, `UserDto.preferences` | 프로필 이미지 업로드는 아직 구현하지 않았습니다. | 프로필 이미지 기능을 Deferred로 문서화하고 화면 안내 문구를 명확히 했습니다. |
| #08 찜·즐겨찾기 | 완료에 가까움 | `LikeVisitController`, `LikeVisitService`, `engagementApi.js`, `uiStore`, 마이페이지 좋아요/방문 상태 목록 | 동시 클릭 같은 live UX edge case는 수동 QA가 필요합니다. | 좋아요/방문 상태 목록 복원과 QA 항목을 문서화했습니다. |
| #09 팔로우·팔로잉 | 보류 | 없음, `deferred-scope-decisions.md` | follow/following social graph는 현재 구현하지 않았습니다. | Week 3 핵심 범위 밖으로 결정하고, K-content 탐색/AI 코스/커뮤니티에 집중하는 Deferred 사유를 명시했습니다. |
| #10 게시판 CRUD | 완료에 가까움 | `CommunityController`, `CommunityService`, `CommunityMapper`, `communityApi.js`, `CommunityView.vue`, `CommunityWriteView.vue` | live server에서 pagination/sort/detail error QA는 수동 확인이 필요합니다. | 게시글 CRUD, 검색/정렬/페이징 증거 문서와 manual QA steps를 추가했습니다. |
| #11 댓글 CRUD | 부분 완료 | `CommunityController`, `CommunityService`, `communityApi.updateComment/deleteComment`, `uiStore.editComment/removeComment`, `CommunityView.vue` | 대댓글 UI는 구현하지 않았고, 댓글 수정/삭제 live QA가 필요합니다. | 댓글 작성자 전용 수정/삭제 UI와 owner permission 근거를 문서화했습니다. |
| #12 계획·챌린지 관리 | 완료에 가까움 | `RouteController`, `RouteService`, `RoutePlaceMapper`, `routeApi.js`, `AiResultView.vue`, `CourseDetailView.vue`, `CourseEditView.vue` | route place add/update/delete/reorder live QA가 남아 있습니다. | route save/edit 오류 복구 항목을 최종 QA 체크리스트에 포함했습니다. |
| #13 생성형 AI 활용 기능 | 완료에 가까움 | `AiRouteController`, `AiRouteService`, `aiApi.js`, `AiLoadingView.vue`, `AiResultView.vue` | 실제 RAG/OpenAI/ChromaDB 기반 품질 개선은 Week 4 범위입니다. | deterministic DB-backed behavior, prompt context, JSON output, guardrail, fallback 문서를 작성했습니다. |
| #14 설계·기획 문서 | 부분 완료 | `PRD.md`, API docs, submission docs, `evaluation-evidence-map.md`, `presentation-outline.md` | 외부 설계 산출물의 최종 위치는 제출 전 한 번 더 묶어야 합니다. | 기능별 증거 맵, 발표 개요, 제출 체크리스트를 보강했습니다. |
| #15 제출물 완성도 | 부분 완료 | `final-report-outline.md`, `presentation-outline.md`, `dataset-summary.md`, `external-api-ai-usage.md`, `demo-scenario.md`, `submission-checklist.md` | 실제 발표 PPT와 최종 보고서 파일 자체는 별도 작성이 필요합니다. | 제출 체크리스트와 평가 증거 맵을 추가하고 API 상태 수치를 최신화했습니다. |

## Notion API 상태 요약

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 20 |
| Deferred | 15 |
| Deprecated | 14 |
| Not progress | 0 |

상세 근거:
- `backend/docs/api/scenik-api-inventory.md`
- `backend/docs/api/scenik-api-status.md`
- `backend/docs/api/notion-row-mapping.md`
- `backend/docs/api/notion-api-status-update.csv`
- `backend/docs/api/notion-api-update-guide.md`

## QA 결과

| 항목 | 결과 | 비고 |
| --- | --- | --- |
| Backend build | PASS | `.\gradlew.bat clean build` 통과 |
| Frontend build | PASS | `npm run build` 통과, Vite bundle size warning만 발생 |
| Frontend lint | PASS | `npm run lint` 통과 |
| Course smoke | PASS | `npm run smoke:course` 통과 |
| Error smoke | PASS | `npm run smoke:errors` 통과 |
| AI pytest | PASS | `.\.venv\Scripts\python.exe -m pytest` 결과 4개 통과, Starlette deprecation warning 1개 |
| Deprecated API source scan | PASS | 구버전 API 호출 없음. 화면 라우터 경로 false positive만 확인했습니다. |
| Frontend direct AI call scan | PASS | frontend에서 `sceniK-ai` 또는 `localhost:8000` 직접 호출 없음 |
| Secret pattern scan | PASS | 실제 API key/token 패턴 없음. 테스트용 `password123`만 확인했습니다. |

## 제출 코멘트

Week 3 기준 핵심 사용자 플로우는 대부분 구현되어 있으며, 평가에서 누락되기 쉬운 리뷰 기능과 Notion API row mapping을 별도 증거 문서로 보강했습니다. 남은 항목은 live QA, 외부 map provider 검증, Week 4 AI 품질 개선 범위로 분리했습니다.
