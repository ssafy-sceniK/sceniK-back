# sceniK API 상태 보고서

생성일: 2026-06-09

이 문서는 Week 3 마감 기준 API 구현 상태, QA 근거, Notion 업데이트 방향을 발표/제출용으로 정리한 문서입니다. 상세 Notion 반영 표는 [notion-api-status-update.csv](./notion-api-status-update.csv)를 기준으로 합니다.

## 최상단 요약

| 구분 | 완료율 | 근거 |
| --- | ---: | --- |
| Week 2 | 95% | 콘텐츠/장소/탐색/검색/상세 핵심 흐름은 백엔드와 프론트 연결이 완료되었습니다. 외부 map provider live QA는 남아 있습니다. |
| Week 3 | 94% | 인증, 마이페이지, 좋아요, 방문 상태, AI 경로 생성, 경로 저장, 커뮤니티, 댓글 좋아요, 알림, 리뷰, 팔로우 최소 API가 구현되었습니다. 실제 live E2E와 map provider 검증은 수동 QA가 필요합니다. |
| Week 4 준비도 | 58% | `sceniK-ai` FastAPI skeleton, pytest, readiness 문서, backend-FastAPI 계약 초안이 준비되었습니다. ChromaDB, 실제 embedding, RAG, OpenAI orchestration, Spring Boot production 연동은 아직 구현 전입니다. |

## Week 3 완료 감사

| 목표 | 상태 | 근거 |
| --- | --- | --- |
| content like | Done | `POST /api/contents/{contentId}/likes`, `DELETE /api/contents/{contentId}/likes` 구현 및 프론트 store 연결 확인 |
| place like | Done | `POST /api/places/{placeId}/likes`, `DELETE /api/places/{placeId}/likes` 구현 및 프론트 store 연결 확인 |
| visit status WANT_TO_GO | Done | `POST/PUT /api/places/{placeId}/visit-status`에서 `WANT_TO_GO` 지원 |
| visit status VISITED | Done | `POST/PUT /api/places/{placeId}/visit-status`에서 `VISITED` 지원 |
| liked contents page | Done | `GET /api/users/me/liked-contents` 및 마이페이지 목록 연결 |
| liked places page | Done | `GET /api/users/me/liked-places` 및 마이페이지 목록 연결 |
| visit status page | Done | `GET /api/users/me/visit-status` 및 방문 예정/완료 목록 연결 |
| mypage integration | Done | `/api/users/me`, 활동 요약, 이력, 좋아요, 방문 상태, 저장 경로, 내 게시글 연동 |
| content detail | Done | `GET /api/contents/{contentId}`, `GET /api/contents/{contentId}/places` 구현 |
| place detail | Done | `GET /api/places/{placeId}`, 좋아요, 방문 상태, 리뷰 목록/작성 UI 연결 |
| explore page | Done | 콘텐츠/장소 store가 백엔드 목록/검색 endpoint를 사용 |
| map display | In progress | map UI와 백엔드 map endpoint는 있으나 외부 provider live QA 미실행 |
| community list | Done | `GET /api/posts` 구현, 검색/지역/정렬/pagination 지원 |
| post create | Done | `POST /api/posts` 및 프론트 작성 플로우 구현 |
| comment create | Done | `POST /api/posts/{postId}/comments` 및 상세 화면 작성 플로우 구현 |
| comment like | Done | `POST/DELETE /api/posts/comments/{commentId}/likes` 구현, `CommentDto.likeCount/likedByMe` 응답 및 프론트 pending UX 연결 |
| notifications | Done | `GET /api/notifications`, `PATCH /api/notifications/read`, 단일 읽음 처리 구현 |
| AI route generation | Done | `POST /api/ai/routes` 구현, 검증 장소 기반 deterministic 경로 생성 |
| route save | Done | `POST /api/routes` 구현 및 저장 플로우 연결 |
| saved routes | Done | `GET /api/users/me/routes`, route detail/manage 흐름 구현 |
| follow/following | In progress | `POST/DELETE /api/users/{userId}/follow`, followers/following 목록 구현 및 프론트 진입점 연결, live QA 필요 |

## 새로 추가된 Review API 상태

