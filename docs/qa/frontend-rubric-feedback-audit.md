# 프론트엔드 루브릭 피드백 감사

생성일: 2026-06-09

이 문서는 최신 프론트엔드/루브릭 피드백을 기준으로 현재 구현이 왜 `Partial` 또는 `Missing`으로 분석될 수 있는지 점검하고, 화면에서 보이는 개선 방향을 우선순위별로 정리한 baseline audit입니다. 이 감사 단계에서는 코드 변경 전 현재 구현과 문서 증거를 확인했습니다.

| Rubric | Current feedback | Current evidence | Why analyzer may still mark partial/missing | Improvement plan | Risk |
| --- | --- | --- | --- | --- | --- |
| #01 Core data CRUD | Partial | 사용자-facing route/post/comment/review CRUD, admin content/place API, `schema.sql` | 콘텐츠/장소 create/edit/delete는 일반 화면에서 보이지 않고 admin API 중심입니다. | `core-crud-evidence.md`를 추가해 사용자 CRUD와 backend-only admin CRUD를 분리 설명합니다. | 낮음 |
| #02 List/search/sort | Implemented, but pagination/sort exposure weak | `GET /api/contents`, `GET /api/places`, `GET /api/posts`, `ListQuerySupport`, 목록 화면 검색/필터 | 목록 화면에 sort select와 pagination/더 보기 UI가 약하게 보입니다. | 콘텐츠/장소/커뮤니티에 compact sort control과 더 보기 노출을 추가합니다. | 낮음 |
| #03 Detail retrieval | Implemented, but 404/loading/error consistency needed | `ContentDetailView`, `PlaceDetailView`, `CourseDetailView`, `CommunityView`, `EmptyState` | 오류 제목이 “불러오지 못했습니다” 중심이라 404/not-found 의도가 약하게 보일 수 있습니다. | 상세 화면의 not-found 문구를 명확히 하고 QA 문서에 시나리오를 추가합니다. | 낮음 |
| #04 Review create/read | Partial | `PlaceReviewController`, `reviewApi.js`, `PlaceDetailView` | 장소 상세 제목이 단순 “리뷰”이고 마이페이지 “내 리뷰” 진입점이 없습니다. | “방문 리뷰” 섹션, 평균/개수 표시, 마이페이지 내 리뷰 목록 진입점과 `SimpleListView` 표시를 추가합니다. | 낮음 |
| #05 Login/logout | Partial | `AuthController`, `authApi`, `SettingsView`, `uiStore.logout` | 로그아웃이 Settings에만 있어 MyPage 핵심 흐름에서 잘 보이지 않습니다. | MyPage 프로필 영역에 로그아웃 버튼을 추가하고 evidence 문서를 작성합니다. | 낮음 |
| #06 Signup | Implemented | signup view/store, 중복 이메일/닉네임 오류 매핑 | 이미 구현되어 있으나 이메일 인증은 없습니다. | 현 상태 유지, 이메일 인증은 Deferred로 설명합니다. | 낮음 |
| #07 Profile management | Partial | `GET/PATCH /api/users/me`, `MyProfileEditView` | 프로필 이미지 업로드가 실제 기능처럼 보이지 않습니다. | disabled/coming-soon 안내와 evidence 문서를 강화합니다. | 낮음 |
| #08 Favorites/bookmarks | Implemented, but toggle/idempotent UI can improve | content/place/post likes, mypage liked lists | 더블 클릭 방지와 `aria-pressed`가 일부 버튼에 약합니다. | like pending guard, disabled state, `aria-pressed`를 추가합니다. | 낮음 |
| #09 Follow/following | Missing | 없음 | backend/frontend vertical slice가 없습니다. | 안전한 최소 구현이 가능하면 follows table, endpoint, author follow button, MyPage 팔로워/팔로잉 목록을 추가합니다. | 중간 |
| #10 Board CRUD | Implemented | community post list/detail/write/update/delete API와 UI | CRUD는 있으나 목록 sort/pagination 노출이 약할 수 있습니다. | 커뮤니티 sort와 더 보기 UI를 추가하고 evidence를 갱신합니다. | 낮음 |
| #11 Comment CRUD | Partial | 댓글 작성/수정/삭제 backend/UI | `parentCommentId`는 있으나 답글 UI가 보이지 않습니다. | 1단계 답글 작성/표시 UI를 추가하고 nested reply 제한을 문서화합니다. | 중간 |
| #12 Route management | Implemented | AI 생성, 저장, 상세, 편집, 삭제 | 오류 복구 메시지가 분석기에 명확히 드러나지 않을 수 있습니다. | QA 문서와 evidence map에 route save/edit error 시나리오를 보강합니다. | 낮음 |
| #13 Generative AI | Partial | `POST /api/ai/routes`, `AiLoadingView`, `AiResultView`, AI docs | 화면에서 AI 입력 컨텍스트와 guardrail 설명이 약하게 보일 수 있습니다. | 조건/로딩/결과 화면에 “AI가 참고하는 조건”, “검증된 장소 데이터 기반 추천” 패널을 추가합니다. | 낮음 |
| #14 Planning/design docs | Better organization needed | submission docs, API docs, QA docs | 문서 위치가 많아 evaluator가 빠르게 찾기 어렵습니다. | `document-index.md`를 추가해 PRD/WBS/API/ERD/QA/submission 문서를 연결합니다. | 낮음 |
| #15 Submission completeness | Needs evidence | final report/presentation/dataset/API/AI docs | 제출물과 구현 근거가 화면/루브릭별로 완전히 연결되어 보이지 않을 수 있습니다. | `evaluation-evidence-map.md`, `rubric-response.md`, QA 체크리스트를 최신 변경 기준으로 갱신합니다. | 낮음 |

## 우선순위별 조치

### 1. Must fix now

- 장소 상세의 리뷰 섹션을 “방문 리뷰”로 더 명확하게 보이게 합니다.
- 마이페이지와 `SimpleListView`에 “내 리뷰” 진입점과 목록을 추가합니다.
- 콘텐츠/장소/커뮤니티 목록에 sort와 더 보기 UI를 노출합니다.
- 좋아요 버튼 pending guard와 `aria-pressed`를 추가합니다.
- AI 조건/결과 화면에 입력 컨텍스트와 검증 장소 guardrail을 표시합니다.
- 최종 QA 체크리스트와 evidence map을 최신화합니다.

### 2. Should fix if safe

- follow/following 최소 vertical slice를 backend와 frontend에 함께 추가합니다.
- 댓글 1단계 답글 UI를 추가합니다.
- MyPage 프로필 영역에 명확한 로그아웃 버튼을 추가합니다.

### 3. Document only

- admin content/place CRUD는 public admin UI를 만들지 않고 backend-only로 문서화합니다.
- map provider live QA는 실제 credential 검증 전까지 `In progress`로 유지합니다.
- real RAG/OpenAI/ChromaDB는 Week 4 범위로 유지합니다.

### 4. Keep deferred

- social login
- notification settings
- route public sharing
- full admin dashboard
- complex social feed
