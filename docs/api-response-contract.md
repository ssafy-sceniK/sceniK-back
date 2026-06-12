# sceniK API 응답 계약

## 적용 목적

인증, 좋아요/방문 상태, 팔로우, 콘텐츠/장소 조회, 관리자 CRUD API의 성공/오류 응답 형식을 같은 규칙으로 맞춰 프론트엔드 오류 처리와 QA 재현성을 높인다.

이번 정리는 전체 API를 한 번에 바꾸는 대규모 마이그레이션이 아니라, 예외 케이스가 자주 발생하는 다음 컨트롤러를 우선 표준화한 증분 개선이다.

- `src/main/java/com/scenik/controller/AuthController.java`
- `src/main/java/com/scenik/controller/LikeVisitController.java`
- `src/main/java/com/scenik/controller/FollowController.java`
- `src/main/java/com/scenik/controller/ContentController.java`
- `src/main/java/com/scenik/controller/PlaceController.java`
- `src/main/java/com/scenik/controller/AdminController.java`
- `src/main/java/com/scenik/exception/GlobalExceptionHandler.java`

## 성공 응답 형식

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

필드 의미:

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `success` | boolean | 성공 응답은 항상 `true` |
| `code` | string | 성공 코드는 `SUCCESS` |
| `message` | string | 사용자에게 보여줄 수 있는 한국어 메시지 |
| `data` | object/array/null | 실제 응답 데이터. 단순 액션 성공은 `null` |

## 오류 응답 형식

```json
{
  "success": false,
  "code": "AUTH_REQUIRED",
  "message": "로그인이 필요합니다.",
  "data": null,
  "fieldErrors": []
}
```

필드 의미:

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `success` | boolean | 오류 응답은 항상 `false` |
| `code` | string | 프론트엔드 분기와 QA 확인에 사용하는 오류 코드 |
| `message` | string | 사용자에게 보여줄 수 있는 한국어 오류 메시지 |
| `data` | null | 오류 응답에서는 `null` |
| `fieldErrors` | array | 입력값 검증 오류가 있을 때 필드별 메시지 제공 |

## HTTP 상태와 오류 코드 매핑

| HTTP 상태 | 대표 코드 | 의미 | 예시 상황 |
| --- | --- | --- | --- |
| `400` | `VALIDATION_ERROR` | 입력값 검증 실패 | 필수 값 누락, 형식 오류 |
| `400` | `INVALID_REQUEST` | 요청 본문 또는 도메인 값 오류 | 잘못된 방문 상태, 잘못된 코스 값 |
| `401` | `AUTH_REQUIRED` | 인증 필요 | 토큰 누락 |
| `401` | `TOKEN_EXPIRED` | 토큰 유효성 실패 | 만료/무효 토큰, 사용자 조회 실패 |
| `403` | `FORBIDDEN` | 권한 없음 | 소유자 불일치, 관리자 권한 부족 |
| `404` | `NOT_FOUND` | 대상 리소스 없음 | 콘텐츠/장소/게시글/댓글 없음 |
| `409` | `DUPLICATE_ACTION` | 중복 액션 | 중복 팔로우, 중복 리뷰, 중복 신고 |
| `409` | `CONFLICT` | 데이터 상태 충돌 | 이미 존재하는 콘텐츠/장소 등 |
| `500` | `DB_ERROR` | DB 처리 오류 | 스키마/쿼리 오류 |
| `500` | `INTERNAL_ERROR` | 예상하지 못한 서버 오류 | 기타 서버 오류 |

## 표준화된 주요 API

### Auth

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `POST /api/auth/signup` | `AuthResponse` | 회원가입 후 토큰과 사용자 정보 반환 |
| `POST /api/auth/login` | `AuthResponse` | 로그인 후 토큰과 사용자 정보 반환 |
| `POST /api/auth/oauth/exchange` | `AuthResponse` | OAuth 코드 교환 후 토큰과 사용자 정보 반환 |
| `GET /api/auth/check-email` | `EmailAvailabilityResponse` | 이메일 사용 가능 여부 |
| `POST /api/auth/logout` | `null` | 로그아웃 성공 메시지 반환 |
| `POST /api/auth/token/refresh` | `AuthResponse` | 토큰 갱신 결과 |
| `GET /api/users/me` | `UserDto` | 현재 사용자 |
| `PATCH /api/users/me` | `UserDto` | 수정된 사용자 |

OAuth 시작 엔드포인트인 `GET /api/auth/oauth/google`, `GET /api/auth/oauth/kakao`는 브라우저 리다이렉트가 목적이므로 JSON 래퍼를 적용하지 않는다.

### 좋아요/방문 상태

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `POST /api/contents/{contentId}/likes` | `null` | 콘텐츠 좋아요 |
| `DELETE /api/contents/{contentId}/likes` | `null` | 콘텐츠 좋아요 취소 |
| `POST /api/places/{placeId}/likes` | `null` | 장소 좋아요 |
| `DELETE /api/places/{placeId}/likes` | `null` | 장소 좋아요 취소 |
| `GET /api/users/me/likes` | `LikesDto` | 좋아요 요약 |
| `GET /api/users/me/liked-contents` | `List<String>` | 좋아요한 콘텐츠 ID 목록 |
| `GET /api/users/me/liked-places` | `List<String>` | 좋아요한 장소 ID 목록 |
| `PUT /api/places/{placeId}/visit-status` | `VisitStatusDto` | 방문 상태 저장 |
| `DELETE /api/places/{placeId}/visit-status` | `null` | 방문 상태 삭제 |
| `GET /api/users/me/visit-status` | `List<VisitStatusDto>` | 내 방문 상태 목록 |

