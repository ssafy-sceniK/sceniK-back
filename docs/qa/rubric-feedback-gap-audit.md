# 루브릭 피드백 Gap 감사

생성일: 2026-06-08

이 문서는 73/100 피드백을 기준으로 #01~#15 루브릭 항목의 현재 구현/문서 근거와 안전한 보강 방향을 정리한 baseline audit입니다. 이 문서 작성 단계에서는 코드 변경 없이 현재 코드와 문서 상태를 확인했습니다.

| 항목 | 현재 점수 | 현재 상태 | 피드백 요약 | 실제 코드/문서 근거 | 보강 필요 여부 | 추천 조치 | 위험도 |
| --- | ---: | --- | --- | --- | --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 미제공 | 부분 완료 | 핵심 콘텐츠/장소/경로/게시글 데이터 CRUD는 있으나 admin UI는 제한적입니다. | `AdminController`, `ContentController`, `PlaceController`, `RouteController`, `CommunityController`, `schema.sql` | 필요 | admin은 backend-only utility로 문서화하고 핵심 사용자 CRUD 증거를 제출 문서에 명확히 연결합니다. | 낮음 |
| #02 목록 조회·검색·정렬 | 8/10 | 부분 완료 | 검색/필터는 있으나 서버 페이징과 정렬 옵션 노출이 제한적입니다. | `ListQuerySupport`, `ContentController`, `PlaceController`, `CommunityController`, `RouteController`, `LikeVisitController`, `frontend/src/views/ContentListView.vue`, `PlaceListView.vue`, `CommunityView.vue` | 필요 | endpoint별 query parameter 증거 문서와 HTTP 테스트를 추가하고, 큰 UI 변경 없이 기존 controls가 backend params를 쓰는지 정리합니다. | 낮음 |
| #03 상세 정보 조회 | 8/10 | 부분 완료 | 404 처리와 로딩/에러 화면 일관화가 필요합니다. | `ContentDetailView.vue`, `PlaceDetailView.vue`, `CommunityView.vue`, `CourseDetailView.vue`, `EmptyState.vue`, `GlobalExceptionHandler` | 필요 | 상세 화면별 loading/not-found/error UX 증거와 수동 QA 체크리스트를 추가합니다. | 낮음 |
| #04 리뷰 작성·조회 | 0/10 | 구현됨, 증거 부족 | 리뷰 엔티티/API/UI가 미발견으로 평가되었습니다. | `PlaceReviewController`, `PlaceReviewService`, `PlaceReviewMapper.xml`, `ReviewDtos`, `schema.sql`의 `place_reviews`, `frontend/src/api/reviewApi.js`, `PlaceDetailView.vue` | 강하게 필요 | 검증 메시지, 평균 평점/리뷰 수, 증거 문서를 보강합니다. evaluator가 찾기 쉽게 `review-feature-evidence.md`를 작성합니다. | 낮음 |
| #05 로그인·로그아웃 | 미제공 | 완료 | 명시 피드백은 없으나 인증 흐름은 핵심 demo flow입니다. | `AuthController`, `AuthService`, `authApi.js`, login/signup views, auth store | 낮음 | 현재 상태 유지. 에러 메시지와 token refresh 증거만 제출 문서에 연결합니다. | 낮음 |
| #06 회원가입 | 9/10 | 완료 | 이메일 인증/닉네임 중복 UX 보강 가능성이 있습니다. | `AuthService.signup`, `UserMapper`, `GlobalExceptionHandler`, signup view/store | 선택 | 이메일 인증은 Deferred로 문서화하고, 중복 이메일/닉네임 한국어 메시지 근거를 보강합니다. | 낮음 |
| #07 프로필 관리 | 8/10 | 부분 완료 | 프로필 이미지 업로드 같은 도메인 속성이 부족합니다. | `GET/PATCH /api/users/me`, `MyProfileEditView.vue`, `AuthDtos.UpdateUserRequest`, `UserDto.preferences` | 선택 | 이미지 업로드는 Deferred로 유지하고 화면 안내 문구와 Deferred 문서를 명확히 합니다. | 낮음 |
| #08 찜·즐겨찾기 | 8/10 | 완료에 가까움 | 멱등성 있는 토글 UX와 목록 상태 복원 보강 가능성이 있습니다. | `LikeVisitController`, `LikeVisitService`, `engagementApi.js`, `uiStore.toggleContentLike`, `uiStore.togglePlaceLike`, 마이페이지 목록 | 선택 | double click 보호는 큰 변경 없이 문서화하고, 실패 시 복구 메시지와 목록 refresh 근거를 정리합니다. | 낮음 |
| #09 팔로우·팔로잉 | 0/10 | Deferred | follow/following 엔티티/API/화면이 없습니다. | `notion-row-mapping.md`, `deferred-scope-decisions.md`, `rubric-response.md` | 결정 필요 | 작은 구현이 가능해도 UX/데이터 모델 변화가 있어 이번 pass에서는 Deferred 근거를 강화하는 쪽이 안전합니다. | 중간 |
| #10 게시판 CRUD | 8/10 | 완료에 가까움 | 목록/상세 서버 pagination 보강이 필요합니다. | `CommunityController`, `CommunityService`, `CommunityMapper`, `communityApi.js`, `CommunityView.vue`, `CommunityWriteView.vue` | 필요 | post CRUD와 pagination/sort evidence 문서를 추가합니다. | 낮음 |
| #11 댓글 CRUD | 8/10 | 부분 완료 | 대댓글, 댓글 수정/삭제 UI를 더 명시적으로 드러내면 좋습니다. | `CommunityController`, `CommunityService`, `CommunityDtos.CommentRequest`, `communityApi.updateComment/deleteComment`, `CommunityView.vue` | 필요 | owner-only 수정/삭제 UI와 evidence 문서를 보강합니다. 대댓글은 API 필드만 있으며 UI는 In progress로 문서화합니다. | 낮음 |
| #12 계획·챌린지 관리 | 9/10 | 완료에 가까움 | 저장/수정 흐름의 오류 복구를 더 세밀하게 다듬으면 좋습니다. | `RouteController`, `RouteService`, `routeApi.js`, `CourseEditView.vue`, `AiResultView.vue`, `uiStore.saveCourse/updateSavedCourse` | 선택 | route save/edit 오류 메시지와 manual QA steps를 문서화합니다. | 낮음 |
| #13 생성형 AI 활용 기능 | 9/10 | 완료에 가까움 | LLM 연동 시 프롬프트/컨텍스트 설계 문서화가 필요합니다. | `AiRouteController`, `AiRouteService`, `backend-fastapi-contract.md`, `sceniK-ai/docs/week4-task-plan.md` | 필요 | `ai-prompt-context-design.md`를 작성해 deterministic behavior, prompt fields, RAG guardrail을 명확히 합니다. | 낮음 |
| #14 설계·기획 문서 | 8/10 | 부분 완료 | 기능명세/WBS/화면설계/ERD/Use-case 문서 위치 체계화가 필요합니다. | `backend/docs/project/PRD.md`, `scope-decisions.md`, submission docs, API docs | 필요 | 제출 문서 인덱스와 evaluation evidence map을 작성합니다. | 낮음 |
| #15 제출물 완성도 | 7/10 | 부분 완료 | 최종 보고서, 발표자료, 데이터셋, 외부 API·AI 활용 문서 보강이 필요합니다. | `backend/docs/submission/*`, `backend/docs/api/*`, `backend/docs/ai/*` | 강하게 필요 | `submission-checklist.md`, `evaluation-evidence-map.md`, slide-ready presentation outline 보강이 필요합니다. | 낮음 |

## 판단 요약

- 가장 큰 점수 회복 기회는 #04 리뷰 기능입니다. 실제 구현이 있으나 evaluator가 찾지 못한 것으로 보이며, 증거 문서와 aggregate 보강이 필요합니다.
- #09 follow/following은 새 social layer가 필요해 이번 pass에서 구현하면 위험 대비 효과가 낮습니다. Deferred 사유를 제출 문서와 evidence map에 명확히 남기는 전략이 안전합니다.
- #02, #03, #10, #11, #13, #15는 구현보다 증거성 문서와 작은 UI/QA 보강으로 점수 회복 가능성이 있습니다.
