# 제출물 체크리스트

생성일: 2026-06-08

| 항목 | 상태 | 위치 | 메모 |
| --- | --- | --- | --- |
| ERD | 준비 필요 | `backend/docs/project/PRD.md`, schema 기반 ERD 산출물 | 발표 자료에 ERD 이미지 또는 Mermaid 추가 필요 |
| API spec | 준비 완료 | `backend/docs/api/scenik-api-inventory.md`, `backend/docs/api/notion-row-mapping.md` | Notion API 테이블 업데이트 기준 |
| Backend source | 준비 완료 | `backend/src/main/java/com/scenik` | Spring Boot/MyBatis |
| Frontend source | 준비 완료 | `frontend/src` | Vue/Vite |
| AI source | 준비 완료 | `sceniK-ai/app` | FastAPI skeleton |
| DB schema | 준비 완료 | `backend/src/main/resources/schema.sql` | MySQL schema |
| seed dataset | 준비 완료 | `backend/data`, `frontend/src/data/mockData.js` | 제출 전 위치 확인 필요 |
| final report | 초안 준비 | `backend/docs/submission/final-report-outline.md` | 최종 문장 보강 필요 |
| presentation slides | 초안 준비 | `backend/docs/submission/presentation-outline.md` | slide-ready 구성으로 보강 |
| demo scenario | 준비 완료 | `backend/docs/submission/demo-scenario.md` | 시연 순서 확인 |
| external API / AI usage | 준비 완료 | `backend/docs/submission/external-api-ai-usage.md` | Kakao/OpenAI 범위 명시 |
| test results | 준비 완료 | `backend/docs/qa/final-rubric-live-qa-checklist.md`, `backend/docs/qa`, 최종 보고 | build/smoke/pytest 결과 반영, live QA 항목 분리 |
| known limitations | 준비 완료 | `backend/docs/project/deferred-scope-decisions.md` | Deferred 항목 설명 |
| future work | 준비 완료 | `sceniK-ai/docs/week4-task-plan.md` | Week 4 AI/RAG |

## 제출 전 확인

- 실제 secret이 포함되지 않았는지 확인합니다.
- `.venv`, `node_modules`, `dist`, `build` output을 커밋하지 않습니다.
- Notion API table은 `notion-row-mapping.md`와 `notion-api-status-update.csv` 기준으로 업데이트합니다.
- 발표 자료에는 review 기능, AI guardrail, dataset 출처, QA 결과를 명확히 넣습니다.
- 제출 직전 `final-rubric-live-qa-checklist.md`의 수동 확인 필요 항목을 실제 실행 환경에서 한 번 더 확인합니다.
