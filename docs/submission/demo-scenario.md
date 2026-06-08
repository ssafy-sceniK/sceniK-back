# 데모 시나리오

생성일: 2026-06-08

## 기본 데모 흐름

1. 회원가입 또는 로그인합니다.
2. 홈/탐색 화면에서 K-content를 확인합니다.
3. 콘텐츠 상세 화면에서 관련 촬영지를 확인합니다.
4. 장소 상세 화면으로 이동합니다.
5. 장소 좋아요를 누릅니다.
6. 방문 상태를 `WANT_TO_GO`로 저장합니다.
7. 방문 상태를 `VISITED`로 변경합니다.
8. 장소 리뷰를 작성합니다.
9. 선택한 콘텐츠/지역 조건으로 AI 코스 생성을 시작합니다.
10. 생성된 AI 코스를 확인합니다.
11. 생성된 코스를 저장합니다.
12. 마이페이지 또는 내 코스 목록에서 저장 경로를 확인합니다.
13. 저장한 route를 기반으로 커뮤니티 게시글을 작성합니다.
14. 게시글 상세에서 댓글을 작성합니다.
15. 알림 목록을 확인하고 읽음 처리합니다.
16. 마이페이지에서 좋아요 콘텐츠, 좋아요 장소, 방문 상태, 저장 코스, 내 게시글을 확인합니다.

## HTTP 기반 검증

REST Client 수동 검증 파일:

- `backend/api-tests/week3-live-e2e.http`

이 파일은 signup, login, users/me, content/place, like, visit status, AI route, route save, community, notification, review list/create 흐름을 순서대로 검증합니다.

## 데모 중 fallback 설명

지도 provider credential이 없을 경우:
- Spring Boot map endpoint의 fallback 응답을 사용합니다.
- map provider live QA는 `In progress`로 유지합니다.

FastAPI AI 서버가 실행 중이 아닐 경우:
- Week 3 canonical path는 Spring Boot deterministic AI route generation입니다.
- `sceniK-ai`는 Week 4 내부 AI/RAG 품질 개선을 위한 skeleton이라고 설명합니다.

## 데모 전 체크리스트

- backend build 통과
- frontend build/lint/smoke 통과
- AI pytest 통과
- MySQL service running
- backend manual run 가능
- frontend manual dev server 가능
- 테스트 계정 email 중복 여부 확인
