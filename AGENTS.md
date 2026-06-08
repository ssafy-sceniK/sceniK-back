# AGENTS.md

## Scope

This folder is the Spring Boot backend for sceniK and is intended to become the
`sceniK-back` repository.

## Rules

- The backend is the public API server for the frontend.
- The backend may call `sceniK-ai` internally later.
- Do not expose or commit secrets.
- Follow the existing Spring Boot, MyBatis, MySQL, and Gradle conventions.
- `contentId` and `placeId` are string IDs such as `C_DRAMA_001` and `P_001`.
- Keep the current `routeId` contract consistent with the frontend.
- DB seed data belongs in `data/seed/` and runtime SQL belongs in
  `src/main/resources/`.
- Prefer incremental fixes over broad API rewrites.

## Error Contract

API errors should use the standard response shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력값을 확인해 주세요.",
  "fieldErrors": [
    {
      "field": "password",
      "message": "비밀번호는 8자 이상이어야 합니다."
    }
  ]
}
```

Log method, URI, exception class, and stack traces server-side. Do not expose
stack traces, SQL, tokens, or internal details to clients.

## Validation

Before finalizing backend work, run:

```powershell
.\gradlew.bat clean build
```

If MySQL is required for a live smoke test, either provide manual commands or
start and stop the backend within the task. Do not leave a server running.
