# 최종 루브릭 Live QA 체크리스트

생성일: 2026-06-09

이 문서는 루브릭 피드백 보강 이후 실제 실행 환경에서 마지막으로 확인해야 할 수동 QA 항목입니다. 이번 문서 정리 작업에서는 장기 실행 서버를 남기지 않았으며, frontend build/smoke, backend build, 정적 검색을 수행했습니다. `sceniK-ai`는 수정하지 않았고 이번 pass에서 pytest를 다시 실행하지 않았습니다.

최신 analyzer 결과는 89/100, Complete 14/15, Partial 1/15이며, 남은 약점은 기능 미구현보다 follow two-account live QA와 browser-based verification evidence 부족에 가깝습니다.

## 자동 QA 결과

| 항목 | 결과 | 비고 |
| --- | --- | --- |
| Backend build | PASS | `.\gradlew.bat clean build` 통과 |
| Frontend build | PASS | `npm run build` 통과, Vite bundle size warning만 발생 |
| Frontend lint | PASS | `npm run lint` 통과 |
| Course smoke | PASS | `npm run smoke:course` 통과 |
| Error smoke | PASS | `npm run smoke:errors` 통과 |
| AI pytest | 이번 pass 미실행 | `sceniK-ai`는 수정하지 않았습니다. 제출 전 AI repo 연결 후 별도 실행을 권장합니다. |
| Frontend direct AI call scan | PASS | frontend에서 `sceniK-ai`, `localhost:8000`, `127.0.0.1:8000` 직접 호출 없음 |
| Deprecated API source scan | PASS | 구버전 API 호출 없음. `/ai/courses/:routeId`, `/my/visit-*`는 화면 라우터 경로 false positive입니다. |
| Korean text scan | PASS | replacement character 또는 명백한 mojibake 패턴 없음 |
| Secret pattern scan | PASS | 실제 API key/token 패턴 없음. 테스트용 `password123`만 존재합니다. |
| Build output tracking | PASS | `frontend/dist/`는 `.gitignore`로 무시되며 Git 추적 대상이 아닙니다. |
| AI venv tracking | 확인 필요 | 현재 `sceniK-ai` 폴더는 이 workspace에서 Git 저장소가 아니므로 `.venv` 추적 여부는 remote 연결 후 확인해야 합니다. |

## 최종 analyzer 이후 추가 확인

| 항목 | 기대 결과 | 상태 |
| --- | --- | --- |
| AI 코스 콘텐츠 선택 화면 typography | `어떤 콘텐츠로 여행할까요?` 제목이 393px 기준 22~24px, 굵은 weight, 안정적인 line-height로 보입니다. | 코드 보강 완료, 시각 확인 필요 |
| AI 코스 콘텐츠 선택 화면 spacing | progress, title/subtitle, 검색, chip, grid 사이 간격이 과도하게 벌어지지 않습니다. | 코드 보강 완료, 시각 확인 필요 |
| 360px/393px/430px overflow | 콘텐츠 카드, chip, bottom CTA에 horizontal overflow가 없습니다. | 수동 확인 필요 |
| follow two-account 캡처 | Account A/B follow, followers/following, 맞팔 badge, unfollow 장면이 캡처됩니다. | 수동 캡처 필요 |
| review/detail/notification/community 캡처 | `browser-live-qa-evidence.md`의 권장 캡처 파일이 생성됩니다. | 수동 캡처 필요 |
| 제출 산출물 위치 | `submission-artifact-index.md`에서 outline/제출 가능/수동 캡처 필요 상태가 구분됩니다. | 문서 보강 완료 |

## Live QA 실행 준비 명령

서버를 오래 실행하지 않는 방식으로 각 터미널에서 실행하고, QA 완료 후 종료합니다.

```powershell
cd C:\Users\jaemy\Downloads\sceniK\backend
.\gradlew.bat bootRun
```

```powershell
cd C:\Users\jaemy\Downloads\sceniK\frontend
npm run dev
```

AI 서비스 live QA가 필요한 경우에만 별도 터미널에서 실행합니다.

