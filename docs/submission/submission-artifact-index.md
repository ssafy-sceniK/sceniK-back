# 제출 산출물 인덱스

생성일: 2026-06-09

이 문서는 sceniK 최종 제출 전 산출물의 실제 위치와 완성도를 한눈에 확인하기 위한 index입니다. 파일이 존재하지 않거나 outline만 있는 경우에는 제출 가능으로 표시하지 않습니다.

| 산출물 | 파일 위치 | 상태 | 설명 | 제출 전 확인 |
| --- | --- | --- | --- | --- |
| 최종 보고서 | [final-report-outline.md](./final-report-outline.md) | outline | 최종 보고서 구조와 포함 항목 초안입니다. 실제 제출용 문장/서식 확정이 필요합니다. | 최종 보고서 파일로 export 필요 |
| 발표자료 | [presentation-outline.md](./presentation-outline.md) | outline | 발표 흐름과 slide-ready 목차입니다. PPT/PDF 파일은 아직 별도 생성되지 않았습니다. | 발표자료 export 필요 |
| 시연 시나리오 | [demo-scenario.md](./demo-scenario.md) | 제출 가능 | 시연 순서와 핵심 사용자 흐름을 정리했습니다. | demo 계정/데이터와 경로 맞춤 확인 |
| 데이터셋 요약 | [dataset-summary.md](./dataset-summary.md) | 제출 가능 | 콘텐츠/장소 seed dataset 범위와 출처 설명입니다. | 최종 seed 데이터와 수량 재확인 |
| 외부 API/AI 사용 정리 | [external-api-ai-usage.md](./external-api-ai-usage.md) | 제출 가능 | Kakao/OpenAI/AI 사용 범위와 secret 미포함 정책을 정리했습니다. | 실제 key 미포함 재확인 |
| API 상태 문서 | [scenik-api-status.md](../api/scenik-api-status.md) | 제출 가능 | Week 2/3/4 준비도, Done/In progress/Deferred/Deprecated 상태 정리입니다. | Notion 테이블과 상태 일치 확인 |
| Notion API 매핑 | [notion-row-mapping.md](../api/notion-row-mapping.md) | 제출 가능 | Notion API row별 canonical endpoint와 상태 매핑입니다. | 모든 Notion row 1회 매핑 확인 |
| 루브릭 응답 | [rubric-response.md](./rubric-response.md) | 제출 가능 | #01~#15 루브릭별 구현/한계/보강 내용 정리입니다. | analyzer 재실행 결과 반영 |
| 평가 증거 맵 | [evaluation-evidence-map.md](./evaluation-evidence-map.md) | 제출 가능 | 루브릭별 backend/frontend/docs 근거 연결표입니다. | 새 증거 문서 링크 확인 |
| live QA 체크리스트 | [final-rubric-live-qa-checklist.md](../qa/final-rubric-live-qa-checklist.md) | 제출 가능, 수동 확인 남음 | 자동 QA와 수동 live QA 항목을 분리했습니다. | 실제 live QA 완료 항목 상태 갱신 |
| follow live QA evidence | [follow-live-qa-evidence.md](../evidence/follow-live-qa-evidence.md) | 수동 캡처 필요 | 두 계정 follow/unfollow/mutual badge 시나리오와 캡처 계획입니다. | screenshots/follow-two-account 캡처 추가 |
| browser live QA evidence | [browser-live-qa-evidence.md](../evidence/browser-live-qa-evidence.md) | 수동 캡처 필요 | 리뷰/상세/알림/커뮤니티 브라우저 QA 캡처 계획입니다. | screenshots/browser-live-qa 캡처 추가 |
| review evidence | [review-feature-evidence.md](../evidence/review-feature-evidence.md) | 제출 가능, live QA 필요 | 장소 리뷰 목록/작성/수정/삭제, 콘텐츠 상세의 관련 장소 리뷰 안내 근거입니다. | 실제 place review 시나리오 캡처 |
| community/comment evidence | [community-comment-evidence.md](../evidence/community-comment-evidence.md) | 제출 가능, live QA 필요 | 게시글 CRUD, 댓글 CRUD, 답글, 댓글 좋아요 근거입니다. | 작성자 권한 시나리오 캡처 |
| auth/profile evidence | [auth-profile-evidence.md](../evidence/auth-profile-evidence.md) | 제출 가능 | 회원가입, 로그인/로그아웃, 프로필/취향 근거입니다. | demo 계정으로 로그인 흐름 확인 |
| core CRUD evidence | [core-crud-evidence.md](../evidence/core-crud-evidence.md) | 제출 가능 | 핵심 사용자-facing CRUD와 backend-only admin 범위 분리입니다. | admin UI 부재 설명 유지 |
| AI prompt/context design | [ai-prompt-context-design.md](../ai/ai-prompt-context-design.md) | 제출 가능 | deterministic AI route와 Week 4 RAG/OpenAI 전환 설계입니다. | 실제 OpenAI 미연동 범위 설명 |
| backend-FastAPI contract | [backend-fastapi-contract.md](../ai/backend-fastapi-contract.md) | 제출 가능 | Spring Boot와 FastAPI 내부 연동 계약 초안입니다. | Week 4 이전 production 연동 아님 명시 |
| Week 4 AI plan | [week4-task-plan.md](../../../sceniK-ai/docs/week4-task-plan.md) | 제출 가능 | ChromaDB, embedding, RAG, OpenAI 품질 개선 계획입니다. | `sceniK-ai` repo 연결 후 위치 재확인 |
| Week 4 AI readiness | [week4-ai-readiness.md](../../../sceniK-ai/docs/week4-ai-readiness.md) | 제출 가능 | Week 4 착수 전 준비도와 gap 정리입니다. | AI repo 독립성 확인 |

## 제출 전 마지막 확인

- analyzer를 다시 실행해 최신 점수와 Partial 항목을 확인합니다.
- follow two-account live QA screenshot을 추가합니다.
- review/detail/notification/community browser QA screenshot을 추가합니다.
- demo account credentials를 준비하되 repository에는 커밋하지 않습니다.
- 최종 보고서를 제출 포맷으로 export합니다.
- 발표자료를 PPT/PDF로 export합니다.
- `.env`, API key, DB password, private token이 포함되지 않았는지 확인합니다.
- GitHub `frontend/main`, `backend/main`, `sceniK-ai/main`의 최신 commit을 확인합니다.
- `frontend/dist`, `backend/build`, `.gradle`, `.venv`, `node_modules`, `logs`가 stage되지 않았는지 확인합니다.

## 현재 남은 제출 리스크

- 최종 보고서와 발표자료는 outline 상태이므로 실제 제출 파일로 export해야 합니다.
- live browser screenshot은 아직 수동 캡처 필요 상태입니다.
- `sceniK-ai`는 현재 로컬 workspace에서 Git 저장소로 인식되지 않는 상태였으므로, 별도 repo 연결을 제출 전 확인해야 합니다.
