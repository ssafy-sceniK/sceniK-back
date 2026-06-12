# 게시판/댓글 CRUD QA 증거

생성일: 2026-06-12

Rubric #10 `Board CRUD`와 Rubric #11 `Comment CRUD`의 추가 피드백인 권한, 정렬/필터, 첨부 정책, 댓글 삭제 정책, 댓글 수/목록 동기화 기준을 정리합니다.

## Board CRUD Traceability

| 계층 | 파일 | 확인 내용 |
| --- | --- | --- |
| Controller | `src/main/java/com/scenik/controller/CommunityController.java` | 게시글 목록/상세/작성/수정/삭제/좋아요/신고 |
| Service | `src/main/java/com/scenik/service/CommunityService.java` | 작성자 권한 검증, 삭제 처리, 댓글 포함 상세 조회 |
| Mapper | `src/main/resources/mapper/CommunityMapper.xml` | 게시글 목록, 정렬, 검색, 댓글 수 계산 |
| Frontend API | `frontend/src/api/communityApi.js` | 게시글 API 호출, 상세 조회 시 댓글 목록 동기화 |
| Frontend UI | `frontend/src/views/CommunityView.vue` | 상세, 수정/삭제, 댓글 영역, 검색/정렬/필터 UI |
| Write UI | `frontend/src/views/CommunityWriteView.vue` | 게시글 작성, 게시판/말머리, 첨부 메타데이터 안내 |

## 게시글 권한/정렬/필터 기준

| 항목 | 현재 정책 |
| --- | --- |
| 작성자 수정 | 작성자만 가능, 불일치 시 403 |
| 작성자 삭제 | 작성자만 가능, 불일치 시 403 |
| 정렬 | `sort=latest`, `sort=popular` |
| 검색 | `keyword` 기반 제목/본문/태그 검색 |
| 필터 | `region` 또는 게시판 tag 기반 필터 |
| 빈 결과 | 프론트 `EmptyState`로 안내 |
| 첨부 | 실제 파일 업로드는 미지원, MVP에서는 파일 이름/형식 메타데이터만 저장 |

## 댓글 CRUD Traceability

| 기능 | Endpoint | 구현 위치 |
| --- | --- | --- |
| 댓글 목록 | `GET /api/posts/{postId}/comments` | `CommunityController.comments`, `CommunityService.listComments` |
| 댓글 작성 | `POST /api/posts/{postId}/comments` | `CommunityController.createComment`, `CommunityService.createComment` |
| 댓글 수정 | `PATCH /api/posts/{postId}/comments/{commentId}` | `CommunityController.updateComment`, 작성자 검증 |
| 댓글 삭제 | `DELETE /api/posts/{postId}/comments/{commentId}` | `CommunityController.deleteComment`, soft delete |
| 댓글 좋아요 | `POST/DELETE /api/posts/comments/{commentId}/likes` | `CommunityController.likeComment/unlikeComment` |

## 댓글 수/목록 동기화 정책

| 항목 | 정책 |
| --- | --- |
| 댓글 수 기준 | 화면에 표시 가능한 댓글과 답글만 count |
| 삭제 댓글 | 목록과 count에서 제외 |
| 삭제된 부모의 답글 | 목록과 count에서 함께 제외 |
| 답글 depth | 1단계 답글만 지원 |
| 답글의 답글 | 프론트에서 최상위 부모 기준 1단계 답글로 매핑 |
| 상세 화면 동기화 | `communityApi.getPost`가 게시글 상세와 댓글 목록을 함께 가져온 뒤 `commentCount = comments.length`로 정규화 |

`CommunityMapper.xml`의 댓글 count와 댓글 목록은 동일한 visible 조건을 사용합니다. 따라서 count가 2이면 상세 화면에서도 표시 가능한 댓글/답글 2개가 렌더링되어야 합니다.

## Frontend Guidance

- 댓글 영역에는 “한 단계 답글까지 지원”과 “삭제된 댓글/답글은 목록과 댓글 수에서 제외” 안내를 표시합니다.
- 로그인 사용자는 상세 하단에서 댓글 입력창을 볼 수 있습니다.
- 비로그인 사용자는 로그인 필요 empty state를 봅니다.
- 댓글 버튼은 댓글 섹션으로 스크롤하거나 포커스가 이동하도록 유지합니다.
- 게시글 첨부 UI는 실제 업로드가 아니라 메타데이터 저장임을 명시합니다.

## QA 체크리스트

1. 작성자 계정으로 게시글을 생성하고 목록에서 댓글 수가 0인지 확인합니다.
2. 상세 화면으로 들어가 댓글 영역과 입력창이 보이는지 확인합니다.
3. 댓글 1개를 작성하고 상세 댓글 수와 표시 댓글 수가 1인지 확인합니다.
4. 답글 1개를 작성하고 댓글 수가 2이며 중첩 구조가 보이는지 확인합니다.
5. 목록으로 돌아가 같은 게시글의 댓글 수가 2인지 확인합니다.
6. 댓글을 삭제하고 상세 댓글 수와 목록 댓글 수가 함께 감소하는지 확인합니다.
7. 다른 사용자 계정으로 작성자 전용 게시글/댓글 수정·삭제가 차단되는지 확인합니다.
8. `sort=latest`, `sort=popular`, `keyword`, 게시판/지역 필터 조합에서 목록이 정상 갱신되는지 확인합니다.
9. 결과가 없는 필터 조합에서 빈 상태가 표시되는지 확인합니다.
10. 첨부 선택 시 실제 업로드 미지원 안내가 보이는지 확인합니다.

## 남은 한계

- 실제 바이너리 첨부 업로드는 현재 MVP 범위가 아니며 구현하지 않았습니다.
- 이번 패스에서는 브라우저 스크린샷을 새로 캡처하지 않았습니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 실제 검증: 게시글 생성/목록/상세/수정/삭제, 비작성자 수정 차단, 댓글 생성/답글/목록/수정/삭제, 댓글 수 동기화
- 확인 결과: 임시 게시글의 댓글+답글 생성 후 댓글 목록은 `X-Total-Count: 2`를 반환했고, 비작성자 댓글 수정은 `403 FORBIDDEN`으로 차단됐습니다.
- 브라우저 DOM 검증: 댓글 섹션, 입력창, 인라인 `답글/수정/삭제`, 댓글 버튼 포커스를 headless Chrome에서 확인했습니다.