```powershell
cd C:\Users\jaemy\Downloads\sceniK\sceniK-ai
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

## 인증 QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| 회원가입 | 정상 가입 후 로그인 가능한 상태가 됩니다. | 수동 확인 필요 |
| 이메일 중복 확인 | `중복 확인` 버튼이 backend `GET /api/auth/check-email` 결과를 표시합니다. | 수동 확인 필요 |
| 이메일 변경 후 재확인 | 이메일을 수정하면 이전 중복 확인 결과가 무효화됩니다. | 수동 확인 필요 |
| 중복 이메일 회원가입 | `이미 사용 중인 이메일입니다.` 메시지가 표시됩니다. | 수동 확인 필요 |
| 중복 닉네임 회원가입 | `이미 사용 중인 닉네임입니다.` 메시지가 표시됩니다. | 수동 확인 필요 |
| 8자 미만 비밀번호 | `비밀번호는 8자 이상이어야 합니다.` 메시지가 표시됩니다. | 수동 확인 필요 |
| 로그인 | access token 기반으로 홈/마이페이지 상태가 갱신됩니다. | 수동 확인 필요 |
| 로그아웃 | 인증 상태와 사용자 데이터가 정리됩니다. | 수동 확인 필요 |
| Settings 세션 관리 | 현재 로그인 계정과 “로그아웃하면 이 기기에서 세션이 종료됩니다.” 안내가 표시됩니다. | 수동 확인 필요 |
| `/api/users/me` | 로그인 사용자 프로필이 backend 기준으로 표시됩니다. | 수동 확인 필요 |

## 콘텐츠/장소 QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| 콘텐츠 목록 검색 | `keyword`, `category`, `sort`, `page`, `size`가 backend query로 전달됩니다. | 수동 확인 필요 |
| 콘텐츠 상세 정상 조회 | 콘텐츠 상세와 관련 장소가 표시됩니다. | 수동 확인 필요 |
| 콘텐츠 상세 404 | 빈 화면 대신 한국어 not-found/error UI가 표시됩니다. | 수동 확인 필요 |
| 장소 목록 검색 | `keyword`, `region`, `category`, `sort`, `page`, `size`가 동작합니다. | 수동 확인 필요 |
| 장소 상세 정상 조회 | 장소 정보, 지도 preview, 관련 콘텐츠가 표시됩니다. | 수동 확인 필요 |
| 장소 상세 404 | 빈 화면 대신 한국어 not-found/error UI가 표시됩니다. | 수동 확인 필요 |
| 장소 좋아요 | 좋아요 상태와 마이페이지 좋아요 장소 목록이 갱신됩니다. | 수동 확인 필요 |
| 방문 예정 | `WANT_TO_GO` 상태가 저장되고 방문 예정 목록에 표시됩니다. | 수동 확인 필요 |
| 방문 완료 | `VISITED` 상태가 저장되고 방문 완료 목록에 표시됩니다. | 수동 확인 필요 |
| map provider | 좌표 기반 표시와 외부 provider fallback을 확인합니다. | BLOCKED: credential/server 필요 |

## 리뷰 QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| 리뷰 목록 | `GET /api/places/{placeId}/reviews` 결과가 장소 상세에 표시됩니다. | 수동 확인 필요 |
| 리뷰 빈 상태 | 리뷰가 없을 때 한국어 empty state가 표시됩니다. | 수동 확인 필요 |
| 리뷰 작성 | 로그인 사용자가 평점/내용을 입력해 등록할 수 있습니다. | 수동 확인 필요 |
| 리뷰 중복 작성 | 이미 작성한 장소에서는 작성 폼이 숨겨지거나 409 메시지가 표시됩니다. | 수동 확인 필요 |
| 리뷰 상태 영역 | 작성/수정/삭제 성공 또는 실패가 화면 안의 상태 영역에 표시됩니다. | 수동 확인 필요 |
| 평점 분포 | 리뷰가 있을 때 평균 평점과 간단한 평점 분포가 표시됩니다. | 수동 확인 필요 |
| 리뷰 수정 | 작성자에게만 수정 버튼이 보이고 저장 후 목록이 갱신됩니다. | 수동 확인 필요 |
| 리뷰 삭제 | 작성자에게만 삭제 버튼이 보이고 삭제 후 작성 폼이 다시 표시됩니다. | 수동 확인 필요 |
| 빈 리뷰 내용 | `리뷰 내용을 입력해 주세요.` 메시지가 표시됩니다. | 수동 확인 필요 |
| 평점 범위 오류 | `평점은 0점 이상 5점 이하로 입력해 주세요.` 메시지가 표시됩니다. | 수동 확인 필요 |
| 평균 평점/리뷰 수 | 리뷰 등록/수정/삭제 후 header 수치가 갱신됩니다. | 수동 확인 필요 |
| 내 리뷰 목록 | 마이페이지 `내 리뷰`에서 `GET /api/users/me/reviews` 결과가 표시됩니다. | 수동 확인 필요 |

## AI Route / Route QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| AI 조건 입력 | 콘텐츠, 지역, 기간, 이동수단, 동행, 스타일, 예산 조건이 유지됩니다. | 수동 확인 필요 |
| AI 참고 조건 패널 | 조건/로딩 화면에서 선택 콘텐츠, 지역, 일수, 이동, 예산/테마가 표시됩니다. | 수동 확인 필요 |
| AI route generation | `POST /api/ai/routes`가 검증 장소 기반 route를 반환합니다. | 수동 확인 필요 |
| AI 결과 근거 패널 | 결과 화면에서 `검증된 장소 데이터 기반 추천`, 추천 이유, 예상 체류 시간, 이동 팁이 표시됩니다. | 수동 확인 필요 |
| 후보 장소 없음 | 한국어 안내 메시지로 조건 변경을 유도합니다. | 수동 확인 필요 |
| route save | 생성 결과를 `POST /api/routes`로 저장합니다. | 수동 확인 필요 |
| my routes | `GET /api/users/me/routes` 목록에 저장 코스가 표시됩니다. | 수동 확인 필요 |
| route detail | 저장 코스 상세가 blank 없이 표시됩니다. | 수동 확인 필요 |
| route save/delete 상태 | 코스 저장, 편집 저장, 삭제 결과가 화면 안의 상태 영역에 표시됩니다. | 수동 확인 필요 |
| route edit error | 잘못된 place/order 입력 시 한국어 오류가 표시됩니다. | 수동 확인 필요 |

## 커뮤니티/댓글 QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| 게시글 목록 | `GET /api/posts`에서 `page`, `size`, `sort`, `keyword`, `region`이 동작합니다. | 수동 확인 필요 |
| 게시글 작성 | 로그인 사용자가 게시글을 작성할 수 있습니다. | 수동 확인 필요 |
| 게시글 작성 상태 | 작성 중/성공/실패 상태가 `CommunityWriteView`에 표시됩니다. | 수동 확인 필요 |
| 게시글 상세 | 상세와 댓글 목록이 표시됩니다. | 수동 확인 필요 |
| 게시글 수정 | 작성자에게만 수정 UI가 보이고 저장 후 `수정되었습니다.` 상태가 표시됩니다. | 수동 확인 필요 |
| 게시글 삭제 | 작성자에게만 삭제 UI가 보이고 삭제 후 목록으로 이동합니다. | 수동 확인 필요 |
| 게시글 404 | 한국어 not-found/error UI가 표시됩니다. | 수동 확인 필요 |
| 댓글 작성 | `POST /api/posts/{postId}/comments` 후 목록이 갱신됩니다. | 수동 확인 필요 |
| 댓글 action sheet | 작성자에게만 `더보기`가 보이고 action sheet에서 수정/삭제를 선택합니다. | 수동 확인 필요 |
| 댓글 수정 | action sheet에서 수정 진입 후 저장할 수 있습니다. | 수동 확인 필요 |
| 댓글 삭제 | action sheet에서 삭제 후 목록에서 제거됩니다. | 수동 확인 필요 |
| 1단계 답글 | 최상위 댓글과 답글에서 답글 입력은 가능하되 backend 저장 구조는 1단계로 유지됩니다. | 수동 확인 필요 |
| 댓글 상태 영역 | 댓글/답글 작성, 수정, 삭제 성공·실패 상태가 댓글 섹션 안에 표시됩니다. | 수동 확인 필요 |
| 댓글 좋아요 | 댓글과 답글의 좋아요 count/state가 backend와 동기화됩니다. | 수동 확인 필요 |

## 알림/마이페이지 QA

| 시나리오 | 기대 결과 | 상태 |
| --- | --- | --- |
| 알림 목록 | `GET /api/notifications` 결과가 표시됩니다. | 수동 확인 필요 |
| 알림 읽음 | 단일 읽음과 전체 읽음 처리 후 badge/count가 갱신됩니다. | 수동 확인 필요 |
| 마이페이지 | 사용자 정보, 활동 요약, 저장 경로, 좋아요, 방문 상태가 backend 기준으로 표시됩니다. | 수동 확인 필요 |
| 좋아요 콘텐츠 목록 | `GET /api/users/me/liked-contents` 결과가 표시됩니다. | 수동 확인 필요 |
| 좋아요 장소 목록 | `GET /api/users/me/liked-places` 결과가 표시됩니다. | 수동 확인 필요 |
| 방문 상태 목록 | `GET /api/users/me/visit-status` 결과가 표시됩니다. | 수동 확인 필요 |
| 내 게시글 목록 | `GET /api/users/me/posts` 결과가 표시됩니다. | 수동 확인 필요 |
| 팔로우 | 커뮤니티 게시글 상세에서 다른 작성자를 팔로우할 수 있습니다. | 수동 확인 필요 |
| 팔로잉 목록 | 마이페이지 `팔로잉`에서 내가 팔로우한 사용자가 표시되고 언팔로우할 수 있습니다. | 수동 확인 필요 |
| 팔로워 목록 | 마이페이지 `팔로워`에서 나를 팔로우한 사용자가 표시되고 follow/following 상태가 보입니다. | 수동 확인 필요 |
| 맞팔 badge | 서로 팔로우한 사용자에게 `맞팔` badge가 표시됩니다. | 수동 확인 필요 |
| 팔로우 pending | 버튼이 `처리 중` 상태로 중복 클릭을 막습니다. | 수동 확인 필요 |

## 최종 회귀 QA — 경계값/빈 상태/상태 전환

이 섹션은 analyzer가 마지막으로 지적한 boundary case, empty state, state transition 확인 항목입니다. 실제 live QA를 수행하기 전까지 상태는 `수동 확인 필요`로 유지합니다.

### #01 CRUD

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| invalid input | 필수 형식이 맞지 않는 입력에서 한국어 validation message가 표시됩니다. | 수동 확인 필요 |
| missing required fields | 제목, 내용, 장소, 평점 등 필수값 누락 시 400 또는 화면 validation이 동작합니다. | 수동 확인 필요 |
| create success | route/post/comment/review 생성 후 목록 또는 상세가 갱신됩니다. | 수동 확인 필요 |
| update success | 작성자 수정 후 변경 내용과 성공 상태가 표시됩니다. | 수동 확인 필요 |
| delete success | 삭제 후 목록 이동 또는 empty state 전환이 깨지지 않습니다. | 수동 확인 필요 |
| unauthorized edit/delete | 다른 사용자 데이터 수정/삭제 시 403 또는 권한 안내가 표시됩니다. | 수동 확인 필요 |

### #02 Search/sort

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| empty keyword | 빈 검색어에서 기본 목록이 유지됩니다. | 수동 확인 필요 |
| no result | 결과가 없을 때 한국어 empty state가 표시됩니다. | 수동 확인 필요 |
| sort latest/popular | 최신순/인기순 전환 시 결과 순서와 query가 갱신됩니다. | 수동 확인 필요 |
| category + keyword | 카테고리와 검색어 조합이 동시에 적용됩니다. | 수동 확인 필요 |
| pagination/more | 더 보기 또는 page/size query가 중복/누락 없이 동작합니다. | 수동 확인 필요 |
| URL query restore | frontend가 지원하는 화면에서 새로고침 후 query 상태가 복원됩니다. | 수동 확인 필요 |

### #03 Detail

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| invalid contentId | 없는 콘텐츠 ID에서 blank screen 대신 안내가 표시됩니다. | 수동 확인 필요 |
| invalid placeId | 없는 장소 ID에서 blank screen 대신 안내가 표시됩니다. | 수동 확인 필요 |
| invalid routeId | 없는 route ID에서 blank screen 대신 안내가 표시됩니다. | 수동 확인 필요 |
| related data empty | 관련 장소/콘텐츠가 없을 때 fallback 안내가 표시됩니다. | 수동 확인 필요 |
| fallback image | 잘못된 thumbnail은 neutral placeholder로만 대체되고 깨진 이미지 아이콘이 보이지 않습니다. | 수동 확인 필요 |
| back/retry recovery | 오류 화면에서 뒤로가기 또는 다시 시도 후 정상 목록으로 복구됩니다. | 수동 확인 필요 |

### #04 Review

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| rating min/max | 0~5 범위 외 평점이 차단됩니다. | 수동 확인 필요 |
| empty review content | 빈 리뷰 내용 저장이 차단됩니다. | 수동 확인 필요 |
| duplicate review | 같은 사용자의 같은 장소 중복 리뷰가 차단됩니다. | 수동 확인 필요 |
| average rating after create/update/delete | 작성/수정/삭제 후 평균 평점과 리뷰 수가 갱신됩니다. | 수동 확인 필요 |
| owner-only edit/delete | 작성자에게만 수정/삭제 UI가 보이고 비작성자는 차단됩니다. | 수동 확인 필요 |

### #05 Auth

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| missing token | 인증 필요 API에서 token 누락 시 401과 로그인 안내가 표시됩니다. | 수동 확인 필요 |
| invalid token | 잘못된 token에서 401과 한국어 안내가 표시됩니다. | 수동 확인 필요 |
| logout token reuse | 로그아웃 후 같은 token 재사용이 차단되는지 확인합니다. | 수동 확인 필요 |
| expired token if supported | token 만료 정책이 적용되는 환경에서 로그인 만료 안내가 표시됩니다. | 수동 확인 필요 |
| frontend login guidance | 401 이후 로그인 화면 안내와 redirect가 반복 loop 없이 동작합니다. | 수동 확인 필요 |

### #06 Signup

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| invalid email | 잘못된 이메일 형식이 frontend/server에서 일관되게 안내됩니다. | 수동 확인 필요 |
| duplicate email | 중복 이메일이 차단되고 메시지가 표시됩니다. | 수동 확인 필요 |
| password too short | 8자 미만 비밀번호가 차단됩니다. | 수동 확인 필요 |
| password confirmation mismatch | 비밀번호 확인 불일치가 차단됩니다. | 수동 확인 필요 |
| signup success route | 가입 성공 후 로그인/홈 이동 흐름이 명확합니다. | 수동 확인 필요 |

### #07 Profile

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| nickname too short | 닉네임 최소 길이 validation이 동작합니다. | 수동 확인 필요 |
| preference chips partial update | 일부 취향만 수정해도 기존 사용자 정보가 손상되지 않습니다. | 수동 확인 필요 |
| image preview/deferred upload copy | 이미지 미리보기는 로컬 동작이며 서버 업로드 보류 안내가 표시됩니다. | 수동 확인 필요 |
| save success/failure | 저장 성공/실패 상태가 화면 안에 표시됩니다. | 수동 확인 필요 |

### #08 Likes/favorites

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| double-click guard | 좋아요 버튼 연타 중 pending/disabled 상태가 적용됩니다. | 수동 확인 필요 |
| duplicate like | 중복 좋아요가 count를 중복 증가시키지 않습니다. | 수동 확인 필요 |
| unlike missing | 없는 좋아요 취소가 count를 손상하지 않습니다. | 수동 확인 필요 |
| list refresh | 마이페이지 좋아요 목록이 toggle 후 갱신됩니다. | 수동 확인 필요 |
| user isolation | 사용자 A/B 좋아요 상태가 서로 섞이지 않습니다. | 수동 확인 필요 |

### #09 Follow

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| self follow hidden/blocked | 자기 자신 팔로우가 숨겨지거나 backend에서 차단됩니다. | 수동 확인 필요 |
| duplicate follow | 중복 팔로우가 count를 중복 증가시키지 않습니다. | 수동 확인 필요 |
| unfollow missing | 없는 follow 취소가 count를 손상하지 않습니다. | 수동 확인 필요 |
| follower/following count | 팔로우/언팔로우 후 count와 목록이 일치합니다. | 수동 확인 필요 |
| mutual badge | 상호 팔로우 후 `맞팔` badge가 표시됩니다. | 수동 확인 필요 |
| two-account QA | 일반 창/시크릿 창 등 두 세션으로 A/B 시나리오를 확인합니다. | 수동 캡처 필요 |

### #10 Board

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| owner edit/delete | 작성자에게만 수정/삭제 UI가 표시됩니다. | 수동 확인 필요 |
| non-owner blocked | 비작성자 수정/삭제가 차단됩니다. | 수동 확인 필요 |
| search/sort | 게시글 검색/정렬이 query와 결과에 반영됩니다. | 수동 확인 필요 |
| empty board state | 게시글이 없거나 검색 결과가 없을 때 empty state가 표시됩니다. | 수동 확인 필요 |

### #11 Comments

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| empty comment | 빈 댓글 작성이 차단됩니다. | 수동 확인 필요 |
| reply | 최상위 댓글에 답글을 작성할 수 있습니다. | 수동 확인 필요 |
| reply-to-reply one-level mapping | 답글의 답글 입력이 backend 1단계 구조로 저장됩니다. | 수동 확인 필요 |
| comment like | 댓글/답글 좋아요 count와 state가 갱신됩니다. | 수동 확인 필요 |
| owner edit/delete | 작성자 댓글에만 action sheet 수정/삭제가 보입니다. | 수동 확인 필요 |
| non-owner blocked | 비작성자 댓글 수정/삭제가 차단됩니다. | 수동 확인 필요 |

### #12 Course

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| save route | AI 결과를 저장하고 내 코스 목록에 표시합니다. | 수동 확인 필요 |
| edit title | 저장 코스 제목 수정이 반영됩니다. | 수동 확인 필요 |
| reorder place | 코스 장소 순서 변경이 저장됩니다. | 수동 확인 필요 |
| remove all places validation | 모든 장소 제거 시 validation 또는 안내가 표시됩니다. | 수동 확인 필요 |
| delete route | 코스 삭제 후 목록/상세가 복구됩니다. | 수동 확인 필요 |
| progress summary | 코스 진행/장소 수/일차 요약이 화면에 표시됩니다. | 수동 확인 필요 |

### #13 AI

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| missing conditions | 필수 조건 누락 시 다음 단계 진입이 차단됩니다. | 수동 확인 필요 |
| fallback route generation | 후보 장소 부족 시 한국어 fallback 안내가 표시됩니다. | 수동 확인 필요 |
| prompt context panel | 선택 조건이 AI 참고 조건 패널에 표시됩니다. | 수동 확인 필요 |
| result reason panel | 결과 화면에 추천 이유와 검증 장소 기반 설명이 표시됩니다. | 수동 확인 필요 |
| backend-only AI call | frontend가 `sceniK-ai` 또는 `localhost:8000`을 직접 호출하지 않습니다. | 정적 확인 필요 |

### #14/#15 Docs

| 항목 | 확인 내용 | 상태 |
| --- | --- | --- |
| document links | README, PRD, 구현 명세, QA, evidence 문서 링크가 유효합니다. | 문서 확인 필요 |
| final artifact paths | final Markdown과 PDF/PPT planned export path가 분리되어 있습니다. | 문서 확인 완료 |
| screenshot paths | 수동 캡처 저장 예정 경로가 문서화되어 있습니다. | 문서 확인 완료, 캡처 필요 |
| README/project index | 제출 산출물 index와 project document index가 최신 보강 문서를 가리킵니다. | 문서 확인 완료 |

## 루브릭별 최종 확인

| 루브릭 | 최종 확인 포인트 | 상태 |
| --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 사용자-facing CRUD 결과 상태와 backend-only admin 범위가 문서화되어야 합니다. | 문서 확인 완료, live QA 필요 |
| #02 목록 조회·검색·정렬 | HTTP 테스트와 화면 query param 연결을 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #03 상세 정보 조회 | invalid ID에서 blank screen이 없어야 합니다. | 자동/문서 확인 완료, live QA 필요 |
| #04 리뷰 작성·조회 | 리뷰 create/list/edit/delete와 aggregate를 확인합니다. | 구현 완료, live QA 필요 |
| #05 로그인·로그아웃 | token 기반 로그인/로그아웃과 Settings 세션 종료 안내를 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #06 회원가입 | 이메일 중복 확인, 중복 이메일/닉네임, 비밀번호 정책 메시지를 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #07 프로필 관리 | 프로필 수정과 이미지 업로드 Deferred 안내를 확인합니다. | 구현/문서 확인 완료 |
| #08 찜·즐겨찾기 | 좋아요와 방문 상태가 마이페이지에 반영되는지 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #09 팔로우·팔로잉 | 최소 vertical slice와 live QA 필요 상태가 문서화되어야 합니다. | 구현/문서 확인 완료, live QA 필요 |
| #10 게시판 CRUD | 게시글 목록/상세/작성/수정/삭제를 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #11 댓글 CRUD | 댓글 작성/수정/삭제/1단계 답글 owner UI를 확인합니다. | 구현 완료, live QA 필요 |
| #12 계획·챌린지 관리 | route save/edit/detail 오류 복구를 확인합니다. | 자동/문서 확인 완료, live QA 필요 |
| #13 생성형 AI 활용 기능 | 화면의 AI 참고 조건 패널과 deterministic behavior, Week 4 RAG/OpenAI 계획을 설명할 수 있어야 합니다. | 구현/문서 확인 완료 |
| #14 설계·기획 문서 | API, ERD, 기능 범위, 증거 맵 위치를 제출물에 연결합니다. | 문서 index 추가 완료 |
| #15 제출물 완성도 | 최종 보고서, 발표 개요, 데이터셋, 외부 API/AI, demo scenario가 준비되어야 합니다. | 문서 확인 완료 |
