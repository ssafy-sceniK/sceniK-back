# 코스/계획 관리 런타임 QA 근거

대상 루브릭: #12 Course/Plan/Challenge Management  
통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)

## 소스 코드 추적성

| 영역 | 파일 |
| --- | --- |
| Backend controller | `src/main/java/com/scenik/controller/RouteController.java` |
| Backend service | `src/main/java/com/scenik/service/RouteService.java` |
| Backend mapper | `src/main/resources/mapper/RouteMapper.xml`, `src/main/resources/mapper/RoutePlaceMapper.xml` |
| Frontend API | `frontend/src/api/routeApi.js` |
| Frontend 화면 | `frontend/src/views/CourseDetailView.vue`, `frontend/src/views/CourseEditView.vue`, `frontend/src/views/SimpleListView.vue` |

## 2026-06-12 실제 런타임 QA

| 시나리오 | Endpoint | 실제 결과 |
| --- | --- | --- |
| 코스 저장 | `POST /api/routes` | `200`, routeId 반환 |
| 내 코스 목록 | `GET /api/users/me/routes?page=0&size=10&sort=latest` | `200`, `X-Total-Count` 확인 |
| 코스 상세 | `GET /api/routes/{routeId}` | `200` |
| 코스 장소 추가 | `POST /api/routes/{routeId}/places` | `200` |
| 코스 장소 순서 변경 | `PATCH /api/routes/{routeId}/places/reorder` | `200` |
| 비소유자 접근 차단 | `GET /api/routes/{routeId}` with another user token | `403 FORBIDDEN` |
| 코스 삭제 | `DELETE /api/routes/{routeId}` | `204 No Content` |

임시 코스 데이터는 삭제 시나리오에서 정리했습니다.

## 남은 한계

- 실제 브라우저에서 코스 카드 순서가 재진입 후 동일하게 보이는지는 이번 런타임 API 중심 검증 범위에서는 별도 스크린샷으로 남기지 않았습니다.
- API 기준으로는 저장, 목록, 상세, 장소 추가, 순서 변경, 삭제, 소유자 권한 검증이 통과했습니다.