| API | 상태 | 설명 |
| --- | --- | --- |
| `GET /api/places/{placeId}/reviews` | In progress | 백엔드와 장소 상세 UI는 연결되어 있으나 live QA가 필요합니다. |
| `POST /api/places/{placeId}/reviews` | In progress | 장소 상세에서 작성 UI가 있으나 live QA가 필요합니다. |
| `PATCH /api/places/{placeId}/reviews/{reviewId}` | In progress | 백엔드와 장소 상세 수정 UI는 연결되었으나 live QA가 필요합니다. |
| `DELETE /api/places/{placeId}/reviews/{reviewId}` | In progress | 백엔드와 장소 상세 삭제 UI는 연결되었으나 live QA가 필요합니다. |
| `GET /api/users/me/reviews` | In progress | 백엔드와 마이페이지 리뷰 목록 UI는 연결되었으나 live QA가 필요합니다. |

## 새로 추가된 Follow API 상태

| API | 상태 | 설명 |
| --- | --- | --- |
| `POST /api/users/{userId}/follow` | In progress | backend와 커뮤니티 작성자 팔로우 버튼은 연결되었으나 live QA가 필요합니다. |
| `DELETE /api/users/{userId}/follow` | In progress | backend와 언팔로우 UI는 연결되었으나 live QA가 필요합니다. |
| `GET /api/users/{userId}/followers` | In progress | follower 목록 API와 마이페이지 목록 UI는 연결되었으나 live QA가 필요합니다. |
| `GET /api/users/{userId}/following` | In progress | following 목록 API가 구현되었습니다. |
| `GET /api/users/me/following` | In progress | 내 팔로잉 목록 단축 endpoint와 프론트 상태 복원이 연결되었습니다. |

## 새로 추가된 Comment Like API 상태

| API | 상태 | 설명 |
| --- | --- | --- |
| `POST /api/posts/comments/{commentId}/likes` | Done | `comment_likes`에 `(user_id, comment_id)` 기준으로 저장하며 중복 요청은 `INSERT IGNORE`로 count를 손상하지 않습니다. |
| `DELETE /api/posts/comments/{commentId}/likes` | Done | 좋아요가 없어도 삭제 요청은 idempotent하게 처리되며 최신 `CommentDto`를 반환합니다. |
| `GET /api/posts/{postId}/comments` | Done | 댓글과 답글 응답에 `likeCount`, `likes`, `likedByMe`, `isLiked`를 포함합니다. |
| `GET /api/posts/{postId}` | Done | 게시글 상세의 `comments` 배열에도 댓글 좋아요 상태를 포함합니다. |

## Partial / In progress로 남는 항목

| 영역 | 항목 | 남은 이유 |
| --- | --- | --- |
| Place | map provider live QA | `/api/maps/geocode`, `/api/maps/reverse-geocode`, `/api/maps/local-search`는 fallback이 있으나 외부 provider 환경 검증이 필요합니다. |
| Place | review edit/delete UI | 장소 상세 수정/삭제 UI는 연결되었으나 live QA가 필요합니다. |
| Route | route place 편집 | add/update/delete/reorder 백엔드는 있으나 편집 UI live QA가 필요합니다. |
| Community | comment update/delete/reply/like | 댓글 작성자 수정/삭제, 1단계 답글, 댓글 좋아요 UI/API는 연결되었으나 live QA가 필요합니다. |
| User | follow/following | 최소 vertical slice는 구현되었으나 두 사용자 계정 기반 live QA가 필요합니다. |
| Admin | admin content/place endpoint | 백엔드 전용 endpoint는 있으나 admin UI가 없습니다. |

## Deferred 항목

| 항목 | 상태 | 결정 |
| --- | --- | --- |
| Social login | Deferred | Week 3 MVP는 이메일/비밀번호 인증 기준입니다. |
| Notification settings | Deferred | Week 3 알림 범위는 목록/읽음 처리입니다. |
| Route public sharing | Deferred | 공개 공유 제품 플로우가 아직 완성되지 않았습니다. |
| Comment reports | Deferred | 댓글 신고는 현재 구현되지 않았으며 Week 3 MVP 범위 밖입니다. |
| Real RAG/OpenAI/ChromaDB | Deferred | Week 4 AI 품질 개선 범위입니다. |

## Deprecated 경로

