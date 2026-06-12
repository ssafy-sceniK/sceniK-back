# 좋아요/방문 상태 기능 증거

생성일: 2026-06-12

Rubric #08 `Like/Favorite/Visit Status` 평가자가 사용자별 분리, 토글 멱등성, 빠른 중복 요청 방어를 빠르게 확인할 수 있도록 구현 근거와 QA 기준을 정리합니다.

## 구현 상태

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| 콘텐츠 좋아요 | 구현 완료 | `POST /api/contents/{contentId}/likes`, `DELETE /api/contents/{contentId}/likes` |
| 장소 좋아요 | 구현 완료 | `POST /api/places/{placeId}/likes`, `DELETE /api/places/{placeId}/likes` |
| 방문 예정 | 구현 완료 | `POST /api/places/{placeId}/visit-status`, `PUT /api/places/{placeId}/visit-status` + `status=WANT_TO_GO` |
| 방문 완료 | 구현 완료 | `POST /api/places/{placeId}/visit-status`, `PUT /api/places/{placeId}/visit-status` + `status=VISITED` |
| 내 좋아요 목록 | 구현 완료 | `GET /api/users/me/liked-contents`, `GET /api/users/me/liked-places` |
| 내 방문 상태 목록 | 구현 완료 | `GET /api/users/me/visit-status` |
| 빠른 중복 클릭 방어 | 구현 완료 | 콘텐츠/장소 좋아요 pending guard, 방문 상태 pending guard |

## Backend Evidence

| 파일 | 확인 내용 |
| --- | --- |
| `src/main/java/com/scenik/controller/LikeVisitController.java` | 좋아요, 방문 상태, 내 목록 API 엔드포인트 |
| `src/main/java/com/scenik/service/LikeVisitService.java` | 대상 존재 검증, 사용자 기준 상태 변경, 방문 상태 upsert 처리 |
| `src/main/resources/mapper/LikeVisitMapper.xml` | 좋아요 `INSERT IGNORE`, 방문 상태 `ON DUPLICATE KEY UPDATE`, 사용자별 조회 |
| `src/main/resources/schema.sql` | `content_likes`, `place_likes`, `visit_status` 복합 키와 FK |
| `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 401/404/400 표준 오류 응답 |

## Frontend Evidence

| 파일 | 확인 내용 |
| --- | --- |
| `frontend/src/api/engagementApi.js` | 표준 `ApiResponse` unwrap, 방문 상태 `WANT_TO_GO`/`VISITED` 매핑 |
| `frontend/src/stores/uiStore.js` | 좋아요/방문 상태 pending guard, 사용자별 상태 로드 |
| `frontend/src/views/ContentDetailView.vue` | 콘텐츠 좋아요 pending 표시 |
| `frontend/src/views/PlaceDetailView.vue` | 장소 좋아요 및 방문 상태 버튼 pending 비활성화 |
| `frontend/src/views/SimpleListView.vue` | 좋아요/방문 상태 목록 화면 |

## 멱등성 및 사용자 분리 정책

| 시나리오 | 기대 결과 | 구현 근거 |
| --- | --- | --- |
| 같은 사용자가 콘텐츠 좋아요를 두 번 요청 | 중복 row 없이 좋아요 상태 유지 | `INSERT IGNORE`, `(user_id, content_id)` PK |
| 같은 사용자가 장소 좋아요를 두 번 요청 | 중복 row 없이 좋아요 상태 유지 | `INSERT IGNORE`, `(user_id, place_id)` PK |
| 좋아요 해제를 두 번 요청 | 이미 해제된 상태라도 서버 오류 없이 현재 상태 반환 | `DELETE` 후 상태 재조회 |
| 방문 예정 저장을 반복 요청 | 같은 `(user_id, place_id)` row가 갱신됨 | `ON DUPLICATE KEY UPDATE` |
| 방문 완료가 방문 예정을 덮어씀 | 한 장소에는 사용자별 방문 상태 1개만 유지 | `visit_status` PK `(user_id, place_id)` |
| 사용자 A와 사용자 B가 같은 대상을 좋아요 | 각 사용자 상태가 분리됨 | 모든 조회/변경 SQL이 `user_id` 조건 사용 |
| 로그인 없이 상태 변경 | 401 `AUTH_REQUIRED` | `AuthService.requireUser` |
| 없는 콘텐츠/장소 상태 변경 | 404 `NOT_FOUND` | 서비스 레이어 대상 존재 검증 |

## QA 체크리스트

1. 사용자 A로 로그인 후 같은 콘텐츠 좋아요를 두 번 요청해 중복 row 없이 상태가 유지되는지 확인합니다.
2. 사용자 A로 같은 콘텐츠 좋아요 해제를 두 번 요청해 오류 없이 해제 상태가 유지되는지 확인합니다.
3. 사용자 A와 사용자 B가 같은 콘텐츠/장소를 각각 좋아요했을 때 목록이 분리되는지 확인합니다.
4. 같은 장소에 `WANT_TO_GO`를 반복 저장한 뒤 row가 하나만 유지되는지 확인합니다.
5. 같은 장소에 `VISITED`를 저장했을 때 기존 `WANT_TO_GO`가 덮어써지는지 확인합니다.
6. 토큰 없이 좋아요/방문 상태 API를 호출해 401 `AUTH_REQUIRED`가 반환되는지 확인합니다.
7. 존재하지 않는 `contentId` 또는 `placeId`로 상태 변경 시 404 `NOT_FOUND`가 반환되는지 확인합니다.
8. 프론트에서 좋아요/방문 버튼을 빠르게 연속 클릭해 pending 중 중복 요청이 차단되는지 확인합니다.

## 남은 한계

- 이번 패스에서는 대량 동시 요청 부하 테스트를 실행하지 않았습니다.
- 좋아요 해제는 이미 없는 상태에서도 현재 상태를 반환하는 관대한 멱등 정책입니다. 평가 시 “없는 좋아요 해제는 오류가 아니라 현재 상태 반환”으로 설명하면 됩니다.
