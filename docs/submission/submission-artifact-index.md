# 제출 산출물 인덱스

생성일: 2026-06-09

이 문서는 sceniK 최종 제출 산출물의 실제 위치와 제출 가능 상태를 한눈에 확인하기 위한 index입니다. Markdown final본과 PDF/PPT export 여부를 분리해 표시합니다.

| 산출물 | 파일 위치 | 상태 | 설명 | 제출 전 확인 |
| --- | --- | --- | --- | --- |
| 최종 보고서 Markdown final본 | [final-report.md](./final-report.md) | 제출 가능 | 프로젝트 개요, 기능, 기술 스택, 아키텍처, DB, 사용자 여정, 루브릭, QA, 한계를 포함한 Markdown final본입니다. | 제출 포맷에 맞춰 PDF export |
| 최종 보고서 PDF export | `backend/docs/submission/final-report.pdf` | export 필요 | `final-report.md`에서 생성할 최종 제출용 PDF입니다. 현재 fake placeholder는 만들지 않습니다. | PDF 생성 후 파일 존재 확인 |
| 발표자료 Markdown final본 | [presentation.md](./presentation.md) | 제출 가능 | 20장 구성의 발표용 Markdown final본입니다. | PPT 또는 PDF로 변환 |
| 발표자료 PDF export | `backend/docs/submission/presentation.pdf` | export 필요 | `presentation.md`에서 생성할 발표 제출용 PDF입니다. 현재 fake placeholder는 만들지 않습니다. | PDF 생성 후 파일 존재 확인 |
| 발표자료 PPTX export | `backend/docs/submission/presentation.pptx` | export 필요 | 발표 환경에서 PPTX가 요구될 경우 생성할 파일입니다. 현재 fake placeholder는 만들지 않습니다. | PPTX 생성 후 파일 존재 확인 |
| 최종 보고서 outline | [final-report-outline.md](./final-report-outline.md) | 초안/참고 | final report 작성에 사용한 구조 초안입니다. | 최종 제출물로 사용하지 않음 |
| 발표자료 outline | [presentation-outline.md](./presentation-outline.md) | 초안/참고 | slide 구성 초안입니다. | 최종 제출물로 사용하지 않음 |
| 시연 시나리오 | [demo-scenario.md](./demo-scenario.md) | 제출 가능 | 시연 순서와 핵심 사용자 흐름입니다. | demo 계정/데이터와 경로 맞춤 확인 |
| 데이터셋 요약 | [dataset-summary.md](./dataset-summary.md) | 제출 가능 | 콘텐츠/장소 seed dataset 범위와 설명입니다. | 최종 seed 데이터 수량 재확인 |
| 외부 API/AI 사용 정리 | [external-api-ai-usage.md](./external-api-ai-usage.md) | 제출 가능 | Kakao/OpenAI/AI 사용 범위와 secret 미포함 정책입니다. | 실제 key 미포함 재확인 |
| API 상태 문서 | [scenik-api-status.md](../api/scenik-api-status.md) | 제출 가능 | Week 2/3/4 준비도와 API 상태 요약입니다. | Notion 테이블 상태와 일치 확인 |
| Notion API 매핑 | [notion-row-mapping.md](../api/notion-row-mapping.md) | 제출 가능 | Notion API row별 canonical endpoint와 상태 매핑입니다. | 모든 Notion row 1회 매핑 확인 |
| Notion 상태 변경 목록 | [notion-status-change-list.md](../api/notion-status-change-list.md) | 제출 가능 | Done/In progress/Deferred/Deprecated/Not progress 변경 목록입니다. | Notion 업데이트 후 상태 재확인 |
| 루브릭 응답 | [rubric-response.md](./rubric-response.md) | 제출 가능 | #01~#15 루브릭별 구현/한계/보강 내용입니다. | analyzer 재실행 결과 반영 |
| 평가 증거 맵 | [evaluation-evidence-map.md](./evaluation-evidence-map.md) | 제출 가능 | 루브릭별 backend/frontend/docs 근거 연결표입니다. | 새 final 파일 링크 확인 |
| QA 체크리스트 | [final-rubric-live-qa-checklist.md](../qa/final-rubric-live-qa-checklist.md) | 제출 가능, 수동 확인 남음 | 자동 QA와 live QA 항목을 분리했습니다. | 실제 live QA 완료 항목 상태 갱신 |
| follow live QA evidence | [follow-live-qa-evidence.md](../evidence/follow-live-qa-evidence.md) | 수동 캡처 필요 | 두 계정 follow/unfollow/mutual badge 시나리오입니다. | screenshots/follow-two-account 캡처 추가 |
| browser live QA evidence | [browser-live-qa-evidence.md](../evidence/browser-live-qa-evidence.md) | 수동 캡처 필요 | 리뷰/상세/알림/커뮤니티/코스/프로필 캡처 계획입니다. | screenshots/browser-live-qa 캡처 추가 |
| review evidence | [review-feature-evidence.md](../evidence/review-feature-evidence.md) | 제출 가능, live QA 필요 | 장소 리뷰 목록/작성/수정/삭제 근거입니다. | 실제 place review 시나리오 캡처 |
| community comment evidence | [community-comment-evidence.md](../evidence/community-comment-evidence.md) | 제출 가능, live QA 필요 | 게시글 CRUD, 댓글 CRUD, 답글, 댓글 좋아요 근거입니다. | 작성자 권한 시나리오 캡처 |
| auth/profile evidence | [auth-profile-evidence.md](../evidence/auth-profile-evidence.md) | 제출 가능 | 회원가입, 로그인/로그아웃, 프로필/취향 근거입니다. | 프로필 preview/preference 화면 캡처 |
| core CRUD evidence | [core-crud-evidence.md](../evidence/core-crud-evidence.md) | 제출 가능 | 사용자-facing CRUD와 backend-only admin 범위 분리입니다. | 코스 CRUD 보강 상태 확인 |
| AI prompt/context design | [ai-prompt-context-design.md](../ai/ai-prompt-context-design.md) | 제출 가능 | deterministic AI route와 Week 4 RAG/OpenAI 전환 설계입니다. | 실제 OpenAI 미연동 범위 설명 |
| backend-FastAPI contract | [backend-fastapi-contract.md](../ai/backend-fastapi-contract.md) | 제출 가능 | Spring Boot와 FastAPI 내부 연동 계약 초안입니다. | Week 4 이전 production 연동 아님 명시 |
| Week 4 AI plan | [week4-task-plan.md](../../../sceniK-ai/docs/week4-task-plan.md) | 제출 가능, repo 위치 확인 필요 | ChromaDB, embedding, RAG, OpenAI 품질 개선 계획입니다. | `sceniK-ai` 독립 repo 연결 재확인 |
| Week 4 AI readiness | [week4-ai-readiness.md](../../../sceniK-ai/docs/week4-ai-readiness.md) | 제출 가능, repo 위치 확인 필요 | Week 4 착수 전 준비도와 gap 정리입니다. | AI repo 독립성 확인 |
| frontend prompt coverage doc | [frontend-prompt-coverage-improvement.md](../../../frontend/docs/qa/frontend-prompt-coverage-improvement.md) | 제출 가능 | 프론트 analyzer visibility 보강 내역입니다. | 최신 프론트 commit 반영 확인 |

