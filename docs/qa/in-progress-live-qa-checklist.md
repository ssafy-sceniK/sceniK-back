# In Progress Live QA 체크리스트

생성일: 2026-06-08

이 체크리스트는 서버를 직접 실행할 수 있는 환경에서 수동으로 검증하기 위한 문서입니다. 이 작업에서는 장기 실행 서버를 시작하지 않습니다.

## 실행 전 준비

Backend 수동 실행:

```powershell
cd C:\Users\jaemy\Downloads\sceniK\backend
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:DB_USERNAME="ssafy"
$env:DB_PASSWORD="ssafy"
.\gradlew.bat bootRun
```

Frontend 수동 실행:

```powershell
cd C:\Users\jaemy\Downloads\sceniK\frontend
npm run dev
```

## 댓글 수정/삭제

| 단계 | 기대 결과 |
| --- | --- |
| 로그인 후 커뮤니티 게시글 상세로 이동 | 댓글 목록과 댓글 작성 입력창이 표시됩니다. |
| 내 댓글 작성 | 댓글이 목록에 추가됩니다. |
| 내 댓글의 `수정` 클릭 | 편집 textarea와 저장/취소 버튼이 표시됩니다. |
| 빈 댓글 저장 시도 | `댓글 내용을 입력해 주세요.` 메시지가 표시됩니다. |
| 댓글 수정 저장 | 수정된 내용이 목록에 반영됩니다. |
| 내 댓글의 `삭제` 클릭 후 확인 | 댓글이 목록에서 제거됩니다. |
| 다른 사용자의 댓글 확인 | 수정/삭제 버튼이 보이지 않아야 합니다. |

## 코스 장소 추가/수정/삭제/순서 변경

| 단계 | 기대 결과 |
| --- | --- |
| 저장한 AI 코스 상세에서 편집 화면 진입 | 기존 Day/장소 목록이 표시됩니다. |
| 추천 장소 또는 검색 장소 추가 | 중복 장소가 아니면 선택한 Day에 추가됩니다. |
| 장소 제거 | 각 Day에 최소 1개 장소가 남는 경우 제거됩니다. |
| 장소 순서 드래그 | 화면상 순서와 visitOrder가 갱신됩니다. |
| 수정 완료 | 현재 화면은 전체 route patch로 저장됩니다. 개별 route-place endpoint는 HTTP 파일로 별도 검증합니다. |

## 리뷰 생성/수정/삭제

| 단계 | 기대 결과 |
| --- | --- |
| 장소 상세로 이동 | 리뷰 목록 또는 빈 상태가 표시됩니다. |
| 리뷰 작성 | 내 리뷰가 목록에 추가되고 작성 폼이 숨겨집니다. |
| 내 리뷰의 `수정` 클릭 | 평점/내용 편집 폼이 표시됩니다. |
| 빈 리뷰 저장 시도 | `리뷰 내용을 입력해 주세요.` 메시지가 표시됩니다. |
| 리뷰 수정 저장 | 수정된 평점과 내용이 목록에 반영됩니다. |
| 리뷰 삭제 확인 | 리뷰가 목록에서 제거되고 작성 폼이 다시 표시됩니다. |
| 다른 사용자의 리뷰 확인 | 수정/삭제 버튼이 보이지 않아야 합니다. |

## 지도 fallback/provider

| 단계 | 기대 결과 |
| --- | --- |
| 장소 상세 지도 패널 확인 | `지도 API 없이 제공되는 경로 미리보기` fallback이 표시되고 화면이 깨지지 않습니다. |
| 지도 탐색 화면 확인 | backend 장소 좌표 기반 목록과 fallback map preview가 표시됩니다. |
| `GET /api/maps/geocode` 호출 | Kakao key가 없으면 안전한 empty result와 fallback 메시지가 반환됩니다. |
| `GET /api/maps/reverse-geocode` 호출 | Kakao key가 없으면 안전한 empty result와 fallback 메시지가 반환됩니다. |
| `GET /api/maps/local-search` 호출 | Kakao key가 없으면 안전한 empty result와 fallback 메시지가 반환됩니다. |
| Kakao key 설정 후 호출 | 현재 `MapService`는 skeleton fallback이므로 실제 provider live QA는 아직 Done 처리하지 않습니다. |

## 관리자 콘텐츠/장소 API 테스트

| 단계 | 기대 결과 |
| --- | --- |
| 관리자 콘텐츠 생성 API 호출 | 인증/권한 정책 범위 내에서 backend-only utility 동작을 확인합니다. |
| 관리자 콘텐츠 수정 API 호출 | contentId 기준 수정 결과를 확인합니다. |
| 관리자 콘텐츠 삭제 API 호출 | 삭제 또는 비활성화 결과를 확인합니다. |
| 관리자 장소 생성 API 호출 | placeId 기반 생성 결과를 확인합니다. |
| 관리자 장소 수정 API 호출 | 장소 정보 수정 결과를 확인합니다. |
| 관리자 장소 삭제 API 호출 | 삭제 또는 비활성화 결과를 확인합니다. |

## 알림

| 단계 | 기대 결과 |
| --- | --- |
| 알림 화면 진입 | `GET /api/notifications` 결과가 표시됩니다. |
| 모두 읽음 클릭 | `PATCH /api/notifications/read` 호출 후 화면상 read 상태가 반영됩니다. |
| 단일 알림 읽음 | `PATCH /api/notifications/{notificationId}/read` 호출이 성공해야 합니다. |

## 마이페이지

| 단계 | 기대 결과 |
| --- | --- |
| 마이페이지 진입 | `GET /api/users/me`, activity summary, liked lists, visit status가 화면에 반영됩니다. |
| 좋아요한 콘텐츠 목록 | `GET /api/users/me/liked-contents` 기반 목록이 표시됩니다. |
| 좋아요한 장소 목록 | `GET /api/users/me/liked-places` 기반 목록이 표시됩니다. |
| 방문 예정/완료 목록 | `GET /api/users/me/visit-status` 기반 목록이 표시됩니다. |
| 저장한 AI 코스 목록 | `GET /api/users/me/routes` 기반 목록이 표시됩니다. |
