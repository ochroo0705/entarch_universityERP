# EC2 Backend Deploy

This folder is the production runtime bundle for the backend on EC2.

## Pipeline

1. Push backend changes to `main`.
2. GitHub Actions runs backend tests.
3. If tests pass, the workflow builds the backend Docker image from `WebAdv-backend/Dockerfile`.
4. The image is pushed to `ghcr.io/<github-owner>/edusys-backend`.
5. GitHub Actions SSHes into EC2.
6. EC2 runs `docker compose pull` and `docker compose up -d`.

## First-time EC2 setup

Install Docker and Docker Compose plugin, then create a deployment folder:

```bash
sudo mkdir -p /opt/edusys/backend
sudo chown -R $USER:$USER /opt/edusys/backend
```

Copy these two files into `/opt/edusys/backend`:

- `deploy/ec2/backend/docker-compose.yml`
- `deploy/ec2/backend/.env.example` copied as `.env`

Then update `.env` with your real production values.

## Recommended infrastructure

- EC2: runs Docker only
- RDS PostgreSQL: persistent database
- S3 or Cloudflare R2: file storage for uploads
- Nginx or ALB: TLS termination and reverse proxy to `127.0.0.1:8080`

## Required GitHub secrets

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`
- `EC2_APP_DIR`
- `GHCR_USERNAME`
- `GHCR_TOKEN`

`GHCR_TOKEN` should be a GitHub personal access token with at least package read access.

## Health check

- Backend health endpoint: `/actuator/health`

## University ERP vendor integrations

Use `vendor-integrations.env.example` as the template for LMS, bank, notification, and government-reporting vendor endpoints and secret references.

After real vendor values are loaded into `.env` and the backend is restarted, run:

```powershell
pwsh ./smoke-test-university-integrations.ps1 -BaseUrl "https://your-backend-domain.com" -AdminUsername "admin" -AdminPassword "your-admin-password" -EnvFile ".env"
```

See `UNIVERSITY_ERP_VENDOR_ONBOARDING.md` for the full onboarding checklist and acceptance criteria.
