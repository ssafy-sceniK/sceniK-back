# 팔로우 Live QA 증거 절차

생성일: 2026-06-09

이 문서는 루브릭 #09 팔로우·팔로잉 항목을 두 계정 기준으로 검증하기 위한 수동 QA 절차입니다. 최신 analyzer 피드백에서 팔로우 기능은 구현 완료로 판단되었지만 live QA 증거가 상대적으로 약하다고 지적되었으므로, 발표자와 평가자가 같은 시나리오를 반복할 수 있도록 계정, route, 기대 결과, 캡처 위치를 명확히 분리합니다.

이번 pass에서는 seeded DB와 두 개의 실제 로그인 세션이 준비되지 않았고 장기 실행 서버를 남기지 않는 조건을 유지해야 하므로 브라우저 캡처를 수행하지 않았습니다. 따라서 아래 항목은 구현/빌드 근거와 수동 캡처 절차를 구분해 기록합니다.

## 현재 증거 상태

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| backend API 구현 | 구현됨 | `FollowController`, `FollowService`, `FollowMapper.xml` |
| frontend author follow | 구현됨 | `CommunityView.vue` |
| follower/following 목록 | 구현됨 | `SimpleListView.vue` |
| 맞팔 badge | 구현됨, live QA 필요 | 현재 사용자의 following/follower 목록 비교로 파생 |
| 두 계정 브라우저 QA | 수동 캡처 필요 | 실제 로그인 세션 2개 필요 |
| 캡처 증거 | 수동 캡처 필요 | `backend/docs/evidence/screenshots/follow-two-account/`에 저장 예정 |

## 계정 전제 조건

| 계정 | 조건 | 권장 준비 |
| --- | --- | --- |
| Account A | 일반 사용자 | 팔로우를 누르는 사용자입니다. |
| Account B | 일반 사용자 | 커뮤니티 게시글 작성자입니다. |
| 브라우저 세션 | 서로 다른 로그인 세션 2개 | 일반 창과 시크릿 창, 또는 서로 다른 브라우저를 사용합니다. |
| 데이터 | B가 작성한 커뮤니티 게시글 1개 이상 | 작성자 영역의 `팔로우` 버튼 확인에 필요합니다. |

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

## 권장 캡처 목록

| 파일명 | 캡처 장면 | 평가 포인트 |
| --- | --- | --- |
| `01-a-follows-b-community-author.png` | A가 B 게시글 상세에서 `팔로우`를 누른 직후 | author follow 진입점과 pending/완료 상태 |
| `02-a-following-list-b.png` | A의 마이페이지 팔로잉 목록 | `GET /api/users/me/following` UI 연결 |
| `03-b-followers-list-a.png` | B의 마이페이지 팔로워 목록 | `GET /api/users/{userId}/followers` UI 연결 |
| `04-mutual-follow-badge.png` | B가 A를 맞팔한 뒤 양쪽 목록 | `맞팔` badge 표시 |
| `05-a-unfollow-b.png` | A가 B를 언팔로우한 뒤 | 목록 갱신과 중복 클릭 방지 |

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

## 최종 상태 판단

현재 코드와 build/smoke 근거로 기능 구현은 확인되었지만, analyzer가 요구한 live evidence 관점에서는 아직 `수동 캡처 필요` 상태입니다. Notion/API 상태는 `In progress`로 유지하는 것이 안전합니다.
