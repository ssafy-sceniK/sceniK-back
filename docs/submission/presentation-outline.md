# sceniK 발표 자료 개요

상태: 초안/참고 문서입니다. 실제 발표자료 Markdown final본은 [presentation.md](./presentation.md)를 사용합니다.

생성일: 2026-06-08

이 문서는 최종 발표 슬라이드 제작자가 바로 옮길 수 있도록 14장 구성으로 정리한 발표 개요입니다.

## 1장. 제목

sceniK - K-content 촬영지 기반 여행 코스 플래너

핵심 메시지:
- 검증된 K-content 촬영지 데이터를 기반으로 탐색, AI 코스 생성, 저장, 커뮤니티 공유까지 연결하는 모바일 서비스입니다.

## 2장. 문제 정의

K-content 팬은 드라마, 예능, 영화 속 촬영지를 실제로 방문하고 싶어 하지만 다음 문제가 있습니다.

- 촬영지 정보가 여러 채널에 흩어져 있습니다.
- 실제 방문 가능한 장소인지 검증하기 어렵습니다.
- 여러 장소를 여행 동선으로 묶는 과정이 번거롭습니다.
- 생성한 코스를 저장하고 다른 사용자와 공유하는 흐름이 분리되어 있습니다.

## 3장. 서비스 개요

sceniK는 콘텐츠 중심 탐색에서 장소 상세, AI 추천 코스, 저장 경로, 커뮤니티 공유까지 이어지는 K-content 여행 플래너입니다.

핵심 가치:
- 검증된 촬영지 기반 탐색
- 콘텐츠와 장소의 자연스러운 연결
- AI route generation
- 개인화된 저장/방문 상태 관리
- 여행 경험을 공유하는 커뮤니티

## 4장. 핵심 사용자 플로우

대표 플로우:

1. 콘텐츠 목록에서 관심 K-content를 탐색합니다.
2. 콘텐츠 상세에서 관련 촬영지를 확인합니다.
3. 장소 상세에서 위치, 관련 콘텐츠, 리뷰를 확인합니다.
4. 좋아요 또는 방문 상태를 저장합니다.
5. AI 조건을 입력해 코스를 생성합니다.
6. 생성된 코스를 저장하고 내 코스에서 다시 확인합니다.
7. 커뮤니티에 여행 경험을 공유하고 댓글/알림을 확인합니다.

## 5장. 시스템 아키텍처

구성:
- `frontend/`: Vue/Vite 모바일 우선 SPA
- `backend/`: Spring Boot, MyBatis, MySQL 기반 public API
- `sceniK-ai/`: Week 4 확장을 위한 FastAPI 내부 AI 서비스

원칙:
- frontend는 Spring Boot backend만 호출합니다.
- backend는 추후 내부적으로 `sceniK-ai`를 호출합니다.
- `contentId`, `placeId`는 `C_DRAMA_001`, `P_0001` 같은 문자열 ID를 사용합니다.
- AI 추천은 DB의 검증된 장소 후보만 사용하도록 제한합니다.

## 6장. ERD

발표에 포함할 핵심 엔티티:
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

강조할 관계:
- 콘텐츠와 장소는 다대다 관계입니다.
- 사용자는 콘텐츠/장소 좋아요와 방문 상태를 가집니다.
- AI 생성 결과는 저장 경로로 이어집니다.
- 커뮤니티 게시글과 댓글은 사용자 활동, 알림과 연결됩니다.
- 장소 리뷰는 사용자와 장소 기준으로 중복 작성을 방지합니다.

## 7장. 데이터셋

데이터셋 설명:
- K-content별 촬영지와 관련 장소 데이터를 seed로 구성합니다.
- AI route generation은 임의 장소가 아니라 DB에 있는 검증 장소만 후보로 사용합니다.
- map/provider 값이 없거나 외부 연동이 불가한 경우에도 DB 좌표와 fallback 표시를 유지합니다.

제출 근거:
- `backend/docs/submission/dataset-summary.md`
- `backend/src/main/resources/schema.sql`
- `backend/data/seed/`

## 8장. API

Notion API 정리 결과:

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 20 |
| Deferred | 15 |
| Deprecated | 14 |
| Not progress | 0 |

