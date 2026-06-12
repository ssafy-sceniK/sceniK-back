# 최종 런타임 루브릭 QA 근거

생성 시각: 2026-06-12 13:45~13:52 KST  
검증 환경: Spring Boot `http://localhost:8080`, Vite `http://localhost:5173`, MySQL `MYSQL84`  
검증 방식: Node `fetch` 기반 실제 HTTP 요청, MySQL `EXPLAIN`, headless Chrome DOM 검증  
원칙: 실제 응답만 기록하며, 실행하지 않은 결과나 스크린샷은 포함하지 않습니다.

## 실행 요약

| 범위 | 결과 | 비고 |
| --- | --- | --- |
| API 런타임 스모크 | 1차 80건 중 78건 PASS, 2건 보정 후 PASS | 실패 1건은 잘못된 장소 검증 페이로드, 1건은 로그아웃 기대 상태 코드 오기 |
| 임시 데이터 정리 | PASS | 콘텐츠, 장소, 리뷰, 게시글, 댓글, 코스, 좋아요, 방문 상태, 팔로우는 삭제/해제 확인 |
| 프론트 댓글 DOM 검증 | PASS | 댓글/답글/입력창/인라인 액션/댓글 버튼 포커스 확인 |
| MySQL `EXPLAIN` | PARTIAL | 팔로우/댓글은 인덱스 사용, 콘텐츠/장소 대표 검색은 현재 데이터 규모에서 full scan/filesort 확인 |

QA 회원 계정은 회원 탈퇴 API가 없어 DB에 남습니다. 기능 데이터는 삭제 시나리오 이후 정리했습니다.

## 루브릭별 실제 결과

| Rubric | 검증 항목 | 실제 결과 | 근거 |
| --- | --- | --- | --- |
| #01 CRUD | 공개 콘텐츠/장소 조회 | `GET /api/contents`, `GET /api/places` 모두 `200 SUCCESS` | `ContentController`, `PlaceController` |
| #01 CRUD | 관리자 콘텐츠 생성/수정/삭제 | `POST 201 SUCCESS` + `Location`, `PATCH 200 SUCCESS`, `DELETE 204` | `AdminController`, `AdminService` |
| #01 CRUD | 관리자 장소 생성/수정/삭제 | `POST 201 SUCCESS` + `Location`, `PATCH 200 SUCCESS`, `DELETE 204` | `AdminController`, `AdminService` |
| #01 CRUD | 오류 계약 | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND`, `409 CONFLICT` 확인 | `GlobalExceptionHandler`, `ApiResponse` |
| #02 목록/검색/정렬/페이지 | 콘텐츠/장소/게시글 목록 | 검색 결과 없음은 `200` + 빈 배열, 잘못된 sort/size는 `400 VALIDATION_ERROR` | `ListQuerySupport`, mapper SQL |
| #03 상세 조회 | 콘텐츠/장소 상세 | 정상 상세 `200 SUCCESS`, 없는 리소스 `404 NOT_FOUND`, 관련 장소는 배열로 반환 | `ContentController`, `PlaceController` |
| #04 리뷰 | 생성/중복/권한/수정/삭제 | 생성 `200`, 중복 `409 DUPLICATE_ACTION`, 비작성자 수정 `403`, 삭제 `204` | `PlaceReviewController`, `PlaceReviewService` |
| #05 로그인/로그아웃 | 로그인/refresh/logout/토큰 재사용 | 로그인 `200 SUCCESS`, refresh `200`, logout `200 SUCCESS`, 로그아웃 토큰 재사용 `401 TOKEN_EXPIRED` | `AuthController`, `AuthService` |
| #06 회원가입 | 성공/중복/검증 | 회원가입 `200 SUCCESS`, 중복 이메일 `409 DUPLICATE_EMAIL`, 중복 닉네임 `409 DUPLICATE_NICKNAME`, 약한 비밀번호 `400 VALIDATION_ERROR` | `AuthController`, `AuthDtos` |
| #07 프로필 | 조회/수정/검증 | `GET /api/users/me` 성공, `PATCH /api/users/me` 성공, 빈 닉네임 `400 VALIDATION_ERROR` | `AuthController`, `AuthService` |
| #08 좋아요/방문 상태 | 멱등성/격리/검증 | 같은 사용자 중복 좋아요 `200 SUCCESS`, 취소 `200 SUCCESS`, 다른 사용자 목록 분리, 잘못된 방문 상태 `400 INVALID_REQUEST` | `LikeVisitController` |
| #09 팔로우 | 생성/중복/페이지 | 팔로우 `200 SUCCESS`, 중복 `409 DUPLICATE_ACTION`, 팔로워/팔로잉 목록 `X-Total-Count` 확인 | `FollowController`, `FollowMapper.xml` |
| #10 게시판 CRUD | 생성/상세/수정 권한/삭제 | 작성자 CRUD 성공, 비작성자 수정 `403 FORBIDDEN`, 목록 검색/정렬 `200` | `CommunityController`, `CommunityService` |
| #11 댓글 CRUD | 댓글/답글/권한/카운트 | 댓글+답글 생성 후 목록 `X-Total-Count: 2`, 비작성자 수정 `403`, 작성자 수정 성공, 삭제 `204` | `CommunityController`, `CommunityMapper.xml` |
| #12 코스 관리 | 저장/목록/상세/장소 추가/순서 변경/권한/삭제 | 저장 `200`, 목록 `X-Total-Count`, 장소 추가/순서 변경 성공, 비소유자 상세 `403`, 삭제 `204` | `RouteController`, `RouteService` |

