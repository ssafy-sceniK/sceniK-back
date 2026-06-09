# 인증·프로필 기능 증거

생성일: 2026-06-09

루브릭 #05 로그인·로그아웃, #06 회원가입, #07 프로필 관리 항목의 구현 근거를 정리합니다.

## 구현 상태

| 기능 | 상태 | 근거 |
| --- | --- | --- |
| 회원가입 | 완료 | `POST /api/auth/signup`, signup view/store |
| 이메일 중복 확인 | 완료 | `GET /api/auth/check-email?email=`, signup view |
| 로그인 | 완료 | `POST /api/auth/login`, access token 저장 |
| 로그아웃 | 완료 | `POST /api/auth/logout`, MyPage/Settings logout, Settings 세션 관리 |
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
| `frontend/src/views/SignupView.vue` | 회원가입, 이메일 중복 확인, 비밀번호 정책 안내 |
| `frontend/src/views/SettingsView.vue` | 세션 관리, 로그아웃 안내, 로그아웃 결과 상태 |
| `frontend/src/views/MyPageView.vue` | 사용자 정보, 로그아웃 버튼, 활동 요약 |
| `frontend/src/views/MyProfileEditView.vue` | 닉네임, 이메일 read-only, 소개, 취향 수정, 이미지 로컬 미리보기, 업로드 보류 안내 |

## QA 포인트

- 로그아웃 후 token과 사용자 상태가 비워져야 합니다.
- Settings의 `세션 관리` 섹션에서 현재 로그인 계정, 세션 종료 안내, 로그아웃 결과 상태가 표시되어야 합니다.
- 회원가입 전 `중복 확인`으로 이메일 사용 가능 여부를 확인해야 합니다.
- 이메일을 수정하면 이전 중복 확인 결과가 무효화되어야 합니다.
- 비밀번호 정책은 backend와 동일하게 `8자 이상`을 안내해야 합니다.
- 만료/누락 token이면 로그인 필요 메시지가 보여야 합니다.
- 프로필 저장은 닉네임 2자 이상 검증 후 수행해야 합니다.
- 이미지 선택은 로컬 미리보기로만 동작하고 실제 업로드를 가장하지 않아야 합니다.

## 2026-06-09 보강 사항

- `GET /api/auth/check-email?email=` endpoint를 추가해 회원가입 화면의 이메일 중복 확인이 실제 backend 상태를 사용하도록 했습니다.
- 회원가입 화면에 이메일 확인 상태, 비밀번호 정책 checklist, 비밀번호 확인 일치 여부, 성공/실패 상태 영역을 추가했습니다.
- Settings에 `세션 관리` 섹션을 추가해 로그아웃하면 이 기기에서 세션이 종료된다는 안내와 결과 상태를 표시합니다.

## 2026-06-09 최종 targeted pass 보강 사항

- `MyProfileEditView.vue`에 profile image preview panel과 `<input type="file" accept="image/*">`를 추가했습니다.
- 선택한 이미지는 `URL.createObjectURL` 기반 로컬 미리보기로만 표시하고, `프로필 이미지 업로드는 추후 지원 예정입니다.` 문구로 서버 업로드가 아님을 명시했습니다.
- 이메일은 read-only field로 표시하고, 저장 가능한 필드는 nickname, bio, preferences로 분리했습니다.
- 콘텐츠/지역/테마 preference chips에 `aria-pressed`를 추가해 선택 상태를 명확히 했습니다.
- `MyPageView.vue` profile header에 preference chip summary와 profile image 표시 경로를 추가했습니다.
- 저장 성공/실패/검증 실패는 `FormStatusNotice`로 화면 안에 표시합니다.