## 제출 전 마지막 확인

- analyzer를 다시 실행해 최신 점수와 Partial 항목을 확인합니다.
- 최종 보고서를 `backend/docs/submission/final-report.pdf`로 export합니다.
- 발표자료를 `backend/docs/submission/presentation.pdf` 또는 `backend/docs/submission/presentation.pptx`로 export합니다.
- follow two-account live QA screenshot을 추가합니다.
- review/detail/course/profile/community browser QA screenshot을 추가합니다.
- demo account credentials를 준비하되 repository에는 커밋하지 않습니다.
- `.env`, API key, DB password, private token이 포함되지 않았는지 확인합니다.
- `frontend/dist`, `backend/build`, `.gradle`, `.venv`, `node_modules`, `logs`가 stage되지 않았는지 확인합니다.

## 현재 남은 제출 리스크

- PDF/PPT export는 아직 수행하지 않았습니다.
- live browser screenshot은 아직 수동 캡처 필요 상태입니다.
- `sceniK-ai`는 로컬 workspace에서 Git 저장소로 인식되지 않았으므로 제출 전 독립 repo 연결 확인이 필요합니다.

## Planned export path 고정

| 변환 대상 | 계획된 export 경로 | 현재 상태 |
| --- | --- | --- |
| `backend/docs/submission/final-report.md` | `backend/docs/submission/final-report.pdf` | export 필요 |
| `backend/docs/submission/presentation.md` | `backend/docs/submission/presentation.pdf` | export 필요 |
| `backend/docs/submission/presentation.md` | `backend/docs/submission/presentation.pptx` | PPTX 제출이 필요할 경우 export 필요 |

현재 저장소에는 위 PDF/PPTX 파일을 fake placeholder로 만들지 않았습니다. 실제 변환이 완료된 뒤 파일 존재 여부와 제출 포맷을 다시 확인합니다.
