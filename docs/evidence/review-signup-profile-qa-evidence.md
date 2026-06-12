# 리뷰/회원가입/프로필 QA 증거

생성일: 2026-06-12

Rubric #04, #06, #07의 추가 피드백인 사용자 메시지, 검증 실패 안내, 저장 실패 복구 가이드를 평가자가 빠르게 확인할 수 있도록 정리합니다.

## Rubric #04 리뷰 작성/조회

### 구현 추적 경로

| 계층 | 파일 | 확인 내용 |
| --- | --- | --- |
| Controller | `src/main/java/com/scenik/controller/PlaceReviewController.java` | 리뷰 목록, 작성, 수정, 삭제, 내 리뷰 목록 |
| Service | `src/main/java/com/scenik/service/PlaceReviewService.java` | 빈 내용/평점 범위 검증, 중복 리뷰 방지, 작성자 권한 검증 |
| Mapper | `src/main/resources/mapper/PlaceReviewMapper.xml` | 리뷰 CRUD SQL |
| Place Aggregation | `src/main/resources/mapper/PlaceMapper.xml` | `review_count`, `average_rating` 집계 |
| DTO | `src/main/java/com/scenik/dto/ReviewDtos.java` | 요청/응답 구조 |
| Frontend API | `frontend/src/api/reviewApi.js` | 리뷰 API 호출 |
| Frontend UI | `frontend/src/views/PlaceDetailView.vue` | 리뷰 목록, 작성/수정/삭제, 평균/개수 표시, 오류 메시지 |

### 오류 코드와 프론트 메시지

| 상황 | HTTP/Code | 사용자 메시지 정책 |
| --- | --- | --- |
| 비로그인 작성/수정/삭제 | 401 `AUTH_REQUIRED` | 로그인 후 다시 시도하도록 안내 |
| 다른 사용자 리뷰 수정/삭제 | 403 `FORBIDDEN` | 본인이 작성한 리뷰만 수정/삭제 가능하다고 안내 |
| 없는 장소 또는 리뷰 | 404 `NOT_FOUND` | 목록 새로고침 후 재시도 안내 |
| 같은 장소 중복 리뷰 | 409 `DUPLICATE_ACTION` | 중복 작성 대신 내 리뷰 수정/삭제 안내 |
| 빈 리뷰 내용 | 400 `VALIDATION_ERROR` | 리뷰 내용을 입력하라고 안내 |
| 평점 범위 오류 | 400 `VALIDATION_ERROR` | 0~5점 범위 안내 |

### QA 체크리스트

1. 로그인 후 장소 상세에서 리뷰를 작성하면 목록 최상단에 표시되는지 확인합니다.
2. 작성 성공 후 리뷰 개수와 평균 평점 문구가 갱신되는지 확인합니다.
3. 같은 사용자로 같은 장소에 두 번째 리뷰 작성을 시도해 409 안내가 표시되는지 확인합니다.
4. 작성자 계정으로 리뷰 수정/삭제가 성공하는지 확인합니다.
5. 다른 사용자 계정으로 리뷰 수정/삭제를 시도해 403 안내가 표시되는지 확인합니다.
6. 빈 내용 또는 0~5 범위를 벗어난 평점이 프론트에서 차단되는지 확인합니다.
7. 리뷰를 모두 삭제했을 때 리뷰 개수가 0으로 표시되는지 확인합니다.

## Rubric #06 회원가입

### 구현 추적 경로

