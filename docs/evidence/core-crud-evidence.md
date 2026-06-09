# 핵심 데이터 CRUD 증거

생성일: 2026-06-09

루브릭 #01 핵심 데이터 관리 CRUD 항목은 사용자 화면 CRUD와 backend-only 관리 CRUD를 분리해 판단해야 합니다.

## 사용자 화면에서 보이는 CRUD

| 도메인 | Create | Read | Update | Delete | 근거 |
| --- | --- | --- | --- | --- | --- |
| 여행 경로 | `POST /api/routes` | `GET /api/users/me/routes`, `GET /api/routes/{routeId}` | `PATCH /api/routes/{routeId}` | `DELETE /api/routes/{routeId}` | `RouteController`, `AiResultView`, `CourseEditView` |
| 코스 장소 | `POST /api/routes/{routeId}/places` | route detail | `PATCH /api/routes/{routeId}/places/{routePlaceId}`, reorder | `DELETE /api/routes/{routeId}/places/{routePlaceId}` | `RouteController`, `routeApi.js` |
| 커뮤니티 게시글 | `POST /api/posts` | `GET /api/posts`, `GET /api/posts/{postId}` | `PATCH /api/posts/{postId}` | `DELETE /api/posts/{postId}` | `CommunityController`, `CommunityWriteView`, `CommunityView`, `uiStore.editPost/removePost` |
| 댓글 | `POST /api/posts/{postId}/comments` | `GET /api/posts/{postId}/comments` | `PATCH /api/posts/{postId}/comments/{commentId}` | `DELETE /api/posts/{postId}/comments/{commentId}` | `CommunityController`, `CommunityView` |
| 장소 리뷰 | `POST /api/places/{placeId}/reviews` | `GET /api/places/{placeId}/reviews` | `PATCH /api/places/{placeId}/reviews/{reviewId}` | `DELETE /api/places/{placeId}/reviews/{reviewId}` | `PlaceReviewController`, `PlaceDetailView`, `FormStatusNotice` |
| 사용자 프로필 | signup | `GET /api/users/me` | `PATCH /api/users/me` | Deferred | `AuthController`, profile screens |

## Backend-only 관리 CRUD

| 도메인 | Endpoint | 상태 | 비고 |
| --- | --- | --- | --- |
| 콘텐츠 생성 | `POST /api/admin/contents` | In progress | admin UI 없음 |
| 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | In progress | admin UI 없음 |
| 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | In progress | admin UI 없음 |
| 장소 생성 | `POST /api/admin/places` | In progress | admin UI 없음 |
| 장소 수정 | `PATCH /api/admin/places/{placeId}` | In progress | admin UI 없음 |
| 장소 삭제 | `DELETE /api/admin/places/{placeId}` | In progress | admin UI 없음 |

## 판단

일반 사용자 데모 흐름에서 필요한 CRUD는 경로, 게시글, 댓글, 리뷰, 프로필 중심으로 구현되어 있습니다. 콘텐츠/장소 원본 데이터 관리 화면은 관리자 권한 정책과 운영 UI가 필요하므로 Week 3에서는 backend-only `In progress`로 유지합니다.

## 2026-06-09 보강 사항

- `frontend/src/components/common/FormStatusNotice.vue`를 추가해 create/update/delete 결과가 toast에만 의존하지 않고 화면 안에 남도록 했습니다.
- 커뮤니티 글 작성 화면은 작성 중, 작성 성공, 작성 실패, 입력 검증 실패 상태를 명시합니다.
- 커뮤니티 게시글 상세는 작성자에게만 게시글 수정/삭제 UI를 보여주고 `PATCH /api/posts/{postId}`, `DELETE /api/posts/{postId}` 결과를 상태 영역으로 표시합니다.
- 코스 편집 화면은 장소 추가/삭제, 코스 수정 저장 성공/실패 상태를 표시합니다.
- 코스 상세 화면은 저장, 공유, 삭제 결과를 상태 영역으로 표시합니다.
- 장소 리뷰 작성/수정/삭제는 각 결과를 `FormStatusNotice`로 표시합니다.

## 2026-06-09 최종 targeted pass 보강 사항

- `frontend/src/components/course/CourseCrudPanel.vue`를 추가해 코스 상세에서 `코스 관리` 섹션, routeId, 장소 수, 저장 상태, 수정/저장/공유/삭제 action을 한곳에 표시했습니다.
- `frontend/src/components/course/CourseSaveStatusPanel.vue`를 추가해 AI 결과 화면에서 `생성된 AI 코스`, `저장 전 확인`, 저장 성공/실패 상태, 저장 후 내 코스에서 수정 가능 안내를 표시했습니다.
- `frontend/src/components/course/CourseDeleteActionSheet.vue`를 추가해 저장 코스 삭제 confirmation과 삭제 중 상태를 명시했습니다.
- `CourseEditView.vue`에 코스 제목 field, 저장 방식 안내, 장소 수 validation, `변경사항 저장`, `취소`, 저장 중/성공/실패 상태를 보강했습니다.
- `CourseView.vue`에 생성한 코스가 저장 후 마이페이지에서 수정·공유·삭제 가능하다는 안내를 추가했습니다.

## 남은 한계

- 콘텐츠/장소 원본 데이터 생성·수정·삭제는 admin backend endpoint만 있고 운영자용 UI는 아직 없습니다.
- 위 CRUD 상태는 build/smoke로 정적 검증되며, 실제 브라우저 live QA는 `final-rubric-live-qa-checklist.md` 절차에 따라 수동 확인이 필요합니다.
