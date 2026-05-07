# University ERP Execution Plan

This plan aligns EduSys with the referenced university enterprise architecture paper. The goal is a university SIS/ERP demonstration, not a full production ERP rewrite.

## Architecture Direction

- **Enterprise method:** TOGAF ADM for architecture framing and migration planning.
- **Software architecture:** modular monolith.
- **Frontend:** React role portals.
- **Backend:** Spring Boot REST API.
- **Database:** PostgreSQL with Flyway migrations.
- **Governance:** role-based access control, auditability, privacy boundaries, and phased delivery.

## Paper-Aligned Target Modules

| Paper module | Demo interpretation | Current status |
|---|---|---|
| Admissions | Applicant intake, status review, conversion to student | Demo surface needed |
| Student registration | Student profile, academic status, enrollment history | Partially implemented |
| Academic management | Courses, sections, schedules, faculty workload | Partially implemented |
| Course selection | Credit limits, prerequisites, enrolled course list | Demo surface needed |
| Learning progress and assessment | Attendance, assignments, assessments, grades | Partially implemented |
| Finance | Tuition invoices, payments, balances, reports | Partially implemented |
| Student services | Requests, certificates, notifications, service status | Demo surface needed |
| HR/faculty information | Faculty profiles, departments, workload | Partially represented |
| Reporting and analytics | Enrollment, academic, finance, KPI dashboard | Demo surface needed |
| User/access management | Login, roles, permissions, audit | Partially implemented |

## Suspend For University Scope

- Parent portal as a primary module.
- Parent-student link management as a main workflow.
- Cafeteria/lunch operations.
- Middle-school health, nurse, bus, and homeroom workflows.
- Behavior/discipline workflows unless reframed as university student support.

## Execution Phases

### Phase 1: Minimal Working Module Exchange

- Rename visible school terminology toward university terminology.
- Hide/suspend non-university modules from navigation.
- Add admin demo pages for missing paper modules.
- Add a working backend slice for the core paper flow:
  Admissions -> Student registration -> Course selection -> Finance -> Reporting.
- Use existing user, course, and finance tables where possible, adding only university-specific intake and selection tables.
- Keep the architecture as a modular monolith inside the current Spring Boot and React applications.

### Phase 2: Expanded Working University Modules

- Student service request entity and APIs.
- Finance-hold checks for official student services and follow-up course selections.
- Academic standing and prerequisite checks.
- More complete reporting endpoints based on existing academic and finance data.
- Optional data seeding for repeatable classroom demonstrations.

### Phase 3: Governance And Integration

- Audit logs for high-impact student, finance, and access changes.
- Role-specific access checks for admissions, finance, academic office, and IT.
- Optional integration stubs for bank payment, LMS, email/SMS, and government reporting.

## Current Executed Slice

Phase 1 now implements the first working ERP data exchange. The admin demo pages are backed by APIs that create applicants, convert accepted applicants into student users, select courses for those students, create finance invoices from selected credits, and report aggregate ERP metrics.

Phase 2 extends that exchange with live student service requests and academic eligibility checks. Requests route to a default office, official-document requests check finance balances, held/open request counts feed reporting, course selection blocks additional selections while a student has an outstanding finance balance, and prerequisite rules are checked against completed academic records.

The demo also includes an admin-only seed action that prepares a repeatable classroom scenario with a demo student, university courses, a prerequisite rule, a completed academic record, and a student-service request.

Phase 3 starts the governance layer with a university ERP event log. High-impact demo actions record module, action, entity, student, actor, details, and timestamp, and the reporting module exposes recent audit events.

Phase 3 also includes API-oriented integration stubs for LMS roster sync, bank payment confirmation, notification delivery, and government reporting. Each stub can be simulated from the reporting page and records an audit event.

The integration stubs have now been productionized into persisted demo integration runs. Each run records the integration key, name, direction, status, exchange payload, actor, timestamp, and result message. The payloads are assembled from current ERP data so the LMS, bank, notification, and government exchanges demonstrate real module-to-module data movement even though the external adapters remain safe classroom simulations.

## Productionization Plans

These plans move the remaining demo-level modules toward actually working order while keeping the modular monolith approach. Each plan is scoped so it can be implemented and verified independently.

