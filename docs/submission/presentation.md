# sceniK 발표자료

생성일: 2026-06-09
상태: Markdown final본, PPT/PDF export 필요

## 1장. Title

**sceniK - K-content 촬영지 기반 AI 여행 코스 플래너**

- K-content와 실제 한국 여행 장소를 연결하는 모바일 서비스
- 검증된 촬영지 데이터 기반 탐색과 AI 코스 생성
- 저장, 편집, 커뮤니티 공유까지 이어지는 통합 여행 플로우

발표자 노트: sceniK는 팬심에서 시작한 관심을 실제 여행 계획과 기록으로 연결하는 서비스라는 점을 먼저 강조합니다.

## 2장. Problem

- 촬영지 정보가 여러 사이트와 SNS에 흩어져 있음
- 실제 방문 가능한 장소인지 검증하기 어려움
- 여러 장소를 현실적인 여행 동선으로 구성하기 번거로움
- 생성한 코스를 저장하고 공유하는 흐름이 분리되어 있음

추천 화면: 문제 정의 한 장짜리 다이어그램 또는 사용자 pain point 캡처

발표자 노트: 단순 장소 추천이 아니라 콘텐츠, 장소, 동선, 기록이 분리되어 있다는 문제를 설명합니다.

## 3장. Target Users

- 드라마, 영화, 예능 촬영지를 방문하고 싶은 K-content 팬
- 국내 여행지를 콘텐츠 테마로 찾고 싶은 여행자
- 친구·가족과 촬영지 투어를 계획하는 사용자
- 방문 경험을 리뷰와 커뮤니티로 공유하려는 사용자

발표자 노트: 관광 일반 사용자가 아니라 K-content 맥락이 있는 여행자를 중심으로 설명합니다.

## 4장. Service Concept

- 콘텐츠를 고르면 관련 촬영지를 보여줌
- 장소 상세에서 좋아요, 방문 상태, 리뷰를 관리
- AI가 검증 장소 기반 코스를 생성
- 코스를 저장·편집하고 커뮤니티에서 공유

추천 화면: 홈, 콘텐츠 상세, AI 결과 화면

발표자 노트: “콘텐츠 → 장소 → 코스 → 공유”의 연결성을 핵심 컨셉으로 잡습니다.

## 5장. Core Journey

1. 회원가입 또는 로그인
2. K-content 선택
3. 관련 촬영지 확인
4. AI 조건 입력과 코스 생성
5. 코스 저장·편집
6. 커뮤니티 공유와 마이페이지 관리

발표자 노트: 시연 순서와 동일하게 설명하면 평가자가 구현 범위를 쉽게 따라올 수 있습니다.

## 6장. K-content Exploration

- 콘텐츠 목록, 검색, 카테고리 필터, 정렬
- 콘텐츠 상세에서 관련 장소 연결
- 콘텐츠 좋아요와 마이페이지 좋아요 목록
- URL query sync로 검색/정렬 상태 복원

추천 화면: 콘텐츠 목록과 콘텐츠 상세

발표자 노트: 단순 static list가 아니라 backend-backed list/detail/like 흐름임을 설명합니다.

## 7장. Place Detail and Review

- 장소 상세, 관련 콘텐츠, 지도 preview
- 장소 좋아요와 방문 예정/완료 상태
- 리뷰 평균 평점, 리뷰 수, 평점 분포
- 리뷰 작성, 수정, 삭제, 중복 작성 방지

추천 화면: 장소 상세 리뷰 섹션, 리뷰 작성/수정 상태

발표자 노트: 평가에서 누락되기 쉬운 리뷰 CRUD를 명확히 보여줍니다.

## 8장. AI Course Generation

- 선택 콘텐츠와 여행 조건 기반 코스 생성
- DB의 검증 장소 후보만 사용
- 조건 요약과 추천 근거 표시
- 후보가 부족하거나 잘못된 장소 ID가 있으면 한국어 오류 처리

추천 화면: AI 조건 입력, AI loading, AI 결과

발표자 노트: 현재 Week 3는 deterministic DB-backed generation이며 Week 4에서 RAG/OpenAI로 품질을 확장합니다.

## 9장. Course Editing and Save

- 생성된 AI 코스 저장
- 저장한 코스 상세 조회
- 코스 제목, 장소 순서, 방문 시간 편집
- 삭제 확인 sheet와 저장/삭제 상태 안내

추천 화면: `CourseCrudPanel`, `CourseEditView`

발표자 노트: 이번 최종 pass에서 Course CRUD가 evaluator에게 보이도록 명시적인 관리 섹션을 강화했습니다.

## 10장. Community and Comments

- 게시글 목록, 작성, 상세
- 작성자 owner-only 수정/삭제
- 댓글 작성, 수정, 삭제
- 답글과 댓글 좋아요
- compact segmented sort UI

추천 화면: 커뮤니티 상세와 댓글 action sheet

