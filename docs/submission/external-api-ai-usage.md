# 외부 API 및 AI 사용 정리

생성일: 2026-06-08

## Week 3 현재 사용 방식

- frontend-facing AI route API는 Spring Boot의 `POST /api/ai/routes`입니다.
- 현재 route generation은 Spring Boot backend의 deterministic DB-backed 방식입니다.
- frontend는 `sceniK-ai`를 직접 호출하지 않습니다.
- 지도/geocode endpoint는 Spring Boot에 있으며, 외부 provider가 없을 때 fallback 응답을 유지합니다.

## 현재 AI 서비스 상태

`sceniK-ai`는 Week 4를 위한 FastAPI 내부 skeleton입니다.

확인된 endpoint:
- `GET /health`
- `POST /ai/routes`
- `POST /ai/embeddings/rebuild`
- `PUT /ai/embeddings/{placeId}`

현재 상태:
- production RAG는 아직 아닙니다.
- 실제 OpenAI 호출은 아직 없습니다.
- embedding/vector store는 in-memory stub입니다.
- 내부 API 인증은 `Internal-Api-Key` 헤더를 사용합니다.

## 아직 구현하지 않은 항목

- 실제 OpenAI 호출
- ChromaDB 또는 production vector store
- 실제 embedding 생성
- RAG retrieval/ranking
- Spring Boot -> FastAPI production callout
- AI 품질 평가 fixture

## Secret 관리 원칙

- 실제 `OPENAI_API_KEY`, Kakao key, `Internal-Api-Key`, DB password, private token은 commit하지 않습니다.
- `.env.example`에는 placeholder만 둘 수 있습니다.
- OpenAI 기능은 `ENABLE_OPENAI=true`일 때만 켜는 방식으로 관리합니다.
- OpenAI/RAG 실패 시 deterministic fallback을 유지합니다.

## 제출 시 설명 포인트

Week 3에서는 검증 장소 기반 deterministic AI route generation을 완성했고, Week 4에서는 FastAPI, ChromaDB, embedding, RAG, OpenAI를 이용해 품질을 개선할 예정입니다. production AI 품질 개선은 아직 완료로 표시하지 않습니다.