강조할 API 범위:
- Auth/User/Mypage
- Content/Place 탐색과 상세
- 좋아요/방문 상태
- 장소 리뷰
- AI route generation
- Route save/my routes
- Community posts/comments
- Notifications

제출 근거:
- `backend/docs/api/scenik-api-status.md`
- `backend/docs/api/scenik-api-inventory.md`
- `backend/docs/api/notion-row-mapping.md`
- `backend/docs/api/notion-api-status-update.csv`

## 9장. AI 추천 구조

Week 3 현재 구조:
- Spring Boot backend가 DB의 검증 장소를 기반으로 deterministic route를 생성합니다.
- 후보 장소가 없으면 한국어 오류 메시지로 사용자에게 안내합니다.
- unknown `placeId`는 저장 단계에서 거부합니다.

Week 4 확장 구조:
- Spring Boot -> FastAPI 내부 호출
- ChromaDB/vector store
- embedding 생성
- RAG candidate retrieval
- OpenAI route quality improvement
- deterministic fallback 유지

제출 근거:
- `backend/docs/ai/ai-prompt-context-design.md`
- `backend/docs/ai/backend-fastapi-contract.md`
- `sceniK-ai/docs/week4-task-plan.md`

## 10장. 주요 화면

발표 화면 후보:
- 홈 화면
- 콘텐츠 목록/상세
- 장소 목록/상세
- 장소 리뷰 작성/수정/삭제
- AI 조건 입력
- AI 결과
- 저장 코스 상세
- 커뮤니티 목록/상세/작성
- 마이페이지
- 알림

강조:
- 모바일 앱 형태의 간결한 UI
- K-content 여행 분위기에 맞춘 시각 톤
- backend-backed data hydration

## 11장. 시연 시나리오

추천 demo flow:

1. 회원가입 또는 로그인
2. 콘텐츠 목록 검색/정렬
3. 콘텐츠 상세에서 관련 장소 확인
4. 장소 상세에서 좋아요, 방문 예정, 리뷰 작성
5. AI 조건 입력 후 코스 생성
6. 코스 저장 후 내 코스에서 확인
7. 커뮤니티 게시글 작성
8. 댓글 작성/수정/삭제
9. 알림과 마이페이지 확인

제출 근거:
- `backend/docs/submission/demo-scenario.md`
- `backend/api-tests/week3-live-e2e.http`
- `backend/docs/qa/final-rubric-live-qa-checklist.md`

## 12장. 트러블슈팅

주요 이슈와 대응:
- mockData fallback 제거 후 backend hydration 기준으로 상태를 정리했습니다.
- 기존 Notion API의 구버전 경로를 canonical endpoint로 매핑했습니다.
- 리뷰 기능이 평가에서 누락되지 않도록 controller, service, schema, frontend, QA 근거를 별도 문서화했습니다.
- map provider live QA는 credential/server 조건 때문에 `In progress`로 분리했습니다.
- follow/following은 Week 3 핵심 MVP 밖으로 판단해 `Deferred`로 문서화했습니다.

## 13장. 성과와 한계

성과:
- Week 2 완료율: 95%
- Week 3 완료율: 90%
- Week 4 준비도: 55%
- 주요 demo flow build/smoke/pytest 통과
- 리뷰 기능과 제출 증거 문서 보강

한계:
- live E2E는 수동 실행 필요
- map provider live QA 미실행
- review/comment 수정·삭제는 UI와 API가 연결되었지만 live QA 필요
- follow/following, social login, notification settings는 Deferred
- 실제 RAG/OpenAI 품질 개선은 Week 4 범위

## 14장. 향후 계획

Critical:
- Spring Boot -> FastAPI 내부 호출 계약 구현
- ChromaDB/vector store 연결
- embedding 생성과 rebuild flow
- RAG가 검증된 DB 장소만 반환하도록 guardrail 구현
- OpenAI route quality improvement

Recommended:
- map provider live QA
- review/comment 수정·삭제 live QA
- AI 추천 품질 평가 fixture 추가

Optional:
- my reviews page
- notification settings
- route public sharing
- follow/following social layer
