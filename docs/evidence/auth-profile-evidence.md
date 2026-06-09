# 인증·프로필 기능 증거

생성일: 2026-06-09

루브릭 #05 로그인·로그아웃, #06 회원가입, #07 프로필 관리 항목의 구현 근거를 정리합니다.

## 구현 상태

| 기능 | 상태 | 근거 |
| --- | --- | --- |
| 회원가입 | 완료 | `POST /api/auth/signup`, signup view/store |
| 로그인 | 완료 | `POST /api/auth/login`, access token 저장 |
| 로그아웃 | 완료 | `POST /api/auth/logout`, MyPage/Settings logout |
| 사용자 조회 | 완료 | `GET /api/users/me` |
| 프로필 수정 | 완료 | `PATCH /api/users/me` |
| 취향 저장 | 완료 | `preferences` 배열 저장 |
| 프로필 이미지 업로드 | Deferred | 화면에 “프로필 이미지는 추후 지원 예정입니다.” 안내 표시 |

## Backend Evidence

| 파일 | 설명 |
| --- | --- |
| `backend/src/main/java/com/scenik/controller/AuthController.java` | auth endpoint |
| `backend/src/main/java/com/scenik/service/AuthService.java` | signup/login/logout/updateMe business logic |
| `backend/src/main/resources/mapper/UserMapper.xml` | user insert/update/select |
| `backend/src/main/resources/mapper/AuthTokenMapper.xml` | token hash 저장과 revoke |
| `backend/src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 인증/검증 오류 한국어 메시지 |

## Frontend Evidence

| 파일 | 설명 |
| --- | --- |
| `frontend/src/api/authApi.js` | auth API adapter |
| `frontend/src/stores/uiStore.js` | login, logout, session hydration, profile update |
| `frontend/src/views/LoginView.vue` | 로그인 화면 |
| `frontend/src/views/SignupView.vue` | 회원가입 화면 |
| `frontend/src/views/MyPageView.vue` | 사용자 정보, 로그아웃 버튼, 활동 요약 |
| `frontend/src/views/MyProfileEditView.vue` | 닉네임, 소개, 취향 수정, 이미지 업로드 보류 안내 |

## QA 포인트

- 로그아웃 후 token과 사용자 상태가 비워져야 합니다.
- 만료/누락 token이면 로그인 필요 메시지가 보여야 합니다.
- 프로필 저장은 닉네임 2자 이상 검증 후 수행해야 합니다.
- 이미지 업로드 버튼은 실제 업로드를 가장하지 않고 추후 지원 안내만 표시합니다.