### Plan A: HR And Faculty ERP

**Goal:** Replace the current teacher/faculty representation with a working HR/faculty module for university operations.

**Backend scope**

- Add `university_departments` for academic and administrative departments.
- Add `university_faculty_profiles` linked to `users`, with employee number, department, title/rank, employment type, hire date, office, status, and specialization.
- Add `university_faculty_workload` for term-based teaching, advising, research, and committee workload.
- Add `university_faculty_leave_requests` for leave workflow.
- Add repositories, DTOs, service methods, and controller endpoints under the existing `university` package or a new `university.hr` package.
- Connect faculty workload to existing `TeachingAssignment` records where possible.
- Record ERP audit events for profile changes, workload changes, and leave decisions.

**Frontend scope**

- Add an admin HR/faculty module page.
- Add department and faculty profile management views.
- Add faculty workload dashboard by academic year and semester.
- Add leave request workflow with status updates.
- Add role-aware access for admin and HR/faculty staff.

**Acceptance criteria**

- Admin can create a department.
- Admin can create/update a faculty profile linked to an existing user.
- Admin can assign workload for a term.
- Leave requests can be submitted, approved, rejected, and reported.
- Reporting dashboard includes faculty count, active faculty, workload total, and leave counts.
- `mvn test` and `npm run build` pass.

**Recommended slices**

1. Department and faculty profile CRUD.
2. Workload model and dashboard.
3. Leave request workflow.
4. Reporting and audit integration.

### Plan B: Real Integration Layer

**Goal:** Replace simulated integration stubs with working internal integration records and realistic external adapter contracts.

**Backend scope**

- Add `university_integration_connections` for LMS, bank, notification, and government reporting connection settings.
- Add `university_integration_runs` with run status, direction, payload summary, request body, response body, error message, started/completed timestamps, and retry count.
- Define adapter interfaces:
  - `LmsIntegrationAdapter`
  - `BankPaymentIntegrationAdapter`
  - `NotificationIntegrationAdapter`
  - `GovernmentReportingIntegrationAdapter`
- Implement safe demo adapters first:
  - file/log-backed adapters or local mock HTTP adapters
  - no secret exposure in frontend responses
- Add retry and failure handling.
- Connect actual internal data:
  - LMS roster export from course selections
  - Bank payment import against invoices
  - Notification events from admissions and service requests
  - Government report export from enrollment/finance/reporting summary
- Record audit events for every integration run.

**Frontend scope**

- Add integration admin page with connection status, last run, run history, payload summary, and retry action.
- Add per-integration detail view.
- Add failure states and response preview.

**Acceptance criteria**

- Admin can run LMS roster export and see a persisted run record.
- Admin can simulate/import a bank payment callback that updates a finance invoice/payment.
- Admissions/service workflow can trigger persisted notification runs.
- Government reporting export produces a downloadable/reportable payload record.
- Failed runs are visible and retryable.
- Secrets are never rendered in the UI.
- `mvn test` and `npm run build` pass.

**Recommended slices**

1. Integration connection/run persistence.
2. LMS roster export using real course-selection data.
3. Bank payment callback to actual finance payment records.
4. Notification dispatch records.
5. Government reporting export.

### Plan C: Reporting And BI Module

**Goal:** Move reporting from simple KPI summary to a working reporting/analytics module.

**Backend scope**

- Add `university_report_definitions` for saved report metadata.
- Add `university_report_runs` for generated report snapshots.
- Add report services for:
  - enrollment funnel
  - course selection and credit load
  - finance billing and outstanding balance
  - student services SLA/status
  - faculty workload
  - integration run health
- Add date range, academic year, semester, program, department, and status filters.
- Add CSV export endpoints.
- Add scheduled or on-demand materialized snapshot generation.
- Record audit events for generated/exported reports.

**Frontend scope**

- Add reporting workspace with report catalog, filters, charts/tables, run history, and CSV export.
- Add drill-down links from KPI cards to detail tables.
- Add role-based report visibility.

**Acceptance criteria**

