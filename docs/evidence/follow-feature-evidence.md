# 팔로우 기능 구현 증거

생성일: 2026-06-09

루브릭 #09 팔로우·팔로잉 항목의 최소 vertical slice 구현 근거를 정리합니다. 이 기능은 복잡한 소셜 피드가 아니라 커뮤니티 작성자 팔로우와 마이페이지 목록 확인에 한정합니다.

## 구현 상태

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| 팔로우 | 구현, live QA 필요 | `POST /api/users/{userId}/follow` |
| 언팔로우 | 구현, live QA 필요 | `DELETE /api/users/{userId}/follow` |
| 팔로워 목록 | 구현, live QA 필요 | `GET /api/users/{userId}/followers` |
| 팔로잉 목록 | 구현, live QA 필요 | `GET /api/users/{userId}/following`, `GET /api/users/me/following` |
| 맞팔 표시 | 구현, live QA 필요 | frontend가 현재 사용자의 following/follower 목록을 비교해 파생 |
| 목록 내 언팔로우 | 구현, live QA 필요 | `SimpleListView.vue`에서 pending guard와 상태 영역 표시 |
| 자기 자신 팔로우 방지 | 구현 | `Cannot follow self.` → `자기 자신은 팔로우할 수 없습니다.` |
| 중복 팔로우 방지 | 구현 | `Follow already exists.` → `이미 팔로우한 사용자입니다.` |

## Backend Evidence

| 파일 | 설명 |
| --- | --- |
| `backend/src/main/resources/schema.sql` | `follows` table, unique key, foreign key |
| `backend/src/main/java/com/scenik/controller/FollowController.java` | follow/follower/following endpoint |
| `backend/src/main/java/com/scenik/service/FollowService.java` | 자기 자신, 중복, 대상 사용자 검증 |
| `backend/src/main/java/com/scenik/mapper/FollowMapper.java` | MyBatis mapper interface |
| `backend/src/main/resources/mapper/FollowMapper.xml` | follow insert/delete/list/count SQL |
| `backend/src/main/java/com/scenik/dto/FollowDtos.java` | follow user/status response DTO |
| `backend/src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 사용자 친화적 한국어 오류 메시지 |

## Frontend Evidence

| 파일 | 설명 |
| --- | --- |
| `frontend/src/api/followApi.js` | follow API adapter |
| `frontend/src/stores/uiStore.js` | follow state, pending guard, follow/unfollow action |
| `frontend/src/views/CommunityView.vue` | 게시글 상세 작성자 팔로우/언팔로우 버튼 |
| `frontend/src/views/MyPageView.vue` | 팔로잉/팔로워 진입점과 카운트 표시 |
| `frontend/src/views/SimpleListView.vue` | 팔로잉/팔로워 목록, 맞팔 badge, follow/unfollow 버튼, 결과 상태 |

## Manual QA Steps

1. 사용자 A와 사용자 B를 각각 가입/로그인합니다.
2. 사용자 B가 커뮤니티 게시글을 작성합니다.
3. 사용자 A로 로그인해 게시글 상세에서 작성자 `팔로우` 버튼을 누릅니다.
4. 버튼이 `팔로잉` 상태로 바뀌고 중복 클릭이 막히는지 확인합니다.
5. 마이페이지 `팔로잉` 목록에서 사용자 B가 보이는지 확인합니다.
6. 사용자 B로 로그인해 마이페이지 `팔로워` 목록에서 사용자 A가 보이는지 확인합니다.
7. 사용자 B가 사용자 A를 팔로우해 양방향 관계를 만듭니다.
8. 양쪽 목록에서 `맞팔` badge가 보이는지 확인합니다.
9. 사용자 A가 `팔로잉` 목록에서 언팔로우했을 때 목록이 갱신되고 `팔로우를 취소했습니다.` 상태가 보이는지 확인합니다.
10. 자기 자신에 대한 follow action이 숨겨지거나 backend에서 차단되는지 확인합니다.

## 2026-06-09 보강 사항

- follower/following 목록에서 `followingUsers`와 `followerUsers`를 비교해 `맞팔` badge를 표시합니다.
- 팔로잉 목록에서도 직접 언팔로우할 수 있고, follower 목록에서는 현재 내가 팔로우 중인지에 따라 `팔로우`, `팔로잉`, `맞팔` 상태를 보여줍니다.
- pending 상태는 `처리 중` label과 disabled button으로 표시해 중복 클릭을 막습니다.
- 실제 두 계정 기반 live QA는 `follow-live-qa-evidence.md`에 별도 절차로 분리했습니다.

## 상태 판단

백엔드와 프론트 진입점은 구현되었지만 live E2E QA 전이므로 Notion 상태는 `In progress`로 표시합니다.
