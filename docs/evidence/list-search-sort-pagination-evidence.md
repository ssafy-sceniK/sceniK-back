# 목록 조회·검색·정렬·페이징 증거

생성일: 2026-06-09

루브릭 #02 목록 조회·검색·정렬 항목의 구현 범위와 남은 한계를 정리합니다.

## Endpoint Coverage

| Endpoint | page | size | sort | keyword | category/theme | region | 근거 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/contents` | 지원 | 지원 | `latest`, `popular` | 지원 | `category` | 해당 없음 | `ContentController`, `ContentService` |
| `GET /api/contents/categories` | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 카테고리 목록 | 해당 없음 | `ContentController` |
| `GET /api/places` | 지원 | 지원 | `name`, `popular`, `latest` 계열 정렬 후보 | 지원 | `category`, `theme` | 지원 | `PlaceController`, `PlaceService` |
| `GET /api/places/nearby` | 제한 | `limit` | 거리순 | 해당 없음 | 해당 없음 | 좌표 기반 | `PlaceController` |
| `GET /api/posts` | 지원 | 지원 | `latest`, `popular` | 지원 | tag/filter는 frontend | 지원 | `CommunityController` |
| `GET /api/users/me/routes` | 지원 | 지원 | `latest`, `updated` | 해당 없음 | 해당 없음 | 해당 없음 | `RouteController` |
| `GET /api/users/me/liked-contents` | 지원 | 지원 | 기본 정렬 | 해당 없음 | 해당 없음 | 해당 없음 | `LikeVisitController` |
| `GET /api/users/me/liked-places` | 지원 | 지원 | 기본 정렬 | 해당 없음 | 해당 없음 | 해당 없음 | `LikeVisitController` |
| `GET /api/users/me/visit-status` | 지원 | 지원 | 기본 정렬 | 해당 없음 | status filtering은 frontend | 해당 없음 | `LikeVisitController` |
| `GET /api/notifications` | 지원 | 지원 | `latest` | 해당 없음 | 해당 없음 | 해당 없음 | `CommunityController` |

## Frontend Screens

| 화면 | 연결 상태 |
| --- | --- |
| `ContentListView.vue` | 콘텐츠 목록, keyword/category/sort select, 결과 수, “더 보기” UI |
| `PlaceListView.vue` | 장소 목록, keyword/category/region, sort select, 결과 수, “더 보기” UI |
| `MapView.vue` | 장소 좌표 기반 fallback map preview |
| `CommunityView.vue` | 게시글 목록, 검색어, 게시판 필터, sort select, 결과 수, “더 보기” UI, post detail 연결 |
| `SimpleListView.vue` | 좋아요/방문 상태/저장 코스/내 게시글 목록 |

## Manual Test Examples

```http
GET /api/contents?page=0&size=10&sort=popular&keyword=드라마
GET /api/places?page=0&size=10&region=서울&keyword=카페
GET /api/posts?page=0&size=10&sort=popular&keyword=코스
GET /api/users/me/routes?page=0&size=10&sort=updated
GET /api/notifications?page=0&size=10&sort=latest
```

## 남은 한계

- 모든 목록 화면에 명시적 페이지 번호 UI가 드러나지는 않습니다.
- 일부 마이페이지 목록은 backend pagination을 지원하지만 모바일 UX에서는 간단 목록으로 표시합니다.
- 외부 map provider live search는 아직 완료되지 않아 map provider 관련 항목은 `In progress`입니다.

## 보강 판단

서버 페이징과 정렬은 backend 중심으로 구현되어 있으며, evaluator가 확인하기 쉽도록 HTTP 테스트 파일과 evidence 문서로 보강했습니다. 이번 pass에서는 콘텐츠, 장소, 커뮤니티 화면에 sort와 incremental “더 보기” UI를 추가해 화면에서도 목록/정렬/페이지네이션 성격이 드러나게 했습니다.
