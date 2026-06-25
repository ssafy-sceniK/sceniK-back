# sceniK Backend

sceniK 백엔드는 Vue 프론트엔드가 호출하는 Spring Boot 기반 공개 API 서버입니다. 콘텐츠, 장소, 검색, 커뮤니티, 인증, 팔로우, 좋아요/방문 상태, AI 여행 코스 생성 API를 제공합니다.

AI 코스 생성은 Spring Boot 백엔드에서만 처리합니다. 프론트엔드는 AI API 키를 직접 다루지 않으며, 백엔드는 DB에 저장된 검증 장소 후보만 AI 프롬프트 컨텍스트로 전달하고 응답의 `placeId`를 다시 검증합니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.6
- Gradle Wrapper
- Spring MVC REST API
- Spring Security / OAuth2 Client
- MyBatis 3.0.4
- MySQL
- Bean Validation
- springdoc-openapi / Swagger UI

## 폴더 구조

```text
sceniK-back/
├── api-tests/                  # HTTP API 스모크 테스트 파일
├── data/
│   └── seed/                   # CSV 시드 데이터
├── docs/                       # API, DB, QA, 제출 문서
├── gradle/wrapper/             # Gradle Wrapper 파일
├── scripts/                    # DB 초기화, CSV import, 좌표 동기화 스크립트
├── src/
│   ├── main/
│   │   ├── java/com/scenik/
│   │   │   ├── config/         # Spring Security, CORS, OAuth, OpenAPI 설정
│   │   │   ├── controller/     # REST Controller
│   │   │   ├── domain/         # 도메인 모델
│   │   │   ├── dto/            # 요청/응답 DTO
│   │   │   ├── exception/      # 전역 예외 처리
│   │   │   ├── mapper/         # MyBatis Mapper 인터페이스
│   │   │   ├── service/        # 비즈니스 로직
│   │   │   └── util/           # 공통 유틸
│   │   └── resources/
│   │       ├── mapper/         # MyBatis XML Mapper
│   │       ├── application.yml
│   │       ├── application-example.yml
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/                   # 테스트 코드
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## 환경 변수 설정

백엔드는 프로젝트 루트의 `.env` 파일을 선택적으로 읽습니다.

```yaml
spring:
  config:
    import: "optional:file:.env[.properties]"
```

환경 변수 또는 `.env` 파일을 사용할 수 있습니다. 실제 API 키와 OAuth secret은 커밋하지 않습니다.

### DB 설정

현재 로컬 DB 설정:

```properties
DB_URL=jdbc:mysql://localhost:3306/scenik?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=ssafy
DB_PASSWORD=ssafy
```

### OAuth 설정

```properties
GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret
KAKAO_OAUTH_CLIENT_ID=your_kakao_rest_api_key
KAKAO_OAUTH_CLIENT_SECRET=your_kakao_client_secret
OAUTH_BASE_URL=http://localhost:8080
OAUTH_FRONTEND_REDIRECT_URI=http://127.0.0.1:5173/oauth/callback
```

OAuth Redirect URI:

```text
Google: http://localhost:8080/login/oauth2/code/google
Kakao:  http://localhost:8080/login/oauth2/code/kakao
```

OAuth 흐름:

1. 브라우저에서 `/api/auth/oauth/google` 또는 `/api/auth/oauth/kakao`로 이동합니다.
2. OAuth Provider 인증 후 Spring Security callback으로 돌아옵니다.
3. 백엔드는 5분짜리 일회용 `code`를 프론트엔드 callback으로 전달합니다.
4. 프론트엔드는 `POST /api/auth/oauth/exchange`로 code를 교환해 access token과 refresh token을 받습니다.

### AI API 연동 설정

GMS Chat Completions 연동은 백엔드에서만 수행합니다.

```properties
GMS_BASE_URL=https://gms.ssafy.io/gmsapi/
GMS_KEY=your_gms_key
GMS_MODEL=gpt-5.4-mini
GMS_TRANSLATION_MODEL=gemini-2.5-flash-lite
GMS_TRANSLATION_FALLBACK_MODEL=
GMS_CONNECT_TIMEOUT_MS=1000
GMS_READ_TIMEOUT_MS=5000
```

주의:

- `GMS_KEY` 실제 값은 README, 프론트엔드 코드, Git commit에 남기지 않습니다.
- 프론트엔드는 `/api/ai/routes`만 호출합니다.
- AI 응답이 DB 후보 장소 밖의 `placeId`를 포함하면 백엔드가 결과를 폐기하고 DB 기반 fallback 코스를 생성합니다.

## 실행 방법

macOS/Linux:

```bash
cd /path/to/sceniK/sceniK-back
sh gradlew bootRun
```

Windows PowerShell:

```powershell
cd C:\path\to\sceniK\sceniK-back
.\gradlew.bat bootRun
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## 빌드와 테스트

macOS/Linux:

```bash
sh gradlew clean test
sh gradlew clean build
```

Windows PowerShell:

```powershell
.\gradlew.bat clean test
.\gradlew.bat clean build
```

## DB 초기화

`src/main/resources/schema.sql`은 `CREATE TABLE IF NOT EXISTS` 기반입니다. 로컬 스키마 변경 후 기존 테이블을 초기화해야 할 수 있습니다.

macOS/Linux:

```bash
sh scripts/reset-local-db.sh
```

Windows PowerShell:

```powershell
.\scripts\reset-local-db.ps1 -User ssafy -Password ssafy
```

시드 CSV는 `data/seed/`에 있고, 런타임 SQL은 `src/main/resources/schema.sql`, `src/main/resources/data.sql`에 있습니다.

## API 오류 응답 규칙

사용자에게 노출되는 API 오류는 한국어 메시지와 표준 JSON 형식을 사용합니다.

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "입력값을 확인해 주세요.",
  "data": null,
  "fieldErrors": [
    {
      "field": "password",
      "message": "비밀번호는 8자 이상이어야 합니다."
    }
  ]
}
```

서버 로그에는 method, URI, 예외 클래스, stack trace를 남길 수 있지만, 클라이언트 응답에는 stack trace, SQL, token, secret 값을 노출하지 않습니다.

## 개발 규칙

- 백엔드는 프론트엔드가 호출하는 공개 API 서버입니다.
- 프론트엔드는 AI 서버나 GMS API를 직접 호출하지 않습니다.
- secret, API key, OAuth secret, DB password를 Git에 커밋하지 않습니다.
- `contentId`, `placeId`는 `C_DRAMA_001`, `P_001` 같은 문자열 ID입니다.
- 프론트엔드와 연결된 현재 `routeId` 계약을 유지합니다.
- DB seed 데이터는 `data/seed/`, 런타임 SQL은 `src/main/resources/`에 둡니다.
- 큰 API 재설계보다 기존 Spring Boot, MyBatis, MySQL, Gradle 구조에 맞춘 점진적 수정을 우선합니다.