## 대표 응답 계약

| 시나리오 | Endpoint | 실제 상태 | 실제 code/message |
| --- | --- | --- | --- |
| 관리자 콘텐츠 생성 | `POST /api/admin/contents` | `201 Created` | `SUCCESS / 생성되었습니다.` |
| 관리자 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | `204 No Content` | 본문 없음 |
| 관리자 장소 필수값 누락 | `POST /api/admin/places` | `400 Bad Request` | `VALIDATION_ERROR / 이름을 입력해 주세요.` |
| 관리자 API 미인증 | `POST /api/admin/contents` | `401 Unauthorized` | `AUTH_REQUIRED / 로그인이 필요합니다.` |
| 일반 사용자 관리자 API 접근 | `POST /api/admin/contents` | `403 Forbidden` | `FORBIDDEN / 관리자 권한이 필요합니다.` |
| 없는 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | `404 Not Found` | `NOT_FOUND / 존재하지 않는 콘텐츠입니다.` |
| 중복 콘텐츠 생성 | `POST /api/admin/contents` | `409 Conflict` | `CONFLICT / 이미 등록된 콘텐츠입니다.` |
| 로그아웃 | `POST /api/auth/logout` | `200 OK` | `SUCCESS / 로그아웃되었습니다.` |
| 로그아웃 토큰 재사용 | `GET /api/users/me` | `401 Unauthorized` | `TOKEN_EXPIRED / 로그인이 만료되었습니다. 다시 로그인해 주세요.` |

## #02 `EXPLAIN` 결과 요약

| 쿼리 | 실제 key | 결과 |
| --- | --- | --- |
| 콘텐츠 category + popular 정렬 | `NULL` | 현재 대표 쿼리는 full scan + filesort. 데이터 규모는 작지만 복합 인덱스 개선 여지가 있습니다. |
| 장소 region/category + name 정렬 | `NULL` | 현재 대표 쿼리는 full scan + filesort. 지역/카테고리/이름 복합 인덱스 개선 여지가 있습니다. |
| 팔로잉 목록 | `uq_follows_pair`, `PRIMARY` | `follows`와 `users` JOIN에서 인덱스 사용 확인 |
| 댓글 목록 | `idx_comments_post_id`, `PRIMARY` | postId 필터와 user JOIN에서 인덱스 사용 확인 |

## #11 댓글 화면 DOM 검증

검증 대상: `frontend/src/views/CommunityView.vue`, `frontend/src/components/community/CommentThread.vue`  
검증 방식: headless Chrome에서 임시 게시글/댓글/답글 생성 후 `/community/post/{postId}` 진입

| 항목 | 실제 결과 |
| --- | --- |
| 댓글 섹션 표시 | `#community-comments` 존재, `display: block`, 높이 확인 |
| 댓글/답글 표시 | 임시 댓글과 답글 텍스트 모두 DOM에서 확인 |
| 로그인 사용자 입력창 | `input[placeholder="댓글을 입력하세요"]` 확인 |
| 작성자 인라인 액션 | `답글`, `수정`, `삭제` 버튼 확인 |
| 정책 안내 제거 | `댓글은 한 단계 답글까지 지원합니다...` 문구 없음 |
| 더보기/바텀시트 제거 | 댓글 액션 `더보기` 없음 |
| 댓글 버튼 동작 | `a[href="#community-comments"]` 클릭 후 `window.scrollY` 증가, 댓글 입력창 포커스 확인 |

실제 클릭 검증 값: 클릭 전 `sectionTop=500`, `scrollY=0`; 클릭 후 `sectionTop=196`, `scrollY=304`, active element placeholder `댓글을 입력하세요`.

## 남은 리스크

- #02는 기능적으로 통과하지만, 콘텐츠/장소 대표 검색에서 MySQL이 full scan/filesort를 선택했습니다. 데이터가 커질 경우 복합 인덱스 또는 DB 레벨 pagination/정렬 최적화가 추가 개선점입니다.
- 일부 기존 도메인 API는 전체 프로젝트 차원의 `ApiResponse` 래핑이 아니라 raw DTO/배열을 유지합니다. 이번 검증에서는 기능/상태 코드/오류 계약 관점으로 확인했습니다.
- 실제 스크린샷 파일은 생성하지 않았습니다. 대신 headless Chrome DOM 결과로 브라우저 표시 상태를 검증했습니다.
