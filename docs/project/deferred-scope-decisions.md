# Deferred 범위 결정 문서

생성일: 2026-06-08

이 문서는 Week 3 마감 기준 `Deferred`로 유지할 Notion API 행과 보류 사유를 정리합니다. Deferred 항목은 미완성 실패가 아니라 MVP 범위 밖으로 명시적으로 분리한 작업입니다.

## Deferred에서 제외된 항목

| 항목 | 현재 상태 | 변경 사유 |
| --- | --- | --- |
| 팔로우 | In progress | `POST /api/users/{userId}/follow`와 커뮤니티 작성자 버튼을 최소 구현했습니다. |
| 언팔로우 | In progress | `DELETE /api/users/{userId}/follow`와 마이페이지 목록 액션을 최소 구현했습니다. |
| 팔로워 목록 조회 | In progress | `GET /api/users/{userId}/followers`와 마이페이지 팔로워 목록을 연결했습니다. |
| 팔로잉 목록 조회 | In progress | `GET /api/users/{userId}/following`, `GET /api/users/me/following`과 마이페이지 팔로잉 목록을 연결했습니다. |

| 기능명 | 현재 상태 | 보류 이유 | 후속 구현 시점 | 현재 대체 흐름 |
| --- | --- | --- | --- | --- |
| 카카오 소셜 로그인 | Deferred | Week 3 MVP에서는 이메일/비밀번호 인증을 기준으로 완료했으며, OAuth provider 설정과 callback 검증이 필요합니다. | Week 5 이후 인증 확장 | `POST /api/auth/signup`, `POST /api/auth/login` |
| 구글 소셜 로그인 | Deferred | OAuth consent, callback, provider token 검증이 필요해 Week 3 범위에서 제외했습니다. | Week 5 이후 인증 확장 | 이메일/비밀번호 로그인 |
| 회원 탈퇴 | Deferred | 사용자 데이터 보존, 게시글/댓글 처리, soft delete 정책을 먼저 정해야 합니다. | 운영 정책 확정 후 | 로그아웃과 프로필 수정 |
| 내 프로필 이미지 업로드 | Deferred | 파일 저장소, 확장자/용량 제한, 이미지 처리 정책이 필요합니다. | 프로필 고도화 단계 | 기본 아바타와 닉네임 표시 |
| 코스 공개 여부 변경 | Deferred | 공개 URL, 권한, 공유 범위, 검색 노출 정책이 필요합니다. | Route 공유 기능 고도화 단계 | 커뮤니티 게시글 작성으로 코스 경험 공유 |
| 댓글 신고 | Deferred | 게시글 신고는 구현되어 있으나 댓글 moderation workflow는 아직 없습니다. | 커뮤니티 moderation 단계 | 게시글 신고 |
| 알림 수신 설정 조회 | Deferred | Week 3 알림 범위는 목록 조회와 읽음 처리입니다. | 알림 개인화 단계 | 알림 목록 조회 |
| 알림 수신 설정 수정 | Deferred | 알림 유형별 설정 UI와 저장 정책이 필요합니다. | 알림 개인화 단계 | 전체 알림 읽음 처리 |
| 전체 사용자 목록 조회 | Deferred | 일반 사용자 데모 흐름과 무관한 admin 기능이며 관리자 권한 정책이 필요합니다. | Admin dashboard 단계 | 없음 |
| 사용자 계정 상태 변경 | Deferred | 계정 정지/활성화 정책, 감사 로그, 권한 검증이 필요합니다. | Admin dashboard 단계 | 없음 |
| 신고된 게시글 및 댓글 조회 | Deferred | moderation 전용 admin UI와 report 상태 모델이 필요합니다. | Admin moderation 단계 | 게시글 신고 저장 |
| 신고 처리 상태 변경 | Deferred | 신고 처리 상태와 운영 기준이 필요합니다. | Admin moderation 단계 | 게시글 신고 저장 |
| 신고된 게시글 검토 후 삭제 | Deferred | 신고 검토 workflow와 관리자 권한 검증이 필요합니다. | Admin moderation 단계 | 게시글 작성자 삭제 기능 |
| 신고된 댓글 검토 후 삭제 | Deferred | 댓글 신고와 moderation workflow가 아직 없습니다. | Admin moderation 단계 | 댓글 작성자 삭제 기능 |
| OpenAI/RAG 경로 품질 개선 | Deferred | 실제 OpenAI, embedding, ChromaDB, RAG 품질 검증은 Week 4 범위입니다. | Week 4 AI/RAG | Spring Boot deterministic route generation |
| 전체 장소 데이터 벡터 임베딩 | Deferred | vector store와 embedding batch pipeline이 필요합니다. | Week 4 AI/RAG | MySQL 장소 데이터 기반 deterministic 후보 선택 |
| 장소 데이터 벡터 임베딩 갱신 | Deferred | 장소 변경 시 재색인 정책과 internal API가 필요합니다. | Week 4 AI/RAG | seed/DB 데이터 직접 조회 |
| 임베딩 처리 상태 조회 | Deferred | embedding job 상태 저장소와 조회 endpoint가 필요합니다. | Week 4 AI/RAG | 수동 실행 로그 확인 |

## Deferred 유지 원칙

- Week 3 데모 핵심 흐름은 회원가입, 로그인, 콘텐츠/장소 탐색, 좋아요, 방문 상태, AI 경로 생성, 경로 저장, 커뮤니티, 알림입니다.
- Deferred 항목은 지금 구현하지 않아도 Week 3 MVP 설명과 시연을 막지 않습니다.
- Week 4에서는 FastAPI, ChromaDB, embedding, RAG, OpenAI 품질 개선에 집중하고, OAuth/Admin/Social 확장은 이후 범위로 분리합니다.
