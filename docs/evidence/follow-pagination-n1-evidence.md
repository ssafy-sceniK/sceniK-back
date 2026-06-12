# 팔로우 목록 Pagination/N+1 증거

생성일: 2026-06-12

Rubric #09 `Follow/Following`의 추가 피드백인 대량 목록 동작, pagination, 상호 팔로우 판단, N+1 방지 근거를 정리합니다.

## 적용 범위

| 기능 | Endpoint | 상태 |
| --- | --- | --- |
| 팔로워 목록 | `GET /api/users/{userId}/followers?page=&size=` | 구현 완료 |
| 팔로잉 목록 | `GET /api/users/{userId}/following?page=&size=` | 구현 완료 |
| 내 팔로잉 목록 | `GET /api/users/me/following?page=&size=` | 구현 완료 |
| 전체 개수 헤더 | `X-Total-Count` | 구현 완료 |
| 상호 팔로우 판단 | `followedByMe` | 구현 완료 |

## Source Traceability

| 계층 | 파일 | 확인 내용 |
| --- | --- | --- |
| Controller | `src/main/java/com/scenik/controller/FollowController.java` | `page`, `size`, `X-Total-Count`, `ApiResponse` |
| Service | `src/main/java/com/scenik/service/FollowService.java` | 목록 DTO 생성 시 추가 count/status 쿼리 반복 제거 |
| Mapper Interface | `src/main/java/com/scenik/mapper/FollowMapper.java` | actor user id를 받아 상호 팔로우 상태 계산 |
| Mapper SQL | `src/main/resources/mapper/FollowMapper.xml` | 목록 SQL에서 `follower_count`, `following_count`, `followed_by_me` 동시 조회 |
| Schema | `src/main/resources/schema.sql` | `follows` unique key와 follower/following index |
| Frontend API | `frontend/src/api/followApi.js` | 표준 `ApiResponse` unwrap |
| Frontend State | `frontend/src/stores/uiStore.js` | follow/unfollow pending guard와 목록 refresh |

## N+1 방지 방식

이전 구조는 follow 목록을 먼저 조회한 뒤, 목록의 각 사용자마다 다음 쿼리를 추가 호출할 수 있었습니다.

- `countFollowers(userId)`
- `countFollowing(userId)`
- `existsFollow(actorUserId, userId)`

현재 구조는 `FollowMapper.xml`의 목록 SQL에서 위 값을 함께 조회합니다.

```sql
COALESCE((SELECT COUNT(*) FROM follows ff WHERE ff.following_id = u.user_id), 0) AS follower_count,
COALESCE((SELECT COUNT(*) FROM follows fg WHERE fg.follower_id = u.user_id), 0) AS following_count,
CASE
  WHEN #{actorUserId} IS NULL THEN FALSE
  ELSE EXISTS(
    SELECT 1
    FROM follows fm
    WHERE fm.follower_id = #{actorUserId}
      AND fm.following_id = u.user_id
  )
END AS followed_by_me
```

따라서 서비스 레이어는 목록 사용자별 반복 쿼리 없이 mapper가 내려준 집계 컬럼으로 `FollowUserDto`를 생성합니다.

## Pagination 규칙

| 입력 | 기대 결과 |
| --- | --- |
| `page`/`size` 생략 | 전체 목록 반환 |
| `page=0&size=10` | 첫 10개 반환 |
| `page`가 음수 | 400 `VALIDATION_ERROR` |
| `size<=0` | 400 `VALIDATION_ERROR` |
| `size>100` | 400 `VALIDATION_ERROR` |
| 범위를 넘는 page | 빈 배열 `[]` |

## 인덱스 근거

`schema.sql` 기준 `follows` 테이블은 다음 제약과 인덱스를 사용합니다.

- `UNIQUE KEY uq_follows_pair (follower_id, following_id)`
- `KEY idx_follows_follower_id (follower_id)`
- `KEY idx_follows_following_id (following_id)`

팔로워 목록은 `following_id`, 팔로잉 목록은 `follower_id`를 조건으로 사용하므로 기본 목록 조회와 count 서브쿼리에 필요한 접근 경로가 준비되어 있습니다.

## QA 체크리스트

1. 사용자 A/B/C를 준비하고 A가 B/C를 팔로우하게 만듭니다.
2. `GET /api/users/{A}/following?page=0&size=1` 호출 시 1개만 반환되고 `X-Total-Count`가 전체 개수로 내려오는지 확인합니다.
3. `GET /api/users/{B}/followers?page=0&size=10` 호출 시 A가 포함되는지 확인합니다.
4. 사용자 A 토큰으로 목록을 조회했을 때 `followedByMe`가 실제 A의 팔로우 상태와 일치하는지 확인합니다.
5. 이미 존재하는 follow를 다시 생성하면 409 `CONFLICT` 또는 `DUPLICATE_ACTION` 계열 응답이 반환되는지 확인합니다.
6. 자기 자신 follow 요청은 400 `VALIDATION_ERROR` 계열 응답으로 차단되는지 확인합니다.
7. 대량 follow fixture가 준비된 환경에서는 `EXPLAIN`으로 `idx_follows_follower_id`, `idx_follows_following_id` 사용 여부를 확인합니다.

## 실행 증거 상태

이번 패스에서는 대량 follow fixture를 새로 생성하지 않았기 때문에 `EXPLAIN`과 응답 시간 측정은 실행하지 않았습니다. 대신 코드와 SQL 구조를 보강했고, `api-tests/final-smoke.http`에 pagination 재현 요청을 추가했습니다.
## 2026-06-12 실제 런타임 QA 보강

- 통합 근거: [final-runtime-rubric-qa-evidence.md](./final-runtime-rubric-qa-evidence.md)
- 실제 검증: 팔로우, 중복 팔로우, 팔로워/팔로잉 페이지네이션, 언팔로우
- 확인 결과: 중복 팔로우는 `409 DUPLICATE_ACTION`, 목록 응답은 `X-Total-Count`를 포함합니다.
- `EXPLAIN` 결과: 팔로잉 목록은 `uq_follows_pair`와 `users.PRIMARY` 인덱스를 사용하는 것으로 확인했습니다.
