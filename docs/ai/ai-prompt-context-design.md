# AI Prompt / Context 설계 문서

생성일: 2026-06-09

이 문서는 루브릭 #13 생성형 AI 활용 기능의 프롬프트/컨텍스트 설계를 설명합니다. Week 3는 Spring Boot의 deterministic DB-backed route generation을 사용하며, Week 4에서 FastAPI, ChromaDB, embedding, RAG, OpenAI를 붙여 품질을 개선합니다.

## 현재 동작

- Public endpoint는 `POST /api/ai/routes`입니다.
- Frontend는 Spring Boot backend만 호출합니다.
- Spring Boot는 MySQL에 저장된 검증 장소 후보만 사용합니다.
- 현재는 OpenAI 호출 없이 deterministic ordering과 조건 기반 후보 선택으로 route를 생성합니다.
- 생성 결과는 `POST /api/routes`로 저장할 수 있습니다.
- Frontend는 조건 입력, 로딩, 결과 화면에서 AI가 참고하는 입력 조건과 검증 장소 기반 추천 근거를 사용자에게 표시합니다.

## Prompt Input Fields

| Field | 설명 |
| --- | --- |
| `contentIds` | 사용자가 선택한 K-content ID 목록 |
| `region` | 여행 지역 |
| `durationDays` | 여행 일수 |
| `startDate`, `endDate` | 날짜 조건 |
| `transport` | 이동 수단 |
| `companion` | 동행 유형 |
| `style` | 여행 스타일/테마 |
| `budgetMin`, `budgetMax` | 예산 범위 |
| `candidatePlaces` | backend DB에서 검증한 장소 후보 |

## Frontend 표시 컨텍스트

| 화면 | 표시 내용 |
| --- | --- |
| `CourseConditionsView.vue` | 선택 콘텐츠, 지역, 여행 기간, 이동 수단, 예산/테마 |
| `AiLoadingView.vue` | 지역, 일수, 이동, 테마, 예산 |
| `AiResultView.vue` | 검증된 장소 데이터 기반 추천, 추천 이유, 예상 체류 시간, 이동 팁 |

## Expected JSON Output

```json
{
  "summary": "추천 경로 요약",
  "days": [
    {
      "dayNumber": 1,
      "places": [
        {
          "placeId": "P_0001",
          "visitOrder": 1,
          "recommendReason": "추천 이유",
          "estimatedStayMinutes": 60,
          "estimatedTravelMinutes": 25,
          "travelTip": "방문 팁"
        }
      ]
    }
  ]
}
```

## Guardrails

- AI는 DB에 존재하는 검증 장소만 추천해야 합니다.
- 응답의 모든 `placeId`는 backend candidate list에 있어야 합니다.
- 알 수 없는 `placeId`는 거부하거나 결과에서 제거합니다.
- JSON schema validation에 실패하면 deterministic fallback을 사용합니다.
- OpenAI/RAG/ChromaDB 실패는 사용자에게 raw error로 노출하지 않습니다.
- secret, prompt 내부 key, provider raw error는 응답에 포함하지 않습니다.

## Week 4 RAG Context Plan

1. MySQL 장소 데이터에서 `embedding_text`를 구성합니다.
2. `contentId`, `placeId`, `region`, `category`, `verified` metadata를 함께 저장합니다.
3. OpenAI embedding으로 vector를 생성합니다.
4. ChromaDB에서 조건과 style query로 후보를 검색합니다.
5. 검색 결과를 backend DB candidate list와 교차 검증합니다.
6. 검증된 후보만 prompt context에 넣습니다.
7. AI 결과를 `AiRouteDto` 형태로 변환합니다.

## Fallback Behavior

| 실패 상황 | 처리 |
| --- | --- |
| FastAPI 연결 실패 | Spring Boot deterministic route generation |
| `Internal-Api-Key` 인증 실패 | fallback 후 server log 기록 |
| OpenAI timeout/quota | fallback |
| ChromaDB retrieval 실패 | fallback |
| JSON schema mismatch | fallback |
| unknown `placeId` | 해당 장소 제거, 결과가 비면 fallback |

## Week 4 Test Fixtures

| Fixture | 목적 |
| --- | --- |
| 서울 1일 감성 촬영지 | 기본 route 품질 비교 |
| 부산 2일 바다 배경 명소 | 지역별 후보 검색 검증 |
| 제주 3일 자연/카페 | duration/day 분배 검증 |
| 없는 contentId | 후보 없음 fallback 검증 |
| AI 서버 unavailable | Spring Boot fallback 검증 |
