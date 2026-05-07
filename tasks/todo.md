# Task Plan

## Objective

Implement the first missing middle-school operations slice: fees, payments, billing, lunch, and cafeteria support.

## Plan

- [x] Inspect existing backend and frontend CRUD patterns.
- [x] Design a minimal finance and cafeteria data model.
- [x] Add backend models, repositories, DTOs, services, and controllers.
- [x] Add frontend API helpers, routes, navigation, and views.
- [x] Add translation keys for new frontend content.
- [x] Run backend and frontend verification commands.
- [x] Summarize modified files and remaining follow-up work.

## Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- `git status` and `git diff` could not run because the workspace folder was not detected as a Git repository by the shell.

## Phase 1 Follow-Up Slice

- [x] Inspect existing finance/cafeteria API contracts and frontend forms.
- [x] Add searchable student lookup to admin invoice and meal-purchase forms.
- [x] Add multi-line invoice creation using the existing invoice `lines` API payload.
- [x] Add English and Mongolian translation keys for new finance UI text.
- [x] Keep invoice-line controls responsive on mobile.
- [x] Rerun frontend verification with `npm run build`.

## Phase 1 Follow-Up Review

- Frontend verification passed with `npm run build`.
- Backend code was inspected but not modified, respecting the confirmation rule for backend changes.

## Phase 1 Backend Follow-Up Slice

- [x] Add `CANCELLED` invoice status through a Flyway migration.
- [x] Add admin invoice cancel and waive backend endpoints.
- [x] Prevent payment recording against cancelled or waived invoices.
- [x] Make waived/cancelled invoice balances report as zero in summaries.
- [x] Add invoice status filtering and cancel/waive actions to the admin finance UI.
- [x] Add English and Mongolian translation keys for invoice statuses/actions.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## Phase 1 Backend Follow-Up Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Maven wrapper verification could not run because `.mvn/wrapper/maven-wrapper.properties` is missing; system Maven was used instead.

## Phase 1 Continued Follow-Up Slice

- [x] Add cafeteria meal-purchase date filters to the backend API.
- [x] Add cafeteria daily summary backend DTO/API.
- [x] Add admin cafeteria date filters and daily summary cards.
- [x] Add printable payment receipts for recorded invoice payments.
- [x] Add unpaid invoice editing through the existing multi-line invoice form.
- [x] Add English and Mongolian translation keys for the new controls.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## Phase 1 Continued Follow-Up Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.

## Phase 2 Access Control Foundation Slice

- [x] Extend backend role flags for counselor, nurse, finance staff, librarian, transport coordinator, admissions staff, and cafeteria staff.
- [x] Add backend role catalog and admin role-assignment endpoints.
- [x] Add Spring Security authorities and JWT role claims for the new staff roles.
- [x] Add frontend role API helpers and shared role-label utility.
- [x] Add admin staff-permissions screen with responsive role assignment cards.
- [x] Add English and Mongolian translation keys for new role names and permission descriptions.
- [x] Reuse new role labels in admin user list and user detail views.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## Phase 2 Access Control Foundation Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Root cause for the conservative implementation: the existing system stores roles as integer bit flags, so this slice extends that model instead of introducing a larger normalized role/permission refactor mid-plan.
- Remaining Phase 2 work: add domain-specific method-level checks and manual smoke-test a new staff-only account once the first staff module is added.

## Phase 2 Finance/Cafeteria Staff Access Slice

- [x] Return the full login role list from password and OTP login responses.
- [x] Route finance and cafeteria staff users into a narrow staff portal.
- [x] Add a staff sidebar with only the finance/cafeteria operations entry.
- [x] Split finance/cafeteria page loading and rendering by finance versus cafeteria capability.
- [x] Allow finance staff through billing, invoice, payment, and finance summary endpoints.
- [x] Allow cafeteria staff through meal plan, meal item, meal purchase, and daily summary endpoints.
- [x] Allow finance/cafeteria staff to use the student lookup with the existing student-only user filter.
- [x] Add English and Mongolian translation keys for the staff portal labels.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## Phase 2 Finance/Cafeteria Staff Access Review

