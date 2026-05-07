# Middle School Feature Implementation Plan

This roadmap covers the missing middle-school system features previously identified for EduSys. It assumes the existing React frontend and Spring Boot backend architecture, and it treats the recently implemented finance/cafeteria slice as Phase 1 groundwork.

## Guiding Principles

- Keep each phase independently shippable.
- Reuse existing role portals: admin, teacher, student, and parent.
- Add new backend tables through Flyway migrations because Hibernate runs with `ddl-auto: validate`.
- Add translation keys for all frontend text.
- Keep mobile responsiveness in scope for every new frontend view.
- Use existing access checks and extend them carefully where new staff roles are introduced.
- Verify each phase with backend tests/build and frontend build.

## Phase 1: Finance, Billing, And Cafeteria Foundation

Status: first slice implemented.

### Current Coverage

- Fee item catalog.
- Student invoices.
- Invoice lines.
- Payment recording.
- Meal plans.
- Meal items.
- Meal purchase records.
- Admin finance/cafeteria screen.
- Student and parent finance/cafeteria summary screens.

### Follow-Up Work

- [Shipped 2026-05-02] Add invoice edit actions for unpaid open invoices.
- [Shipped 2026-05-02] Add invoice cancel/waive actions.
- [Shipped 2026-05-02] Add multi-line invoice creation in the admin UI.
- Add receipt export.
- [Shipped 2026-05-02] Add printable payment confirmation.
- [Shipped 2026-05-02] Add invoice status filters for billing/payment follow-up.
- [Shipped 2026-05-02] Add searchable student lookup to admin invoice and meal-purchase forms instead of manual student ID entry.
- [Shipped 2026-05-02] Add cafeteria date filters and daily meal summaries.
- [Shipped 2026-05-02] Add permission split foundation for future finance staff.

## Phase 2: Access Control And Staff Roles

Purpose: create the permission foundation needed before adding nurse, counselor, finance, library, and transport modules.

Status: first access-control foundation slice implemented.

### Backend

- [Shipped 2026-05-02] Extend role modeling beyond the current admin, teacher, student, and parent flags.
- [Shipped 2026-05-02] Add roles for counselor, nurse, finance staff, librarian, transport coordinator, admissions staff, and cafeteria staff.
- [Shipped 2026-05-02] Add role/permission DTOs and admin APIs for assigning staff roles.
- [Shipped 2026-05-02] Add method-level access checks for the existing finance and cafeteria domains.
- Add method-level access checks for each new domain as it ships.
- Add audit-friendly `createdBy`, `updatedBy`, and timestamp fields to new sensitive modules.

### Frontend

- [Shipped 2026-05-02] Add admin staff-permissions screen.
- [Shipped 2026-05-02] Show a narrow staff sidebar entry for finance/cafeteria staff.
- Show sidebar entries conditionally based on future role/permission-specific modules.
- [Shipped 2026-05-02] Add role labels to user list and detail views.
- [Shipped 2026-05-02] Add translation keys for new role names and permission descriptions.

### Verification

- Backend role access tests for each new domain.
- [Passed 2026-05-02] Backend test suite with `mvn test`.
- [Passed 2026-05-02] Frontend build with `npm run build`.
- Manual smoke test with admin, parent, student, and one new staff role.

## Phase 3: Student Support And Safety

Purpose: track student behavior, discipline, counseling, and intervention workflows.

### Backend

- Add behavior incident tables: incident, involved student, reporter, action taken, follow-up status.
- Add discipline action tables: detention, warning, suspension, restorative action, parent notification.
- Add counseling case tables: counselor notes, intervention plans, follow-up dates.
- Add student support flags for repeated incidents, open interventions, and escalation status.
- Add APIs for admin/counselor incident management.
- Add read-only parent/student views where appropriate, with privacy controls.

### Frontend

- Admin/counselor incident dashboard.
- Student support profile section.
- Incident creation and follow-up forms.
- Parent-visible behavior summary, limited to approved/shareable records.
- Mobile card views for incident history.

### Verification

- Backend tests for access control and privacy filtering.
- Frontend build.
- Test that parents cannot see private counseling notes.

## Phase 4: Health, Nurse, Allergies, And Emergency Contacts

Purpose: support student safety and nurse office operations.

### Backend

- Add emergency contact model linked to students.
- Add health profile model: allergies, medications, medical notes, physician contact.
- Add nurse visit model: visit time, symptoms, action taken, pickup required, parent notified.
- Add medication administration log if needed.
- Add APIs for nurse/admin management and parent read-only emergency/health views.

### Frontend

- Nurse dashboard.
- Student health profile form.
- Emergency contacts editor.
- Nurse visit log.
- Parent emergency information view.
- Clear warning badges for allergies and urgent health notes.

### Verification

