# Frontend Deployment

## Vercel settings
- Framework preset: `Vite`
- Root directory: `frontend`
- Build command: `npm run build`
- Output directory: `dist`

## Required environment variables
- For Vercel with backend proxy rewrites: `VITE_API_BASE_URL=/api`

## Routing
- `vercel.json` rewrites `/api/*` to the EC2 backend at `http://54.91.216.9/api/*`.
- `vercel.json` also rewrites all non-API routes to `index.html` so `BrowserRouter` works on refresh and deep links.

## Backend connection
- Backend CORS must allow the deployed frontend origin, not the EC2 public IP.
- Example: `APP_CORS_ALLOWED_ORIGINS=https://<your-frontend>.vercel.app`

## Notes
- The frontend `axios` client already defaults to `/api` when `VITE_API_BASE_URL` is not set.
- If the EC2 public IP changes, update `frontend/vercel.json` to the new backend address and redeploy Vercel.
