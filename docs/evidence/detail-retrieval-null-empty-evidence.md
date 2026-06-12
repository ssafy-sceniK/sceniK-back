# 상세 조회 null/empty 처리 증거

작성일: 2026-06-12

Rubric #03 평가자가 상세 조회, 404 처리, 관련 데이터의 `null`/빈 배열 처리 기준을 빠르게 확인할 수 있도록 현재 구현과 프론트 방어 로직을 정리합니다.

## 핵심 규칙

| 상황 | Backend 계약 | Frontend 처리 |
| --- | --- | --- |
| 상세 리소스가 없음 | `404 NOT_FOUND`, `success:false`, `data:null` | `DetailNotFoundPanel`로 재시도/목록 이동 안내 |
| 관련 장소/콘텐츠 목록이 없음 | `200 SUCCESS`, `data: []` | 빈 상태 메시지 표시 |
| 선택 scalar 필드가 없음 | DTO 필드 값 `null` 허용 | 화면에서 `?? ''`, fallback copy, 이미지 fallback 사용 |
| count/rating 값이 없음 | DTO에서 `0` 또는 `0.0` 기본값 | 숫자 UI가 깨지지 않도록 기본값 사용 |
| 이미지 URL이 실패함 | API 값은 유지 | 프론트 이미지 fallback 적용 |

## Endpoint별 동작

| Endpoint | 정상 응답 | 없음/빈 데이터 응답 | 근거 |
| --- | --- | --- | --- |
| `GET /api/contents/{contentId}` | `ApiResponse<ContentDto>` | 없는 `contentId`는 `404 NOT_FOUND` | `ContentController.detail`, `ContentService.getContent` |
| `GET /api/contents/{contentId}/places` | `ApiResponse<List<PlaceDto>>` | 관련 장소가 없으면 `data: []` | `ContentController.relatedPlaces`, `ContentService.getRelatedPlaces` |
| `GET /api/places/{placeId}` | `ApiResponse<PlaceDto>` | 없는 `placeId`는 `404 NOT_FOUND` | `PlaceController.detail`, `PlaceService.getPlace` |
| `GET /api/places/{placeId}`의 `relatedContents` | `PlaceDto.relatedContents` | 관련 콘텐츠가 없으면 `[]` | `PlaceDto.from(place, relatedContents)` |
| `GET /api/places/nearby` | `ApiResponse<List<PlaceDto>>` | 좌표가 없거나 결과가 없으면 `[]` | `PlaceController.nearby`, `PlaceService.getNearbyPlaces` |

## Source Code Traceability

| 계층 | 파일 | 확인 포인트 |
| --- | --- | --- |
| Content Controller | `src/main/java/com/scenik/controller/ContentController.java` | 상세/관련 장소 조회를 `ApiResponse.ok(...)`로 반환 |
| Content Service | `src/main/java/com/scenik/service/ContentService.java` | 없는 콘텐츠는 `ResponseStatusException(HttpStatus.NOT_FOUND)` |
| Content Mapper | `src/main/resources/mapper/ContentMapper.xml` | `findById`, `findPlacesByContentId` |
| Place Controller | `src/main/java/com/scenik/controller/PlaceController.java` | 장소 목록/주변/상세 조회 |
| Place Service | `src/main/java/com/scenik/service/PlaceService.java` | 없는 장소는 `ResponseStatusException(HttpStatus.NOT_FOUND)` |
| Place DTO | `src/main/java/com/scenik/dto/PlaceDto.java` | `relatedContents == null ? List.of() : relatedContents` |
| Content DTO | `src/main/java/com/scenik/dto/ContentDto.java` | `tags == null ? List.of() : ...` |
| Exception Handler | `src/main/java/com/scenik/exception/GlobalExceptionHandler.java` | `404 NOT_FOUND`를 표준 오류 응답으로 변환 |
| Frontend Content API | `frontend/src/api/contentApi.js` | 콘텐츠/관련 장소 배열 정규화 |
| Frontend Place API | `frontend/src/api/placeApi.js` | 장소 상세의 `relatedContents` 배열 정규화 |
| Content Detail UI | `frontend/src/views/ContentDetailView.vue` | 관련 장소 빈 상태 표시 |
| Place Detail UI | `frontend/src/views/PlaceDetailView.vue` | 관련 콘텐츠 빈 상태 표시 |

## Frontend 정규화 정책

프론트 API 경계에서 다음 값을 배열로 정규화합니다.

- `ContentDto.tags`
- `ContentDto.relatedPlaces`
- `ContentDto.relatedContents`
- `PlaceDto.tags`
- `PlaceDto.relatedContentIds`
- `PlaceDto.relatedContents`
- `GET /api/contents/{contentId}/places` 응답

이 정책의 목적은 상세 화면 컴포넌트가 관련 데이터의 null 여부에 의존하지 않고, 항상 `v-for`와 빈 상태 UI를 안전하게 사용할 수 있게 하는 것입니다.

## UI 빈 상태 정책

| 화면 | 관련 데이터 없음 | 표시 방식 |
| --- | --- | --- |
| 콘텐츠 상세 | 관련 장소 없음 | `연결된 촬영지가 아직 없어요` |
| 장소 상세 | 관련 콘텐츠 없음 | `연결된 콘텐츠가 아직 없어요` |
| 상세 리소스 없음 | 콘텐츠/장소 자체 없음 | `DetailNotFoundPanel` |

## 수동 QA 체크리스트

| 단계 | 요청/화면 | 기대 결과 |
| --- | --- | --- |
| 1 | `GET /api/contents/C_NOT_EXISTS` | `404 NOT_FOUND`, `data:null` |
| 2 | 없는 콘텐츠 상세 URL 진입 | 상세 실패 패널과 목록 이동/재시도 표시 |
| 3 | 관련 장소가 없는 콘텐츠 상세 | 화면 crash 없이 빈 상태 표시 |
| 4 | `GET /api/places/P_NOT_EXISTS` | `404 NOT_FOUND`, `data:null` |
| 5 | 관련 콘텐츠가 없는 장소 상세 | 화면 crash 없이 빈 상태 표시 |
| 6 | 이미지 URL 실패 콘텐츠/장소 | fallback 이미지 또는 기본 UI 표시 |

## 남은 한계

- `GET /api/contents/{contentId}/places`는 현재 content 존재 여부를 별도로 먼저 검증하지 않고 관계 목록을 조회합니다. 따라서 존재하지 않는 contentId라도 관계가 없으면 `data: []`가 될 수 있습니다. 상세 리소스 자체의 404 검증은 `GET /api/contents/{contentId}`에서 담당합니다.
- 관련 데이터가 정말 없는지, 존재하지 않는 상위 리소스인지까지 엄격히 구분하려면 관련 목록 endpoint에서도 상위 리소스 존재 검증을 추가할 수 있습니다. 이번 pass에서는 기존 API 동작 호환성을 우선했습니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 실제 검증: `GET /api/contents/{contentId}`, `GET /api/contents/{contentId}/places`, `GET /api/places/{placeId}`
- 확인 결과: 정상 상세는 `200 SUCCESS`, 없는 콘텐츠/장소는 `404 NOT_FOUND`로 반환됩니다.
- 관련 장소 목록은 배열로 반환되어 프론트가 `null`을 직접 순회하지 않아도 되는 계약을 확인했습니다.