- Admin can run at least four reports with filters.
- Reports show both aggregate KPIs and detail rows.
- CSV export works.
- Report run history is persisted.
- Dashboard can show trend-ready snapshots, not only live counts.
- `mvn test` and `npm run build` pass.

**Recommended slices**

1. Report definitions and report run persistence.
2. Enrollment/admissions funnel report.
3. Finance balance report.
4. Student services SLA report.
5. Faculty workload and integration health reports.
6. CSV export and audit logging.

### Plan D: Full Student Services And Academic Rules

**Goal:** Expand minimal student services and academic rules into real university workflows.

**Backend scope**

- Extend service requests with configurable service types:
  - transcript
  - enrollment certificate
  - leave request
  - graduation clearance
  - advising appointment
  - program change
- Add `university_service_types` with required office, SLA days, finance clearance flag, required documents, and allowed statuses.
- Add service request attachments using the existing file upload pattern.
- Add comments/history for each request.
- Add academic policy tables:
  - prerequisite groups
  - co-requisites
  - repeated-course rules
  - max/min credit load
  - academic standing
  - program requirements
- Add graduation/progress checks based on completed academic records.
- Connect academic standing to course selection eligibility.
- Record audit events for policy changes and service decisions.

**Frontend scope**

- Add service-type management page.
- Add richer student service request page with attachments, comments, history, SLA status, and office filters.
- Add academic policy management page.
- Add student progress/graduation-check view.
- Improve course-selection UI to show exactly why a course is blocked.

**Acceptance criteria**

- Admin can configure service types without code changes.
- Student/admin can create requests with required metadata and attachments.
- Request history and comments are visible.
- Finance clearance, SLA, and office routing are enforced from service type configuration.
- Course selection checks prerequisite groups, credit load, finance holds, repeated-course rules, and academic standing.
- Graduation/progress check shows completed/missing requirements.
- `mvn test` and `npm run build` pass.

**Recommended slices**

1. Configurable service types.
2. Attachments, comments, and request history.
3. SLA and office queues.
4. Academic policy tables.
5. Course selection eligibility engine.
6. Graduation/progress check.

## Recommended Implementation Order

1. **Plan D, slice 1-3:** Student services should become configurable first because it is already close to working and improves visible workflow quality quickly.
2. **Plan A, slice 1-2:** HR/faculty profile and workload makes the ERP module map feel complete.
3. **Plan C, slice 1-3:** Reporting should then consume the richer data from services, finance, admissions, and HR.
4. **Plan B, slice 1-3:** Real integration persistence and LMS/bank adapters should come after the internal data contracts are stable.
5. **Plan D, slice 4-6:** Finish academic policy and graduation checks after the reporting and integration contracts are clearer.

## Definition Of Actually Working

A module counts as actually working when it has:

- Persistent database tables with Flyway migration.
- REST APIs with validation and role checks.
- Frontend workflows that create, update, and read real data.
- Cross-module data exchange where the paper expects integration.
- Reporting/audit visibility.
- Repeatable seed or test data.
- Verification with backend tests and frontend build.

## Remaining Work / Gap Register

This section tracks what is still incomplete after the implemented production slices. It is intentionally explicit so the demo can show both delivered architecture increments and remaining TOGAF ADM migration work.

| Area | Current implemented state | Remaining gap | Related plan |
|---|---|---|---|
| HR and Faculty | Faculty profiles are persistent and linked to existing faculty users. Departments are first-class records. Term workload can record teaching, advising, research, and committee credits. Leave requests can be submitted and approved or rejected. Workload comparison is calculated from active teaching assignments. | Richer HR-specific reporting is still pending. | Plan A |
| Integrations | Integration runs are persistent. LMS, bank, notification, and government exchanges save data-backed payload summaries. LMS roster export creates a concrete roster artifact. Bank payment callback records a real finance payment. Notification dispatch records are generated from admissions and student-service events. Government reporting export creates a statutory summary artifact. Connection configuration, adapter mode, auth type, secret references, failure simulation, retry runs, real generic HTTP adapter calls, environment/property secret resolution, and smoke-test checks are working. | Live vendor onboarding still requires institution-specific endpoint contracts and private credentials. | Plan B |
| Reporting and BI | Reporting has live BI breakdowns, persistent report definitions, generated report snapshots, report-run history, basic filters, CSV export, drill-down detail rows, and role-specific report visibility metadata. | Richer snapshot payloads and deeper warehouse-style analytics are still pending. | Plan C |
| Student Services | Service types, request workflow, comments, attachments, history, queues, SLA, assignment, finance-clearance checks, advising workflow, program-change workflow, and graduation-clearance evaluation are working. | More complete document/attachment validation is still pending. | Plan D |
| Academic Rules | Academic policy, prerequisites, prerequisite groups, co-requisites, repeat-course checks, credit limits, academic records, and graduation progress checks are working. | Deeper academic standing rules and clearer blocked-course explanations are still pending. | Plan D |