- Frontend verification passed with `npm run build`.
- Backend verification initially failed because OTP login still used the old `LoginResponseDTO` constructor; after adding the `roles` field there too, `mvn test` passed.

## University ERP Demo Repositioning Slice

- [x] Extract university SIS/ERP module expectations from the referenced enterprise architecture paper.
- [x] Find frontend labels/navigation that imply middle-school scope.
- [x] Rename visible academic concepts toward university terminology.
- [x] Suspend non-university demo modules from visible navigation/routes without backend changes.
- [x] Run frontend verification with `npm run build`.

## University ERP Paper Execution Slice

- [x] Write `UNIVERSITY_ERP_EXECUTION_PLAN.md` aligned to the paper.
- [x] Add admin demo pages for missing paper-required ERP modules.
- [x] Wire the demo pages into admin navigation and routes.
- [x] Add translation keys for the new visible frontend content.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 2 Student Services Slice

- [x] Inspect existing service/controller patterns for the university ERP package.
- [x] Add a student service request table and backend workflow APIs.
- [x] Connect the Student Services ERP page to live API data.
- [x] Add minimal course-selection governance refinements where they fit Phase 2.
- [x] Add translations and responsive styling for the live service-request workflow.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 2 Student Services Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Student Services now has live request creation, office routing, status updates, finance holds for official documents, and reporting metrics.
- Course selection now enforces a finance hold before additional selection batches when a student has an outstanding balance.

## University ERP Phase 2 Academic Eligibility Slice

- [x] Inspect existing grade/course models for reusable academic completion data.
- [x] Add prerequisite and academic-record backend schema/models/APIs.
- [x] Enforce prerequisite checks during course selection.
- [x] Expose prerequisite/completion controls in the course-selection demo.
- [x] Add reporting metrics for prerequisite rules and academic records.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 2 Academic Eligibility Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Course selection now checks prerequisite rules against completed academic records.
- The course-selection demo can add prerequisite rules and mark a student's completed courses.

## University ERP Phase 2 Demo Seed Slice

- [x] Design an idempotent university ERP demo seeding workflow.
- [x] Add backend seed DTO/API that creates sample linked records safely.
- [x] Add an admin blueprint button to run the seed.
- [x] Add translation keys and update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 2 Demo Seed Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Admins can seed a linked university ERP classroom scenario from the blueprint page.

## University ERP Phase 3 Governance Audit Slice

- [x] Add Phase 3 governance checklist and inspect existing audit patterns.
- [x] Create university ERP event log schema/model/repository/DTO.
- [x] Record audit events from admissions, course selection, student services, prerequisites, academic records, and seed actions.
- [x] Expose recent audit events in the reporting demo.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 3 Governance Audit Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- High-impact ERP actions now write university-specific audit events.
- Reporting displays recent ERP audit events alongside aggregate metrics.

## University ERP Phase 3 Integration Stub Slice

- [x] Define lightweight ERP integration stub contract.
- [x] Add backend DTOs/service/controller endpoints for integration status and simulated runs.
- [x] Expose integration status/actions in the reporting demo.
- [x] Add translation keys and update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Phase 3 Integration Stub Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Reporting now shows LMS, bank, notification, and government-reporting integration stubs with simulated exchange actions.

## University ERP Productionization Planning Slice

- [x] Create a production plan for HR/faculty ERP.
- [x] Create a production plan for real LMS/bank/notification/government integrations.
- [x] Create a production plan for reporting and BI.
- [x] Create a production plan for full student services and academic rules.
- [x] Define recommended implementation order and acceptance criteria.

## University ERP Production Slice: Configurable Student Service Types

- [x] Inspect current student service request implementation and frontend page.
- [x] Add configurable service type schema/model/repository/DTOs.
- [x] Update backend service request logic to use service type configuration.
- [x] Add frontend service type management controls.
- [x] Add translations/docs updates.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Configurable Student Service Types Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Service request type routing, SLA days, attachment requirement flags, active state, and finance clearance are now configurable instead of hardcoded.

## University ERP Production Slice: Service Request Activity

