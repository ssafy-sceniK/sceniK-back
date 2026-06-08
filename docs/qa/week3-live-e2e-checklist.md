# Week 3 Live E2E 수동 QA 체크리스트

생성일: 2026-06-08

## 목적

Week 3 핵심 사용자 흐름이 실제 Spring Boot backend, MySQL, Vue frontend 조합에서 동작하는지 확인합니다. 자동화 작업에서는 장시간 서버를 남겨두지 않기 위해, 이 체크리스트와 `backend/api-tests/week3-live-e2e.http`를 사용해 수동 검증합니다.

## 사전 준비

### Backend 실행

```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:DB_USERNAME="ssafy"
$env:DB_PASSWORD="ssafy"
.\gradlew.bat bootRun
```

### Frontend 실행

```powershell
cd frontend
npm run dev
```

### AI 테스트

FastAPI 서버는 Week 3 live E2E 필수 조건이 아닙니다. Week 3의 canonical AI route path는 Spring Boot `POST /api/ai/routes`입니다.

AI 서버 자체 확인:

```powershell
cd sceniK-ai
.\.venv\Scripts\Activate.ps1
python -m pytest
```

## HTTP 검증 파일

```text
backend/api-tests/week3-live-e2e.http
```

REST Client에서 위 파일을 열고 1번부터 순서대로 실행합니다.

## 체크리스트

| 순서 | 흐름 | 예상 결과 |
| ---: | --- | --- |
| 1 | signup | 200 또는 201, `accessToken`, `refreshToken`, `user` 반환 |
| 2 | login | 200, `accessToken`, `refreshToken`, `user` 반환 |
| 3 | `GET /api/users/me` | 200, 현재 사용자 정보 반환 |
| 4 | contents list | 200, ContentDto 배열과 `X-Total-Count` |
| 5 | content detail | 200, `contentId`, `title`, `category`, `isLiked` 포함 |
| 6 | related places | 200, PlaceDto 배열 |
| 7 | places list | 200, PlaceDto 배열과 `X-Total-Count` |
| 8 | place detail | 200, `placeId`, `placeName`, `region`, `visitStatus` 포함 |
| 9 | content like | 204 |
| 10 | place like | 204 |
| 11 | visit status `WANT_TO_GO` | 200, `status`가 `WANT_TO_GO` |
| 12 | visit status `VISITED` | 200, `status`가 `VISITED` |
| 13 | liked contents | 200, contentId 문자열 배열 |
| 14 | liked places | 200, placeId 문자열 배열 |
| 15 | visit status list | 200, VisitStatusDto 배열 |
| 16 | AI route generation | 200, `generationId`, `candidatePlaces`, `days` 포함 |
| 17 | route save | 200, `routeId`, `days` 포함 |
| 18 | my routes | 200, 저장 route 배열 |
| 19 | route detail | 200, 저장한 `routeId` 상세 |
| 20 | community posts list | 200, PostDto 배열 |
| 21 | post create | 200, `postId` 반환 |
| 22 | comment create | 200, `commentId` 반환 |
| 23 | notifications | 200, NotificationDto 배열과 `X-Unread-Count` |
| 24 | review list | 200, ReviewDto 배열 |
| 25 | review create | 200, `reviewId`, `rating`, `content` 반환 |
| 26 | invalid visit status | 400, 방문 상태 오류 메시지 |
| 27 | unauthorized users/me | 401, 로그인 필요 메시지 |

## 실패 시 기록할 내용

각 실패는 아래 형식으로 기록합니다.

```text
단계:
요청:
예상 상태:
실제 상태:
응답 body:
backend log 핵심 줄:
추정 원인:
다음 조치:
```

## 완료 기준

Week 3 live E2E를 완료로 판단하려면 다음이 모두 통과해야 합니다.

- 회원가입/로그인/현재 사용자 조회
- 콘텐츠/장소 목록 및 상세
- 콘텐츠/장소 좋아요
- 방문 상태 `WANT_TO_GO`, `VISITED`
- 마이페이지 engagement 목록
- AI route generation
- route save/my routes/route detail
- 게시글 작성
- 댓글 작성
- 알림 목록
- 리뷰 목록/작성

## 현재 자동화 상태

2026-06-08 기준 자동 QA:
- Backend build: PASS
- Frontend build/lint/smoke: PASS
- AI pytest: PASS

Live E2E:
- 서버를 남겨두지 않는 원칙 때문에 자동 실행하지 않았습니다.
- 위 HTTP 파일과 체크리스트로 수동 검증이 필요합니다.