### Next Planned Slices

1. Optional institution onboarding execution: replace the vendor onboarding template placeholders with real institution/vendor credentials, restart the backend, and run the provided smoke-test script against the live deployment.

## Production Execution Progress

### Configurable Student Service Types

Implemented as the first productionization slice for Student Services.

- Added persistent service type configuration for code, name, default office, SLA days, finance clearance, attachment requirement, and active/inactive state.
- Seeded default university service types through Flyway.
- Replaced hardcoded service routing and finance-clearance behavior with service type lookup.
- Added admin-facing service type management controls to the Student Services ERP page.
- Kept fallback behavior for older request strings that predate the configuration table.

### Service Request Activity

Implemented as the second productionization slice for Student Services.

- Added persistent request comments.
- Added persistent request history/timeline events.
- Added persistent attachment metadata using the existing file storage service.
- Added APIs for request detail bundles, comment creation, and attachment uploads.
- Updated the Student Services ERP page with request details, comments, attachments, and history.

### Service Request Queues and SLA

Implemented as the third productionization slice for Student Services.

- Added persistent request due dates and assigned staff ownership.
- Added office queue summaries for open, unassigned, due-soon, and overdue requests.
- Added request filtering by office, SLA status, and assigned staff user.
- Added assignment APIs that record request history and ERP audit events.
- Enforced service-type attachment requirements before approval or delivery.
- Updated the Student Services ERP page with queue cards, SLA badges, filters, and assignment controls.

### Advising And Program-Change Workflows

Implemented as the fourth productionization slice for Student Services.

- Seeded program-change and graduation-clearance service types.
- Advising appointments are exposed as a workflow shortcut using the configurable service-type engine.
- Program-change requests are exposed as a registrar workflow requiring finance clearance and attachments.
- Updated the Student Services ERP page with workflow shortcuts that prefill the correct request type and description.

### Graduation Clearance Workflow

Implemented as the fifth productionization slice for Student Services.

- Added a graduation-clearance evaluation API for graduation service requests.
- The evaluation checks degree audit eligibility, remaining credits, finance balance, and required attachment satisfaction.
- Eligible requests are moved to `APPROVED`; blocked requests are moved to `ON_HOLD` with a concrete reason.
- Evaluation writes service request history and a university ERP audit event.
- Updated Student Services with a graduation-clearance shortcut and detail-panel evaluation controls.

### Academic Policy Engine

Implemented as the first productionization slice for Academic Rules.

- Added persistent active academic policy configuration.
- Added term minimum/maximum credit settings, probation credit limits, good-standing grade threshold, probation blocking, and repeat-course configuration.
- Replaced the hardcoded term credit limit with policy-driven eligibility checks.
- Course selection now blocks repeated completed courses unless the active policy allows repeats.
- Course selection now applies reduced credit limits or full blocking when a student's completed academic record average falls below the good-standing threshold.
- Updated the Course Selection ERP page with academic policy controls.

### Graduation Progress Checks

Implemented as the second productionization slice for Academic Rules.

- Added persistent program requirements by program name.
- Added support for required-course requirements and general credit-bucket requirements.
- Added student degree-audit API that compares completed academic records against program requirements.
- Degree audit now reports total required credits, completed credits, matched credits, remaining credits, per-requirement satisfaction, and graduation eligibility.
- Updated the Course Selection ERP page with program requirement setup and a student graduation progress check.

### Academic Co-Requisites And Prerequisite Groups

Implemented as the third productionization slice for Academic Rules.