| 구버전 경로 | 현재 canonical endpoint |
| --- | --- |
| `/api/contents/{contentId}/like` | `/api/contents/{contentId}/likes` |
| `/api/places/{placeId}/like` | `/api/places/{placeId}/likes` |
| `/api/posts/{postId}/like` | `/api/posts/{postId}/likes` |
| `/api/posts/{postId}/report` | `/api/posts/{postId}/reports` |
| `/api/posts/{postId}/comments/{commentId}/report` | `/api/posts/{postId}/comments/{commentId}/reports` |
| `/api/places/{placeId}/visit-plan` | `/api/places/{placeId}/visit-status` + `WANT_TO_GO` |
| `/api/places/{placeId}/visit-complete` | `/api/places/{placeId}/visit-status` + `VISITED` |
| `/api/ai-routes` | `/api/ai/routes` |
| `/api/ai/courses` | `/api/ai/routes` |
| `/api/courses` | `/api/routes`, `/api/users/me/routes` |
| `/api/users/me/courses` | `/api/users/me/routes` |
| `/api/notifications/read-all` | `/api/notifications/read` |
| `/api/content` | `/api/contents` |
| `/api/content/{id}` | `/api/contents/{contentId}` |

## ID 타입 기준

| ID | 현재 타입 | 설명 |
| --- | --- | --- |
| `contentId` | String | 예: `C_DRAMA_001` |
| `placeId` | String | 예: `P_0001` |
| `routeId` | String | 현재 route schema/DTO 기준 문자열입니다. |
| `userId` | Long / BIGINT | 사용자 PK는 숫자입니다. |
| `postId` | String | 현재 코드와 schema에서 문자열입니다. |
| `commentId` | String | 현재 코드와 schema에서 문자열입니다. |
| `notificationId` | String | 현재 코드와 schema에서 문자열입니다. |
| `reportId` | String | 현재 코드와 schema에서 문자열입니다. |
| `reviewId` | Long / BIGINT | 리뷰 PK는 숫자입니다. |

## QA 결과

| 항목 | 결과 | 설명 |
| --- | --- | --- |
| Backend build | PASS | `.\gradlew.bat clean build` 통과 |
| Frontend build | PASS | `npm run build` 통과, bundle size warning만 발생 |
| Frontend lint | PASS | `npm run lint` 통과 |
| Course smoke | PASS | `npm run smoke:course` 통과 |
| Error smoke | PASS | `npm run smoke:errors` 통과 |
| AI pytest | PASS | `sceniK-ai` venv에서 `python -m pytest` 실행 결과 4개 테스트 통과 |
| Map live QA | BLOCKED | 외부 provider credential/server를 시작하지 않아 live QA 미실행 |
| Deprecated source scan | PASS | source code에서 구버전 API endpoint 사용은 발견되지 않았습니다. |
| Frontend direct AI call scan | PASS | frontend가 `sceniK-ai`를 직접 호출하지 않습니다. |

수동 live E2E 준비 파일:
- `backend/api-tests/week3-live-e2e.http`
- `backend/docs/qa/week3-live-e2e-checklist.md`

## Notion 업데이트 기준

최종 Notion 반영은 [notion-api-update-guide.md](./notion-api-update-guide.md)와 [notion-api-status-update.csv](./notion-api-status-update.csv)를 사용합니다.

상태 카운트:

| 상태 | 개수 |
| --- | ---: |
| Done | 50 |
| In progress | 24 |
| Deferred | 11 |
| Deprecated | 14 |
| Missing / Not progress | 0 |

## Week 4 시작 전 필수 정리

| 우선순위 | 작업 |
| --- | --- |
| Critical | Spring Boot -> FastAPI 내부 호출 계약 확정 |
| Critical | ChromaDB 또는 승인된 vector store 도입 |
| Critical | 실제 embedding 생성과 deterministic fallback 정책 정리 |
| Critical | RAG가 검증된 DB 장소만 반환하도록 guardrail 구현 |
| Recommended | AI 품질 평가 fixture 추가 |
| Recommended | map provider live QA 수행 |
| Optional | review edit/delete, follow/following live QA 추가 |

## Commit 판단

문서/상태 정리 변경은 커밋 가능 상태입니다. 단, 기존 workspace에 다른 변경이 함께 있으므로 stage 전에 변경 범위를 다시 확인해야 합니다.