- [x] Inspect existing file upload/attachment patterns and current service request model.
- [x] Add service request comments/history/attachment schema, models, repositories, DTOs.
- [x] Add backend APIs and wire history recording into request lifecycle.
- [x] Update Student Services UI for comments, history, and attachments metadata.
- [x] Add translations/docs updates.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Service Request Activity Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Student service requests now support persistent comments, timeline history, and uploaded attachment metadata.

## University ERP Production Slice: Service Request Queues and SLA

- [x] Inspect current service-request schema, endpoints, and UI bindings.
- [x] Add backend queue ownership fields for due dates and assigned staff users.
- [x] Add queue summary and assignment APIs.
- [x] Enforce required attachments before approval or delivery.
- [x] Add frontend queue cards, SLA filters, assignee filters, and assignment controls.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Service Request Queues and SLA Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Student service requests now support due dates, SLA status, queue summaries, assignee filtering, staff assignment, and required-attachment enforcement.

## University ERP Production Slice: Academic Policy Engine

- [x] Inspect current academic records, prerequisites, and course selection flow.
- [x] Add persistent academic policy schema/model/repository/DTOs.
- [x] Add backend APIs for reading and updating the active policy.
- [x] Enforce policy-driven maximum credits, probation limits, optional probation blocking, and repeat-course rules during course selection.
- [x] Add academic policy controls to the course-selection ERP page.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Academic Policy Engine Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Course selection now uses persistent academic policy configuration for credit limits, academic probation behavior, and repeat-course eligibility.

## University ERP Production Slice: Graduation Progress Checks

- [x] Inspect available course, student, and academic-record data for progress checks.
- [x] Add persistent program requirement schema/model/repository/DTOs.
- [x] Add backend APIs for program requirements and student degree audit.
- [x] Calculate completed, matched, remaining credits, requirement satisfaction, and graduation eligibility.
- [x] Add requirement setup and progress-check controls to the Course Selection ERP page.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Graduation Progress Checks Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Program requirements and degree audits now provide a working graduation/progress check over completed academic records.

## University ERP Production Slice: Reporting BI Breakdowns

- [x] Inspect current reporting DTO/service/frontend summary implementation.
- [x] Extend backend reporting summary with admissions, services, finance, queue, academic policy, and program requirement breakdowns.
- [x] Add BI breakdown panels to the Reporting ERP page.
- [x] Add responsive BI panel styling.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Reporting BI Breakdowns Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Reporting now shows grouped BI panels for admissions, services, finance, queues, academic rules, and program requirements.

## University ERP Production Slice: HR Faculty Profiles

- [x] Inspect existing teacher/faculty user and workload structures.
- [x] Add persistent faculty HR profile schema/model/repository/DTOs.
- [x] Add backend APIs to list and save faculty profiles linked to existing faculty users.
- [x] Reuse active teaching assignments to calculate workload assignments, assigned credits, and workload variance.
- [x] Add HR and Faculty ERP page and expose it from the blueprint.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: HR Faculty Profiles Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- HR and Faculty is now a live ERP module with faculty profiles, department/rank/employment data, and workload comparison from teaching assignments.

## University ERP Production Slice: Integration Run Persistence

- [x] Map current integration API/UI and record task checklist.
- [x] Add persistent integration run schema/model/repository/DTOs.
- [x] Update integration run logic to exchange real ERP data summaries.
- [x] Add backend API for integration run history.
- [x] Expose integration history in the reporting ERP page.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Integration Run Persistence Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Integration exchanges now create persistent run records with live ERP payload summaries and reporting-page history.

## University ERP Production Slice: Reporting Run Persistence

- [x] Inspect current reporting DTO and identify reusable report data.
- [x] Add report definition and report run schema/model/repository/DTOs.
- [x] Add backend APIs for report catalog, report generation, and run history.
- [x] Generate saved snapshots from live ERP report data.
- [x] Add reporting workspace controls and run history to the ERP UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Reporting Run Persistence Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Reporting now has persistent definitions, generated report snapshots, report-run history, and UI controls for admins to generate report artifacts.

## University ERP Production Slice: Bank Payment Callback

- [x] Inspect existing finance payment model/service behavior.
- [x] Add bank callback DTO/API under the ERP integration layer.
- [x] Record a real finance payment against an outstanding invoice from the bank callback.
- [x] Persist an integration run for the bank callback.
- [x] Expose bank callback simulation in the reporting integration UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Bank Payment Callback Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- The bank integration can now simulate a real payment callback that records a finance payment, updates invoice status, and persists an integration run.

