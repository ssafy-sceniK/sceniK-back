# Notion 복붙 요약

생성일: 2026-06-08

## 상태 카운트

| 상태 | 개수 |
| --- | ---: |
| Done | 49 |
| In progress | 20 |
| Deferred | 15 |
| Deprecated | 14 |
| Missing / Not progress | 0 |

## 프로젝트 최종 상태 코멘트

Week 3 핵심 API는 대부분 구현 및 프론트 연결이 완료되었습니다. Auth, User, Content, Place, AI route, Route save, Community, Notification 주요 흐름은 `Done`으로 정리할 수 있습니다. Review 수정/삭제와 댓글 수정/삭제는 UI 연결까지 진행되었지만 live QA 전이므로 `In progress`로 유지합니다. Route place 편집 live QA, map provider live QA, admin UI도 `In progress`로 남깁니다. Social login, follow/following, notification settings, route public sharing, real RAG/OpenAI/ChromaDB 작업은 Week 3 MVP 범위 밖이므로 `Deferred`로 표시합니다. 구버전 singular endpoint와 `/courses`, `/ai-routes`, `/notifications/read-all` 계열은 모두 `Deprecated`로 정리합니다.

## Notion에 붙여넣을 짧은 문장

Week 3 기준 행별 API 테이블 정리 완료: `Done 49`, `In progress 20`, `Deferred 15`, `Deprecated 14`, `Missing 0`. 핵심 사용자 플로우는 build/smoke 기준으로 구현 완료이며, live E2E는 `backend/api-tests/week3-live-e2e.http`로 수동 검증하면 됩니다. map live QA, route place 편집 live QA, review/comment 편집 live QA, admin UI만 후속 확인 대상으로 남았습니다. Week 4는 FastAPI, ChromaDB, embedding, RAG, OpenAI 품질 개선에 집중하면 됩니다.
