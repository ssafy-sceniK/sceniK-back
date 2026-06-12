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

### Traceability Map

Rubric #01은 아래 순서로 바로 추적할 수 있습니다.

| 단계 | 확인 위치 | 평가자가 확인할 내용 |
| --- | --- | --- |
| Rubric | Rubric #01 Core Data Management CRUD | 콘텐츠/장소 원본 데이터 Create, Read, Update, Delete 완성도 |
| Evidence | `backend/docs/evidence/core-crud-evidence.md` | CRUD endpoint, 응답 계약, validation, 401/403/404/409 처리 근거 |
| API contract | `backend/docs/api-response-contract.md` | `ApiResponse<T>` 성공 응답과 `GlobalExceptionHandler.ErrorResponse` 오류 응답 표준 |
| HTTP QA | `backend/api-tests/content-crud-validation.http` | 관리자 token 발급 후 C/R/U/D, validation, not-found, 권한 오류 재현 순서 |
| Controller | `src/main/java/com/scenik/controller/ContentController.java` | 공개 콘텐츠 Read endpoint가 `ApiResponse`로 응답하는지 확인 |
| Controller | `src/main/java/com/scenik/controller/PlaceController.java` | 공개 장소 Read endpoint가 `ApiResponse`로 응답하는지 확인 |
| Controller | `src/main/java/com/scenik/controller/AdminController.java` | 콘텐츠/장소 Create/Update는 `ApiResponse`, Delete는 `204 No Content`로 응답하는지 확인 |
| Service | `src/main/java/com/scenik/service/ContentService.java` | 공개 조회 결과 변환과 soft-deleted 콘텐츠 제외 흐름 |
| Service | `src/main/java/com/scenik/service/PlaceService.java` | 장소 목록, 주변 장소, 상세 조회 결과 변환 |
| Service | `src/main/java/com/scenik/service/AdminService.java` | `ROLE_ADMIN` 확인, 필수값/길이 검증, 중복/존재 여부 처리 |
| DTO | `src/main/java/com/scenik/dto/ApiResponse.java` | 성공 응답 공통 shape: `success`, `code`, `message`, `data` |
| DTO | `src/main/java/com/scenik/dto/AdminDtos.java` | `ContentUpsertRequest`, `PlaceUpsertRequest` 요청 shape |
| DTO | `src/main/java/com/scenik/dto/ContentDto.java`, `src/main/java/com/scenik/dto/PlaceDto.java` | 콘텐츠/장소 응답 data shape |
| Exception | `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | `VALIDATION_ERROR`, `AUTH_REQUIRED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT` 매핑 |
| Mapper | `src/main/resources/mapper/ContentMapper.xml`, `src/main/resources/mapper/PlaceMapper.xml` | insert/update/delete/select SQL과 soft delete/참조 정리 동작 |
| Frontend | `frontend/src/api/adminApi.js`, `frontend/src/api/contentApi.js`, `frontend/src/api/placeApi.js` | 표준 `ApiResponse`의 `data` unwrap 처리 |
| Admin UI | `frontend/src/views/AdminView.vue` | `/admin/contents`, `/admin/places` 브라우저 CRUD 검증 화면 |

### CRUD 일관성 매트릭스

아래 표는 Rubric #01 평가자가 모든 CRUD 관련 endpoint의 응답 계약과 검증 흐름을 한눈에 확인하기 위한 최종 점검표입니다.

| Endpoint | CRUD Type | Success Contract | Error Contract | Validation | Source |
| --- | --- | --- | --- | --- | --- |
| `GET /api/contents` | Read List | `200 OK`, `ApiResponse<List<ContentDto>>`, `X-Total-Count` | `GlobalExceptionHandler.ErrorResponse` | `sort`, `page`, `size` query 검증 | `ContentController.list`, `ListQuerySupport` |
| `GET /api/contents/categories` | Read List | `200 OK`, `ApiResponse<List<ContentCategoryDto>>` | `GlobalExceptionHandler.ErrorResponse` | 별도 입력값 없음 | `ContentController.categories` |
| `GET /api/contents/popular` | Read List | `200 OK`, `ApiResponse<List<ContentDto>>` | `GlobalExceptionHandler.ErrorResponse` | 선택적 token만 처리 | `ContentController.popular` |
| `GET /api/contents/{contentId}` | Read Detail | `200 OK`, `ApiResponse<ContentDto>` | `404 NOT_FOUND`, `success:false`, `data:null` | 존재하지 않는 `contentId` 처리 | `ContentController.detail`, `ContentService.getContent` |
| `GET /api/contents/{contentId}/places` | Related Read | `200 OK`, `ApiResponse<List<PlaceDto>>` | `GlobalExceptionHandler.ErrorResponse` | `contentId` 기준 관련 장소 조회 | `ContentController.relatedPlaces`, `ContentService.getRelatedPlaces` |
| `GET /api/places` | Read List | `200 OK`, `ApiResponse<List<PlaceDto>>`, `X-Total-Count` | `GlobalExceptionHandler.ErrorResponse` | `sort`, `page`, `size` query 검증 | `PlaceController.list`, `ListQuerySupport` |
| `GET /api/places/nearby` | Read List | `200 OK`, `ApiResponse<List<PlaceDto>>` | `GlobalExceptionHandler.ErrorResponse` | 좌표 누락 시 빈 목록, `radiusKm`, `limit` 정규화 | `PlaceController.nearby`, `PlaceService.getNearbyPlaces` |
| `GET /api/places/{placeId}` | Read Detail | `200 OK`, `ApiResponse<PlaceDto>` | `404 NOT_FOUND`, `success:false`, `data:null` | 존재하지 않는 `placeId` 처리 | `PlaceController.detail`, `PlaceService.getPlace` |
| `POST /api/admin/contents` | Create | `201 Created`, `Location: /api/contents/{contentId}`, `ApiResponse<ContentDto>` | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `409 CONFLICT` | `contentId`, `title`, `category` 필수 및 길이 검증 | `AdminController.createContent`, `AdminService.createContent` |
| `PATCH /api/admin/contents/{contentId}` | Update | `200 OK`, `ApiResponse<ContentDto>` | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND` | path `contentId` 존재/길이, `title`, `category` 필수 및 길이 검증 | `AdminController.updateContent`, `AdminService.updateContent` |
| `DELETE /api/admin/contents/{contentId}` | Delete | `204 No Content`, body 없음 | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND` | path `contentId` 존재/길이 검증 | `AdminController.deleteContent`, `AdminService.deleteContent`, `ContentMapper.softDelete` |
| `POST /api/admin/places` | Create | `201 Created`, `Location: /api/places/{placeId}`, `ApiResponse<PlaceDto>` | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `409 CONFLICT` | `placeId`, `name`, `region` 필수 및 길이 검증 | `AdminController.createPlace`, `AdminService.createPlace` |
| `PATCH /api/admin/places/{placeId}` | Update | `200 OK`, `ApiResponse<PlaceDto>` | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND` | path `placeId` 존재/길이, `name`, `region` 필수 및 길이 검증 | `AdminController.updatePlace`, `AdminService.updatePlace` |
| `DELETE /api/admin/places/{placeId}` | Delete | `204 No Content`, body 없음 | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND` | path `placeId` 존재/길이 검증 | `AdminController.deletePlace`, `AdminService.deletePlace`, `PlaceMapper.delete*References` |

관리자 upsert/delete 성공 메시지는 콘텐츠와 장소 모두 action 기준으로 통일합니다.

| 동작 | 성공 메시지 | data |
| --- | --- | --- |
| Create | `생성되었습니다.` | 생성된 `ContentDto` 또는 `PlaceDto` |
| Update | `수정되었습니다.` | 수정된 `ContentDto` 또는 `PlaceDto` |
| Delete | body 없음 | `204 No Content` |

### Consistency Verification Checklist

| 확인 항목 | 상태 | 근거 |
| --- | --- | --- |
| `ApiResponse` 성공 응답 사용 | 확인 완료 | 공개 Read와 관리자 Create/Update는 `ApiResponse`, 관리자 Delete는 `204 No Content` |
| 표준 오류 응답 사용 | 확인 완료 | `GlobalExceptionHandler.ErrorResponse`가 `success:false`, `code`, `message`, `data:null`, `fieldErrors`를 반환 |
| Validation 적용 | 확인 완료 | `AdminService`에서 필수값, 공백, 길이 초과를 `ResponseStatusException`으로 변환 |
| HTTP status와 body code 정합성 | 확인 완료 | `400 VALIDATION_ERROR`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`, `404 NOT_FOUND`, `409 CONFLICT` |
| 관리자 권한 분리 | 확인 완료 | `AuthService.requireUser()` 후 `AdminService.requireAdmin()`에서 `ROLE_ADMIN` 검사 |
| QA 시나리오 존재 | 확인 완료 | `backend/api-tests/content-crud-validation.http`에 create/read/update/delete, 401, 403, 404, 409, validation 포함 |
| Frontend 표준 응답 호환 | 확인 완료 | `frontend/src/api/contentApi.js`, `frontend/src/api/placeApi.js`, `frontend/src/api/adminApi.js`에서 `data` unwrap 처리 |
| 문서 추적성 | 확인 완료 | 이 문서의 `Traceability Map`과 `CRUD 일관성 매트릭스`에서 evidence → source code 경로 명시 |

