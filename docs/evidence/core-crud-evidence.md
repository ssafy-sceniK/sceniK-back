# 핵심 데이터 CRUD 증거

생성일: 2026-06-09

루브릭 #01 핵심 데이터 관리 CRUD 항목은 사용자 화면 CRUD와 운영자 관리 CRUD를 함께 판단해야 합니다.

## 사용자 화면에서 보이는 CRUD

| 도메인 | Create | Read | Update | Delete | 근거 |
| --- | --- | --- | --- | --- | --- |
| 여행 경로 | `POST /api/routes` | `GET /api/users/me/routes`, `GET /api/routes/{routeId}` | `PATCH /api/routes/{routeId}` | `DELETE /api/routes/{routeId}` | `RouteController`, `AiResultView`, `CourseEditView` |
| 코스 장소 | `POST /api/routes/{routeId}/places` | route detail | `PATCH /api/routes/{routeId}/places/{routePlaceId}`, reorder | `DELETE /api/routes/{routeId}/places/{routePlaceId}` | `RouteController`, `routeApi.js` |
| 커뮤니티 게시글 | `POST /api/posts` | `GET /api/posts`, `GET /api/posts/{postId}` | `PATCH /api/posts/{postId}` | `DELETE /api/posts/{postId}` | `CommunityController`, `CommunityWriteView`, `CommunityView`, `uiStore.editPost/removePost` |
| 댓글 | `POST /api/posts/{postId}/comments` | `GET /api/posts/{postId}/comments` | `PATCH /api/posts/{postId}/comments/{commentId}` | `DELETE /api/posts/{postId}/comments/{commentId}` | `CommunityController`, `CommunityView` |
| 장소 리뷰 | `POST /api/places/{placeId}/reviews` | `GET /api/places/{placeId}/reviews` | `PATCH /api/places/{placeId}/reviews/{reviewId}` | `DELETE /api/places/{placeId}/reviews/{reviewId}` | `PlaceReviewController`, `PlaceDetailView`, `FormStatusNotice` |
| 사용자 프로필 | signup | `GET /api/users/me` | `PATCH /api/users/me` | Deferred | `AuthController`, profile screens |

## 운영자 관리 CRUD

| 도메인 | Endpoint | 상태 | 비고 |
| --- | --- | --- | --- |
| 콘텐츠 생성 | `POST /api/admin/contents` | Backend + Admin UI 구현 | `/admin/contents`, `ROLE_ADMIN` 필요 |
| 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | Backend + Admin UI 구현 | `/admin/contents`, `ROLE_ADMIN` 필요 |
| 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | Backend + Admin UI 구현 | soft delete, `/admin/contents`, `ROLE_ADMIN` 필요 |
| 장소 생성 | `POST /api/admin/places` | Backend + Admin UI 구현 | `/admin/places`, `ROLE_ADMIN` 필요 |
| 장소 수정 | `PATCH /api/admin/places/{placeId}` | Backend + Admin UI 구현 | `/admin/places`, `ROLE_ADMIN` 필요 |
| 장소 삭제 | `DELETE /api/admin/places/{placeId}` | Backend + Admin UI 구현 | 물리 삭제 및 관련 참조 정리, `/admin/places`, `ROLE_ADMIN` 필요 |

## 판단

일반 사용자 데모 흐름에서 필요한 CRUD는 경로, 게시글, 댓글, 리뷰, 프로필 중심으로 구현되어 있습니다. 콘텐츠/장소 원본 데이터 관리 endpoint는 backend에 구현되어 있으며, `/admin/contents`, `/admin/places` 운영자 화면에서도 실제 API로 생성·수정·삭제를 검증할 수 있습니다. Admin CUD 요청은 `AuthService.requireUser()`로 401 인증을 먼저 분리한 뒤 `AdminService.requireAdmin()`에서 `ROLE_ADMIN`을 확인하므로, 비관리자 로그인 사용자는 `403 FORBIDDEN`을 받습니다.

## Rubric #01 콘텐츠 CRUD 상세 근거

