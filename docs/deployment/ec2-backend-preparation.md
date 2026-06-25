# EC2 backend deployment preparation

This document prepares the application only. It does not create or configure AWS resources.

## Profiles

- `local` is the default profile. It keeps the existing local MySQL defaults and runs `schema.sql` and `data.sql` (`spring.sql.init.mode=always`).
- Set `SPRING_PROFILES_ACTIVE=prod` on EC2. The production profile requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, both OAuth client credentials, `OAUTH_BASE_URL`, and `OAUTH_FRONTEND_REDIRECT_URI`.
- Production sets `spring.sql.init.mode=never`; apply schema migrations and initial data deliberately before starting the service.

## Environment file

Copy `.env.example` to `.env` only on the deployment host and replace every placeholder. Keep `.env` outside Git and readable only by the service account. The application loads `.env` from its working directory with `optional:file:.env[.properties]`.

Set `UPLOAD_DIR` to a persistent absolute directory, for example `/opt/scenik/uploads`. Back up both this directory and MySQL. The API exposes it under `/api/uploads/**`.


## Upload storage

`scenik.upload-dir` is resolved from the `UPLOAD_DIR` environment variable and falls back to the relative `uploads` directory only when the variable is absent. Production should always set an absolute persistent path such as `/opt/scenik/uploads`.

The runtime upload contract is:

- Community image files live under `$UPLOAD_DIR/community`.
- Profile image files live under `$UPLOAD_DIR/profile`.
- DB/API image metadata stores relative URLs such as `/api/uploads/community/{filename}`.
- `WebConfig` serves `/api/uploads/**` from the configured upload root.

The Spring Boot service account must be able to create, read, and write the upload root and its subdirectories. Directories need execute permission for path traversal, and files must be readable by the service account. If Docker is used, mount a persistent host volume at the same path and set `UPLOAD_DIR` inside the container to that mounted path. Do not store uploads only inside an ephemeral container layer.

DB metadata alone is not enough for uploaded images to render. The actual file must exist at the matching path. To verify production storage without printing secrets:

```bash
ls -lah "$UPLOAD_DIR/community"
curl -I https://api.scenik.p-e.kr/api/uploads/community/<filename>.jpg
```

Expected results:

- `200` with an image content type means the file exists and is readable through the reverse proxy and Spring Boot.
- `404` means the DB may reference a file that is missing from `$UPLOAD_DIR/community`, or the filename/path does not match.
- `500` should be treated as a server configuration, permission, or unexpected resource handling problem and investigated from application logs.

## CORS and OAuth

`ALLOWED_ORIGINS` is comma-separated. Include the S3 static website endpoint during the first deployment; add the CloudFront or custom-domain origin later. Do not use `*` with authenticated APIs.

The provider callback URLs are:

```text
${OAUTH_BASE_URL}/login/oauth2/code/google
${OAUTH_BASE_URL}/login/oauth2/code/kakao
```

After login, Spring redirects the browser to `${OAUTH_FRONTEND_REDIRECT_URI}`. Register the provider callbacks in Google and Kakao after the final public URL is known. Google production OAuth requires HTTPS for non-localhost callback URLs.

## Runtime contract for the later EC2 step

- Java 17 runtime
- `java -jar build/libs/<jar-name>.jar`
- private MySQL on port 3306 and Spring Boot on port 8080
- Nginx or another reverse proxy should expose `/api/**` and `/login/oauth2/**` to Spring Boot. The OAuth callback is not under `/api`.
