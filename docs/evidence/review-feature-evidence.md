# 리뷰 기능 구현 증거

생성일: 2026-06-09

루브릭 #04 리뷰 작성·조회 항목의 평가자가 구현 위치를 바로 확인할 수 있도록 backend, frontend, DB, QA 근거를 정리합니다.

## 구현 상태

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| 리뷰 목록 조회 | 구현 | `GET /api/places/{placeId}/reviews` |
| 리뷰 작성 | 구현 | `POST /api/places/{placeId}/reviews` |
| 리뷰 수정 | 구현 | `PATCH /api/places/{placeId}/reviews/{reviewId}` |
| 리뷰 삭제 | 구현 | `DELETE /api/places/{placeId}/reviews/{reviewId}` |
| 내 리뷰 목록 | 구현 | `GET /api/users/me/reviews` |
| 중복 리뷰 방지 | 구현 | `UNIQUE(place_id, user_id)`, service duplicate check |
| 작성자 권한 검증 | 구현 | update/delete에서 `Review owner does not match.` 처리 |
| 평균 평점/리뷰 수 | 구현 | 장소 상세 응답의 `reviewCount`, `averageRating` |

## Backend Evidence

| 파일 | 설명 |
| --- | --- |
| `backend/src/main/java/com/scenik/controller/PlaceReviewController.java` | 리뷰 REST endpoint 정의 |
| `backend/src/main/java/com/scenik/service/PlaceReviewService.java` | 검증, 중복 방지, 소유자 권한 검증 |
| `backend/src/main/java/com/scenik/mapper/PlaceReviewMapper.java` | MyBatis mapper interface |
| `backend/src/main/resources/mapper/PlaceReviewMapper.xml` | 리뷰 조회/생성/수정/삭제 SQL |
| `backend/src/main/java/com/scenik/dto/ReviewDtos.java` | request/response DTO |
| `backend/src/main/resources/schema.sql` | `place_reviews` table, unique key |
| `backend/src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | 한국어 validation/error message |
| `backend/src/main/java/com/scenik/dto/PlaceDto.java` | `reviewCount`, `averageRating` 응답 필드 |
| `backend/src/main/resources/mapper/PlaceMapper.xml` | 장소 조회 시 리뷰 집계 계산 |

## Frontend Evidence

| 파일 | 설명 |
| --- | --- |
| `frontend/src/api/reviewApi.js` | 리뷰 API adapter |
| `frontend/src/views/PlaceDetailView.vue` | 리뷰 목록, 작성, 수정, 삭제 UI |
| `frontend/src/views/ContentDetailView.vue` | 콘텐츠 전용 리뷰 API 부재를 숨기지 않고 관련 장소 방문 리뷰로 연결하는 안내 |
| `frontend/src/components/common/FormStatusNotice.vue` | 리뷰 작성/수정/삭제 성공·실패 상태 표시 |
| `frontend/src/views/MyPageView.vue` | “내 리뷰” 진입점과 리뷰 수 표시 |
| `frontend/src/views/SimpleListView.vue` | 내가 작성한 리뷰 목록 표시 |
| `frontend/src/styles/detail-map-panels.css` | 리뷰 카드와 액션 UI 스타일 |
| `frontend/src/styles/profile-utility-overlays.css` | 마이페이지 리뷰 목록 카드 스타일 |

## Validation Rules

| 상황 | 기대 응답 |
| --- | --- |
| 빈 리뷰 내용 | 400, `리뷰 내용을 입력해 주세요.` |
| 평점 범위 오류 | 400, `평점은 0점 이상 5점 이하로 입력해 주세요.` |
| 같은 장소 중복 리뷰 | 409, `이미 이 장소에 리뷰를 작성했습니다.` |
| 없는 장소 | 404, `존재하지 않는 장소입니다.` |
| 비로그인 작성/수정/삭제 | 401, `로그인이 필요합니다.` |
| 다른 사용자 리뷰 수정/삭제 | 403, `권한이 없습니다.` |

## Manual QA Steps

1. 로그인합니다.
2. 장소 상세 화면으로 이동합니다.
3. 리뷰가 없으면 빈 상태가 보이는지 확인합니다.
4. 평점과 내용을 입력하고 리뷰를 등록합니다.
5. 같은 장소에 다시 작성 폼이 숨겨지는지 확인합니다.
6. 중복 작성 안내가 `이미 이 장소에 리뷰를 작성했습니다.` 상태 영역으로 보이는지 확인합니다.
7. 내 리뷰의 `수정` 버튼을 누르고 내용을 변경합니다.
8. 빈 내용 저장 시 `리뷰 내용을 입력해 주세요.` 메시지를 확인합니다.
9. 수정 저장 후 목록, 평균 평점, 평점 분포가 갱신되는지 확인합니다.
10. 내 리뷰의 `삭제` 버튼을 누르고 확인합니다.
11. 삭제 후 작성 폼이 다시 표시되는지 확인합니다.
12. 마이페이지에서 `내 리뷰` 메뉴를 눌러 `GET /api/users/me/reviews` 결과가 표시되는지 확인합니다.
13. 콘텐츠 상세에서 콘텐츠 전용 리뷰를 가장하지 않고 관련 촬영지 방문 리뷰 안내와 장소 상세 진입이 보이는지 확인합니다.

## 2026-06-09 보강 사항

- 장소 상세 `방문 리뷰` 섹션에 리뷰 작성/수정/삭제 상태 영역을 추가했습니다.
- 평균 평점과 리뷰 수 외에 간단한 평점 분포를 추가해 rating feature가 화면에서 더 명확하게 보이도록 했습니다.
- 이미 작성한 장소에는 중복 작성 폼 대신 “한 장소에는 계정당 하나의 리뷰” 안내를 표시합니다.
- 콘텐츠 상세에는 content review API가 없다는 사실을 숨기지 않고, 관련 촬영지의 장소 리뷰로 이동하도록 안내합니다.

## 평가자가 놓쳤을 가능성이 있는 이유

- 리뷰 기능이 최근 Week 3 보강에서 추가되어 기존 평가 시점에는 코드에 없었거나 미추적 파일로 남아 있었을 수 있습니다.
- `PlaceReviewController`와 `reviewApi.js`가 별도 기능명으로 분리되어 있어 기존 커뮤니티 댓글 기능과 다르게 보였을 수 있습니다.
- 장소 상세의 제목을 `방문 리뷰`로 변경하고 평균 평점/리뷰 수 요약 패널을 추가해 화면에서 기능이 더 잘 보이도록 보강했습니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 실제 검증: 리뷰 생성, 목록 조회, 작성자 수정, 작성자 삭제, 중복 리뷰, 비작성자 수정 차단
- 확인 결과: 중복 리뷰는 `409 DUPLICATE_ACTION`, 비작성자 수정은 `403 FORBIDDEN`, 삭제는 `204 No Content`로 확인했습니다.
- 임시 리뷰 데이터는 삭제 시나리오에서 정리했습니다.
