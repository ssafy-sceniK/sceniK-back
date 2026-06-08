# In Progress / Deferred 우선순위 감사

생성일: 2026-06-08

기준 문서:
- [notion-row-mapping.md](../api/notion-row-mapping.md)
- [notion-status-change-list.md](../api/notion-status-change-list.md)
- [scenik-api-status.md](../api/scenik-api-status.md)
- [scenik-api-inventory.md](../api/scenik-api-inventory.md)

이 문서는 Week 4 AI/RAG 작업으로 넘어가기 전에 남은 `In progress`와 `Deferred` 항목 중 지금 마무리할 가치가 있는 작업만 선별하기 위한 감사 기록입니다. 새로운 Notion API 행은 추가하지 않고, 기존 Notion 행명 기준으로만 판단합니다.

## In Progress 행 감사

| Notion Row | Current Status | Recommended Action | Reason | Risk | Files likely affected |
| --- | --- | --- | --- | --- | --- |
| 내가 작성한 리뷰 목록 조회 | In progress | Keep In progress | 백엔드와 API adapter는 있으나 별도 마이리뷰 화면은 Week 3 데모 핵심 흐름이 아닙니다. | 낮음 | `frontend/src/api/reviewApi.js`, 향후 my reviews page |
| 장소 리뷰 목록 조회 | In progress | Should finish if quick | 장소 상세 데모 품질에 직접 영향을 주며 현재 목록 UI는 있습니다. live QA가 통과하면 Done 후보입니다. | 낮음 | `frontend/src/views/PlaceDetailView.vue`, `backend/src/main/java/com/scenik/controller/PlaceReviewController.java` |
| 장소 리뷰 작성 | In progress | Should finish if quick | 장소 상세에서 사용자 참여 흐름을 보여줄 수 있습니다. live QA가 통과하면 Done 후보입니다. | 낮음 | `frontend/src/views/PlaceDetailView.vue`, `frontend/src/api/reviewApi.js` |
| 장소 리뷰 수정 | In progress | Must finish now | 이미 backend/API adapter가 있어 소유자 수정 UI만 연결하면 rubric 보완 효과가 큽니다. | 낮음 | `frontend/src/views/PlaceDetailView.vue`, `frontend/src/styles/detail-map-panels.css` |
| 장소 리뷰 삭제 | In progress | Must finish now | 이미 backend/API adapter가 있어 소유자 삭제 UI만 연결하면 CRUD 완성도가 올라갑니다. | 낮음 | `frontend/src/views/PlaceDetailView.vue`, `frontend/src/styles/detail-map-panels.css` |
| 지도 주소 검색 | In progress | Keep In progress | backend fallback은 있으나 실제 Kakao provider 호출이 아직 구현되지 않았고 key를 하드코딩할 수 없습니다. | 중간 | `backend/src/main/java/com/scenik/service/MapService.java`, manual QA |
| 지도 역지오코딩 | In progress | Keep In progress | fallback은 안전하지만 외부 provider live QA가 없어 Done으로 올릴 수 없습니다. | 중간 | `backend/src/main/java/com/scenik/service/MapService.java`, manual QA |
| 지도 로컬 검색 | In progress | Keep In progress | fallback은 안전하지만 외부 provider live QA가 없어 Done으로 올릴 수 없습니다. | 중간 | `backend/src/main/java/com/scenik/service/MapService.java`, manual QA |
| 코스 장소 추가 | In progress | Keep In progress | 저장 코스 편집 화면은 전체 route patch 중심이며 개별 route-place endpoint live QA는 아직 없습니다. | 중간 | `frontend/src/views/CourseEditView.vue`, `frontend/src/api/routeApi.js`, `backend/src/main/java/com/scenik/controller/RouteController.java` |
| 코스 장소 수정 | In progress | Keep In progress | 개별 장소 수정 endpoint는 있으나 화면은 전체 route 저장 흐름을 사용합니다. 수동 API QA가 필요합니다. | 중간 | `frontend/src/api/routeApi.js`, `backend/src/main/java/com/scenik/controller/RouteController.java` |
| 코스 장소 삭제 | In progress | Keep In progress | 화면에서 제거 후 전체 route 저장은 가능하지만 개별 DELETE endpoint live QA가 필요합니다. | 중간 | `frontend/src/views/CourseEditView.vue`, `frontend/src/api/routeApi.js` |
| 코스 장소 순서 변경 | In progress | Keep In progress | 드래그 UI는 있으나 개별 reorder endpoint를 직접 호출하지 않고 전체 route patch로 저장합니다. | 중간 | `frontend/src/views/CourseEditView.vue`, `frontend/src/api/routeApi.js` |
| 게시글 댓글 수정 | In progress | Must finish now | backend와 API adapter가 있어 댓글 작성자 수정 UI만 연결하면 데모 품질이 올라갑니다. | 낮음 | `frontend/src/stores/uiStore.js`, `frontend/src/views/CommunityView.vue`, `frontend/src/styles/community.css` |
| 게시글 댓글 삭제 | In progress | Must finish now | backend와 API adapter가 있어 댓글 작성자 삭제 UI만 연결하면 커뮤니티 CRUD가 완성됩니다. | 낮음 | `frontend/src/stores/uiStore.js`, `frontend/src/views/CommunityView.vue`, `frontend/src/styles/community.css` |
| 관리자 콘텐츠 생성 | In progress | Keep In progress | backend-only utility이며 Week 3 일반 사용자 데모 흐름에 포함하지 않습니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |
| 관리자 콘텐츠 수정 | In progress | Keep In progress | admin UI가 없으므로 Done으로 올리지 않습니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |
| 관리자 콘텐츠 삭제 | In progress | Keep In progress | admin UI가 없으므로 Done으로 올리지 않습니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |
| 관리자 장소 생성 | In progress | Keep In progress | backend-only utility이며 admin dashboard는 Week 3 범위가 아닙니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |
| 관리자 장소 수정 | In progress | Keep In progress | admin UI가 없으므로 Done으로 올리지 않습니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |
| 관리자 장소 삭제 | In progress | Keep In progress | admin UI가 없으므로 Done으로 올리지 않습니다. | 낮음 | `backend/src/main/java/com/scenik/controller/AdminController.java`, HTTP tests |