페이지네이션이 있는 목록 API는 기존 `X-Total-Count` 헤더를 유지한다.

### 팔로우

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `POST /api/users/{userId}/follow` | `FollowStatusDto` | 팔로우 |
| `DELETE /api/users/{userId}/follow` | `FollowStatusDto` | 언팔로우 |
| `GET /api/users/{userId}/followers` | `List<FollowUserDto>` | 팔로워 목록 |
| `GET /api/users/{userId}/following` | `List<FollowUserDto>` | 팔로잉 목록 |
| `GET /api/users/me/following` | `List<FollowUserDto>` | 내 팔로잉 목록 |

### 콘텐츠 조회

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `GET /api/contents` | `List<ContentDto>` | `X-Total-Count` 헤더 유지 |
| `GET /api/contents/categories` | `List<ContentCategoryDto>` | 콘텐츠 카테고리 목록 |
| `GET /api/contents/popular` | `List<ContentDto>` | 인기 콘텐츠 목록 |
| `GET /api/contents/{contentId}` | `ContentDto` | 존재하지 않으면 `404 NOT_FOUND` |
| `GET /api/contents/{contentId}/places` | `List<PlaceDto>` | 콘텐츠 관련 장소 목록 |

### 장소 조회

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `GET /api/places` | `List<PlaceDto>` | `X-Total-Count` 헤더 유지 |
| `GET /api/places/nearby` | `List<PlaceDto>` | 좌표가 없으면 빈 목록 반환 |
| `GET /api/places/{placeId}` | `PlaceDto` | 존재하지 않으면 `404 NOT_FOUND` |

### 관리자 CRUD

| API | 성공 데이터 | 비고 |
| --- | --- | --- |
| `POST /api/admin/contents` | `ContentDto` | `ROLE_ADMIN` 필요 |
| `PATCH /api/admin/contents/{contentId}` | `ContentDto` | `ROLE_ADMIN` 필요 |
| `DELETE /api/admin/contents/{contentId}` | `null` | 콘텐츠 soft delete |
| `POST /api/admin/places` | `PlaceDto` | `ROLE_ADMIN` 필요 |
| `PATCH /api/admin/places/{placeId}` | `PlaceDto` | `ROLE_ADMIN` 필요 |
| `DELETE /api/admin/places/{placeId}` | `null` | 관련 참조 정리 후 장소 삭제 |

## 응답 예시

### 로그인 성공

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "demo@scenik.test",
  "password": "password123"
}
```

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "로그인되었습니다.",
  "data": {
    "accessToken": "jwt-or-session-token",
    "refreshToken": "jwt-or-session-token",
    "user": {
      "userId": 1,
      "email": "demo@scenik.test",
      "nickname": "sceniK 사용자"
    }
  }
}
```

### 인증 누락

```json
{
  "success": false,
  "code": "AUTH_REQUIRED",
  "message": "로그인이 필요합니다.",
  "data": null,
  "fieldErrors": []
}
```

### 검증 오류

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "입력값을 확인해 주세요.",
  "data": null,
  "fieldErrors": [
    {
      "field": "email",
      "message": "올바른 이메일 형식이 아닙니다."
    }
  ]
}
```

### 팔로우 성공

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "팔로우했습니다.",
  "data": {
    "userId": 2,
    "followedByMe": true,
    "followerCount": 12,
    "followingCount": 5
  }
}
```

## 프론트엔드 처리 기준

프론트엔드는 다음 파일에서 표준 응답을 풀어 처리한다.

- `frontend/src/api/error.js`
- `frontend/src/api/http.js`
- `frontend/src/api/authApi.js`
- `frontend/src/api/engagementApi.js`
- `frontend/src/api/followApi.js`
- `frontend/src/api/contentApi.js`
- `frontend/src/api/placeApi.js`
- `frontend/src/api/adminApi.js`

처리 원칙:

1. 성공 응답은 `unwrapApiData(response.data)`로 `data` 필드를 꺼낸다.
2. 아직 표준화되지 않은 raw 응답도 그대로 지원한다.
3. 사용자 메시지는 우선순위에 따라 `fieldErrors[0].message`, `message`, HTTP 상태 기본 메시지 순으로 선택한다.
4. 토큰 갱신 응답도 표준 래퍼와 기존 raw 토큰 응답을 모두 지원한다.
5. 오류 코드 기반 세부 분기는 필요한 화면에서 `response.data.code`를 기준으로 확장한다.

## 남은 범위

아래 API들은 아직 전체 응답 래퍼 마이그레이션 대상이 아니다.

- 커뮤니티 게시글/댓글 API
- 리뷰 API
- 코스/AI 코스 API

위 API들은 기존 프론트 흐름을 우선 보존하고, 추후 동일한 계약으로 단계적으로 정리한다.