| 계층 | 파일 | 확인 내용 |
| --- | --- | --- |
| Controller | `src/main/java/com/scenik/controller/AuthController.java` | `POST /api/auth/signup`, `GET /api/auth/check-email` |
| Service | `src/main/java/com/scenik/service/AuthService.java` | 이메일/닉네임 중복 검사, 사용자 생성 |
| Password | `src/main/java/com/scenik/service/PasswordService.java` | PBKDF2 기반 password hash/verify |
| Mapper | `src/main/resources/mapper/UserMapper.xml` | 사용자 insert/select/update |
| Schema | `src/main/resources/schema.sql` | email/nickname unique 제약 |
| Exception | `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 400 fieldErrors, 409 duplicate 응답 |
| Frontend UI | `frontend/src/views/SignupView.vue`, `frontend/src/components/auth/SignupValidationPanel.vue` | 중복 확인, 비밀번호 정책, fieldErrors 표시 |

### QA 체크리스트

1. 정상 이메일, 8자 이상 비밀번호, 닉네임으로 회원가입이 성공하는지 확인합니다.
2. 이미 존재하는 이메일로 가입해 409 `DUPLICATE_EMAIL` 메시지가 표시되는지 확인합니다.
3. 이미 존재하는 닉네임으로 가입해 409 `DUPLICATE_NICKNAME` 메시지가 표시되는지 확인합니다.
4. 8자 미만 비밀번호로 가입해 400 `VALIDATION_ERROR`와 fieldErrors가 표시되는지 확인합니다.
5. 빈 필수 필드로 가입해 400 `VALIDATION_ERROR`가 필드별로 표시되는지 확인합니다.
6. DB에 저장된 `password_hash`가 원문 비밀번호가 아니라 `pbkdf2_sha256$...` 형식인지 확인합니다.

### 선택 확장 범위

- 이메일 인증은 현재 MVP 필수 범위가 아니며 구현하지 않았습니다.
- 비밀번호 재설정은 현재 MVP 필수 범위가 아니며 구현하지 않았습니다.

## Rubric #07 프로필 관리

### 구현 추적 경로

| 계층 | 파일 | 확인 내용 |
| --- | --- | --- |
| Controller | `src/main/java/com/scenik/controller/AuthController.java` | `GET /api/users/me`, `PATCH /api/users/me` |
| Service | `src/main/java/com/scenik/service/AuthService.java` | 현재 사용자 조회, nickname/preferences 저장 |
| Mapper | `src/main/resources/mapper/UserMapper.xml` | profile update, OAuth profile image update |
| Frontend API | `frontend/src/api/authApi.js` | `fetchMe`, `updateMe` |
| Frontend State | `frontend/src/stores/uiStore.js` | 저장 성공 시 서버 응답으로 currentUser 동기화, 실패 메시지 보존 |
| Frontend UI | `frontend/src/views/MyProfileEditView.vue` | 저장 실패 복구 안내, 이미지 미리보기 안내 |

### 저장 실패 복구 정책

| 상황 | 사용자 안내 |
| --- | --- |
| 네트워크/서버 오류 | 입력한 내용은 화면에 유지되며 연결 상태 확인 후 다시 저장하도록 안내 |
| 닉네임 검증 실패 | 2자 이상 입력하도록 안내 |
| 토큰 만료/누락 | 로그인 후 다시 시도하도록 안내 |
| 이미지 선택 | 현재 서버 업로드가 아니라 로컬 미리보기만 지원한다고 안내 |
| 저장 성공 | 서버 응답 기준으로 `currentUser`를 갱신하고 My Page로 이동 |

### QA 체크리스트

1. 로그인 후 프로필 편집 화면에서 현재 사용자 정보가 표시되는지 확인합니다.
2. 닉네임을 1자로 입력하면 저장이 차단되고 안내가 표시되는지 확인합니다.
3. 취향/지역/테마를 일부만 변경해도 저장 후 서버 응답으로 상태가 갱신되는지 확인합니다.
4. 저장 실패 상황에서 입력값이 화면에 유지되고 재시도 안내가 표시되는지 확인합니다.
5. 이미지 파일 선택 시 로컬 미리보기만 표시되고 서버 업로드 보류 안내가 보이는지 확인합니다.
6. OAuth 프로필 이미지는 backend가 제공한 `profileImageUrl`을 표시하고, 편집 화면에서 선택한 파일은 미리보기로만 동작하는지 확인합니다.

## 실행 증거 상태

이번 패스에서는 새 브라우저 스크린샷이나 HTTP 런타임 응답 캡처를 생성하지 않았습니다. 대신 프론트 메시지/상태 처리를 보강했고, 재현 요청은 `api-tests/rubric-04-07-edge-qa.http`에 추가했습니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 리뷰: 생성, 중복 차단, 비작성자 수정 차단, 작성자 수정, 목록 조회, 삭제를 실제 API로 확인했습니다.
- 회원가입: 성공, 중복 이메일, 중복 닉네임, 약한 비밀번호를 실제 API로 확인했습니다.
- 프로필: 조회, 수정, 빈 닉네임 검증 오류를 실제 API로 확인했습니다.
- 비밀번호 hash 값 자체는 보안상 문서에 노출하지 않았고, 로그인 성공/실패 흐름으로 저장된 비밀번호 검증 동작을 확인했습니다.