- Added prerequisite group codes so alternative prerequisites can be modeled as "complete any one course from this group".
- Added persistent co-requisite rules linking courses that must be selected together, already selected in the same term, or already completed.
- Course selection now enforces ungrouped prerequisites, grouped prerequisite alternatives, co-requisites, credit limits, finance holds, and repeat-course policy in one eligibility path.
- Added backend APIs for listing and creating co-requisite rules.
- Updated the Course Selection ERP page with prerequisite group and co-requisite controls.

### Reporting BI Breakdowns

Implemented as the first productionization slice for Reporting and BI.

- Extended the ERP reporting summary with grouped operational breakdowns.
- Added admissions pipeline status counts.
- Added student-service workflow status counts and office queue summaries.
- Added finance invoice status counts and billed amounts by status.
- Added academic policy counters for credit rules, prerequisite rules, program requirements, and academic records.
- Added program requirement totals by program for graduation/progress visibility.
- Updated the Reporting ERP page with responsive BI panels.

### Reporting Run Persistence

Implemented as the second productionization slice for Reporting and BI.

- Added persistent report definitions for enrollment funnel, finance balance, student services SLA, faculty workload, and integration health.
- Added persistent report run records with status, filters, snapshot payload, row count, actor, and generated timestamp.
- Added backend APIs for report catalog, report generation, and recent run history.
- Updated the Reporting ERP page with report workspace controls and saved report-run history.
- Report snapshots are generated from the live ERP summary and related HR/integration data, so the reporting module now has both current dashboards and auditable report artifacts.

### Reporting CSV Export And Filters

Implemented as the third productionization slice for Reporting and BI.

- Added CSV export endpoints for report definitions.
- Added basic filter metadata for academic year, semester, and status.
- Updated the Reporting ERP page with filter inputs and CSV export actions for each report definition.
- CSV exports use live ERP breakdown data and produce downloadable report files.

### Reporting Drill-Down And Visibility

Implemented as the fourth productionization slice for Reporting and BI.

- Added role-visibility metadata to report definitions so the reporting catalog is scoped by administrative function.
- Added backend drill-down APIs for enrollment, finance balance, student-services SLA, faculty workload, and integration-health reports.
- Drill-down rows expose source entity type, source id, label, status, amount when applicable, and detail notes.
- Updated the Reporting ERP page so users can load detail rows from each report definition beside generated snapshots and CSV exports.

### HR Faculty Profiles

Implemented as the first productionization slice for HR and Faculty ERP.

- Added persistent faculty HR profiles linked to existing faculty user accounts.
- Added employee number, department, academic rank, employment status, hire date, office location, and target workload credits.
- Reused active teaching assignments to calculate assignment count, assigned credits, and workload variance.
- Added backend APIs for listing and saving faculty HR profiles.
- Updated the blueprint so HR and Faculty is a live demo module instead of a represented-only module.
- Added the HR and Faculty ERP page with profile editing and workload review.

### HR Departments And Term Workload

Implemented as the second productionization slice for HR and Faculty ERP.

- Added persistent university department records.
- Added persistent faculty workload records by academic year and semester.
- Workload records separate teaching, advising, research, and committee credits.
- Added backend APIs for department creation/listing and workload creation/listing.
- Updated the HR and Faculty ERP page with department management and term workload controls.

### HR Leave Request Workflow

Implemented as the third productionization slice for HR and Faculty ERP.

- Added persistent faculty leave requests linked to faculty HR profiles.
- Leave requests capture leave type, date range, status, reason, decision notes, and decision timestamp.
- Added backend APIs for listing, submitting, approving, rejecting, and cancelling leave requests.
- Updated the HR and Faculty ERP page with leave submission and decision controls.

### Integration Run Persistence

Implemented as the first productionization slice for the ERP integration layer.

- Added persistent integration run records for LMS, bank, notification, and government reporting exchanges.
- Replaced one-off simulated responses with saved exchange payloads built from live ERP counts and finance totals.
- Added an integration run-history API.
- Updated the Reporting ERP page so administrators can see latest integration status, payload summary, and recent run history.
- Kept the adapters as safe classroom simulations while making the integration layer auditable and data-backed.