평가 피드백에서 언급된 `ContentController`는 공개 조회 API를 담당합니다. 콘텐츠 생성·수정·삭제는 같은 도메인 CRUD이지만 일반 사용자 화면이 아니라 `AdminController`의 운영자 endpoint로 분리되어 있습니다. 따라서 #01 Core Data Management 판단 시 “공개 조회 API”, “관리자 콘텐츠/장소 원본 데이터 CRUD”, “운영자 UI”, “`ROLE_ADMIN` 권한 분리”를 함께 확인합니다.

### Rubric → Evidence → Source Code 확인 경로

1. Rubric #01 핵심 데이터 관리 CRUD를 확인합니다.
2. 이 문서의 `Rubric #01 콘텐츠 CRUD 상세 근거` 섹션에서 Create/Read/Update/Delete endpoint 표를 확인합니다.
3. `구현 파일` 표에서 controller, service, DTO, mapper, exception handler 위치를 확인합니다.
4. `/admin/contents`, `/admin/places`에서 운영자 CRUD 화면을 확인합니다.
5. `api-tests/content-crud-validation.http`를 REST Client로 실행해 validation/not-found/401/403/delete 흐름을 재현합니다.
6. 실제 source code는 아래 경로에서 확인합니다.

| 확인 대상 | Source code |
| --- | --- |
| 공개 콘텐츠 Read controller | `src/main/java/com/scenik/controller/ContentController.java` |
| 콘텐츠 Create/Update/Delete controller | `src/main/java/com/scenik/controller/AdminController.java` |
| 공개 콘텐츠 조회 service | `src/main/java/com/scenik/service/ContentService.java` |
| 관리자 콘텐츠 CUD service | `src/main/java/com/scenik/service/AdminService.java` |
| 사용자 role domain/DTO | `src/main/java/com/scenik/domain/User.java`, `src/main/java/com/scenik/dto/UserDto.java` |
| 사용자 role mapper | `src/main/resources/mapper/UserMapper.xml` |
| 사용자 role schema/seed | `src/main/resources/schema.sql`, `src/main/resources/data.sql` |
| 운영자 UI | `frontend/src/views/AdminView.vue` |
| 운영자 API client | `frontend/src/api/adminApi.js` |
| Request DTO | `src/main/java/com/scenik/dto/AdminDtos.java`의 `ContentUpsertRequest` |
| Response DTO | `src/main/java/com/scenik/dto/ContentDto.java` |
| Mapper interface | `src/main/java/com/scenik/mapper/ContentMapper.java` |
| SQL mapper | `src/main/resources/mapper/ContentMapper.xml` |
| Exception handler | `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` |

### 구현 파일

