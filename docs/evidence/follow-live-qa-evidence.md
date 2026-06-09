# 팔로우 Live QA 증거 절차

생성일: 2026-06-09

이 문서는 루브릭 #09 팔로우·팔로잉 항목을 두 계정 기준으로 검증하기 위한 수동 QA 절차입니다. 이번 자동 작업에서는 장기 실행 서버와 브라우저 세션을 유지하지 않았으므로 실제 캡처 증거는 아직 없습니다.

## 현재 증거 상태

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| backend API 구현 | 구현됨 | `FollowController`, `FollowService`, `FollowMapper.xml` |
| frontend author follow | 구현됨 | `CommunityView.vue` |
| follower/following 목록 | 구현됨 | `SimpleListView.vue` |
| 맞팔 badge | 구현됨, live QA 필요 | 현재 사용자의 following/follower 목록 비교로 파생 |
| 두 계정 브라우저 QA | 수동 캡처 필요 | 실제 로그인 세션 2개 필요 |

## 수동 실행 준비

```powershell
cd C:\Users\jaemy\Downloads\sceniK\backend
.\gradlew.bat bootRun
```

```powershell
cd C:\Users\jaemy\Downloads\sceniK\frontend
npm run dev
```

## 두 계정 QA 시나리오

| 단계 | 계정 | 작업 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| 1 | A | 회원가입 또는 로그인 | A 계정으로 로그인됩니다. | 수동 확인 필요 |
| 2 | B | 별도 브라우저/시크릿 창에서 회원가입 또는 로그인 | B 계정으로 로그인됩니다. | 수동 확인 필요 |
| 3 | B | 커뮤니티 게시글 작성 | B가 작성자인 게시글이 생성됩니다. | 수동 확인 필요 |
| 4 | A | B 게시글 상세에서 `팔로우` 클릭 | 버튼이 `팔로잉` 또는 `처리 중` 후 `팔로잉`으로 바뀝니다. | 수동 확인 필요 |
| 5 | A | 마이페이지 `팔로잉` 진입 | B가 목록에 표시되고 언팔로우 버튼이 보입니다. | 수동 확인 필요 |
| 6 | B | 마이페이지 `팔로워` 진입 | A가 목록에 표시됩니다. | 수동 확인 필요 |
| 7 | B | A를 팔로우 | A/B가 서로 팔로우 상태가 됩니다. | 수동 확인 필요 |
| 8 | A/B | 팔로워/팔로잉 목록 확인 | `맞팔` badge가 표시됩니다. | 수동 확인 필요 |
| 9 | A | 팔로잉 목록에서 B 언팔로우 | 목록이 갱신되고 `팔로우를 취소했습니다.` 상태가 표시됩니다. | 수동 확인 필요 |
| 10 | A/B | 자기 자신 action 확인 | 자기 자신 팔로우 버튼은 숨겨지거나 backend에서 `자기 자신은 팔로우할 수 없습니다.`로 차단됩니다. | 수동 확인 필요 |

## API 직접 확인 예시

```http
POST /api/users/{targetUserId}/follow
Authorization: Bearer {accessToken}
```

```http
DELETE /api/users/{targetUserId}/follow
Authorization: Bearer {accessToken}
```

```http
GET /api/users/me/following
Authorization: Bearer {accessToken}
```

```http
GET /api/users/{userId}/followers
Authorization: Bearer {accessToken}
```

## 캡처 증거 위치

실제 브라우저 QA를 수행하면 아래 경로에 캡처를 저장하고 이 문서에서 참조합니다.

```text
backend/docs/evidence/screenshots/follow-two-account/
```

현재 상태: 수동 캡처 필요.