### 발견된 예외 및 정책 결정

- 공개 콘텐츠 Read, 공개 장소 Read, 관리자 콘텐츠/장소 Create/Update는 표준 `ApiResponse` 계약으로 맞춰져 있습니다. 관리자 Delete는 HTTP semantics를 우선해 `204 No Content`를 사용합니다.
- 콘텐츠 `category`는 현재 allow-list enum이 아니라 자유 문자열 정책입니다. 따라서 “허용되지 않는 카테고리 값” 자체는 차단하지 않고, 누락/공백/길이 초과만 `VALIDATION_ERROR`로 처리합니다.
- 콘텐츠 삭제는 원본 row를 물리 삭제하지 않고 `status = 'DELETED'`로 바꾸는 soft delete입니다. 삭제 후 공개 목록/상세 조회에서는 제외되며, 관리자 반복 delete는 기존 row 확인 후 같은 soft delete를 다시 수행할 수 있습니다.

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
| 장소 Response DTO | `src/main/java/com/scenik/dto/PlaceDto.java` | 장소 응답 표준 shape |
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
| 목록 조회 | GET | `/api/places` | `PlaceController.list` | 선택 | `ApiResponse<List<PlaceDto>>` 구현 완료 |
| 주변 장소 조회 | GET | `/api/places/nearby` | `PlaceController.nearby` | 선택 | `ApiResponse<List<PlaceDto>>` 구현 완료 |
| 상세 조회 | GET | `/api/places/{placeId}` | `PlaceController.detail` | 선택 | `ApiResponse<PlaceDto>` 구현 완료 |
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
- `contentId` 64자 초과, `title` 255자 초과, `category` 80자 초과는 `400 VALIDATION_ERROR`로 응답합니다.
- request body가 없으면 `400 VALIDATION_ERROR`, `콘텐츠 정보를 입력해 주세요.`
- 중복 `contentId` 생성은 `409 CONFLICT`, `이미 등록된 콘텐츠입니다.`
- 없는 `contentId` 수정/삭제는 `404 NOT_FOUND`, `존재하지 않는 콘텐츠입니다.`