| 구분 | 파일 | 역할 |
| --- | --- | --- |
| 공개 콘텐츠 조회 Controller | `src/main/java/com/scenik/controller/ContentController.java` | 목록, 카테고리, 인기 콘텐츠, 상세, 관련 장소 조회 |
| 관리자 콘텐츠 CUD Controller | `src/main/java/com/scenik/controller/AdminController.java` | `POST/PATCH/DELETE /api/admin/contents` |
| 콘텐츠 조회 Service | `src/main/java/com/scenik/service/ContentService.java` | 조회 결과를 `ContentDto`로 변환 |
| 관리자 CUD Service | `src/main/java/com/scenik/service/AdminService.java` | 인증 확인, 필수값 검증, 중복/존재 여부 처리 |
| 사용자 Role | `src/main/java/com/scenik/domain/User.java`, `src/main/java/com/scenik/dto/UserDto.java`, `src/main/resources/mapper/UserMapper.xml` | `ROLE_USER` 기본값과 `ROLE_ADMIN` 응답 노출 |
| Role 저장소 | `src/main/resources/schema.sql`의 `user_roles`, `src/main/resources/data.sql`의 admin seed | demo admin 계정과 역할 부여 |
| 운영자 화면 | `frontend/src/views/AdminView.vue` | `/admin/contents`, `/admin/places`에서 실제 CRUD 요청 수행 |
| 운영자 API client | `frontend/src/api/adminApi.js` | `/api/admin/contents`, `/api/admin/places` 호출 |
| Request DTO | `src/main/java/com/scenik/dto/AdminDtos.java` | `ContentUpsertRequest` |
| Response DTO | `src/main/java/com/scenik/dto/ContentDto.java` | 콘텐츠 응답 표준 shape |
| Mapper | `src/main/java/com/scenik/mapper/ContentMapper.java` | MyBatis mapper interface |
| SQL Mapper | `src/main/resources/mapper/ContentMapper.xml` | select/insert/update/softDelete SQL |
| 오류 응답 | `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 한국어 error code/message 매핑 |

### 콘텐츠 CRUD endpoint

| 동작 | Method | Endpoint | 구현 위치 | 인증 | 현재 상태 |
| --- | --- | --- | --- | --- | --- |
| 목록 조회 | GET | `/api/contents` | `ContentController.list` | 선택 | 구현 완료 |
| 카테고리 조회 | GET | `/api/contents/categories` | `ContentController.categories` | 불필요 | 구현 완료 |
| 인기 콘텐츠 조회 | GET | `/api/contents/popular` | `ContentController.popular` | 선택 | 구현 완료 |
| 상세 조회 | GET | `/api/contents/{contentId}` | `ContentController.detail` | 선택 | 구현 완료 |
| 관련 장소 조회 | GET | `/api/contents/{contentId}/places` | `ContentController.relatedPlaces` | 불필요 | 구현 완료 |
| 콘텐츠 생성 | POST | `/api/admin/contents` | `AdminController.createContent` | `ROLE_ADMIN` token 필요 | backend + admin UI 구현 |
| 콘텐츠 수정 | PATCH | `/api/admin/contents/{contentId}` | `AdminController.updateContent` | `ROLE_ADMIN` token 필요 | backend + admin UI 구현 |
| 콘텐츠 삭제 | DELETE | `/api/admin/contents/{contentId}` | `AdminController.deleteContent` | `ROLE_ADMIN` token 필요 | backend soft delete + admin UI 구현 |

### 장소 CRUD endpoint

| 동작 | Method | Endpoint | 구현 위치 | 인증 | 현재 상태 |
| --- | --- | --- | --- | --- | --- |
| 목록 조회 | GET | `/api/places` | `PlaceController.list` | 선택 | 구현 완료 |
| 상세 조회 | GET | `/api/places/{placeId}` | `PlaceController.detail` | 선택 | 구현 완료 |
| 장소 생성 | POST | `/api/admin/places` | `AdminController.createPlace` | `ROLE_ADMIN` token 필요 | backend + admin UI 구현 |
| 장소 수정 | PATCH | `/api/admin/places/{placeId}` | `AdminController.updatePlace` | `ROLE_ADMIN` token 필요 | backend + admin UI 구현 |
| 장소 삭제 | DELETE | `/api/admin/places/{placeId}` | `AdminController.deletePlace` | `ROLE_ADMIN` token 필요 | backend + admin UI 구현, 관련 참조 정리 후 물리 삭제 |

### Admin UI

- Route: `/admin`
- 콘텐츠 관리: `/admin/contents`
- 장소 관리: `/admin/places`
- 구현 파일: `frontend/src/views/AdminView.vue`, `frontend/src/api/adminApi.js`
- 접근 정책:
  - access token 없음: 화면에서 로그인 필요 안내
  - `ROLE_USER`: 화면에서 관리자 권한 필요 안내, backend admin API 직접 호출 시 `403 FORBIDDEN`
  - `ROLE_ADMIN`: 콘텐츠/장소 목록 조회, 생성, 수정, 삭제 가능

### Admin demo 계정

`src/main/resources/data.sql`은 로컬/평가용 admin 계정을 seed합니다. 실제 운영 secret이 아니라 Rubric #01 CRUD 검증용 demo 계정입니다.

| Email | Password | Role |
| --- | --- | --- |
| `admin@scenik.local` | `admin1234` | `ROLE_ADMIN` |

### Request DTO: `ContentUpsertRequest`

`POST /api/admin/contents`와 `PATCH /api/admin/contents/{contentId}`는 같은 request shape를 사용합니다.

```json
{
  "contentId": "C_ADMIN_QA_001",
  "title": "QA 검증 콘텐츠",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2026,
  "status": "ACTIVE",
  "description": "Rubric #01 콘텐츠 CRUD 검증용 데이터입니다.",
  "castNames": "김테스트, 이검증",
  "thumbnailUrl": "https://example.com/scenik-qa-content.jpg",
  "isPopular": false,
  "country": "Korea",
  "ottPlatform": "TVING",
  "tags": ["QA", "CRUD", "콘텐츠"]
}
```

필수 검증은 service에서 수행합니다.

- 생성 시 `contentId`가 없으면 `400 VALIDATION_ERROR`, `콘텐츠 ID를 입력해 주세요.`
- 생성/수정 시 `title`이 없으면 `400 VALIDATION_ERROR`, `제목을 입력해 주세요.`
- 생성/수정 시 `category`가 없으면 `400 VALIDATION_ERROR`, `카테고리를 입력해 주세요.`
- request body가 없으면 `400 VALIDATION_ERROR`, `콘텐츠 정보를 입력해 주세요.`
- 중복 `contentId` 생성은 `409 CONFLICT`, `이미 등록된 콘텐츠입니다.`
- 없는 `contentId` 수정/삭제는 `404 NOT_FOUND`, `존재하지 않는 콘텐츠입니다.`

### Response DTO: `ContentDto`

공개 조회와 관리자 생성/수정 응답은 모두 `ContentDto` shape를 사용합니다.

```json
{
  "contentId": "C_ADMIN_QA_001",
  "title": "QA 검증 콘텐츠",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2026,
  "status": "ACTIVE",
  "description": "Rubric #01 콘텐츠 CRUD 검증용 데이터입니다.",
  "castNames": "김테스트, 이검증",
  "thumbnailUrl": "https://example.com/scenik-qa-content.jpg",
  "isPopular": false,
  "country": "Korea",
  "ottPlatform": "TVING",
  "tags": ["QA", "CRUD", "콘텐츠"],
  "likeCount": 0,
  "viewCount": 0,
  "relatedPlaceCount": 0,
  "isLiked": false
}
```

### API request/response 예시

아래 JSON은 현재 `ContentDto`와 `ContentUpsertRequest` shape를 보여주는 현실적인 예시입니다. 실제 seed data 수량, thumbnail URL, like count는 실행 DB 상태에 따라 달라질 수 있습니다.

#### 1. 콘텐츠 목록 조회

```http
GET /api/contents?page=0&size=2&sort=popular&keyword=드라마
```

예상 응답:

```http
HTTP/1.1 200 OK
X-Total-Count: {전체 결과 수}
Content-Type: application/json
```

```json
[
  {
    "contentId": "C_DRAMA_001",
    "title": "선재 업고 튀어",
    "category": "드라마",
    "subcategory": "로맨스",
    "releaseYear": 2024,
    "status": "공개",
    "description": "시간을 거슬러 최애를 구하려는 팬의 타임슬립 로맨스 드라마.",
    "castNames": "변우석, 김혜윤",
    "thumbnailUrl": "https://...",
    "isPopular": true,
    "country": "Korea",
    "ottPlatform": "tvN / TVING",
    "tags": ["드라마", "로맨스"],
    "likeCount": 0,
    "viewCount": 0,
    "relatedPlaceCount": 4,
    "isLiked": false
  }
]
```

#### 2. 콘텐츠 상세 조회

```http
GET /api/contents/C_DRAMA_001
```

예상 응답:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "contentId": "C_DRAMA_001",
  "title": "선재 업고 튀어",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2024,
  "status": "공개",
  "description": "시간을 거슬러 최애를 구하려는 팬의 타임슬립 로맨스 드라마.",
  "castNames": "변우석, 김혜윤",
  "thumbnailUrl": "https://...",
  "isPopular": true,
  "country": "Korea",
  "ottPlatform": "tvN / TVING",
  "tags": ["드라마", "로맨스"],
  "likeCount": 0,
  "viewCount": 0,
  "relatedPlaceCount": 4,
  "isLiked": false
}
```

