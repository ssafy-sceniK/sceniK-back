# 목록 조회·검색·정렬·페이지네이션 증거

작성일: 2026-06-12

Rubric #02 평가자가 목록 조회, 검색, 정렬, 페이지네이션 구현 근거를 빠르게 추적할 수 있도록 현재 backend/frontend 동작과 SQL 근거를 정리합니다.

## 평가 범위 요약

| 영역 | 상태 | 근거 |
| --- | --- | --- |
| 콘텐츠 목록 | 구현 완료 | `GET /api/contents`, `ContentController`, `ContentMapper.xml` |
| 장소 목록 | 구현 완료 | `GET /api/places`, `PlaceController`, `PlaceMapper.xml` |
| 커뮤니티 게시글 목록 | 구현 완료 | `GET /api/posts`, `CommunityController`, `CommunityMapper.xml` |
| 마이페이지 목록 | 구현 완료 | 좋아요/방문 상태/저장 코스 API |
| 프론트 검색/정렬 UI | 구현 완료 | 콘텐츠/장소/커뮤니티 목록 화면 |
| 지도 외부 provider live 검색 | 부분 | 지도 provider 자체의 외부 검색은 별도 고도화 범위 |

## Endpoint Coverage

| Endpoint | page/size | sort | keyword | category/tag | region | 응답 근거 |
| --- | --- | --- | --- | --- | --- | --- |
| `GET /api/contents` | 지원 | `popular`, `latest` | 지원 | `category` 지원 | 해당 없음 | `ApiResponse<List<ContentDto>>`, `X-Total-Count` |
| `GET /api/contents/categories` | 해당 없음 | category ASC | 해당 없음 | 카테고리 목록 | 해당 없음 | `ApiResponse<List<ContentCategoryDto>>` |
| `GET /api/places` | 지원 | `popular`, `name`, `region` | 지원 | `category` 지원 | 지원 | `ApiResponse<List<PlaceDto>>`, `X-Total-Count` |
| `GET /api/places/nearby` | 해당 없음 | 거리순 | 해당 없음 | 해당 없음 | 좌표 기반 | `ApiResponse<List<PlaceDto>>` |
| `GET /api/posts` | 지원 | `latest`, `popular` | 지원 | tag/filter UI와 연계 | 지원 | 게시글 목록 응답 |
| `GET /api/users/me/routes` | 지원 | `latest`, `updated` | 해당 없음 | 해당 없음 | 해당 없음 | 저장 코스 목록 |
| `GET /api/users/me/liked-contents` | 지원 | 기본 정렬 | 해당 없음 | 해당 없음 | 해당 없음 | 좋아요 콘텐츠 목록 |
| `GET /api/users/me/liked-places` | 지원 | 기본 정렬 | 해당 없음 | 해당 없음 | 해당 없음 | 좋아요 장소 목록 |
| `GET /api/users/me/visit-status` | 지원 | 기본 정렬 | 해당 없음 | status 필터는 프론트 탭 | 해당 없음 | 방문 상태 목록 |

## Query Parameter 규칙

| 파라미터 | 규칙 | 오류 처리 |
| --- | --- | --- |
| `page` | 생략 가능, 지정 시 `0` 이상 | 음수면 `400 VALIDATION_ERROR` |
| `size` | 생략 가능, 지정 시 `1~100` | `0` 이하 또는 `100` 초과면 `400 VALIDATION_ERROR` |
| `sort` | endpoint별 allow-list만 허용 | 허용되지 않은 값은 `400 VALIDATION_ERROR` |
| `keyword` | 공백/누락 시 전체 조회 | 검색 결과가 없으면 `data: []`, `X-Total-Count: 0` |
| `category` | `전체` 또는 공백은 필터 미적용 | 유효하지 않은 문자열은 결과 없음 가능 |
| `region` | `전체` 또는 공백은 필터 미적용 | 유효하지 않은 문자열은 결과 없음 가능 |

`ListQuerySupport`는 `page`/`size` 조합을 공통으로 검증합니다. `page`와 `size`를 모두 생략하면 전체 목록을 반환하고, 하나라도 지정하면 서버 측 slicing을 적용합니다.

## SQL 동적 쿼리 구성

### 콘텐츠 목록

파일: `src/main/resources/mapper/ContentMapper.xml`

- `category`가 있고 `전체`가 아니면 `AND c.category = #{category}` 적용
- `popular` 조건이 있으면 `AND c.is_popular = #{popular}` 적용
- `keyword`가 있으면 `title`, `description`, `cast_names`, `tags` 대상 `LIKE` 검색 적용
- 삭제된 콘텐츠 제외: `(c.status IS NULL OR c.status != 'DELETED')`
- 정렬: `c.is_popular DESC, c.created_at DESC`

### 장소 목록

파일: `src/main/resources/mapper/PlaceMapper.xml`

- `region`이 있고 `전체`가 아니면 `p.region = #{region}` 적용
- `district`, `category` 조건은 값이 있을 때만 적용
- `keyword`가 있으면 `place_name`, `region`, `district`, `category`, `description` 대상 `LIKE` 검색 적용
- 정렬: `p.region ASC, p.place_name ASC`
- 주변 장소는 좌표와 반경 기반 거리 계산 후 `distance_km ASC` 정렬

### 커뮤니티 목록

파일: `src/main/resources/mapper/CommunityMapper.xml`

- 게시글 상태, 지역, 검색어, 태그 조건을 조합해 목록을 조회합니다.
- 댓글 수, 좋아요 수, 스크랩 상태 등은 목록 응답에 함께 내려주어 프론트가 카드별 추가 조회를 반복하지 않도록 구성되어 있습니다.

## 인덱스 근거

