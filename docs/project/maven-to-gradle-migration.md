# Maven To Gradle Migration

Date: 2026-05-29

## Decision

The backend now builds with Gradle Wrapper. Maven files were archived outside
the backend folder under:

```text
../archive/root-migration-20260529/backend-maven-backup/
```

Spring Boot was kept at `3.3.6` because that was the working Maven parent
version. The migration does not upgrade to a new Spring Boot major version.

## Dependency Mapping

| Maven POM item | Gradle item |
| --- | --- |
| `spring-boot-starter-parent:3.3.6` | `org.springframework.boot` plugin `3.3.6` |
| Java property `17` | Gradle Java toolchain `17` |
| `spring-boot-starter-web` | `implementation 'org.springframework.boot:spring-boot-starter-web'` |
| `spring-boot-starter-validation` | `implementation 'org.springframework.boot:spring-boot-starter-validation'` |
| `mybatis-spring-boot-starter:3.0.4` | `implementation "org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4"` |
| `mysql-connector-j` runtime | `runtimeOnly 'com.mysql:mysql-connector-j'` |
| `springdoc-openapi-starter-webmvc-ui:2.6.0` | `implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0"` |
| `lombok` optional | `compileOnly` and `annotationProcessor` |
| `spring-boot-devtools` runtime optional | `developmentOnly` |
| `spring-boot-starter-test` test | `testImplementation 'org.springframework.boot:spring-boot-starter-test'` |
| Maven compiler `<parameters>true</parameters>` | `JavaCompile` compiler arg `-parameters` |
| `spring-boot-maven-plugin` | Spring Boot Gradle plugin / `bootJar` |

`spring-boot-starter-security` was added for the Gradle target stack. A minimal
`SecurityConfig` permits requests so the existing custom token validation in
controllers/services continues to define API behavior.

## Commands

Windows:

```powershell
.\gradlew.bat clean test
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew clean test
./gradlew clean build
./gradlew bootRun
```

Do not leave `bootRun` running after local API smoke checks.

## Manual Smoke

```powershell
cd backend
$env:DB_USERNAME = "ssafy"
$env:DB_PASSWORD = "ssafy"
.\gradlew.bat bootRun
```

In another terminal:

```powershell
curl http://localhost:8080/api/contents
curl http://localhost:8080/api/places
```
