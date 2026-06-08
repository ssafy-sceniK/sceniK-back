# Local Seed Reset

Use this when preparing a clean Week 3 demo database.

Important: `schema.sql` uses `CREATE TABLE IF NOT EXISTS`. It does not migrate
old local tables. If a column type changed, drop and recreate the database.

## Database Reset

The backend uses MySQL. The examples assume:

- database: `scenik`
- username: `ssafy`
- password: `ssafy`

```sql
DROP DATABASE IF EXISTS scenik;
CREATE DATABASE scenik
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE scenik;
SOURCE C:/Users/SSAFY/Downloads/sceniK/backend/src/main/resources/schema.sql;
SOURCE C:/Users/SSAFY/Downloads/sceniK/backend/src/main/resources/data.sql;
```

PowerShell alternative:

```powershell
.\scripts\reset-local-db.ps1 -User ssafy -Password ssafy
```

Bash alternative:

```bash
./scripts/reset-local-db.sh
```

## Demo Account

Use the signup endpoint instead of inserting a raw password hash manually.

The backend hashes passwords with PBKDF2-SHA256 and a random salt in `PasswordService`, so the safest repeatable setup is:

```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "demo@scenik.com",
  "password": "password123",
  "nickname": "데모유저",
  "preferences": ["DRAMA", "SEOUL"]
}
```

If the account already exists, use:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "demo@scenik.com",
  "password": "password123"
}
```

## Demo Routes And Posts

- Demo routes should be created through `POST /api/ai/routes` followed by `POST /api/routes` so route places keep valid `placeId` values.
- Demo posts should be created through `POST /api/posts` after login.

## Quick Validation

After reset and backend startup:

```powershell
curl http://localhost:8080/api/contents
curl http://localhost:8080/api/places
curl http://localhost:8080/swagger-ui/index.html
```
