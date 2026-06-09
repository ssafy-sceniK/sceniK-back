# 브라우저 Live QA 증거 계획

생성일: 2026-06-09

이 문서는 최신 analyzer 피드백 이후 리뷰, 상세 상태, 알림, 커뮤니티 흐름의 browser-based verification 증거를 한곳에서 추적하기 위한 문서입니다. 이번 pass에서는 backend/frontend 서버를 장시간 유지하지 않았고, seeded live DB와 demo 계정이 준비되지 않아 실제 브라우저 캡처는 수행하지 않았습니다. 따라서 아래 상태는 구현/정적 QA 근거와 수동 캡처 필요 항목을 명확히 구분합니다.

## 증거 상태 요약

| 영역 | 구현 상태 | 자동 QA 근거 | 브라우저 캡처 상태 | 비고 |
| --- | --- | --- | --- | --- |
| 리뷰 | 구현됨 | frontend build/smoke, backend build | 수동 캡처 필요 | place review create/edit/delete 확인 필요 |
| 상세 loading/not-found/error | 구현됨 | `smoke:errors`, detail error docs | 수동 캡처 필요 | invalid ID route 캡처 필요 |
| 알림 | 구현됨 | API/status docs, frontend build | 수동 캡처 필요 | live notification data 생성 필요 |
| 커뮤니티/댓글 | 구현됨 | frontend build/smoke, backend build | 수동 캡처 필요 | post/comment CRUD와 reply 확인 필요 |

## 실행 준비 명령

QA 완료 후 서버를 종료합니다.

```powershell
cd C:\Users\jaemy\Downloads\sceniK\backend
.\gradlew.bat bootRun
```

```powershell
cd C:\Users\jaemy\Downloads\sceniK\frontend
npm run dev
```

## 캡처 저장 위치

```text
backend/docs/evidence/screenshots/browser-live-qa/
```

현재 상태: 수동 캡처 필요.

## 리뷰 Browser QA

| 단계 | Route/API | 확인 내용 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| 1 | `/places/{placeId}` | 장소 상세 리뷰 섹션 진입 | 리뷰 요약, 평균 평점, 리뷰 수가 보입니다. | 수동 캡처 필요 |
| 2 | `GET /api/places/{placeId}/reviews` | 리뷰 목록 | 기존 리뷰 또는 빈 상태가 한국어로 표시됩니다. | 수동 캡처 필요 |
| 3 | `POST /api/places/{placeId}/reviews` | 리뷰 작성 | 작성 중/성공 상태 영역이 보이고 목록이 갱신됩니다. | 수동 캡처 필요 |
| 4 | 동일 장소 재작성 | 중복 리뷰 guidance | 작성 폼 대신 “이미 이 장소에 리뷰를 작성했습니다.” 안내가 보입니다. | 수동 캡처 필요 |
| 5 | `PATCH /api/places/{placeId}/reviews/{reviewId}` | 리뷰 수정 | 작성자에게만 수정 UI가 보이고 저장 후 목록이 갱신됩니다. | 수동 캡처 필요 |
| 6 | `DELETE /api/places/{placeId}/reviews/{reviewId}` | 리뷰 삭제 | 삭제 후 작성 폼이 다시 표시됩니다. | 수동 캡처 필요 |
| 7 | `/my/activity/reviews` | 내 리뷰 목록 | `GET /api/users/me/reviews` 결과가 표시됩니다. | 수동 캡처 필요 |

## 상세 상태 Browser QA

| 단계 | Route | 확인 내용 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| 1 | `/contents/{validContentId}` | 콘텐츠 상세 loading 후 정상 표시 | hero, metadata, 관련 장소가 표시됩니다. | 수동 캡처 필요 |
| 2 | `/contents/INVALID_CONTENT_ID` | 콘텐츠 상세 not-found/error | blank screen 대신 한국어 오류/empty state가 표시됩니다. | 수동 캡처 필요 |
| 3 | `/places/{validPlaceId}` | 장소 상세 loading 후 정상 표시 | 장소 정보, 지도 preview, 관련 콘텐츠, 리뷰가 표시됩니다. | 수동 캡처 필요 |
| 4 | `/places/INVALID_PLACE_ID` | 장소 상세 not-found/error | blank screen 대신 한국어 오류/empty state가 표시됩니다. | 수동 캡처 필요 |
| 5 | `/ai/courses/{validRouteId}` | 코스 상세 loading 후 정상 표시 | 저장 코스 상세와 장소 timeline이 표시됩니다. | 수동 캡처 필요 |
| 6 | `/ai/courses/INVALID_ROUTE_ID` | 코스 상세 not-found/error | 사용자가 이해할 수 있는 한국어 안내가 표시됩니다. | 수동 캡처 필요 |

