# 데이터셋 요약

생성일: 2026-06-08

## 현재 데이터 유형

| 데이터 | 설명 |
| --- | --- |
| K-content | 드라마/영화/예능 등 콘텐츠 metadata, 썸네일, 카테고리, 인기 여부 |
| 장소 | 검증 촬영지의 `placeId`, 장소명, 주소, 지역, 좌표, 이미지, 운영 정보 |
| 콘텐츠-장소 연결 | 특정 콘텐츠와 실제 촬영지를 연결하는 관계 데이터 |
| 사용자 engagement | 콘텐츠 좋아요, 장소 좋아요, 방문 상태 |
| AI route | 생성/저장된 경로, 일차별 방문 장소, 방문 순서 |
| 커뮤니티 | 게시글, 댓글, 좋아요, 신고, 알림 |
| 리뷰 | 장소 리뷰, 평점, 작성자, 작성 시각 |
| embedding 후보 데이터 | Week 4 RAG를 위한 `embedding_text.csv` 및 stub 구조 |

## ID 정책

| ID | 현재 타입 | 예시 |
| --- | --- | --- |
| `contentId` | String | `C_DRAMA_001` |
| `placeId` | String | `P_0001` |
| `routeId` | String | `route-...` 또는 `gen-...` |
| `userId` | Long / BIGINT | 숫자 PK |
| `postId` | String | 현재 schema 기준 `VARCHAR(64)` |
| `commentId` | String | 현재 schema 기준 `VARCHAR(64)` |
| `notificationId` | String | 현재 schema 기준 `VARCHAR(64)` |
| `reportId` | String | 현재 schema 기준 `VARCHAR(64)` |
| `reviewId` | Long / BIGINT | 숫자 PK |

## 데이터 품질 기준

- AI route generation은 backend DB의 검증 장소만 사용해야 합니다.
- 생성/저장 route는 존재하지 않는 `placeId`를 포함하면 안 됩니다.
- Week 4 RAG 결과도 backend candidate place 목록과 교차 검증해야 합니다.
- ChromaDB에 있는 장소라도 backend DB에 없거나 검증되지 않았으면 추천 결과에서 제외합니다.

## Week 4 데이터 준비

필수 준비:
- 장소별 embedding text 구성 기준 확정
- `placeId`, `contentId`, `region`, `category`, `verified` metadata 저장
- embedding rebuild/upsert flow 구현
- retrieval 결과와 backend DB 후보 장소의 교차 검증