발표자 노트: 게시글과 댓글이 모두 실제 API/store 흐름에 연결되어 있음을 설명합니다.

## 11장. Follow and Following

- 커뮤니티 작성자 follow/unfollow
- 마이페이지 팔로잉/팔로워 목록
- 맞팔 badge
- pending 상태와 자기 자신 팔로우 차단

추천 화면: 작성자 follow card, 팔로잉 목록

발표자 노트: live QA는 두 계정이 필요하므로 제출 전 캡처가 필요하다고 솔직히 말합니다.

## 12장. My Page and Profile

- 좋아요, 방문 상태, 저장 코스, 작성 글, 리뷰 집계
- 프로필 편집: 닉네임, 소개, 이메일 read-only
- 이미지 로컬 미리보기와 업로드 deferred 안내
- 콘텐츠/지역/테마 preference chips
- 저장 성공/실패 상태 안내

추천 화면: MyPage profile header, MyProfileEditView

발표자 노트: 서버 업로드를 구현하지 않은 이미지는 미리보기 전용이라고 명확히 분리했습니다.

## 13장. Architecture

```text
Vue/Vite frontend
  -> Spring Boot backend
      -> MySQL
      -> sceniK-ai FastAPI, Week 4 internal integration
```

- frontend는 backend만 호출
- backend는 public API와 DB 책임
- AI service는 Week 4 내부 연동 예정
- secret은 저장소에 커밋하지 않음

발표자 노트: frontend가 `sceniK-ai`를 직접 호출하지 않는 구조 원칙을 강조합니다.

## 14장. DB / Schema Overview

- `users`, `contents`, `places`, `content_places`
- likes, visit status, reviews
- routes, route_days, route_places
- community_posts, community_comments
- follows, notifications

추천 화면: ERD 또는 schema 요약 표

발표자 노트: `contentId`, `placeId`는 문자열 ID이고 route는 현재 workspace 계약상 string 기반임을 설명합니다.

## 15장. AI / API Usage

- Week 3: Spring Boot deterministic route generation
- Week 4: FastAPI, ChromaDB, embedding, RAG, OpenAI 품질 개선
- 외부 API/AI key는 문서에만 사용 범위 정리, 저장소 미포함
- canonical API endpoint는 Notion row mapping에 반영

추천 문서: [external-api-ai-usage.md](./external-api-ai-usage.md), [ai-prompt-context-design.md](../ai/ai-prompt-context-design.md)

발표자 노트: 현재 구현과 향후 계획을 분리해 과장하지 않습니다.

## 16장. QA and Reliability

- Frontend lint/build/smoke 통과
- Backend build 통과 기준으로 최종 확인
- deprecated API path scan
- frontend direct AI call scan
- secret pattern scan
- live browser screenshots는 수동 캡처 필요

추천 화면: QA 결과 표

발표자 노트: 자동 QA와 live screenshot의 차이를 분명하게 설명합니다.

## 17장. Rubric Evidence

- [rubric-response.md](./rubric-response.md)
- [evaluation-evidence-map.md](./evaluation-evidence-map.md)
- [submission-artifact-index.md](./submission-artifact-index.md)
- [notion-row-mapping.md](../api/notion-row-mapping.md)
- [frontend-prompt-coverage-improvement.md](../../../frontend/docs/qa/frontend-prompt-coverage-improvement.md)

발표자 노트: 평가자가 기능 근거를 바로 찾을 수 있도록 문서 링크를 보여줍니다.

## 18장. Demo Scenario

1. 로그인
2. 콘텐츠 검색과 상세
3. 장소 상세와 리뷰
4. AI 코스 생성
5. 코스 저장과 편집
6. 커뮤니티 게시글/댓글
7. 팔로우와 마이페이지

추천 문서: [demo-scenario.md](./demo-scenario.md)

발표자 노트: 시연이 길어지지 않도록 “콘텐츠 → 장소 → 코스 → 커뮤니티” 흐름으로 유지합니다.

## 19장. Limitations and Next Steps

- PDF/PPT export 필요
- live screenshot 수동 캡처 필요
- map provider live QA 필요
- 프로필 이미지 업로드는 deferred
- Week 4 RAG/OpenAI/ChromaDB 품질 개선 예정

발표자 노트: 남은 항목을 숨기지 말고 다음 작업으로 분리해 신뢰도를 높입니다.

## 20장. Closing

- sceniK는 K-content 팬의 관심을 실제 한국 여행으로 연결합니다.
- 검증 장소 기반 AI 코스로 환각 위험을 줄입니다.
- 저장, 편집, 커뮤니티 공유까지 이어지는 end-to-end 흐름을 구현했습니다.
- Week 4에서는 AI 품질과 RAG 기반 추천 정확도를 높입니다.

발표자 노트: “팬심이 여행 계획과 기록으로 이어지는 서비스”라는 메시지로 마무리합니다.
