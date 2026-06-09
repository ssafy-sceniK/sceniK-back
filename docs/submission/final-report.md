# sceniK 최종 보고서

생성일: 2026-06-09
상태: Markdown final본, PDF export 필요

## 1. 프로젝트 개요

서비스명은 `sceniK`입니다. sceniK는 K-content 팬이 드라마, 영화, 예능 속 촬영지를 실제 여행 장소로 탐색하고, 검증된 장소 데이터를 기반으로 AI 여행 코스를 생성·저장·편집·공유할 수 있도록 돕는 K-content 여행 플래너입니다.

해결하려는 문제는 세 가지입니다. 첫째, K-content 촬영지 정보가 여러 채널에 흩어져 있어 실제 방문 가능한 장소인지 확인하기 어렵습니다. 둘째, 콘텐츠와 장소를 찾더라도 여러 장소를 현실적인 여행 동선으로 구성하는 과정이 번거롭습니다. 셋째, 생성한 코스를 저장하고 커뮤니티에서 공유하는 흐름이 하나의 서비스 안에서 자연스럽게 이어지지 않습니다.

주요 사용자는 K-content 팬, 국내 여행을 준비하는 개인 여행자, 촬영지 투어를 계획하는 친구·가족 단위 사용자, 방문 경험을 기록하고 공유하려는 커뮤니티 사용자입니다.

## 2. 핵심 기능

| 영역 | 구현 내용 |
| --- | --- |
| K-content 탐색 | 콘텐츠 목록, 검색, 정렬, 상세, 관련 촬영지 연결 |
| 장소 탐색 | 장소 목록, 지역/카테고리 필터, 장소 상세, 지도 preview |
| 좋아요·방문 상태 | 콘텐츠/장소 좋아요, `WANT_TO_GO`, `VISITED`, 마이페이지 목록 |
| 리뷰·평점 | 장소 리뷰 목록, 작성, 수정, 삭제, 평균 평점과 분포 표시 |
| AI 여행 코스 | 선택 콘텐츠와 조건 기반 AI route generation, 검증 장소 guardrail |
| 코스 관리 | 코스 저장, 상세 조회, 편집, 공유 문구 복사, 삭제 |
| 커뮤니티 | 게시글 목록, 작성, 상세, 수정, 삭제, 댓글, 답글, 좋아요 |
| 팔로우 | 작성자 팔로우, 팔로잉/팔로워 목록, 맞팔 badge |
| 사용자 기능 | 회원가입, 이메일 중복 확인, 로그인, 로그아웃, 프로필, 마이페이지 |
| 알림 | 알림 목록, 읽음 처리, empty state |

## 3. 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | Vue, Vite, Pinia, Vue Router |
| Backend | Spring Boot, MyBatis |
| Database | MySQL |
| AI service | Week 4 확장용 FastAPI skeleton |
| API/AI 문서 | Spring Boot API inventory, backend-FastAPI contract, RAG/OpenAI 계획 문서 |

외부 API와 AI 사용 범위는 [external-api-ai-usage.md](./external-api-ai-usage.md)에 정리했습니다. 실제 API key, DB password, private token은 저장소에 커밋하지 않습니다.

## 4. 아키텍처

기본 호출 구조는 `frontend -> backend -> DB`입니다. frontend는 Spring Boot backend만 호출하며, `sceniK-ai`를 직접 호출하지 않습니다. Week 4에서는 backend가 내부적으로 FastAPI 기반 `sceniK-ai`를 호출하고, ChromaDB/embedding/RAG/OpenAI를 활용해 AI 추천 품질을 높이는 구조로 확장합니다.

```text
Vue/Vite frontend
  -> Spring Boot backend API
      -> MySQL
      -> sceniK-ai FastAPI, Week 4 internal integration
```

아키텍처 원칙:
- frontend는 `/api` 또는 `VITE_API_BASE_URL`을 통해 backend만 호출합니다.
- backend는 public API와 DB 영속성을 담당합니다.
- AI 추천은 DB의 검증 장소 후보를 벗어나지 않도록 guardrail을 둡니다.
- `contentId`, `placeId`는 문자열 ID를 사용합니다.

## 5. DB와 데이터셋

주요 테이블은 `users`, `contents`, `places`, `content_places`, `content_likes`, `place_likes`, `place_visit_status`, `place_reviews`, `ai_route_generations`, `routes`, `route_days`, `route_places`, `community_posts`, `community_comments`, `notifications`, `follows`입니다.

데이터셋은 K-content와 실제 촬영지 연결을 중심으로 구성했습니다. AI route generation은 임의 장소가 아니라 DB에 존재하는 검증 장소를 후보로 사용합니다. 상세 데이터셋 설명은 [dataset-summary.md](./dataset-summary.md), schema는 `backend/src/main/resources/schema.sql`, seed 데이터는 `backend/data/seed/`에서 확인할 수 있습니다.

## 6. 사용자 여정

1. 사용자는 회원가입 또는 로그인 후 개인 활동 상태를 불러옵니다.
2. 콘텐츠 목록에서 K-content를 검색·정렬하고 상세에서 관련 촬영지를 확인합니다.
3. 장소 상세에서 지도 preview, 관련 콘텐츠, 좋아요, 방문 상태, 리뷰를 확인합니다.
4. AI Course에서 콘텐츠와 여행 조건을 선택해 검증 장소 기반 코스를 생성합니다.
5. 생성된 코스를 저장하고, 내 코스에서 제목·장소 순서·방문 시간을 수정합니다.
6. 커뮤니티에 게시글을 작성하고 댓글, 답글, 좋아요, 팔로우로 상호작용합니다.
7. 마이페이지에서 좋아요, 방문 상태, 저장 코스, 작성 글, 리뷰, 프로필 취향을 관리합니다.

