# University ERP Vendor Integration Onboarding

This checklist turns the classroom ERP integration layer into a deployment-specific vendor setup.

## What The Code Supports

- Generic outbound HTTP POST adapters for LMS, bank, notification, and government-reporting exchanges.
- Auth modes: `NONE`, `API_KEY`, `BEARER_TOKEN`, and `BASIC`.
- Secret references resolved from environment variables or Spring properties.
- Persisted integration run history for success and failure.
- Smoke-test API: `POST /api/university-erp/integrations/smoke-test`.

## Onboarding Steps

1. Request each vendor's sandbox endpoint, auth method, required headers, and expected JSON contract.
2. Copy `vendor-integrations.env.example` into the production `.env` or equivalent secret manager.
3. Replace endpoint placeholders with vendor sandbox URLs.
4. Replace secret placeholders with real values in the deployment environment only.
5. Restart the backend so the process can read the new secret environment variables.
6. Run the smoke-test script:

```powershell
pwsh ./smoke-test-university-integrations.ps1 `
  -BaseUrl "https://your-backend-domain.com" `
  -AdminUsername "admin" `
  -AdminPassword "your-admin-password" `
  -EnvFile ".env"
```

7. In the admin Reporting ERP page, use **Run smoke test** to confirm all configured references remain resolvable.
8. Use **Run exchange** for each integration and review the persisted run history.

## Secret Reference Formats

- `env:UNIVERSITY_ERP_LMS_TOKEN`
- `property:app.vendor.lms.token`
- `UNIVERSITY_ERP_LMS_TOKEN`

The frontend stores only the reference, never the secret value.

## Vendor Mapping

| ERP integration | Internal payload source | Vendor expectation |
|---|---|---|
| LMS | Active course selections and roster export data | Roster import endpoint |
| Bank | Finance invoice/payment callback data | Payment confirmation/import endpoint |
| Notification | Admissions and student-service dispatch data | Message dispatch endpoint |
| Government | Enrollment, finance, services, academic, and audit summary | Statutory reporting endpoint |

## Go-Live Acceptance

- All smoke tests return `READY`.
- Each vendor sandbox accepts at least one exchange and returns a 2xx response.
- Failed vendor responses appear in integration run history with an error message.
- Secrets exist only in environment variables, Spring properties, or a deployment secret manager.
- Production endpoints and sandbox endpoints are clearly separated.