#### 3. 콘텐츠 생성

```http
POST /api/admin/contents
Authorization: Bearer {ROLE_ADMIN accessToken}
Content-Type: application/json
```

```json
{
  "contentId": "C_ADMIN_QA_001",
  "title": "QA 검증 콘텐츠",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2026,
  "status": "ACTIVE",
  "description": "Rubric #01 콘텐츠 CRUD 검증용 데이터입니다.",
  "castNames": "김테스트, 이검증",
  "thumbnailUrl": "https://example.com/scenik-qa-content.jpg",
  "isPopular": false,
  "country": "Korea",
  "ottPlatform": "TVING",
  "tags": ["QA", "CRUD", "콘텐츠"]
}
```

현재 구현상 예상 응답은 `201 Created`가 아니라 `200 OK`입니다.

```json
{
  "contentId": "C_ADMIN_QA_001",
  "title": "QA 검증 콘텐츠",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2026,
  "status": "ACTIVE",
  "description": "Rubric #01 콘텐츠 CRUD 검증용 데이터입니다.",
  "castNames": "김테스트, 이검증",
  "thumbnailUrl": "https://example.com/scenik-qa-content.jpg",
  "isPopular": false,
  "country": "Korea",
  "ottPlatform": "TVING",
  "tags": ["QA", "CRUD", "콘텐츠"],
  "likeCount": 0,
  "viewCount": 0,
  "relatedPlaceCount": 0,
  "isLiked": false
}
```