## University ERP Production Slice: LMS Roster Export

- [x] Add Remaining Work / Gap Register to the ERP execution plan.
- [x] Inspect current course-selection data needed for LMS roster export.
- [x] Add LMS roster export DTO/API under the ERP integration layer.
- [x] Build roster payload from real course selections and persist an LMS integration run.
- [x] Expose LMS roster export action/result in the reporting integration UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: LMS Roster Export Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- The LMS integration can now generate a concrete roster export payload from active course selections and persist it as an integration run.

## University ERP Production Slice: Notification Dispatch Records

- [x] Inspect admissions and student-service data for notification dispatch inputs.
- [x] Add notification dispatch DTO/API under the ERP integration layer.
- [x] Build dispatch payload from real admissions and student-service data.
- [x] Persist a notification integration run with dispatch records.
- [x] Expose notification dispatch action/result in the reporting integration UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Notification Dispatch Records Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- The notification integration now generates dispatch records from pending admissions and open student-service requests, then persists them as an integration run.

## University ERP Production Slice: Government Reporting Export

- [x] Define government report export payload from current ERP summary data.
- [x] Add government export DTO/API under the ERP integration layer.
- [x] Persist a government integration run with statutory summary payload.
- [x] Expose government export action/result in reporting integration UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Government Reporting Export Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- The government integration now generates a statutory summary payload from live ERP data and persists it as an integration run.

## University ERP Production Slice: Reporting CSV Export And Filters

- [x] Add backend CSV generation for report definitions with optional filters.
- [x] Expose CSV export action and filter inputs in the reporting workspace UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Reporting CSV Export And Filters Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Reporting definitions now support CSV export with academic year, semester, and status filter metadata from the Reporting ERP workspace.

## University ERP Production Slice: HR Departments And Term Workload

- [x] Inspect HR/faculty UI and backend profile patterns.
- [x] Add department and faculty workload schema/model/repository/DTO/API.
- [x] Expose department and workload controls in HR/faculty ERP UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: HR Departments And Term Workload Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- HR/faculty now has first-class department records and term workload records for teaching, advising, research, and committee credits.

## University ERP Production Slice: Integration Connection Configuration And Retry

- [x] Add integration connection and retry/failure schema support.
- [x] Add backend APIs for connection config, failure simulation, and retry.
- [x] Expose connection config and retry/failure controls in reporting UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Integration Connection Configuration And Retry Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Integrations now have connection configuration, failure simulation, failed run error messages, and retry-success run records.

## University ERP Production Slice: Advising And Program-Change Workflows

- [x] Inspect student-service type/request UI for advising and program-change workflows.
- [x] Seed program-change and graduation-clearance service types.
- [x] Expose advising and program-change workflow shortcuts in Student Services UI.
- [x] Add English and Mongolian translation keys without replacement-character corruption.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Advising And Program-Change Workflows Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Student Services now exposes advising and program-change workflow shortcuts; program-change and graduation-clearance service types are seeded through Flyway.

## University ERP Production Slice: Academic Co-Requisites And Prerequisite Groups

- [x] Inspect current prerequisite enforcement path.
- [x] Add co-requisite and prerequisite-group schema/API support.
- [x] Enforce grouped prerequisites and co-requisites during course selection.
- [x] Expose rules in Course Selection UI.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Academic Co-Requisites And Prerequisite Groups Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Course selection now supports prerequisite groups, co-requisites, completed-course satisfaction, same-term co-selection, and existing same-term enrollment satisfaction.

## University ERP Production Slice: Reporting Drill-Down And Visibility

- [x] Inspect current reporting definitions, summary DTOs, and UI report workspace.
- [x] Add backend drill-down detail rows and report visibility metadata.
- [x] Expose drill-down rows and role visibility in the Reporting UI.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Reporting Drill-Down And Visibility Review

- Backend verification passed with `mvn test` after fixing two detail-row accessor mismatches caught by the first compile attempt.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Report definitions now expose role visibility metadata and drill-down detail rows for enrollment, finance, student services, faculty workload, and integration health.