### Bank Payment Callback

Implemented as the second productionization slice for the ERP integration layer.

- Added a bank payment callback API under the ERP integration endpoints.
- The callback finds or accepts an outstanding invoice, records a completed online payment, and updates the invoice to paid or partially paid.
- Each callback creates a persisted bank integration run with invoice, amount, remaining balance, and reference details.
- Updated the Reporting ERP page with a bank callback simulation button so the demo can show an external payment event changing finance data.

### LMS Roster Export

Implemented as the third productionization slice for the ERP integration layer.

- Added an LMS roster export API under the ERP integration endpoints.
- The export builds a concrete roster payload from active university course selections, including student identity, course identity, academic year, semester, credits, and selection status.
- Each export creates a persisted LMS integration run, making the roster artifact visible in integration history.
- Updated the Reporting ERP page with an LMS roster export action.

### Notification Dispatch Records

Implemented as the fourth productionization slice for the ERP integration layer.

- Added a notification dispatch API under the ERP integration endpoints.
- The dispatch builds outbound notification records from pending admissions events and open student-service requests.
- Each dispatch creates a persisted notification integration run with recipient, source, reference, status, and subject details.
- Updated the Reporting ERP page with a notification dispatch action.

### Government Reporting Export

Implemented as the fifth productionization slice for the ERP integration layer.

- Added a government reporting export API under the ERP integration endpoints.
- The export builds a statutory summary payload from enrollment, academic, finance, student-service, and governance data.
- Each export creates a persisted government integration run with report period, generated timestamp, and report sections.
- Updated the Reporting ERP page with a government report export action.

### Integration Connection Configuration And Retry

Implemented as the sixth productionization slice for the ERP integration layer.

- Added persistent integration connection records for LMS, bank, notification, and government reporting.
- Added endpoint URL, enabled flag, and last-status tracking without exposing any secrets.
- Added failure simulation to create failed integration run records for demonstration.
- Added retry support that creates a persisted retry-success run linked by integration key and retry count.
- Updated the Reporting ERP page with connection editing, failure simulation, and retry controls.

### External Adapter And Secret Configuration

Implemented as the seventh productionization slice for the ERP integration layer.

- Added adapter mode (`MOCK` or `HTTP`) to integration connection configuration.
- Added auth type (`NONE`, `API_KEY`, `BEARER_TOKEN`, `BASIC`) and secret reference fields without storing secret values directly.
- Backend validation now requires HTTP endpoints for HTTP adapters and secret references for authenticated adapters.
- Integration run payloads now include adapter metadata and secret references, making external exchange configuration auditable without exposing credentials.
- Updated the Reporting ERP page with adapter mode, auth type, and secret reference controls.

### Production Integration Hardening

Implemented as the eighth productionization slice for the ERP integration layer.

- Added a real outbound HTTP adapter path for configured integrations.
- HTTP integrations now POST the generated ERP payload to the configured endpoint and persist success or failure status in integration run history.
- Added secret resolution by reference from environment variables or Spring properties, supporting `env:NAME`, `property:name`, or direct reference names.
- Added API key, bearer token, and basic-auth header handling without storing or rendering secret values.
- Added an integration smoke-test API and Reporting ERP action that validates endpoint configuration and secret availability before a live exchange.
- Real vendor accounts, private credentials, and institution-specific SDK contracts remain deployment/onboarding concerns rather than repository fixtures.

### Vendor Onboarding Package

Implemented as the ninth productionization slice for the ERP integration layer.

- Added `deploy/ec2/backend/vendor-integrations.env.example` for real LMS, bank, notification, and government-reporting endpoint and secret-reference setup.
- Added `deploy/ec2/backend/smoke-test-university-integrations.ps1` to authenticate as an admin, save vendor connection configuration, and run the integration smoke-test API.
- Added `deploy/ec2/backend/UNIVERSITY_ERP_VENDOR_ONBOARDING.md` with endpoint, credential, smoke-test, run-history, and go-live acceptance steps.
- Updated the EC2 backend deployment README to point operators to the onboarding package.
- Actual real-vendor execution now depends only on institution-provided sandbox URLs, credentials, and vendor-specific contract approval.
