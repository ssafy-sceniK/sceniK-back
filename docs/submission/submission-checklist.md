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
| final report Markdown | Markdown final 준비 | `backend/docs/submission/final-report.md` | outline은 참고용 |
| final report PDF | export 필요 | `backend/docs/submission/final-report.pdf` | fake placeholder 생성 금지, 실제 PDF export 후 확인 |
| presentation Markdown | Markdown final 준비 | `backend/docs/submission/presentation.md` | outline은 참고용 |
| presentation PDF/PPTX | export 필요 | `backend/docs/submission/presentation.pdf`, `backend/docs/submission/presentation.pptx` | 제출 포맷에 맞춰 둘 중 하나 또는 둘 다 생성 |
| demo scenario | 준비 완료 | `backend/docs/submission/demo-scenario.md` | 시연 순서 확인 |
| external API / AI usage | 준비 완료 | `backend/docs/submission/external-api-ai-usage.md` | Kakao/OpenAI 범위 명시 |
| test results | 준비 완료 | `backend/docs/qa/final-rubric-live-qa-checklist.md`, `backend/docs/qa`, 최종 보고 | build/smoke/pytest 결과 반영, live QA 항목 분리 |
| live QA evidence | 수동 캡처 필요 | `backend/docs/evidence/follow-live-qa-evidence.md`, `backend/docs/evidence/browser-live-qa-evidence.md` | follow two-account, review/detail/notification/community 캡처 계획 |
| submission artifact index | 준비 완료 | `backend/docs/submission/submission-artifact-index.md` | 산출물 위치와 제출 가능/outline/수동 캡처 필요 상태 구분 |
| known limitations | 준비 완료 | `backend/docs/project/deferred-scope-decisions.md` | Deferred 항목 설명 |
| future work | 준비 완료 | `sceniK-ai/docs/week4-task-plan.md` | Week 4 AI/RAG |

## 제출 전 확인

- 실제 secret이 포함되지 않았는지 확인합니다.
- `.venv`, `node_modules`, `dist`, `build` output을 커밋하지 않습니다.
- Notion API table은 `notion-row-mapping.md`와 `notion-api-status-update.csv` 기준으로 업데이트합니다.
- 발표 자료에는 review 기능, AI guardrail, dataset 출처, QA 결과를 명확히 넣습니다.
- `submission-artifact-index.md`에서 최종 보고서/발표자료 final 파일 링크가 `final-report.md`, `presentation.md`인지 확인합니다.
- `final-report.md`는 `backend/docs/submission/final-report.pdf`로 export합니다.
- `presentation.md`는 `backend/docs/submission/presentation.pdf` 또는 `backend/docs/submission/presentation.pptx`로 export합니다.
- `follow-live-qa-evidence.md`와 `browser-live-qa-evidence.md`의 수동 캡처 필요 항목을 제출 전 실제 스크린샷으로 보강합니다.
- 제출 직전 `final-rubric-live-qa-checklist.md`의 수동 확인 필요 항목을 실제 실행 환경에서 한 번 더 확인합니다.
- `backend/docs/qa/final-analyzer-feedback-hardening.md`에서 #01~#15 잔여 피드백 대응 상태를 확인합니다.
- 제출 직전 GitHub 최신 commit hash와 analyzer 재실행 결과를 이 체크리스트 또는 최종 보고에 반영합니다.
- live screenshot을 수행하지 않은 항목은 완료로 표기하지 않고 `수동 캡처 필요`로 유지합니다.