#### 4. 콘텐츠 수정

```http
PATCH /api/admin/contents/C_ADMIN_QA_001
Authorization: Bearer {ROLE_ADMIN accessToken}
Content-Type: application/json
```

```json
{
  "title": "QA 검증 콘텐츠 수정본",
  "category": "드라마",
  "isPopular": true,
  "tags": ["QA", "CRUD", "수정"]
}
```

예상 응답:

```json
{
  "contentId": "C_ADMIN_QA_001",
  "title": "QA 검증 콘텐츠 수정본",
  "category": "드라마",
  "subcategory": "로맨스",
  "releaseYear": 2026,
  "status": "ACTIVE",
  "description": "Rubric #01 콘텐츠 CRUD 검증용 데이터입니다.",
  "castNames": "김테스트, 이검증",
  "thumbnailUrl": "https://example.com/scenik-qa-content.jpg",
  "isPopular": true,
  "country": "Korea",
  "ottPlatform": "TVING",
  "tags": ["QA", "CRUD", "수정"],
  "likeCount": 0,
  "viewCount": 0,
  "relatedPlaceCount": 0,
  "isLiked": false
}
```

#### 5. 콘텐츠 삭제

```http
DELETE /api/admin/contents/C_ADMIN_QA_001
Authorization: Bearer {ROLE_ADMIN accessToken}
```

예상 응답:

```http
HTTP/1.1 204 No Content
```

삭제는 물리 삭제가 아니라 `contents.status = 'DELETED'`로 바꾸는 soft delete입니다. 공개 목록과 상세 조회는 `(status IS NULL OR status != 'DELETED')` 조건으로 삭제된 콘텐츠를 제외합니다.

soft delete 후에도 같은 `contentId` row는 DB에 남으므로, 수동 smoke를 반복 실행할 때는 `C_ADMIN_QA_{{$timestamp}}`처럼 새 `contentId`를 사용합니다.

#### 6. 잘못된 정렬 기준

```http
GET /api/contents?sort=unknown
```

