# Backend Deployment

## Render service
- Runtime: `Docker`
- Docker image source: `GHCR`
- Image name: `ghcr.io/<github-owner>/edusys-backend:latest`
- Health check: `/actuator/health`
- Java version: `21`
- Profile: `SPRING_PROFILES_ACTIVE=prod`

## Required environment variables
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_STORAGE_TYPE=s3`
- `APP_STORAGE_BUCKET`
- `APP_STORAGE_REGION`
- `APP_STORAGE_ACCESS_KEY`
- `APP_STORAGE_SECRET_KEY`

## Optional environment variables
- `PORT`
- `APP_STORAGE_ENDPOINT`
- `APP_STORAGE_PATH_STYLE`
- `APP_STORAGE_KEY_PREFIX`
- `APP_BOOTSTRAP_ENABLED`
- `APP_BOOTSTRAP_MODE`
- `APP_BOOTSTRAP_ADMIN_USERNAME`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
- `APP_BOOTSTRAP_ADMIN_FIRST_NAME`
- `APP_BOOTSTRAP_ADMIN_LAST_NAME`
- `APP_BOOTSTRAP_ADMIN_PHONE`
- `APP_MAIL_ENABLED`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `APP_TWILIO_ENABLED`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_PHONE_NUMBER`
- `APP_AI_PARENT_MESSAGE_PROVIDER_ENABLED`
- `APP_AI_PARENT_MESSAGE_PROVIDER_BASE_URL`
- `APP_AI_PARENT_MESSAGE_PROVIDER_API_KEY`
- `APP_AI_PARENT_MESSAGE_PROVIDER_MODEL`
- `APP_AI_ANALYTICS_SUMMARY_PROVIDER_ENABLED`
- `APP_AI_ANALYTICS_SUMMARY_PROVIDER_BASE_URL`
- `APP_AI_ANALYTICS_SUMMARY_PROVIDER_API_KEY`
- `APP_AI_ANALYTICS_SUMMARY_PROVIDER_MODEL`
- `APP_AI_RISK_SCHEDULE_ENABLED`
- `APP_AI_ANALYTICS_SUMMARY_SCHEDULE_ENABLED`
- `APP_API_DOCS_ENABLED`
- `MANAGEMENT_PROMETHEUS_ENABLED`

## Bootstrap modes
- `none`: default production mode, no data bootstrap runs.
- `admin-only`: creates the initial admin user if no admin exists.
- `full-demo`: seeds the current demo dataset and optional bulk users.

`admin-only` requires `APP_BOOTSTRAP_ENABLED=true`, `APP_BOOTSTRAP_MODE=admin-only`, and `APP_BOOTSTRAP_ADMIN_PASSWORD`.

For Cloudflare R2 specifically, use the R2 S3 endpoint as `APP_STORAGE_ENDPOINT` and set `APP_STORAGE_REGION=auto`.

## GitHub Actions -> Render
- Workflow file: `.github/workflows/deploy.yml`
- Dockerfile: `Dockerfile`
- CI runs backend tests, builds the Docker image, pushes it to GHCR, then triggers Render.
- Set `RENDER_BACKEND_DEPLOY_HOOK_URL` in GitHub repository secrets.
- Render should track `ghcr.io/<github-owner>/edusys-backend:latest`.
- If the GHCR package is private, configure Render with GHCR registry credentials or make the package public.

## EC2 service
- Workflow file: `../.github/workflows/backend-ec2.yml`
- Runtime bundle: `../deploy/ec2/backend`
- Deployment style: GitHub Actions builds and pushes the image to GHCR, then EC2 pulls and restarts with Docker Compose.
- Health check: `/actuator/health`
- Profile: `SPRING_PROFILES_ACTIVE=prod`

### EC2 pipeline
1. Push backend code to GitHub `main`.
2. GitHub Actions runs `mvn -B test`.
3. GitHub Actions builds `WebAdv-backend/Dockerfile`.
4. GitHub Actions pushes `ghcr.io/<github-owner>/edusys-backend:latest`.
5. GitHub Actions connects to EC2 over SSH.
6. EC2 runs `docker compose pull && docker compose up -d`.

### EC2 server files
Copy these files to your server deployment directory, for example `/opt/edusys/backend`:

- `deploy/ec2/backend/docker-compose.yml`
- `deploy/ec2/backend/.env.example` copied as `.env`

### EC2 GitHub secrets
- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`
- `EC2_APP_DIR`
- `GHCR_USERNAME`
- `GHCR_TOKEN`

### EC2 notes
- Prefer RDS for PostgreSQL instead of running the database on the same EC2 instance.
- Prefer S3 or Cloudflare R2 for file uploads in production.
- Bind the container to `127.0.0.1` and put Nginx or an AWS load balancer in front for TLS.