- Backend tests for nurse/admin access and parent read-only access.
- Frontend build.
- Manual mobile check for health profile and emergency contact screens.

## Phase 5: Academic Records And Documents

Purpose: generate formal academic outputs such as report cards and transcripts.

### Backend

- Add report card generation service using existing grades, attendance, subjects, and comments.
- Add report card records with generated date, term, status, and file reference.
- Add transcript/permanent record export model.
- Add teacher comment capture per subject or term.
- Add APIs for admin generation, teacher comments, parent/student download.

### Frontend

- Admin report card generation screen.
- Teacher term-comment entry screen.
- Student/parent academic documents page.
- Download buttons for generated report cards and transcripts.

### Verification

- Backend tests for report card calculations and permissions.
- Frontend build.
- Generate a sample document and verify content.

## Phase 6: Communication And Calendar

Purpose: move beyond announcements into targeted messaging and school events.

### Backend

- Add conversation/thread model for parent-teacher and internal staff messaging.
- Add message recipients, read receipts, attachments, and moderation/audit metadata.
- Add school calendar event model for assemblies, clubs, deadlines, holidays, and parent meetings.
- Add APIs for role-scoped inboxes and event calendars.

### Frontend

- Parent-teacher messaging inbox.
- Staff messaging inbox.
- Message compose and reply views.
- Calendar page shared by role, with filters for class, school-wide, personal, and exam events.
- Notification badges for unread messages.

### Verification

- Backend tests for recipient scoping.
- Frontend build.
- Manual smoke test: teacher sends message to parent, parent replies, unrelated parent cannot access thread.

## Phase 7: Transportation

Purpose: manage buses, routes, student assignments, and pickup/drop-off details.

### Backend

- Add bus, route, stop, driver, and student transportation assignment models.
- Add pickup/drop-off time windows and emergency contact tie-ins.
- Add APIs for transport coordinator management and parent/student route views.

### Frontend

- Transport coordinator dashboard.
- Route and stop editor.
- Student route assignment screen.
- Parent/student transportation view.

### Verification

- Backend tests for route assignment and parent access.
- Frontend build.
- Mobile smoke test for parent route details.

## Phase 8: Library, Assets, And Textbook Checkout

Purpose: track books, textbooks, devices, and other student-issued assets.

### Backend

- Add catalog item model with type, barcode, title, condition, and replacement cost.
- Add checkout/return model.
- Add overdue and lost/damaged status handling.
- Add APIs for librarian management and student/parent read-only views.

### Frontend

- Library/asset dashboard.
- Catalog search and item detail.
- Checkout and return workflow.
- Student/parent borrowed-items page.

### Verification

- Backend tests for checkout rules and overdue state.
- Frontend build.
- Manual test for checkout, return, lost item.

## Phase 9: Admissions And Registration

Purpose: support new student intake before enrollment.

### Backend

- Add applicant model separate from active student users.
- Add guardian/contact records for applicants.
- Add document checklist and admissions status workflow.
- Add conversion flow from accepted applicant to student user/enrollment.

### Frontend

- Admissions dashboard.
- Applicant creation and review forms.
- Document checklist UI.
- Convert-to-student action.

### Verification

- Backend tests for applicant lifecycle.
- Frontend build.
- Manual test: create applicant, accept, convert to enrolled student.

## Suggested Implementation Order

1. Complete Phase 1 follow-ups that improve usability of the existing finance/cafeteria slice.
2. Implement Phase 2 access control before adding sensitive staff workflows.
3. Implement Phase 4 health/emergency contacts because it is high-value and safety-critical.
4. Implement Phase 3 student support/discipline/counseling.
5. Implement Phase 5 academic documents.
6. Implement Phase 6 messaging and calendar.
7. Implement Phase 7 transportation.
8. Implement Phase 8 library/assets.
9. Implement Phase 9 admissions.

## Cross-Cutting Requirements

- Add Flyway migrations for every backend schema change.
- Add DTOs instead of exposing sensitive entities directly.
- Keep parent/student visibility intentionally limited for health, counseling, discipline, and messaging records.
- Add admin list filters and pagination for every module that can grow large.
- Use mobile card layouts alongside desktop tables.
- Add English and Mongolian translation keys for all new frontend text.
- Update sidebar navigation per role.
- Run `mvn test` for backend changes.
- Run `npm run build` for frontend changes.
- Update this plan after each phase ships.

## Open Decisions

- New staff roles currently remain bit flags for compatibility; revisit a normalized role/permission table when fine-grained per-domain permissions are needed.
- Whether document generation should produce PDF, DOCX, or both.
- Whether online payments should be integrated with a real payment provider or remain manual-record only.
- Whether messaging needs real-time delivery now or can begin with refresh-based inboxes.
- Whether health and counseling records require stricter audit logs than the rest of the system.