예상 응답:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "정렬 기준을 확인해 주세요.",
  "fieldErrors": []
}
```

#### 7. 없는 콘텐츠 상세

```http
GET /api/contents/C_NOT_EXISTS
```

예상 응답:

```json
{
  "code": "NOT_FOUND",
  "message": "존재하지 않는 콘텐츠입니다.",
  "fieldErrors": []
}
```

#### 8. 인증 누락 생성 요청

```http
POST /api/admin/contents
Content-Type: application/json
```

예상 응답:

```json
{
  "code": "UNAUTHORIZED",
  "message": "로그인이 필요합니다.",
  "fieldErrors": []
}
```

#### 9. 비관리자 생성 요청

```http
POST /api/admin/contents
Authorization: Bearer {ROLE_USER accessToken}
Content-Type: application/json
```

예상 응답:

```json
{
  "code": "FORBIDDEN",
  "message": "관리자 권한이 필요합니다.",
  "fieldErrors": []
}
```

### 현재 추가 개선 가능 항목

- Admin CRUD는 `AdminService.requireAdmin()`에서 token과 `ROLE_ADMIN`을 검사합니다. 전체 인증 아키텍처를 Spring Security filter 기반 JWT/authority 구조로 통합하는 작업은 별도 보안 고도화 범위입니다.
- `POST /api/admin/contents`는 현재 `200 OK`를 반환합니다. REST 관례상 `201 Created`와 `Location` header는 추가 개선 가능 항목입니다.
- `ContentUpsertRequest`는 Bean Validation annotation이 아니라 service의 수동 validation으로 검증합니다. evaluator에게는 `GlobalExceptionHandler`가 실제 한국어 error shape로 변환한다는 점을 함께 제시합니다.

### Execution Evidence Not Included

현재 repository에는 `content-crud-validation.http` 실행 결과의 raw response log, screenshot, 또는 캡처 파일이 포함되어 있지 않습니다. 결과를 조작하지 않기 위해 미실행 증거를 완료로 표기하지 않았습니다.

평가자는 다음 절차로 동일한 결과를 재현할 수 있습니다.

1. backend를 실행합니다.
2. `api-tests/auth-flow.http`의 `Login with admin credentials for CRUD` 요청을 실행합니다.
3. admin 로그인 응답의 `accessToken`을 복사합니다.
4. `api-tests/content-crud-validation.http`의 `@accessToken`에 붙여 넣습니다.
5. 비관리자 403까지 확인하려면 `Signup with valid password` 또는 `Login with valid credentials` 요청으로 일반 사용자 token을 발급하고 `@userAccessToken`에 붙여 넣습니다.
6. `content-crud-validation.http`의 요청을 순서대로 실행합니다.
7. status code, `ContentDto`/`PlaceDto` body, `VALIDATION_ERROR`, `NOT_FOUND`, `UNAUTHORIZED`, `FORBIDDEN`, `CONFLICT` 응답을 확인합니다.

### 평가자용 수동 QA 체크리스트

아래 체크리스트는 빠른 평가용입니다. admin token은 `api-tests/auth-flow.http`의 `Login with admin credentials for CRUD`에서 발급하고, 상세 요청 payload는 `api-tests/content-crud-validation.http`를 기준으로 합니다.

| 단계 | 요청 | 기대 결과 |
| --- | --- | --- |
| 1 | `GET /api/contents?page=0&size=5&sort=popular` | `200 OK`, `X-Total-Count`, `ContentDto[]` |
| 2 | `GET /api/contents/{contentId}`와 `GET /api/contents/C_NOT_EXISTS` | 정상 ID는 `200 OK`, 없는 ID는 `404 NOT_FOUND` |
| 3 | `POST /api/admin/contents` without token | `401 UNAUTHORIZED`, `로그인이 필요합니다.` |
| 4 | `POST /api/admin/contents` with `ROLE_USER` token | `403 FORBIDDEN`, `관리자 권한이 필요합니다.` |
| 5 | `POST /api/admin/contents` with `ROLE_ADMIN` token | `200 OK`, 생성된 `ContentDto` |
| 6 | 같은 `contentId` 재생성과 `title/category` 누락 요청 | `409 CONFLICT`, `400 VALIDATION_ERROR` |
| 7 | `PATCH /api/admin/contents/{contentId}`와 `DELETE /api/admin/contents/{contentId}` | 수정 성공 후 `204 No Content`, soft delete |
| 8 | `/admin/contents`, `/admin/places` 브라우저 UI | admin 계정은 CRUD 가능, 비관리자 계정은 권한 필요 안내 |

수동 실행용 REST Client 파일은 `api-tests/content-crud-validation.http`에 추가했습니다. 실제 access token은 로컬 실행 시에만 붙여 넣고 repository에는 커밋하지 않습니다.

## 2026-06-09 보강 사항

- `frontend/src/components/common/FormStatusNotice.vue`를 추가해 create/update/delete 결과가 toast에만 의존하지 않고 화면 안에 남도록 했습니다.
- 커뮤니티 글 작성 화면은 작성 중, 작성 성공, 작성 실패, 입력 검증 실패 상태를 명시합니다.
- 커뮤니티 게시글 상세는 작성자에게만 게시글 수정/삭제 UI를 보여주고 `PATCH /api/posts/{postId}`, `DELETE /api/posts/{postId}` 결과를 상태 영역으로 표시합니다.
- 코스 편집 화면은 장소 추가/삭제, 코스 수정 저장 성공/실패 상태를 표시합니다.
- 코스 상세 화면은 저장, 공유, 삭제 결과를 상태 영역으로 표시합니다.
- 장소 리뷰 작성/수정/삭제는 각 결과를 `FormStatusNotice`로 표시합니다.

## 2026-06-09 최종 targeted pass 보강 사항

- `frontend/src/components/course/CourseCrudPanel.vue`를 추가해 코스 상세에서 `코스 관리` 섹션, routeId, 장소 수, 저장 상태, 수정/저장/공유/삭제 action을 한곳에 표시했습니다.
- `frontend/src/components/course/CourseSaveStatusPanel.vue`를 추가해 AI 결과 화면에서 `생성된 AI 코스`, `저장 전 확인`, 저장 성공/실패 상태, 저장 후 내 코스에서 수정 가능 안내를 표시했습니다.
- `frontend/src/components/course/CourseDeleteActionSheet.vue`를 추가해 저장 코스 삭제 confirmation과 삭제 중 상태를 명시했습니다.
- `CourseEditView.vue`에 코스 제목 field, 저장 방식 안내, 장소 수 validation, `변경사항 저장`, `취소`, 저장 중/성공/실패 상태를 보강했습니다.
- `CourseView.vue`에 생성한 코스가 저장 후 마이페이지에서 수정·공유·삭제 가능하다는 안내를 추가했습니다.

## 2026-06-11 Rubric #01 10점 보강 사항

- `user_roles` table과 `ROLE_ADMIN` demo 계정을 추가해 admin CRUD 권한을 일반 로그인 사용자와 분리했습니다.
- `AdminService.requireAdmin()`에서 token 누락/만료는 `401`, 로그인했지만 admin이 아닌 사용자는 `403`으로 분리합니다.
- `frontend/src/views/AdminView.vue`와 `frontend/src/api/adminApi.js`를 추가해 `/admin/contents`, `/admin/places`에서 실제 콘텐츠/장소 CRUD 요청을 수행합니다.
- `api-tests/content-crud-validation.http`에 콘텐츠 CRUD, 장소 CRUD, 401, 403, validation, not-found, delete 후 조회 확인 흐름을 포함했습니다.
- `frontend/docs/qa/admin-crud-ui.md`에 관리자 화면 수동 검증 절차를 추가했습니다.

## 남은 한계

- Admin CRUD는 기존 token 저장소와 `user_roles` 기반 role 확인으로 보호됩니다. 전체 API 인증을 Spring Security filter/JWT authority 체계로 통합하는 작업은 Rubric #05 보안 고도화 범위입니다.
- 위 CRUD 상태는 build/smoke와 HTTP 파일로 검증 가능하며, 실제 브라우저 live QA는 `/admin/contents`, `/admin/places`에서 admin 계정으로 수동 확인해야 합니다.