## University ERP Production Slice: HR Leave Request Workflow

- [x] Inspect HR/faculty profile and workload backend/frontend patterns.
- [x] Add faculty leave request schema, DTOs, repository, service, and controller endpoints.
- [x] Expose leave request workflow in the HR/faculty ERP UI with translations.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: HR Leave Request Workflow Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- HR/faculty now has persistent faculty leave requests linked to faculty profiles, with submit, approve, reject, and cancel-capable backend workflow.

## University ERP Production Slice: Graduation Clearance Workflow

- [x] Inspect current graduation-clearance service type and degree-audit/service-request code paths.
- [x] Add backend graduation-clearance evaluation data and API workflow.
- [x] Expose graduation-clearance workflow in Student Services UI with translations.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Graduation Clearance Workflow Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Graduation clearance now evaluates degree audit status, finance balance, and attachment satisfaction, then approves or holds the service request with history and audit events.

## University ERP Production Slice: External Adapter And Secret Configuration

- [x] Inspect current integration connection/run model and reporting UI controls.
- [x] Add adapter mode, auth type, and secret-reference configuration to integration connections.
- [x] Use adapter configuration in integration run payloads and expose it in UI.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: External Adapter And Secret Configuration Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Integration connections now support adapter mode, auth type, and secret references; backend validation keeps HTTP/auth configuration explicit while avoiding direct secret storage in the UI.

## University ERP Production Slice: Production Integration Hardening

- [x] Add a real outbound HTTP adapter path for configured ERP integrations.
- [x] Resolve configured secrets from environment variables or Spring properties by reference.
- [x] Record HTTP adapter success/failure status in persisted integration runs.
- [x] Add integration configuration smoke-test API and UI action.
- [x] Update the execution plan.
- [x] Run backend verification with `mvn test`.
- [x] Run frontend verification with `npm run build`.

## University ERP Production Slice: Production Integration Hardening Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- Configured HTTP integrations now perform real outbound POST exchanges, record HTTP success/failure, and resolve secret values from deployment environment or Spring properties by reference.

## University ERP Production Slice: Vendor Onboarding Package

- [x] Add deployment environment template for real LMS, bank, notification, and government-reporting vendors.
- [x] Add admin API smoke-test runner that saves vendor connection config and validates backend readiness.
- [x] Add vendor onboarding documentation and go-live acceptance criteria.
- [x] Update deployment README.
- [x] Update ERP execution plan.
- [x] Run verification.

## University ERP Production Slice: Vendor Onboarding Package Review

- Backend verification passed with `mvn test`.
- Frontend verification passed with `npm run build`.
- PowerShell smoke-test script parsed successfully.
- The deployment package now has a real-vendor env template, admin API configuration/smoke-test runner, and operator onboarding checklist.

## University ERP Frontend Translation Coverage Slice

- [x] Audit university ERP frontend pages for hardcoded visible text.
- [x] Add missing English and Mongolian translation keys for ERP statuses, offices, service types, integration labels, auth modes, adapter modes, and semester labels.
- [x] Replace hardcoded visible ERP labels with translation-aware helpers.
- [x] Remove unused static demo record table with hardcoded sample text.
- [x] Run frontend verification with `npm run build`.
- [x] Run locale JSON corruption check.

## University ERP Frontend Translation Coverage Review

- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` replacement characters.
- The ERP blueprint and module demo now use translation keys for the visible labels found in the audit; remaining literal strings are internal codes, route keys, API enum values, or user/backend data.

## University ERP Mongolian Page Copy Repair

- [x] Inspect ERP page content shown in Mongolian locale.
- [x] Replace remaining English Mongolian-locale copy for ERP common labels, blueprint text, admissions page workflow, business rules, integrations, and shared field labels.
- [x] Repair replacement-character corruption introduced by shell encoding.
- [x] Run frontend build.
- [x] Run strict locale JSON replacement-marker check.

## University ERP Mongolian Page Copy Repair Review

- Frontend verification passed with `npm run build`.
- Locale verification passed for English and Mongolian JSON with no `???` or repeated question-mark replacement runs.
- The admissions ERP page content shown in the screenshot now has Mongolian locale values for the visible page copy.
