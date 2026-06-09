# 커뮤니티 게시글·댓글 기능 증거

생성일: 2026-06-09

루브릭 #10 게시판 CRUD와 #11 댓글 CRUD 항목의 구현 근거를 정리합니다.

## Post CRUD

| 기능 | Endpoint | Backend Evidence | Frontend Evidence |
| --- | --- | --- | --- |
| 게시글 목록 | `GET /api/posts` | `CommunityController.posts`, `CommunityService.listPosts` | `CommunityView.vue`, `communityApi.getPosts` |
| 게시글 상세 | `GET /api/posts/{postId}` | `CommunityController.post` | `CommunityView.vue`, `communityApi.getPost` |
| 게시글 작성 | `POST /api/posts` | `CommunityController.createPost` | `CommunityWriteView.vue`, `uiStore.createPost` |
| 게시글 수정 | `PATCH /api/posts/{postId}` | `CommunityController.updatePost` | `communityApi.updatePost` |
| 게시글 삭제 | `DELETE /api/posts/{postId}` | `CommunityController.deletePost` | `communityApi.deletePost` |
| 게시글 좋아요 | `POST/DELETE /api/posts/{postId}/likes` | `CommunityController.likePost/unlikePost` | `CommunityView.vue` |
| 게시글 신고 | `POST /api/posts/{postId}/reports` | `CommunityController.reportPost` | `communityApi.reportPost` |

## Comment CRUD

| 기능 | Endpoint | Backend Evidence | Frontend Evidence |
| --- | --- | --- | --- |
| 댓글 목록 | `GET /api/posts/{postId}/comments` | `CommunityController.comments` | `CommunityView.vue`, `communityApi.getComments` |
| 댓글 작성 | `POST /api/posts/{postId}/comments` | `CommunityController.createComment` | `CommunityView.vue`, `uiStore.addComment` |
| 답글 작성 | `POST /api/posts/{postId}/comments` + `parentCommentId` | `CommunityService.createComment`, `CommentRequest.parentCommentId` | `CommunityView.vue` 답글 UI |
| 댓글 좋아요 | `POST/DELETE /api/posts/comments/{commentId}/likes` | `CommunityController.likeComment/unlikeComment`, `comment_likes` | `communityApi.likeComment/unlikeComment`, `uiStore.toggleCommentLike`, `CommunityView.vue` |
| 댓글 수정 | `PATCH /api/posts/{postId}/comments/{commentId}` | `CommunityController.updateComment`, owner check | `CommunityView.vue`, `uiStore.editComment` |
| 댓글 삭제 | `DELETE /api/posts/{postId}/comments/{commentId}` | `CommunityController.deleteComment`, owner check | `CommunityView.vue`, `uiStore.removeComment` |

## Owner Permission

- 게시글 수정/삭제는 `Post owner does not match.`를 `403 권한이 없습니다.`로 변환합니다.
- 댓글 수정/삭제는 `Comment owner does not match.`를 `403 권한이 없습니다.`로 변환합니다.
- frontend는 현재 로그인 사용자 `userId`와 댓글 `userId`가 같은 경우에만 수정/삭제 버튼을 보여줍니다.

## Pagination / Sort

| Endpoint | 지원 |
| --- | --- |
| `GET /api/posts` | `page`, `size`, `sort=latest/popular`, `keyword`, `region` |
| `GET /api/posts/{postId}/comments` | `page`, `size` |

## 답글 지원

`CommentRequest.parentCommentId`를 사용해 1단계 답글을 작성할 수 있습니다. backend는 답글의 답글을 막기 위해 `Nested replies are not supported.`를 반환하며, frontend는 최상위 댓글에만 `답글` 버튼을 표시합니다. live QA 전까지 댓글 수정/삭제/답글은 `In progress`로 유지합니다.

## 댓글 좋아요 구현 범위

- `comments.comment_id`가 `VARCHAR(64)`인 현재 schema에 맞춰 `comment_likes.comment_id`도 `VARCHAR(64)`로 추가했습니다.
- `comment_likes`는 `(user_id, comment_id)` 복합 기본키와 FK를 사용해 중복 좋아요를 막습니다.
- `CommunityMapper`는 댓글 목록/단건 조회 시 `like_count`와 로그인 사용자 기준 `is_liked`를 함께 조회합니다.
- `CommentDto`는 `likeCount`, `likes`, `likedByMe`, `isLiked`를 반환합니다.
- `POST /api/posts/comments/{commentId}/likes`는 `INSERT IGNORE` 기반으로 idempotent하게 동작합니다.
- `DELETE /api/posts/comments/{commentId}/likes`는 없는 좋아요를 취소해도 count를 손상하지 않습니다.
- 답글도 `comments` 레코드이므로 독립적인 `commentId` 기준으로 좋아요가 적용됩니다.

## Manual QA Steps

1. 로그인합니다.
2. 커뮤니티 게시글을 작성합니다.
3. 게시글 상세에서 댓글을 작성합니다.
4. 내 댓글의 수정 버튼을 눌러 내용을 변경합니다.
5. 빈 댓글 저장 시 `댓글 내용을 입력해 주세요.` 메시지를 확인합니다.
6. 내 댓글을 삭제하고 목록에서 제거되는지 확인합니다.
7. 최상위 댓글의 `답글` 버튼을 눌러 1단계 답글을 등록합니다.
8. 최상위 댓글의 좋아요를 누르고 count가 1회만 증가하는지 확인합니다.
9. 같은 댓글의 좋아요를 취소하고 count가 1회만 감소하는지 확인합니다.
10. 답글 좋아요가 부모 댓글 count를 변경하지 않는지 확인합니다.
11. 답글에는 다시 `답글` 버튼이 보이지 않는지 확인합니다.
12. 다른 사용자의 댓글에는 수정/삭제 버튼이 보이지 않는지 확인합니다.
13. 게시글 목록에서 `sort=popular`, `keyword`, `region` query를 수동 API로 확인합니다.