## Deferred 행 감사

| Notion Row | Current Status | Recommended Action | Reason | Risk | Files likely affected |
| --- | --- | --- | --- | --- | --- |
| 카카오 소셜 로그인 | Deferred | Keep Deferred | OAuth provider 설정, callback, 보안 검증이 필요하며 Week 3 MVP 범위가 아닙니다. | 중간 | auth backend/frontend |
| 구글 소셜 로그인 | Deferred | Keep Deferred | OAuth provider 설정, callback, 보안 검증이 필요하며 Week 3 MVP 범위가 아닙니다. | 중간 | auth backend/frontend |
| 회원 탈퇴 | Deferred | Keep Deferred | 데이터 보존 정책과 soft delete 정책을 먼저 정해야 합니다. | 중간 | user backend, auth store |
| 내 프로필 이미지 업로드 | Deferred | Keep Deferred | 파일 저장소, 업로드 제한, 이미지 처리 정책이 필요합니다. | 중간 | profile backend/frontend |
| 팔로우 | Deferred | Keep Deferred | 소셜 관계 기능은 Week 3 핵심 데모가 아닙니다. | 중간 | user/social module |
| 언팔로우 | Deferred | Keep Deferred | 소셜 관계 기능은 Week 3 핵심 데모가 아닙니다. | 중간 | user/social module |
| 팔로워 목록 조회 | Deferred | Keep Deferred | follow graph가 없으므로 별도 설계가 필요합니다. | 중간 | user/social module |
| 팔로잉 목록 조회 | Deferred | Keep Deferred | follow graph가 없으므로 별도 설계가 필요합니다. | 중간 | user/social module |
| AI 여행 동선 공개 공유 | Deferred | Keep Deferred | 공개 범위, 공유 URL, 권한 정책이 필요합니다. | 중간 | route backend/frontend |
| 댓글 신고 | Deferred | Keep Deferred | 게시글 신고는 구현되어 있으나 댓글 신고는 Week 3 MVP 밖입니다. | 낮음 | community report backend/frontend |
| 알림 설정 조회 | Deferred | Keep Deferred | 알림 목록/읽음 처리만 Week 3 범위입니다. | 낮음 | notification settings |
| 알림 설정 수정 | Deferred | Keep Deferred | 알림 설정 UI와 저장 정책이 필요합니다. | 낮음 | notification settings |
| 전체 사용자 목록 조회 | Deferred | Keep Deferred | 일반 사용자 데모와 무관한 admin 기능이며 관리자 권한 정책이 필요합니다. | 중간 | admin user backend |
| 사용자 계정 상태 변경 | Deferred | Keep Deferred | 계정 정지/활성화 정책과 감사 로그가 필요합니다. | 중간 | admin user backend |
| 신고된 게시글 및 댓글 조회 | Deferred | Keep Deferred | 신고 moderation admin UI와 권한 정책이 필요합니다. | 중간 | admin report backend/frontend |
| 신고 처리 상태 변경 | Deferred | Keep Deferred | 처리 상태 모델과 운영 정책이 필요합니다. | 중간 | admin report backend/frontend |
| 신고된 게시글 검토 후 삭제 | Deferred | Keep Deferred | moderation workflow가 필요하며 Week 3 범위가 아닙니다. | 중간 | admin report backend/frontend |
| 신고된 댓글 검토 후 삭제 | Deferred | Keep Deferred | moderation workflow가 필요하며 Week 3 범위가 아닙니다. | 중간 | admin report backend/frontend |
| OpenAI/RAG 경로 품질 개선 | Deferred | Needs Week 4 task | Week 4 핵심 범위이며 FastAPI, ChromaDB, embedding, prompt 검증이 필요합니다. | 높음 | `sceniK-ai`, `backend/docs/ai/backend-fastapi-contract.md` |
| 전체 장소 데이터 벡터 임베딩 | Deferred | Needs Week 4 task | ChromaDB와 embedding pipeline이 필요합니다. | 높음 | `sceniK-ai`, vector store |
| 장소 데이터 벡터 임베딩 갱신 | Deferred | Needs Week 4 task | 장소 변경 이벤트와 재색인 정책이 필요합니다. | 높음 | `sceniK-ai`, backend admin/event |
| 임베딩 처리 상태 조회 | Deferred | Needs Week 4 task | embedding job 상태 저장과 조회 API 설계가 필요합니다. | 중간 | `sceniK-ai`, backend internal contract |

## 결론

지금 마무리할 항목은 댓글 수정/삭제 UI와 장소 리뷰 수정/삭제 UI입니다. 코스 장소 개별 endpoint, map provider live QA, admin CRUD는 backend/API 기반은 있으나 live QA나 admin UI가 없어 `In progress`로 유지합니다. OAuth, follow/following, profile image, notification settings, public route sharing, moderation admin, embedding/RAG 항목은 `Deferred`로 확정하고 Week 4 이후 범위로 분리합니다.
