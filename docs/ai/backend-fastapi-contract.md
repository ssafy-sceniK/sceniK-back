# Spring Boot - FastAPI 내부 계약

생성일: 2026-06-08

이 문서는 Week 4에서 Spring Boot backend가 내부 FastAPI AI 서버를 호출할 때 사용할 계약 초안입니다. public API는 계속 Spring Boot가 소유하며, frontend는 `sceniK-ai`를 직접 호출하지 않습니다.

## 아키텍처 원칙

| 원칙 | 설명 |
| --- | --- |
| frontend 직접 호출 금지 | Vue frontend는 `/api` Spring Boot endpoint만 호출합니다. |
| backend가 public contract 소유 | 사용자-facing API schema와 오류 처리는 Spring Boot가 관리합니다. |
| FastAPI는 내부 서비스 | `sceniK-ai`는 backend 내부 호출 대상이며 public API가 아닙니다. |
| fallback 필수 | FastAPI/OpenAI/RAG가 실패해도 기존 deterministic DB route generation으로 대체합니다. |
| secret commit 금지 | `Internal-Api-Key`, `OPENAI_API_KEY`는 환경 변수/secret으로만 주입합니다. |

## Frontend에 노출되는 Spring Boot endpoint

```http
POST /api/ai/routes
Content-Type: application/json
Authorization: Bearer <accessToken>
```

현재 request schema:

```json
{
  "contentId": "C_DRAMA_001",
  "contentIds": ["C_DRAMA_001"],
  "region": "서울",
  "durationDays": 2,
  "startDate": "2026-06-10",
  "endDate": "2026-06-11",
  "dateRangeLabel": "1박 2일",
  "transport": "대중교통",
  "companion": "친구",
  "style": "감성 촬영지 투어",
  "budgetMin": 50000,
  "budgetMax": 150000,
  "budget": "5만~15만원"
}
```

Spring Boot response schema:

```json
{
  "generationId": "gen-...",
  "routeId": "gen-...",
  "title": "콘텐츠 지역 스타일 route",
  "summary": "검증 장소 기반 경로 설명",
  "contentId": "C_DRAMA_001",
  "contentIds": ["C_DRAMA_001"],
  "contentTitle": "콘텐츠 제목",
  "region": "서울",
  "durationDays": 2,
  "requestedDurationDays": 2,
  "transport": "대중교통",
  "companion": "친구",
  "style": "감성 촬영지 투어",
  "trustMessage": "검증 장소만 사용한다는 안내",
  "sourceType": "AI",
  "isPublic": false,
  "candidatePlaces": [],
  "days": [],
  "places": []
}
```

## 내부 FastAPI endpoint

```http
POST /ai/routes
Content-Type: application/json
Internal-Api-Key: <shared-secret>
```

FastAPI request schema:

```json
{
  "contentIds": ["C_DRAMA_001"],
  "region": "서울",
  "durationDays": 2,
  "transport": "대중교통",
  "companion": "친구",
  "style": "감성 촬영지 투어",
  "candidatePlaces": [
    {
      "placeId": "P_0001",
      "name": "장소명",
      "address": "주소",
      "region": "서울",
      "latitude": 37.5665,
      "longitude": 126.978,
      "thumbnailUrl": "https://...",
      "relatedContentId": "C_DRAMA_001",
      "relatedContentTitle": "콘텐츠 제목",
      "sceneDescription": "촬영 장면 설명",
      "metadata": {
        "category": "촬영지",
        "verified": true
      }
    }
  ]
}
```

FastAPI response schema:

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

## Backend 변환 책임

Spring Boot는 FastAPI 응답을 그대로 frontend에 노출하지 않고, 기존 `AiRouteDto`로 변환합니다.

변환 규칙:
- FastAPI `summary` -> Spring `summary`
- FastAPI `days[].places[].placeId` -> DB candidate place와 매칭
- `placeName`, `address`, `latitude`, `longitude`, `thumbnailUrl` 등 표시 필드는 Spring Boot DB candidate에서 채웁니다.
- FastAPI가 반환한 `placeId`가 candidate list에 없으면 해당 장소는 버립니다.
- 최종 route에 남는 장소가 없으면 deterministic fallback을 사용합니다.

## 인증 헤더

| Header | 방향 | 설명 |
| --- | --- | --- |
| `Authorization` | frontend -> Spring Boot | 사용자 인증 token입니다. |
| `Internal-Api-Key` | Spring Boot -> FastAPI | 내부 서비스 인증용 shared secret입니다. |

FastAPI 인증 실패:

```json
{
  "detail": "Invalid internal API key."
}
```

Spring Boot는 이 오류를 사용자에게 그대로 노출하지 않고 fallback 처리합니다.

## Timeout / Retry 정책

권장값:

| 항목 | 값 |
| --- | --- |
| connect timeout | 1초 |
| read timeout | 5초 |
| retry | 1회 이하 |
| circuit behavior | 연속 실패 시 일정 시간 deterministic fallback 사용 |

주의:
- AI route generation은 사용자 대기 시간이 길어질 수 있으므로 timeout을 짧게 시작합니다.
- retry가 길어져 frontend loading이 멈춘 것처럼 보이면 안 됩니다.

## Fallback 동작

fallback 조건:
- FastAPI connection refused
- timeout
- 401/503
- response schema mismatch
- candidate place validation 실패
- OpenAI/RAG 내부 오류

fallback 결과:
- 기존 Spring Boot deterministic DB route generation을 사용합니다.
- `trustMessage`에는 검증 장소 기반 경로라는 메시지를 유지합니다.
- log에는 FastAPI 실패 원인을 남기되 secret/header/body 민감값은 남기지 않습니다.

## 오류 처리 원칙

사용자-facing 오류는 Spring Boot 표준 error response를 사용합니다.

```json
{
  "code": "INVALID_REQUEST",
  "message": "입력값을 확인해 주세요.",
  "fieldErrors": []
}
```

AI 내부 오류는 원칙적으로 사용자에게 raw 500으로 노출하지 않습니다. 후보 장소가 전혀 없을 때만 기존 정책대로 404와 한국어 안내 메시지를 반환합니다.

## Week 4 구현 순서

1. Spring Boot에 FastAPI client 설정 추가
2. `SCENIK_AI_BASE_URL`, `INTERNAL_API_KEY`, timeout 환경 변수 추가
3. candidate place 조회 후 FastAPI request 생성
4. FastAPI response validation
5. DB candidate와 `placeId` 교차 검증
6. 실패 시 deterministic fallback
7. integration test와 unavailable fallback test 추가
