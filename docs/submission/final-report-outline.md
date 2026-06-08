# sceniK 최종 보고서 개요

생성일: 2026-06-08

이 문서는 최종 보고서 작성자가 프로젝트 범위, 구현 성과, 검증 결과, 남은 한계를 빠짐없이 정리할 수 있도록 만든 제출용 개요입니다.

## 1. 프로젝트 요약

sceniK는 K-content 팬이 검증된 국내 촬영지를 발견하고, 콘텐츠와 연결된 장소를 탐색하며, AI 추천 코스를 생성·저장·공유할 수 있는 K-content 여행 플래너입니다.

핵심 기능:
- K-content 목록/상세 조회
- 촬영지 목록/상세 조회
- 콘텐츠와 장소의 연결
- 콘텐츠/장소 좋아요
- 방문 상태 `WANT_TO_GO`, `VISITED`
- 장소 리뷰 작성/조회/수정/삭제
- AI route generation
- route save/my routes
- community post/comment
- notifications
- mypage backend hydration

## 2. 문제 정의

K-content 촬영지 정보는 온라인에 흩어져 있고, 사용자가 실제 방문 가능한 장소인지 확인하거나 여러 장소를 여행 동선으로 구성하기 어렵습니다. 또한 생성한 코스를 저장하고 다른 사용자와 공유하는 흐름이 하나의 서비스 안에서 자연스럽게 이어지지 않는 문제가 있습니다.

## 3. 해결 방향

- 검증된 촬영지 DB를 중심으로 콘텐츠와 장소를 연결합니다.
- 목록/검색/정렬/상세 화면을 모바일 중심으로 제공합니다.
- AI route generation은 DB에 존재하는 검증 장소 후보만 사용합니다.
- 사용자는 좋아요, 방문 상태, 저장 코스로 개인 여행 계획을 관리합니다.
- 커뮤니티와 알림으로 여행 경험 공유 흐름을 제공합니다.

## 4. 아키텍처

| 영역 | 설명 |
| --- | --- |
| Frontend | Vue/Vite 기반 모바일 우선 SPA |
| Backend | Spring Boot, MyBatis, MySQL 기반 public API |
| AI | Week 4 확장용 FastAPI 내부 서비스 |

아키텍처 원칙:
- frontend는 Spring Boot backend만 호출합니다.
- backend는 추후 `sceniK-ai`를 내부 호출합니다.
- frontend는 `sceniK-ai`를 직접 호출하지 않습니다.
- secret, API key, token은 저장소에 커밋하지 않습니다.

## 5. 데이터 모델

보고서에 포함할 핵심 테이블:
- `users`
- `contents`
- `places`
- `content_places`
- `content_likes`
- `place_likes`
- `place_visit_status`
- `ai_route_generations`
- `routes`
- `route_days`
- `route_places`
- `community_posts`
- `community_comments`
- `notifications`
- `place_reviews`

특이사항:
- `contentId`와 `placeId`는 문자열 ID입니다.
- `routeId`는 현재 workspace 계약상 문자열 기반입니다.
- `place_reviews`는 사용자와 장소 기준 중복 리뷰를 제한합니다.

## 6. 구현 범위

### 완료 또는 완료에 가까운 항목

- Auth, signup, login, logout, token refresh
- User profile/mypage hydration
- Content/Place list, search, detail
- Content/Place like
- Visit status
- Liked contents/places/visit status page
- AI route generation
- Route save/my routes/route detail
- Community posts/comments
- Notifications
- Place review API와 장소 상세 UI

### 부분 완료 항목

- map provider live QA
- review create/edit/delete live QA
- comment update/delete live QA
- route place add/update/delete/reorder live QA
- admin UI

### 보류 항목

- social login
- follow/following
- notification settings
- route public sharing
- real RAG/OpenAI/ChromaDB production integration

## 7. API 상태

Notion API table 업데이트 기준:

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 20 |
| Deferred | 15 |
| Deprecated | 14 |
| Not progress | 0 |

근거 문서:
- `backend/docs/api/scenik-api-status.md`
- `backend/docs/api/scenik-api-inventory.md`
- `backend/docs/api/notion-row-mapping.md`
- `backend/docs/api/notion-status-change-list.md`
- `backend/docs/api/notion-copy-summary.md`

## 8. AI 설계

Week 3:
- Spring Boot backend의 deterministic DB-backed route generation을 사용합니다.
- DB에 있는 검증 장소만 candidate로 사용합니다.
- 후보 장소가 없거나 잘못된 `placeId`가 들어오면 한국어 오류 메시지로 처리합니다.

Week 4:
- Spring Boot -> FastAPI 내부 계약 구현
- ChromaDB/vector store 도입
- embedding 생성
- RAG candidate retrieval
- OpenAI route quality improvement
- deterministic fallback 유지

근거 문서:
- `backend/docs/ai/ai-prompt-context-design.md`
- `backend/docs/ai/backend-fastapi-contract.md`
- `sceniK-ai/docs/week4-task-plan.md`

## 9. QA 결과

| 항목 | 결과 |
| --- | --- |
| Backend build | PASS |
| Frontend build | PASS |
| Frontend lint | PASS |
| Frontend smoke:course | PASS |
| Frontend smoke:errors | PASS |
| AI pytest | PASS |
| Deprecated API source scan | PASS |
| Frontend direct AI call scan | PASS |
| Secret pattern scan | PASS |
| Live E2E | 수동 실행 필요 |

실행한 명령:

```powershell
cd backend
.\gradlew.bat clean build

cd ..\frontend
npm run build
npm run lint
npm run smoke:course
npm run smoke:errors

cd ..\sceniK-ai
.\.venv\Scripts\python.exe -m pytest
```

## 10. 시연 시나리오

1. 회원가입 또는 로그인
2. 콘텐츠 목록 검색/정렬
3. 콘텐츠 상세 조회와 관련 장소 확인
4. 장소 상세 조회
5. 장소 좋아요와 방문 상태 설정
6. 장소 리뷰 작성/수정/삭제
7. AI 조건 입력
8. AI route generation
9. route save
10. my routes와 route detail 확인
11. community post create
12. comment create/edit/delete
13. notifications 확인
14. mypage 활동 상태 확인

## 11. 남은 한계

Critical:
- live E2E를 실제 backend/frontend 실행 상태에서 한 번 더 확인해야 합니다.
- map provider credential 설정 후 live QA가 필요합니다.

Recommended:
- review/comment 수정·삭제 live QA 결과를 제출 전에 캡처합니다.
- route place add/update/delete/reorder live QA를 수행합니다.
- 발표 자료에 ERD와 API 상태 표를 명확히 포함합니다.

Optional:
- my reviews page
- notification settings
- route public sharing
- follow/following

## 12. 결론

Week 3의 핵심 demo flow는 build/smoke/pytest 기준으로 안정적으로 통과했습니다. 평가에서 누락되기 쉬운 리뷰 기능, API canonical endpoint, Notion row mapping, 제출 증거 문서를 보강했으며, Week 4에서는 FastAPI, ChromaDB, embedding, RAG, OpenAI 기반 AI 품질 개선에 집중하면 됩니다.