### Response DTO: `ApiResponse<ContentDto>`

공개 콘텐츠 조회와 관리자 생성/수정 응답은 공통 성공 응답 래퍼인 `ApiResponse`를 사용합니다. 관리자 삭제는 HTTP semantics를 우선해 `204 No Content`와 빈 body를 반환합니다. 실제 콘텐츠 데이터는 `data` 필드 안의 `ContentDto` shape로 제공됩니다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
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
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": [
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
}
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
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
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

예상 응답은 `201 Created`이며, `Location` header는 생성된 공개 상세 리소스 경로를 가리킵니다. 본문은 공통 성공 응답 래퍼입니다.

```http
HTTP/1.1 201 Created
Location: /api/contents/C_ADMIN_QA_001
Content-Type: application/json
```

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "생성되었습니다.",
  "data": {
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
  "success": true,
  "code": "SUCCESS",
  "message": "수정되었습니다.",
  "data": {
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
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "정렬 기준을 확인해 주세요.",
  "data": null,
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
  "success": false,
  "code": "NOT_FOUND",
  "message": "존재하지 않는 콘텐츠입니다.",
  "data": null,
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
  "success": false,
  "code": "AUTH_REQUIRED",
  "message": "로그인이 필요합니다.",
  "data": null,
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
  "success": false,
  "code": "FORBIDDEN",
  "message": "관리자 권한이 필요합니다.",
  "data": null,
  "fieldErrors": []
}
```

### 현재 추가 개선 가능 항목

- Admin CRUD는 `AdminService.requireAdmin()`에서 token과 `ROLE_ADMIN`을 검사합니다. 전체 인증 아키텍처를 Spring Security filter 기반 JWT/authority 구조로 통합하는 작업은 별도 보안 고도화 범위입니다.
- 관리자 생성 API는 REST 관례에 맞춰 `201 Created`와 `Location` header를 반환합니다.
- 관리자 삭제 API는 삭제 성공 시 `204 No Content`를 반환합니다. 성공 body가 없는 예외 케이스이므로 프론트엔드는 응답 body unwrap에 의존하지 않고 성공 여부만 처리합니다.
- `ContentUpsertRequest`는 Bean Validation annotation이 아니라 service의 수동 validation으로 검증합니다. evaluator에게는 `GlobalExceptionHandler`가 실제 한국어 error shape로 변환한다는 점을 함께 제시합니다.
- 콘텐츠 카테고리는 현재 seed/category 목록과 관리자 입력을 모두 허용하는 자유 문자열입니다. 따라서 허용 목록 기반의 “invalid category” 검증은 현재 강제하지 않고, 누락/공백/길이 초과 카테고리만 `VALIDATION_ERROR`로 처리합니다.

### Runtime Smoke Test Result - 2026-06-12 10:05 KST

로컬 MySQL 문제 해결 후 backend를 실제로 기동하고 CRUD smoke를 재실행했습니다. 8080 포트가 이미 사용 중이어서 이번 검증은 `http://localhost:18080/api` 기준으로 수행했습니다.

> 참고: 아래 실제 runtime 결과는 `201 Created`/`204 No Content` semantics 적용 전 수집한 기록입니다. 현재 코드의 기대 계약은 생성 `201 Created + Location`, 삭제 `204 No Content`입니다. 새 계약은 `api-tests/content-crud-validation.http`에 반영되어 있으며, 다음 runtime smoke에서 재검증 대상입니다.

#### 실행 환경 확인

| 항목 | 실제 명령 | 실제 결과 | 판정 |
| --- | --- | --- | --- |
| MySQL 서비스 확인 | `Get-Service MYSQL84` | `Running` | PASS |
| MySQL 포트 확인 | `Get-NetTCPConnection -LocalPort 3306` | `Listen` 확인 | PASS |
| backend 기동 | `.\gradlew.bat bootRun --args="--server.port=18080"` | `Tomcat started on port 18080`, `Started ScenikApplication` | PASS |
| smoke base URL | `http://localhost:18080/api` | 실제 요청 전송 가능 | PASS |

#### 실제 CRUD smoke 결과

| 구분 | Scenario | Endpoint | 실제 HTTP status | 실제 response code | 실제 response message | 결과 |
| --- | --- | --- | --- | --- | --- | --- |
| Success | 콘텐츠 목록 조회 | `GET /api/contents` | `200` | `SUCCESS` | `요청이 성공했습니다.` | PASS |
| Success | 장소 목록 조회 | `GET /api/places` | `200` | `SUCCESS` | `요청이 성공했습니다.` | PASS |
| Success | 관리자 콘텐츠 생성 | `POST /api/admin/contents` | `200` | `SUCCESS` | `생성되었습니다.` | PASS |
| Success | 관리자 콘텐츠 수정 | `PATCH /api/admin/contents/{contentId}` | `200` | `SUCCESS` | `수정되었습니다.` | PASS |
| Success | 관리자 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | `200` | `SUCCESS` | `삭제되었습니다.` | PASS |
| Success | 관리자 장소 생성 | `POST /api/admin/places` | `200` | `SUCCESS` | `생성되었습니다.` | PASS |
| Success | 관리자 장소 수정 | `PATCH /api/admin/places/{placeId}` | `200` | `SUCCESS` | `수정되었습니다.` | PASS |
| Success | 관리자 장소 삭제 | `DELETE /api/admin/places/{placeId}` | `200` | `SUCCESS` | `삭제되었습니다.` | PASS |
| Error | 입력값 검증 실패 | `POST /api/admin/contents` invalid payload | `400` | `VALIDATION_ERROR` | `제목을 입력해 주세요.` | PASS |
| Error | 인증 누락 | `POST /api/admin/contents` without token | `401` | `AUTH_REQUIRED` | `로그인이 필요합니다.` | PASS |
| Error | 비관리자 접근 | `POST /api/admin/contents` with `ROLE_USER` token | `403` | `FORBIDDEN` | `관리자 권한이 필요합니다.` | PASS |
| Error | 존재하지 않는 콘텐츠 | `GET /api/contents/C_NOT_FOUND_RUNTIME` | `404` | `NOT_FOUND` | `존재하지 않는 콘텐츠입니다.` | PASS |
| Error | 중복 콘텐츠 생성 | `POST /api/admin/contents` duplicate `contentId` | `409` | `CONFLICT` | `이미 등록된 콘텐츠입니다.` | PASS |

#### 현재 HTTP semantics 기대 계약

| Scenario | Endpoint | 기대 HTTP status | response body | 비고 |
| --- | --- | --- | --- | --- |
| 관리자 콘텐츠 생성 | `POST /api/admin/contents` | `201 Created` | `ApiResponse<ContentDto>` | `Location: /api/contents/{contentId}` |
| 관리자 콘텐츠 삭제 | `DELETE /api/admin/contents/{contentId}` | `204 No Content` | body 없음 | 콘텐츠 soft delete |
| 관리자 장소 생성 | `POST /api/admin/places` | `201 Created` | `ApiResponse<PlaceDto>` | `Location: /api/places/{placeId}` |
| 관리자 장소 삭제 | `DELETE /api/admin/places/{placeId}` | `204 No Content` | body 없음 | 관련 참조 정리 후 삭제 |

#### 실제 응답 body snippet

```json
{"success":true,"code":"SUCCESS","message":"요청이 성공했습니다.","data":[...]}
```

```json
{"success":true,"code":"SUCCESS","message":"생성되었습니다.","data":{"contentId":"C_ADMIN_SMOKE_20260612100548", "...":"..."}}
```

```json
{"success":true,"code":"SUCCESS","message":"삭제되었습니다.","data":null}
```

```json
{"success":false,"code":"VALIDATION_ERROR","message":"제목을 입력해 주세요.","data":null,"fieldErrors":[]}
```

```json
{"success":false,"code":"AUTH_REQUIRED","message":"로그인이 필요합니다.","data":null,"fieldErrors":[]}
```

```json
{"success":false,"code":"FORBIDDEN","message":"관리자 권한이 필요합니다.","data":null,"fieldErrors":[]}
```

```json
{"success":false,"code":"NOT_FOUND","message":"존재하지 않는 콘텐츠입니다.","data":null,"fieldErrors":[]}
```

```json
{"success":false,"code":"CONFLICT","message":"이미 등록된 콘텐츠입니다.","data":null,"fieldErrors":[]}
```

#### 테스트 데이터 정리

| 테스트 데이터 | ID | 정리 결과 |
| --- | --- | --- |
| 관리자 콘텐츠 CRUD | `C_ADMIN_SMOKE_20260612100548` | delete scenario로 `삭제되었습니다.` 응답 확인 |
| 관리자 장소 CRUD | `P_ADMIN_SMOKE_20260612100548` | delete scenario로 `삭제되었습니다.` 응답 확인 |
| 중복 검증용 콘텐츠 | `C_ADMIN_DUP_20260612100548` | duplicate 검증 후 delete scenario로 정리 |

CRUD 리소스는 delete 시나리오로 정리했습니다. 403 검증에는 일반 사용자 token을 사용했고, access token 값은 문서와 repository에 남기지 않았습니다.

### 평가자용 수동 QA 체크리스트

아래 체크리스트는 빠른 평가용입니다. admin token은 `api-tests/auth-flow.http`의 `Login with admin credentials for CRUD`에서 발급하고, 상세 요청 payload는 `api-tests/content-crud-validation.http`를 기준으로 합니다.

| 단계 | 요청 | 기대 결과 |
| --- | --- | --- |
| 1 | `GET /api/contents?page=0&size=5&sort=popular` | `200 OK`, `X-Total-Count`, `ApiResponse<List<ContentDto>>` |
| 2 | `GET /api/contents/{contentId}`와 `GET /api/contents/C_NOT_EXISTS` | 정상 ID는 `200 OK`, 없는 ID는 `404 NOT_FOUND` |
| 3 | `POST /api/admin/contents` without token | `401 AUTH_REQUIRED`, `로그인이 필요합니다.` |
| 4 | `POST /api/admin/contents` with `ROLE_USER` token | `403 FORBIDDEN`, `관리자 권한이 필요합니다.` |
| 5 | `POST /api/admin/contents` with `ROLE_ADMIN` token | `201 Created`, `Location`, 생성된 `ApiResponse<ContentDto>` |
| 6 | 같은 `contentId` 재생성, `title/category` 누락, 길이 초과 요청 | `409 CONFLICT`, `400 VALIDATION_ERROR` |
| 7 | `PATCH /api/admin/contents/{contentId}`와 `DELETE /api/admin/contents/{contentId}` | 수정은 `200 OK`, 삭제는 `204 No Content`, soft delete |
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