## 7. 루브릭 대응 요약

| 루브릭 | 상태 | 핵심 근거 |
| --- | --- | --- |
| #01 핵심 데이터 관리 CRUD | 보강 완료, admin UI 제외 | 코스 저장/수정/삭제, 게시글/댓글 CRUD, 리뷰 CRUD, [core-crud-evidence.md](../evidence/core-crud-evidence.md) |
| #02 목록 조회·검색·정렬 | 완료에 가까움 | 콘텐츠/장소/커뮤니티 query sync, 정렬, 더 보기 |
| #03 상세 정보 조회 | 완료에 가까움 | 콘텐츠/장소/코스/게시글 상세와 not-found 복구 |
| #04 리뷰 작성·조회 | 구현 완료, live QA 필요 | `PlaceReviewController`, `ReviewPanel`, [review-feature-evidence.md](../evidence/review-feature-evidence.md) |
| #05 로그인·로그아웃 | 완료 | Auth API, 세션 패널, 로그아웃 상태 안내 |
| #06 회원가입 | 완료 | 이메일 중복 확인, 비밀번호 정책, signup flow |
| #07 프로필 관리 | 보강 완료 | 이미지 로컬 미리보기, preference chips, 저장 feedback |
| #08 찜·즐겨찾기 | 완료에 가까움 | 콘텐츠/장소 좋아요, 마이페이지 목록, pending guard |
| #09 팔로우·팔로잉 | 구현 완료, live QA 필요 | author follow, 팔로워/팔로잉, 맞팔 badge |
| #10 게시판 CRUD | 완료에 가까움 | 게시글 작성/상세/수정/삭제 |
| #11 댓글 CRUD | 완료에 가까움 | 댓글 작성/수정/삭제, 답글, 댓글 좋아요 |
| #12 계획·챌린지 관리 | 완료에 가까움 | Route save/detail/edit/delete |
| #13 생성형 AI 활용 | 완료에 가까움 | AI 조건, route generation, 검증 장소 기반 추천 |
| #14 설계·기획 문서 | 제출 가능 | PRD, API, evidence map, document index |
| #15 제출물 완성도 | 보강 완료 | final report, presentation, artifact index |

상세 루브릭 근거는 [rubric-response.md](./rubric-response.md)와 [evaluation-evidence-map.md](./evaluation-evidence-map.md)를 기준으로 확인합니다.

## 8. QA 결과

| 항목 | 결과 | 비고 |
| --- | --- | --- |
| Frontend lint | PASS | `npm run lint` |
| Frontend build | PASS | `npm run build`, Vite chunk size warning만 발생 |
| Course smoke | PASS | `npm run smoke:course` |
| Error smoke | PASS | `npm run smoke:errors` |
| Backend build | PASS | `.\gradlew.bat clean build` |
| Frontend direct AI call scan | PASS | `src`, `scripts` 기준 직접 호출 없음 |
| Secret pattern scan | PASS | 실제 key/private token 패턴 없음 |
| Live browser QA | 수동 캡처 필요 | [browser-live-qa-evidence.md](../evidence/browser-live-qa-evidence.md) |

live QA 스크린샷은 아직 제출 전 수동 캡처가 필요합니다. 캡처 우선순위는 follow two-account, 리뷰 CRUD, 커뮤니티 댓글 action sheet, 코스 CRUD 상태, 프로필 이미지 미리보기와 preference chips입니다.

## 9. 한계 및 향후 계획

현재 한계:
- PDF/PPT export는 아직 생성하지 않았습니다.
- live browser QA screenshot은 수동 캡처가 필요합니다.
- map provider credential 기반 live QA는 별도 환경 확인이 필요합니다.
- 프로필 이미지는 현재 로컬 미리보기만 제공하며 서버 업로드는 추후 지원 범위입니다.

Week 4 계획:
- Spring Boot -> FastAPI 내부 호출 구현
- ChromaDB vector store 연결
- embedding 생성과 rebuild flow
- RAG candidate retrieval
- OpenAI 기반 route quality improvement
- deterministic fallback과 검증 장소 guardrail 유지

## 10. 제출 체크리스트

| 항목 | 상태 |
| --- | --- |
| GitHub 최신 commit 확인 | 필요 |
| 최종 보고서 Markdown | 준비 완료, PDF export 필요 |
| 발표자료 Markdown | 준비 완료, PPT/PDF export 필요 |
| 데이터셋 요약 | 준비 완료 |
| 외부 API/AI 문서 | 준비 완료 |
| API 상태와 Notion 매핑 | 준비 완료 |
| demo scenario | 준비 완료 |
| evidence map | 준비 완료 |
| live QA screenshot | 수동 캡처 필요 |
| secrets 미포함 확인 | 필요 |

## 11. 결론

sceniK는 K-content 탐색, 검증 장소 기반 AI 코스 생성, 코스 저장·편집, 커뮤니티 공유, 마이페이지 활동 관리까지 이어지는 핵심 사용자 여정을 구현했습니다. 최종 보강 pass에서는 남은 partial 항목이었던 Course CRUD, Profile management, Submission completeness를 analyzer/evaluator가 식별하기 쉽게 보강했습니다. 제출 전 마지막 단계는 live QA 캡처와 PDF/PPT export입니다.