fresh schema 기준으로 목록 필터와 정렬에 필요한 보조 인덱스를 명시했습니다.

| 테이블 | 인덱스 | 목적 |
| --- | --- | --- |
| `contents` | `idx_contents_status_category (status, category)` | 공개 목록/카테고리 필터 |
| `contents` | `idx_contents_popular_created (is_popular, created_at)` | 인기 우선 및 생성일 정렬 |
| `contents` | `idx_contents_created_at (created_at)` | 최신순 보조 |
| `places` | `idx_places_region_district (region, district)` | 지역/구 단위 필터 |
| `places` | `idx_places_category (category)` | 장소 카테고리 필터 |
| `places` | `idx_places_name (place_name)` | 이름 정렬 및 prefix 검색 보조 |
| `places` | `idx_places_lat_lng (latitude, longitude)` | 주변 장소 후보 범위 축소 |
| `community_posts` | `idx_community_posts_status_updated (status, updated_at)` | 활성 게시글 최신순 |
| `community_posts` | `idx_community_posts_region_updated (region, updated_at)` | 지역 필터와 최신순 |
| `post_tags` | `idx_post_tags_tag (tag)` | 태그 기반 게시글 필터 |

기존 로컬 DB에 이미 테이블이 생성되어 있는 경우 `CREATE TABLE IF NOT EXISTS`는 새 인덱스를 자동 추가하지 않습니다. 평가자가 기존 DB에서 EXPLAIN을 재현하려면 schema 재초기화 또는 별도 `ALTER TABLE ADD INDEX` 적용이 필요합니다.

## EXPLAIN 및 응답 시간 증거

이번 pass에서는 business logic을 변경하지 않고 fresh schema의 인덱스 근거와 HTTP QA 시나리오를 보강했습니다. 기존 로컬 DB가 이미 생성된 상태라 새 인덱스가 자동 반영되지 않을 수 있어, 잘못된 EXPLAIN 결과를 문서에 확정값으로 기록하지 않았습니다.

재현 절차:

1. fresh DB를 생성하거나 위 인덱스를 기존 DB에 `ALTER TABLE`로 적용합니다.
2. `GET /api/contents?page=0&size=10&sort=popular&keyword=드라마`를 실행합니다.
3. 동일 조건의 SQL에 대해 `EXPLAIN`을 실행하고 `idx_contents_status_category` 또는 `idx_contents_popular_created` 사용 여부를 확인합니다.
4. `GET /api/places?page=0&size=10&region=서울&sort=name`를 실행합니다.
5. 동일 조건의 SQL에 대해 `EXPLAIN`을 실행하고 `idx_places_region_district` 또는 `idx_places_name` 사용 여부를 확인합니다.

## Frontend Screens

| 화면 | 연결 상태 |
| --- | --- |
| `frontend/src/views/ContentListView.vue` | 콘텐츠 keyword/category/sort와 더 보기 UI |
| `frontend/src/views/PlaceListView.vue` | 장소 keyword/category/region/sort와 더 보기 UI |
| `frontend/src/views/MapView.vue` | 장소 좌표 기반 지도 preview |
| `frontend/src/views/CommunityView.vue` | 게시글 검색어, 카테고리/지역 필터, sort, 상세 이동 |
| `frontend/src/views/SimpleListView.vue` | 좋아요/방문 상태/저장 코스/내 게시글 목록 |

## Boundary QA Checklist

| 시나리오 | 요청 예시 | 기대 결과 |
| --- | --- | --- |
| 콘텐츠 인기순 | `GET /api/contents?page=0&size=10&sort=popular` | `200`, `X-Total-Count`, 인기 우선 목록 |
| 콘텐츠 최신순 | `GET /api/contents?page=0&size=10&sort=latest` | `200`, 최신순 기준 목록 |
| 콘텐츠 검색 결과 없음 | `GET /api/contents?keyword=zzzz-no-result` | `200`, `data: []`, `X-Total-Count: 0` |
| 콘텐츠 잘못된 sort | `GET /api/contents?sort=unknown` | `400 VALIDATION_ERROR` |
| 장소 지역+정렬 | `GET /api/places?page=0&size=10&region=서울&sort=name` | `200`, 지역 필터 적용 |
| 장소 잘못된 size | `GET /api/places?page=0&size=101` | `400 VALIDATION_ERROR` |
| 장소 no result | `GET /api/places?region=없는지역` | `200`, `data: []`, `X-Total-Count: 0` |
| 커뮤니티 검색+정렬 | `GET /api/posts?page=0&size=10&sort=popular&keyword=코스` | `200`, 검색/정렬 적용 |

## 남은 한계

- 콘텐츠/장소 목록은 MyBatis에서 조건 조합 후 service 계층에서 page slicing을 수행합니다. 대용량 데이터에서는 SQL `LIMIT/OFFSET` 적용으로 추가 고도화할 수 있습니다.
- keyword 검색은 현재 `LIKE` 기반입니다. 대량 데이터에서는 full-text index 또는 검색 엔진 도입이 개선점입니다.
- 외부 지도 provider의 live search 품질은 Rubric #02의 목록 API 구현과 별도 범위입니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 실제 검증: 콘텐츠 검색/정렬, 장소 지역+검색+정렬, 게시글 검색+정렬, 잘못된 sort/size, 검색 결과 없음
- 확인 결과: 빈 결과는 `200`과 빈 배열로 반환되고, 잘못된 sort/size는 `400 VALIDATION_ERROR`로 반환됩니다.
- `EXPLAIN` 결과: 팔로우/댓글 조회는 인덱스 사용을 확인했지만, 콘텐츠/장소 대표 검색은 현재 데이터 규모에서 full scan/filesort가 확인되어 향후 복합 인덱스 최적화 여지가 있습니다.