## 알림 Browser QA

| 단계 | Route/API | 확인 내용 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| 1 | header 또는 마이페이지 진입점 | 알림 진입점 | 알림 아이콘 또는 알림 메뉴가 보입니다. | 수동 캡처 필요 |
| 2 | `/notifications` | 알림 목록 | `GET /api/notifications` 결과가 표시됩니다. | 수동 캡처 필요 |
| 3 | `/notifications` | 알림 empty state | 알림이 없으면 한국어 empty state가 표시됩니다. | 수동 캡처 필요 |
| 4 | `PATCH /api/notifications/read` | 읽음 처리 | 단일 또는 전체 읽음 후 unread count가 갱신됩니다. | 수동 캡처 필요 |

알림 live data는 backend seed 또는 실제 action으로 생성해야 하므로, 데이터가 없는 환경에서는 empty state 캡처만 가능합니다.

## 커뮤니티 Browser QA

| 단계 | Route/API | 확인 내용 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| 1 | `/community/write` + `POST /api/posts` | 게시글 작성 | 작성 중/성공/실패 상태가 표시됩니다. | 수동 캡처 필요 |
| 2 | `/community/{postId}` + `PATCH /api/posts/{postId}` | 게시글 수정 | 작성자에게만 수정 UI가 보이고 저장 후 `수정되었습니다.`가 표시됩니다. | 수동 캡처 필요 |
| 3 | `/community/{postId}` + `DELETE /api/posts/{postId}` | 게시글 삭제 | 삭제 확인 sheet 후 목록으로 이동합니다. | 수동 캡처 필요 |
| 4 | `POST /api/posts/{postId}/comments` | 댓글 작성 | 댓글 상태 영역과 목록 갱신이 확인됩니다. | 수동 캡처 필요 |
| 5 | `PATCH /api/posts/{postId}/comments/{commentId}` | 댓글 수정 | action sheet에서 수정 진입 후 저장됩니다. | 수동 캡처 필요 |
| 6 | `DELETE /api/posts/{postId}/comments/{commentId}` | 댓글 삭제 | action sheet에서 삭제 후 목록에서 제거됩니다. | 수동 캡처 필요 |
| 7 | `parentCommentId` | 답글 | 최상위 댓글과 답글에서 답글 입력이 가능하되 backend 저장 구조는 1단계로 유지됩니다. | 수동 캡처 필요 |
| 8 | `POST/DELETE /api/posts/comments/{commentId}/likes` | 댓글 좋아요 | count/state가 backend와 동기화됩니다. | 수동 캡처 필요 |
| 9 | 작성자 영역 | author follow button | 다른 작성자에게 `팔로우`/`팔로잉` 상태가 표시됩니다. | 수동 캡처 필요 |
| 10 | `/community` | compact sort | native `<select>`가 아니라 segmented sort UI가 유지됩니다. | 수동 캡처 필요 |

## 권장 캡처 파일명

| 파일명 | 장면 |
| --- | --- |
| `review-place-detail-summary.png` | 장소 상세 리뷰 요약/평점 분포 |
| `review-create-edit-delete.png` | 리뷰 작성/수정/삭제 상태 |
| `detail-invalid-content.png` | 콘텐츠 invalid ID error |
| `detail-invalid-place.png` | 장소 invalid ID error |
| `notifications-list-or-empty.png` | 알림 목록 또는 empty state |
| `community-post-comment-crud.png` | 게시글/댓글 CRUD |
| `community-reply-comment-like.png` | 답글/댓글 좋아요 |
| `community-compact-sort.png` | 커뮤니티 정렬 UI |

## 최종 판단

현재 문서와 코드 기준으로 기능 구현 및 build/smoke 근거는 충분하지만, analyzer/evaluator가 직접 확인하기 쉬운 브라우저 캡처는 아직 비어 있습니다. 제출 전에는 위 캡처를 실제 실행 환경에서 생성하고 이 문서에 이미지 링크를 추가해야 합니다.
