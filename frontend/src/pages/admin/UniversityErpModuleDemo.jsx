import { useEffect, useMemo, useState } from 'react';
import { Navigate, Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import SectionCard from '../../components/ui/SectionCard';
import { ErrorState, LoadingState } from '../../components/ui/StateBlock';
import {
  acceptUniversityApplicant,
  addUniversityServiceRequestComment,
  assignUniversityServiceRequest,
  createUniversityAcademicRecord,
  createUniversityApplicant,
  createUniversityCourseCorequisite,
  createUniversityCoursePrerequisite,
  createUniversityCourseSelection,
  createUniversityDepartment,
  createUniversityFacultyLeaveRequest,
  createUniversityFacultyWorkload,
  createUniversityProgramRequirement,
  createUniversityServiceRequest,
  createUniversityServiceType,
  dispatchUniversityNotifications,
  evaluateUniversityGraduationClearance,
  exportUniversityGovernmentReport,
  exportUniversityLmsRoster,
  exportUniversityReportCsv,
  failUniversityIntegration,
  getAllUsers,
  getUniversityAcademicRecords,
  getUniversityAcademicPolicy,
  getUniversityAuditEvents,
  getUniversityIntegrations,
  getUniversityApplicants,
  getUniversityCourses,
  getUniversityCourseCorequisites,
  getUniversityCourseSelections,
  getUniversityDepartments,
  getUniversityDegreeAudit,
  getUniversityFacultyProfiles,
  getUniversityFacultyLeaveRequests,
  getUniversityFacultyWorkloads,
  getUniversityIntegrationRuns,
  getUniversityIntegrationConnections,
  getUniversityProgramRequirements,
  getUniversityReportDefinitions,
  getUniversityReportDetails,
  getUniversityReportRuns,
  getUniversityReportSummary,
  getUniversityServiceRequests,
  getUniversityServiceRequestDetail,
  getUniversityServiceQueues,
  getUniversityServiceTypes,
  rejectUniversityApplicant,
  decideUniversityFacultyLeaveRequest,
  runUniversityIntegration,
  runUniversityReport,
  retryUniversityIntegrationRun,
  saveUniversityIntegrationConnection,
  screenUniversityApplicant,
  simulateUniversityBankPaymentCallback,
  smokeTestUniversityIntegrations,
  updateUniversityServiceRequestStatus,
  updateUniversityAcademicPolicy,
  updateUniversityServiceType,
  upsertUniversityFacultyProfile,
  uploadUniversityServiceRequestAttachments,
} from '../../api/endpoints';

const MODULES = {
  admissions: {
    steps: ['submitted', 'screening', 'decision', 'conversion'],
    rules: ['requirements', 'documents', 'conversion'],
    integrations: ['studentRegistration', 'notification', 'reporting'],
  },
  'course-selection': {
    steps: ['catalog', 'eligibility', 'selection', 'billing'],
    rules: ['creditLimit', 'prerequisite', 'financeHold'],
    integrations: ['academicManagement', 'finance', 'assessment'],
  },
  'student-services': {
    steps: ['request', 'review', 'approval', 'delivery'],
    rules: ['studentAccess', 'serviceStatus', 'financeRestriction'],
    integrations: ['registration', 'finance', 'notification'],
  },
  reporting: {
    steps: ['collect', 'aggregate', 'publish', 'govern'],
    rules: ['freshness', 'roleScope', 'auditTrail'],
    integrations: ['admissions', 'finance', 'assessment'],
  },
  'hr-faculty': {
    steps: ['profile', 'department', 'workload', 'governance'],
    rules: ['profileOwnership', 'workloadTarget', 'employmentStatus'],
    integrations: ['academicManagement', 'reporting', 'accessManagement'],
  },
};

const initialApplicant = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  program: '',
};

const initialServiceRequest = {
  studentId: '',
  requestType: '',
  description: '',
};

const initialServiceType = {
  id: null,
  code: '',
  name: '',
  defaultOffice: '',
  slaDays: 5,
  requiresFinanceClearance: false,
  requiresAttachment: false,
  active: true,
};

const initialAcademicPolicy = {
  policyName: '',
  minTermCredits: 12,
  maxTermCredits: 18,
  probationMaxTermCredits: 12,
  minAverageGradeGoodStanding: 60,
  blockRegistrationWhenProbation: false,
  allowRepeatCompletedCourses: false,
};

const initialProgramRequirement = {
  programName: '',
  requirementName: '',
  subjectId: '',
  requiredCredits: 3,
};

const defaultAcademicYear = `${new Date().getFullYear()}-${new Date().getFullYear() + 1}`;

const initialFacultyProfile = {
  facultyUserId: '',
  employeeNumber: '',
  department: '',
  academicRank: '',
  employmentStatus: 'ACTIVE',
  hireDate: '',
  officeLocation: '',
  workloadTargetCredits: 12,
};

const initialDepartmentForm = {
  code: '',
  name: '',
  active: true,
};

const initialWorkloadForm = {
  facultyProfileId: '',
  academicYear: defaultAcademicYear,
  semester: 1,
  teachingCredits: 0,
  advisingCredits: 0,
  researchCredits: 0,
  committeeCredits: 0,
  notes: '',
};

const initialLeaveForm = {
  facultyProfileId: '',
  leaveType: '',
  startDate: '',
  endDate: '',
  reason: '',
};

const formatMoney = (value) =>
  new Intl.NumberFormat(undefined, { style: 'currency', currency: 'MNT', maximumFractionDigits: 0 }).format(Number(value || 0));

const formatDateTime = (value) => (value ? new Date(value).toLocaleString() : '-');

const fullName = (user) =>
  [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.name || `#${user?.id}`;

const normalizedKey = (value) => String(value || '').replace(/[^A-Za-z0-9]+(.)/g, (_, char) => char.toUpperCase()).replace(/^[A-Z]/, (char) => char.toLowerCase());

const serviceTypeLabel = (t, value) => {
  const code = typeof value === 'string' ? value : value?.code;
  const fallback = typeof value === 'string' ? value : value?.name;
  return t(`universityErp.live.studentServices.serviceTypes.${code}`, { defaultValue: fallback || '-' });
};

const officeLabel = (t, value) =>
  t(`universityErp.live.offices.${normalizedKey(value)}`, { defaultValue: value || '-' });

const statusLabel = (t, value) =>
  t(`universityErp.live.statuses.${value}`, { defaultValue: value || '-' });

const integrationKeyLabel = (t, value) =>
  t(`universityErp.live.reporting.integrationKeys.${value}`, { defaultValue: value || '-' });

const adapterModeLabel = (t, value) =>
  t(`universityErp.live.reporting.adapterModes.${value}`, { defaultValue: value || '-' });

const authTypeLabel = (t, value) =>
  t(`universityErp.live.reporting.authTypes.${value}`, { defaultValue: value || '-' });

function Field({ label, children }) {
  return (
    <label className="erp-field">
      <span>{label}</span>
      {children}
    </label>
  );
}

export default function UniversityErpModuleDemo() {
  const { moduleKey } = useParams();
  const { t } = useTranslation();
  const module = MODULES[moduleKey];

  if (!module) return <Navigate to="/admin/university-erp" replace />;

  return (
    <div className="erp-page">
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('universityErp.kicker')}</div>
          <h1>{t(`universityErp.demo.${moduleKey}.title`)}</h1>
          <p className="page-summary">{t(`universityErp.demo.${moduleKey}.summary`)}</p>
        </div>
        <Link className="btn btn-secondary" to="/admin/university-erp">{t('universityErp.common.backToBlueprint')}</Link>
      </div>

      <div className="erp-workflow">
        {module.steps.map((step, index) => (
          <div className="erp-workflow-step" key={step}>
            <span>{index + 1}</span>
            <strong>{t(`universityErp.demo.${moduleKey}.steps.${step}`)}</strong>
          </div>
        ))}
      </div>

      {moduleKey === 'admissions' ? <AdmissionsDemo t={t} /> : null}
      {moduleKey === 'course-selection' ? <CourseSelectionDemo t={t} /> : null}
      {moduleKey === 'reporting' ? <ReportingDemo t={t} /> : null}
      {moduleKey === 'student-services' ? <StudentServicesDemo t={t} /> : null}
      {moduleKey === 'hr-faculty' ? <HrFacultyDemo t={t} /> : null}

      <div className="erp-two-column">
        <SectionCard title={t('universityErp.common.businessRules')} subtitle={t(`universityErp.demo.${moduleKey}.rulesSubtitle`)}>
          <div className="erp-rule-list">
            {module.rules.map((rule) => (
              <div className="erp-rule-row" key={rule}>
                <strong>{t(`universityErp.demo.${moduleKey}.rules.${rule}.title`)}</strong>
                <span>{t(`universityErp.demo.${moduleKey}.rules.${rule}.body`)}</span>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title={t('universityErp.common.integrations')} subtitle={t('universityErp.common.integrationsSubtitle')}>
          <div className="erp-integration-list">
            {module.integrations.map((integration) => (
              <div className="erp-integration-row" key={integration}>
                <span>{t(`universityErp.integration.${integration}`)}</span>
              </div>
            ))}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

function StudentServicesDemo({ t }) {
  const [students, setStudents] = useState([]);
  const [staffUsers, setStaffUsers] = useState([]);
  const [requests, setRequests] = useState([]);
  const [queues, setQueues] = useState([]);
  const [serviceTypes, setServiceTypes] = useState([]);
  const [form, setForm] = useState(initialServiceRequest);
  const [typeForm, setTypeForm] = useState(initialServiceType);
  const [detail, setDetail] = useState(null);
  const [filters, setFilters] = useState({ assignedOffice: '', slaStatus: '', assignedUserId: '' });
  const [assignment, setAssignment] = useState({ assignedOffice: '', assignedUserId: '', notes: '' });
  const [commentText, setCommentText] = useState('');
  const [commentInternal, setCommentInternal] = useState(false);
  const [attachmentFiles, setAttachmentFiles] = useState([]);
  const [clearanceProgramName, setClearanceProgramName] = useState('');
  const [clearanceResult, setClearanceResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const loadData = async (overrideFilters = filters) => {
    setLoading(true);
    setError('');
    try {
      const requestParams = {
        assignedOffice: overrideFilters.assignedOffice || undefined,
        slaStatus: overrideFilters.slaStatus || undefined,
        assignedUserId: overrideFilters.assignedUserId || undefined,
      };
      const [studentResponse, userResponse, requestResponse, queueResponse, typeResponse] = await Promise.all([
        getAllUsers({ role: 1, page: 1, pageSize: 100 }),
        getAllUsers({ page: 1, pageSize: 100 }),
        getUniversityServiceRequests(requestParams),
        getUniversityServiceQueues(),
        getUniversityServiceTypes(),
      ]);
      const studentData = Array.isArray(studentResponse.data?.items) ? studentResponse.data.items : studentResponse.data || [];
      const allUsers = Array.isArray(userResponse.data?.items) ? userResponse.data.items : userResponse.data || [];
      const staffData = allUsers.filter((user) => Number(user.roleFlags || 0) !== 1);
      const typeData = typeResponse.data || [];
      setStudents(studentData);
      setStaffUsers(staffData);
      setRequests(requestResponse.data || []);
      setQueues(queueResponse.data || []);
      setServiceTypes(typeData);
      setForm((current) => ({
        ...current,
        studentId: current.studentId || !studentData[0]?.id ? current.studentId : String(studentData[0].id),
        requestType: current.requestType || typeData[0]?.code || '',
      }));
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const submitServiceType = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        code: typeForm.code,
        name: typeForm.name,
        defaultOffice: typeForm.defaultOffice,
        slaDays: Number(typeForm.slaDays),
        requiresFinanceClearance: typeForm.requiresFinanceClearance,
        requiresAttachment: typeForm.requiresAttachment,
        active: typeForm.active,
      };
      if (typeForm.id) {
        await updateUniversityServiceType(typeForm.id, payload);
      } else {
        await createUniversityServiceType(payload);
      }
      setTypeForm(initialServiceType);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const editServiceType = (serviceType) => {
    setTypeForm({
      id: serviceType.id,
      code: serviceType.code,
      name: serviceType.name,
      defaultOffice: serviceType.defaultOffice,
      slaDays: serviceType.slaDays,
      requiresFinanceClearance: Boolean(serviceType.requiresFinanceClearance),
      requiresAttachment: Boolean(serviceType.requiresAttachment),
      active: Boolean(serviceType.active),
    });
  };

  useEffect(() => {
    loadData();
  }, []);

  const submitRequest = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityServiceRequest({
        studentId: Number(form.studentId),
        requestType: form.requestType,
        description: form.description,
      });
      setForm((current) => ({ ...initialServiceRequest, studentId: current.studentId }));
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const useWorkflowTemplate = (requestType, description) => {
    setForm((current) => ({
      ...current,
      requestType,
      description,
    }));
  };

  const evaluateGraduationClearance = async () => {
    if (!detail?.request?.id) return;
    setSaving(true);
    setError('');
    setClearanceResult(null);
    try {
      const response = await evaluateUniversityGraduationClearance(detail.request.id, {
        programName: clearanceProgramName,
      });
      setClearanceResult(response.data);
      await loadData();
      await loadDetail(detail.request.id);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const updateStatus = async (id, status) => {
    setSaving(true);
    setError('');
    try {
      await updateUniversityServiceRequestStatus(id, {
        status,
        assignedOffice: assignment.assignedOffice || undefined,
        assignedUserId: assignment.assignedUserId ? Number(assignment.assignedUserId) : undefined,
        notes: t('universityErp.live.studentServices.statusNote'),
      });
      await loadData();
      if (detail?.request?.id === id) await loadDetail(id);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitAssignment = async (event) => {
    event.preventDefault();
    if (!detail?.request?.id) return;
    setSaving(true);
    setError('');
    try {
      await assignUniversityServiceRequest(detail.request.id, {
        assignedOffice: assignment.assignedOffice || undefined,
        assignedUserId: assignment.assignedUserId ? Number(assignment.assignedUserId) : undefined,
        notes: assignment.notes,
      });
      setAssignment({ assignedOffice: '', assignedUserId: '', notes: '' });
      await loadData();
      await loadDetail(detail.request.id);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const loadDetail = async (id) => {
    setError('');
    try {
      const response = await getUniversityServiceRequestDetail(id);
      setDetail(response.data);
      setAssignment({
        assignedOffice: response.data?.request?.assignedOffice || '',
        assignedUserId: response.data?.request?.assignedUserId ? String(response.data.request.assignedUserId) : '',
        notes: '',
      });
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const submitComment = async (event) => {
    event.preventDefault();
    if (!detail?.request?.id) return;
    setSaving(true);
    setError('');
    try {
      await addUniversityServiceRequestComment(detail.request.id, {
        commentText,
        internal: commentInternal,
      });
      setCommentText('');
      setCommentInternal(false);
      await loadDetail(detail.request.id);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitAttachments = async (event) => {
    event.preventDefault();
    if (!detail?.request?.id || attachmentFiles.length === 0) return;
    setSaving(true);
    setError('');
    try {
      await uploadUniversityServiceRequestAttachments(detail.request.id, attachmentFiles);
      setAttachmentFiles([]);
      event.target.reset();
      await loadDetail(detail.request.id);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (error && !requests.length) return <ErrorState title={t('common.error')} description={error} retryLabel={t('common.retry')} onRetry={loadData} />;

  return (
    <SectionCard title={t('universityErp.live.studentServices.title')} subtitle={t('universityErp.live.studentServices.subtitle')}>
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <form className="erp-live-form" onSubmit={submitRequest}>
        <Field label={t('universityErp.live.fields.student')}>
          <select value={form.studentId} onChange={(event) => setForm({ ...form, studentId: event.target.value })} required>
            {students.map((student) => <option key={student.id} value={student.id}>{fullName(student)}</option>)}
          </select>
        </Field>
        <Field label={t('universityErp.live.studentServices.requestType')}>
          <select value={form.requestType} onChange={(event) => setForm({ ...form, requestType: event.target.value })}>
            {serviceTypes.filter((type) => type.active).map((type) => (
              <option value={type.code} key={type.id}>{serviceTypeLabel(t, type)}</option>
            ))}
          </select>
        </Field>
        <Field label={t('universityErp.live.studentServices.description')}>
          <input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </Field>
        <button type="submit" className="btn btn-primary" disabled={saving || !form.studentId}>
          {t('universityErp.live.studentServices.submit')}
        </button>
      </form>

      <div className="erp-subsection">
        <h3>{t('universityErp.live.studentServices.workflowShortcuts')}</h3>
          <div className="erp-governance-grid">
          <div className="erp-governance-item">
            <strong>{t('universityErp.live.studentServices.advisingWorkflow')}</strong>
            <span>{t('universityErp.live.studentServices.advisingWorkflowHint')}</span>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => useWorkflowTemplate('ADVISING_APPOINTMENT', t('universityErp.live.studentServices.advisingDescription'))}>
              {t('universityErp.live.studentServices.useWorkflow')}
            </button>
          </div>
          <div className="erp-governance-item">
            <strong>{t('universityErp.live.studentServices.programChangeWorkflow')}</strong>
            <span>{t('universityErp.live.studentServices.programChangeWorkflowHint')}</span>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => useWorkflowTemplate('PROGRAM_CHANGE', t('universityErp.live.studentServices.programChangeDescription'))}>
              {t('universityErp.live.studentServices.useWorkflow')}
            </button>
          </div>
          <div className="erp-governance-item">
            <strong>{t('universityErp.live.studentServices.graduationClearanceWorkflow')}</strong>
            <span>{t('universityErp.live.studentServices.graduationClearanceWorkflowHint')}</span>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => useWorkflowTemplate('GRADUATION_CLEARANCE', t('universityErp.live.studentServices.graduationClearanceDescription'))}>
              {t('universityErp.live.studentServices.useWorkflow')}
            </button>
          </div>
        </div>
      </div>

      <div className="erp-queue-grid">
        {queues.map((queue) => (
          <button
            type="button"
            className="erp-queue-tile"
            key={queue.office}
            onClick={() => {
              const nextFilters = { ...filters, assignedOffice: queue.office };
              setFilters(nextFilters);
              loadData(nextFilters);
            }}
          >
            <strong>{officeLabel(t, queue.office)}</strong>
            <span>{t('universityErp.live.studentServices.openRequests', { count: queue.openRequests })}</span>
            <small>
              {t('universityErp.live.studentServices.queueMeta', {
                unassigned: queue.unassignedRequests,
                dueSoon: queue.dueSoonRequests,
                overdue: queue.overdueRequests,
              })}
            </small>
          </button>
        ))}
      </div>

      <form className="erp-live-form" onSubmit={(event) => { event.preventDefault(); loadData(filters); }}>
        <Field label={t('universityErp.live.studentServices.officeFilter')}>
          <input value={filters.assignedOffice} onChange={(event) => setFilters({ ...filters, assignedOffice: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.studentServices.slaStatus')}>
          <select value={filters.slaStatus} onChange={(event) => setFilters({ ...filters, slaStatus: event.target.value })}>
            <option value="">{t('common.all')}</option>
            {['ON_TRACK', 'DUE_SOON', 'OVERDUE', 'CLOSED', 'UNSCHEDULED'].map((status) => (
              <option value={status} key={status}>{t(`universityErp.live.studentServices.sla.${status}`)}</option>
            ))}
          </select>
        </Field>
        <Field label={t('universityErp.live.studentServices.assignee')}>
          <select value={filters.assignedUserId} onChange={(event) => setFilters({ ...filters, assignedUserId: event.target.value })}>
            <option value="">{t('common.all')}</option>
            {staffUsers.map((user) => <option key={user.id} value={user.id}>{fullName(user)}</option>)}
          </select>
        </Field>
        <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.studentServices.applyQueueFilters')}</button>
      </form>

      <div className="desktop-table table-container erp-live-table">
        <table>
          <thead>
            <tr>
              <th>{t('common.id')}</th>
              <th>{t('universityErp.live.fields.student')}</th>
              <th>{t('universityErp.live.studentServices.requestType')}</th>
              <th>{t('common.status')}</th>
              <th>{t('universityErp.live.studentServices.office')}</th>
              <th>{t('universityErp.live.studentServices.assignee')}</th>
              <th>{t('universityErp.live.studentServices.slaStatus')}</th>
              <th>{t('universityErp.live.studentServices.hold')}</th>
              <th>{t('common.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {requests.map((request) => (
              <tr key={request.id}>
                <td>{request.requestNumber}</td>
                <td>{request.studentName}</td>
                <td>{serviceTypeLabel(t, request.requestType)}</td>
                <td><span className="badge badge-info">{statusLabel(t, request.status)}</span></td>
                <td>{officeLabel(t, request.assignedOffice)}</td>
                <td>{request.assignedUserName || '-'}</td>
                <td>
                  <span className={`badge erp-sla-${String(request.slaStatus || '').toLowerCase().replace('_', '-')}`}>
                    {t(`universityErp.live.studentServices.sla.${request.slaStatus}`, { defaultValue: request.slaStatus || '-' })}
                  </span>
                  <small className="muted-text">{formatDateTime(request.dueAt)}</small>
                </td>
                <td>{request.holdReason || '-'}</td>
                <td>
                  <div className="erp-action-row">
                    <button type="button" className="btn btn-secondary btn-sm" disabled={saving || request.status === 'DELIVERED'} onClick={() => updateStatus(request.id, 'REVIEW')}>{t('universityErp.live.actions.review')}</button>
                    <button type="button" className="btn btn-primary btn-sm" disabled={saving || request.status === 'DELIVERED'} onClick={() => updateStatus(request.id, 'APPROVED')}>{t('universityErp.live.actions.approve')}</button>
                    <button type="button" className="btn btn-secondary btn-sm" disabled={saving || request.status === 'DELIVERED'} onClick={() => updateStatus(request.id, 'DELIVERED')}>{t('universityErp.live.actions.deliver')}</button>
                    <button type="button" className="btn btn-danger btn-sm" disabled={saving || request.status === 'DELIVERED'} onClick={() => updateStatus(request.id, 'REJECTED')}>{t('universityErp.live.actions.reject')}</button>
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => loadDetail(request.id)}>{t('universityErp.live.studentServices.details')}</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {detail ? (
        <div className="erp-subsection">
          <h3>{detail.request.requestNumber} | {serviceTypeLabel(t, detail.request.requestType)}</h3>
          <div className="erp-request-meta">
            <span>{t('universityErp.live.studentServices.office')}: <strong>{officeLabel(t, detail.request.assignedOffice)}</strong></span>
            <span>{t('universityErp.live.studentServices.assignee')}: <strong>{detail.request.assignedUserName || '-'}</strong></span>
            <span>{t('universityErp.live.studentServices.dueAt')}: <strong>{formatDateTime(detail.request.dueAt)}</strong></span>
            <span>{t('universityErp.live.studentServices.attachmentRule')}: <strong>{detail.request.attachmentRequired ? t('universityErp.live.studentServices.attachmentRequired') : '-'}</strong></span>
          </div>
          <form className="erp-live-form" onSubmit={submitAssignment}>
            <Field label={t('universityErp.live.studentServices.office')}>
              <input value={assignment.assignedOffice} onChange={(event) => setAssignment({ ...assignment, assignedOffice: event.target.value })} />
            </Field>
            <Field label={t('universityErp.live.studentServices.assignee')}>
              <select value={assignment.assignedUserId} onChange={(event) => setAssignment({ ...assignment, assignedUserId: event.target.value })}>
                <option value="">{t('universityErp.live.studentServices.unassigned')}</option>
                {staffUsers.map((user) => <option key={user.id} value={user.id}>{fullName(user)}</option>)}
              </select>
            </Field>
            <Field label={t('universityErp.live.studentServices.assignmentNotes')}>
              <input value={assignment.notes} onChange={(event) => setAssignment({ ...assignment, notes: event.target.value })} />
            </Field>
            <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.studentServices.assignRequest')}</button>
          </form>
          {String(detail.request.requestType || '').toLowerCase().includes('graduation') ? (
            <div className="erp-subsection">
              <h4>{t('universityErp.live.studentServices.graduationClearance')}</h4>
              <div className="erp-live-form">
                <Field label={t('universityErp.live.fields.program')}>
                  <input value={clearanceProgramName} onChange={(event) => setClearanceProgramName(event.target.value)} />
                </Field>
                <button type="button" className="btn btn-primary" disabled={saving || !clearanceProgramName} onClick={evaluateGraduationClearance}>
                  {t('universityErp.live.studentServices.evaluateClearance')}
                </button>
              </div>
              {clearanceResult ? (
                <div className="erp-request-meta">
                  <span>{t('universityErp.live.studentServices.clearanceEligible')}: <strong>{clearanceResult.eligible ? t('common.yes') : t('common.no')}</strong></span>
                  <span>{t('universityErp.live.courseSelection.remainingCredits')}: <strong>{clearanceResult.remainingCredits}</strong></span>
                  <span>{t('universityErp.live.studentServices.financeBalance')}: <strong>{formatMoney(clearanceResult.outstandingBalance)}</strong></span>
                  <span>{t('universityErp.live.studentServices.attachmentRule')}: <strong>{clearanceResult.attachmentSatisfied ? t('common.yes') : t('common.no')}</strong></span>
                  {clearanceResult.missingRequirements?.length ? <span>{t('universityErp.live.studentServices.missingRequirements')}: <strong>{clearanceResult.missingRequirements.join(', ')}</strong></span> : null}
                </div>
              ) : null}
            </div>
          ) : null}
          <div className="erp-two-column">
            <div className="erp-selection-list">
              <strong>{t('universityErp.live.studentServices.comments')}</strong>
              {detail.comments.map((comment) => (
                <div className="erp-rule-row" key={comment.id}>
                  <strong>{comment.authorName || '-'}</strong>
                  <span>{comment.commentText}</span>
                </div>
              ))}
              {!detail.comments.length ? <p className="muted-text">{t('universityErp.live.studentServices.noComments')}</p> : null}
              <form className="erp-stacked-form" onSubmit={submitComment}>
                <textarea value={commentText} onChange={(event) => setCommentText(event.target.value)} required rows={3} />
                <label className="erp-check-option">
                  <input type="checkbox" checked={commentInternal} onChange={(event) => setCommentInternal(event.target.checked)} />
                  <span>{t('universityErp.live.studentServices.internalComment')}</span>
                </label>
                <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.studentServices.addComment')}</button>
              </form>
            </div>
            <div className="erp-selection-list">
              <strong>{t('universityErp.live.studentServices.attachments')}</strong>
              {detail.attachments.map((attachment) => (
                <div className="erp-rule-row" key={attachment.id}>
                  <strong>{attachment.originalFilename}</strong>
                  <span>{Math.ceil((attachment.sizeBytes || 0) / 1024)} KB | {attachment.mimeType}</span>
                  <a className="btn btn-secondary btn-sm" href={attachment.downloadUrl}>{t('common.open')}</a>
                </div>
              ))}
              {!detail.attachments.length ? <p className="muted-text">{t('universityErp.live.studentServices.noAttachments')}</p> : null}
              <form className="erp-stacked-form" onSubmit={submitAttachments}>
                <input type="file" multiple onChange={(event) => setAttachmentFiles(Array.from(event.target.files || []))} />
                <button type="submit" className="btn btn-secondary" disabled={saving || attachmentFiles.length === 0}>{t('universityErp.live.studentServices.uploadAttachments')}</button>
              </form>
            </div>
          </div>
          <div className="erp-selection-list">
            <strong>{t('universityErp.live.studentServices.history')}</strong>
            {detail.history.map((item) => (
              <div className="erp-rule-row" key={item.id}>
                <strong>{item.eventType}</strong>
                <span>{item.fromStatus || '-'} {'->'} {item.toStatus || '-'} | {item.details || '-'}</span>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      <div className="erp-subsection">
        <h3>{t('universityErp.live.studentServices.typeConfigTitle')}</h3>
        <form className="erp-live-form" onSubmit={submitServiceType}>
          <Field label={t('universityErp.live.studentServices.typeCode')}>
            <input value={typeForm.code} onChange={(event) => setTypeForm({ ...typeForm, code: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.studentServices.typeName')}>
            <input value={typeForm.name} onChange={(event) => setTypeForm({ ...typeForm, name: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.studentServices.office')}>
            <input value={typeForm.defaultOffice} onChange={(event) => setTypeForm({ ...typeForm, defaultOffice: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.studentServices.slaDays')}>
            <input type="number" min="1" value={typeForm.slaDays} onChange={(event) => setTypeForm({ ...typeForm, slaDays: event.target.value })} required />
          </Field>
          <label className="erp-check-option">
            <input type="checkbox" checked={typeForm.requiresFinanceClearance} onChange={(event) => setTypeForm({ ...typeForm, requiresFinanceClearance: event.target.checked })} />
            <span>{t('universityErp.live.studentServices.financeClearance')}</span>
          </label>
          <label className="erp-check-option">
            <input type="checkbox" checked={typeForm.requiresAttachment} onChange={(event) => setTypeForm({ ...typeForm, requiresAttachment: event.target.checked })} />
            <span>{t('universityErp.live.studentServices.attachmentRequired')}</span>
          </label>
          <label className="erp-check-option">
            <input type="checkbox" checked={typeForm.active} onChange={(event) => setTypeForm({ ...typeForm, active: event.target.checked })} />
            <span>{t('common.active')}</span>
          </label>
          <button type="submit" className="btn btn-secondary" disabled={saving}>
            {typeForm.id ? t('universityErp.live.studentServices.updateType') : t('universityErp.live.studentServices.createType')}
          </button>
        </form>
        <div className="erp-selection-list">
          {serviceTypes.map((type) => (
            <div className="erp-rule-row" key={type.id}>
              <strong>{type.name}</strong>
              <span>{type.code} | {officeLabel(t, type.defaultOffice)} | {t('universityErp.live.studentServices.slaDays', { days: type.slaDays })} | {type.requiresFinanceClearance ? t('universityErp.live.studentServices.financeClearance') : t('universityErp.live.studentServices.noFinanceClearance')}</span>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => editServiceType(type)}>{t('common.update')}</button>
            </div>
          ))}
        </div>
      </div>
    </SectionCard>
  );
}

function AdmissionsDemo({ t }) {
  const [applicants, setApplicants] = useState([]);
  const [form, setForm] = useState(initialApplicant);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const loadApplicants = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getUniversityApplicants();
      setApplicants(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplicants();
  }, []);

  const submitApplicant = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityApplicant(form);
      setForm(initialApplicant);
      await loadApplicants();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const decide = async (id, action) => {
    setSaving(true);
    setError('');
    try {
      const notes = { notes: t('universityErp.live.admissions.decisionNote') };
      if (action === 'screen') await screenUniversityApplicant(id, notes);
      if (action === 'accept') await acceptUniversityApplicant(id, notes);
      if (action === 'reject') await rejectUniversityApplicant(id, notes);
      await loadApplicants();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionCard title={t('universityErp.live.admissions.title')} subtitle={t('universityErp.live.admissions.subtitle')}>
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <form className="erp-live-form" onSubmit={submitApplicant}>
        <Field label={t('universityErp.live.fields.firstName')}>
          <input value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} required />
        </Field>
        <Field label={t('universityErp.live.fields.lastName')}>
          <input value={form.lastName} onChange={(event) => setForm({ ...form, lastName: event.target.value })} required />
        </Field>
        <Field label={t('universityErp.live.fields.email')}>
          <input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required />
        </Field>
        <Field label={t('universityErp.live.fields.phone')}>
          <input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.fields.program')}>
          <input value={form.program} onChange={(event) => setForm({ ...form, program: event.target.value })} required />
        </Field>
        <button type="submit" className="btn btn-primary" disabled={saving}>{t('universityErp.live.admissions.submit')}</button>
      </form>

      {loading ? <LoadingState label={t('common.loading')} /> : (
        <div className="desktop-table table-container erp-live-table">
          <table>
            <thead>
              <tr>
                <th>{t('common.id')}</th>
                <th>{t('universityErp.common.name')}</th>
                <th>{t('universityErp.live.fields.program')}</th>
                <th>{t('common.status')}</th>
                <th>{t('universityErp.live.admissions.convertedStudent')}</th>
                <th>{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {applicants.map((applicant) => (
                <tr key={applicant.id}>
                  <td>{applicant.applicationNumber}</td>
                  <td>{applicant.firstName} {applicant.lastName}</td>
                  <td>{applicant.program}</td>
                  <td><span className="badge badge-info">{statusLabel(t, applicant.status)}</span></td>
                  <td>{applicant.convertedStudentName || '-'}</td>
                  <td>
                    <div className="erp-action-row">
                      <button type="button" className="btn btn-secondary btn-sm" disabled={saving || applicant.status === 'CONVERTED'} onClick={() => decide(applicant.id, 'screen')}>{t('universityErp.live.actions.screen')}</button>
                      <button type="button" className="btn btn-primary btn-sm" disabled={saving || applicant.status === 'CONVERTED'} onClick={() => decide(applicant.id, 'accept')}>{t('universityErp.live.actions.accept')}</button>
                      <button type="button" className="btn btn-danger btn-sm" disabled={saving || applicant.status === 'CONVERTED'} onClick={() => decide(applicant.id, 'reject')}>{t('universityErp.live.actions.reject')}</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </SectionCard>
  );
}

function CourseSelectionDemo({ t }) {
  const [courses, setCourses] = useState([]);
  const [students, setStudents] = useState([]);
  const [selections, setSelections] = useState([]);
  const [academicRecords, setAcademicRecords] = useState([]);
  const [academicPolicy, setAcademicPolicy] = useState(null);
  const [policyForm, setPolicyForm] = useState(initialAcademicPolicy);
  const [programRequirements, setProgramRequirements] = useState([]);
  const [requirementForm, setRequirementForm] = useState(initialProgramRequirement);
  const [degreeAudit, setDegreeAudit] = useState(null);
  const [studentId, setStudentId] = useState('');
  const [subjectIds, setSubjectIds] = useState([]);
  const [academicYear, setAcademicYear] = useState(defaultAcademicYear);
  const [semester, setSemester] = useState(1);
  const [prerequisiteSubjectId, setPrerequisiteSubjectId] = useState('');
  const [prerequisiteRequiredId, setPrerequisiteRequiredId] = useState('');
  const [prerequisiteGroupCode, setPrerequisiteGroupCode] = useState('');
  const [corequisites, setCorequisites] = useState([]);
  const [corequisiteSubjectId, setCorequisiteSubjectId] = useState('');
  const [corequisiteRequiredId, setCorequisiteRequiredId] = useState('');
  const [completedSubjectId, setCompletedSubjectId] = useState('');
  const [lastInvoice, setLastInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const selectedStudent = useMemo(() => students.find((student) => String(student.id) === String(studentId)), [studentId, students]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [courseResponse, studentResponse] = await Promise.all([
        getUniversityCourses(),
        getAllUsers({ role: 1, page: 1, pageSize: 100 }),
      ]);
      const [policyResponse, requirementResponse, corequisiteResponse] = await Promise.all([
        getUniversityAcademicPolicy(),
        getUniversityProgramRequirements({ programName: requirementForm.programName }),
        getUniversityCourseCorequisites(),
      ]);
      const studentData = Array.isArray(studentResponse.data?.items) ? studentResponse.data.items : studentResponse.data || [];
      setCourses(courseResponse.data || []);
      setStudents(studentData);
      setAcademicPolicy(policyResponse.data);
      setPolicyForm({
        policyName: policyResponse.data.policyName || initialAcademicPolicy.policyName,
        minTermCredits: policyResponse.data.minTermCredits ?? initialAcademicPolicy.minTermCredits,
        maxTermCredits: policyResponse.data.maxTermCredits ?? initialAcademicPolicy.maxTermCredits,
        probationMaxTermCredits: policyResponse.data.probationMaxTermCredits ?? initialAcademicPolicy.probationMaxTermCredits,
        minAverageGradeGoodStanding: policyResponse.data.minAverageGradeGoodStanding ?? initialAcademicPolicy.minAverageGradeGoodStanding,
        blockRegistrationWhenProbation: Boolean(policyResponse.data.blockRegistrationWhenProbation),
        allowRepeatCompletedCourses: Boolean(policyResponse.data.allowRepeatCompletedCourses),
      });
      setProgramRequirements(requirementResponse.data || []);
      setCorequisites(corequisiteResponse.data || []);
      if (!studentId && studentData[0]?.id) setStudentId(String(studentData[0].id));
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadSelections = async (nextStudentId = studentId) => {
    if (!nextStudentId) return;
    try {
      const [selectionResponse, recordResponse] = await Promise.all([
        getUniversityCourseSelections({ studentId: nextStudentId }),
        getUniversityAcademicRecords({ studentId: nextStudentId }),
      ]);
      setSelections(selectionResponse.data || []);
      setAcademicRecords(recordResponse.data || []);
      if (nextStudentId && requirementForm.programName) {
        const auditResponse = await getUniversityDegreeAudit({ studentId: nextStudentId, programName: requirementForm.programName });
        setDegreeAudit(auditResponse.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    loadSelections(studentId);
  }, [studentId]);

  const toggleSubject = (subjectId) => {
    setSubjectIds((current) =>
      current.includes(subjectId) ? current.filter((id) => id !== subjectId) : [...current, subjectId]
    );
  };

  const submitSelection = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const response = await createUniversityCourseSelection({
        studentId: Number(studentId),
        subjectIds,
        academicYear,
        semester: Number(semester),
      });
      setLastInvoice(response.data);
      setSubjectIds([]);
      await loadSelections(studentId);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitPrerequisite = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityCoursePrerequisite({
        subjectId: Number(prerequisiteSubjectId),
        prerequisiteSubjectId: Number(prerequisiteRequiredId),
        groupCode: prerequisiteGroupCode || null,
      });
      const response = await getUniversityCourses();
      setCourses(response.data || []);
      setPrerequisiteSubjectId('');
      setPrerequisiteRequiredId('');
      setPrerequisiteGroupCode('');
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitCorequisite = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityCourseCorequisite({
        subjectId: Number(corequisiteSubjectId),
        corequisiteSubjectId: Number(corequisiteRequiredId),
      });
      const response = await getUniversityCourseCorequisites();
      setCorequisites(response.data || []);
      setCorequisiteSubjectId('');
      setCorequisiteRequiredId('');
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitAcademicRecord = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityAcademicRecord({
        studentId: Number(studentId),
        subjectId: Number(completedSubjectId),
        academicYear,
        semester: Number(semester),
        finalGrade: 85,
        status: 'COMPLETED',
      });
      await loadSelections(studentId);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitAcademicPolicy = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const response = await updateUniversityAcademicPolicy({
        policyName: policyForm.policyName,
        minTermCredits: Number(policyForm.minTermCredits),
        maxTermCredits: Number(policyForm.maxTermCredits),
        probationMaxTermCredits: Number(policyForm.probationMaxTermCredits),
        minAverageGradeGoodStanding: Number(policyForm.minAverageGradeGoodStanding),
        blockRegistrationWhenProbation: policyForm.blockRegistrationWhenProbation,
        allowRepeatCompletedCourses: policyForm.allowRepeatCompletedCourses,
      });
      setAcademicPolicy(response.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitProgramRequirement = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityProgramRequirement({
        programName: requirementForm.programName,
        requirementName: requirementForm.requirementName,
        subjectId: requirementForm.subjectId ? Number(requirementForm.subjectId) : null,
        requiredCredits: Number(requirementForm.requiredCredits),
        active: true,
      });
      setRequirementForm((current) => ({ ...initialProgramRequirement, programName: current.programName }));
      const response = await getUniversityProgramRequirements({ programName: requirementForm.programName });
      setProgramRequirements(response.data || []);
      await loadSelections(studentId);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const refreshDegreeAudit = async () => {
    if (!studentId || !requirementForm.programName) return;
    setSaving(true);
    setError('');
    try {
      const [requirementResponse, auditResponse] = await Promise.all([
        getUniversityProgramRequirements({ programName: requirementForm.programName }),
        getUniversityDegreeAudit({ studentId, programName: requirementForm.programName }),
      ]);
      setProgramRequirements(requirementResponse.data || []);
      setDegreeAudit(auditResponse.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (error && !courses.length) return <ErrorState title={t('common.error')} description={error} retryLabel={t('common.retry')} onRetry={loadData} />;

  return (
    <div className="erp-two-column">
      <SectionCard title={t('universityErp.live.courseSelection.title')} subtitle={t('universityErp.live.courseSelection.subtitle')}>
        {error ? <div className="alert alert-danger">{error}</div> : null}
        {lastInvoice ? (
          <div className="alert alert-success">
            {t('universityErp.live.courseSelection.invoiceCreated', {
              number: lastInvoice.invoiceNumber,
              amount: formatMoney(lastInvoice.invoiceAmount),
            })}
          </div>
        ) : null}
        <form className="erp-live-form" onSubmit={submitSelection}>
          <Field label={t('universityErp.live.fields.student')}>
            <select value={studentId} onChange={(event) => setStudentId(event.target.value)} required>
              {students.map((student) => <option key={student.id} value={student.id}>{fullName(student)}</option>)}
            </select>
          </Field>
          <Field label={t('universityErp.live.fields.academicYear')}>
            <input value={academicYear} onChange={(event) => setAcademicYear(event.target.value)} required />
          </Field>
          <Field label={t('universityErp.live.fields.semester')}>
            <select value={semester} onChange={(event) => setSemester(event.target.value)}>
              <option value={1}>1</option>
              <option value={2}>2</option>
              <option value={3}>3</option>
            </select>
          </Field>
          <div className="erp-course-picker">
            {courses.map((course) => (
              <label className="erp-course-option" key={course.id}>
                <input type="checkbox" checked={subjectIds.includes(course.id)} onChange={() => toggleSubject(course.id)} />
                <span>
                  <strong>{course.courseCode || `#${course.id}`} {course.courseName}</strong>
                  <small>
                    {course.credits} {t('universityErp.live.fields.credits')}
                    {course.prerequisites?.length ? ` | ${t('universityErp.live.courseSelection.requires')}: ${course.prerequisites.join(', ')}` : ''}
                  </small>
                </span>
              </label>
            ))}
          </div>
          <button type="submit" className="btn btn-primary" disabled={saving || !studentId || subjectIds.length === 0}>
            {t('universityErp.live.courseSelection.submit')}
          </button>
        </form>
      </SectionCard>

      <SectionCard title={t('universityErp.live.courseSelection.currentSelections')} subtitle={selectedStudent ? fullName(selectedStudent) : ''}>
        <div className="erp-selection-list">
          {selections.map((selection) => (
            <div className="erp-rule-row" key={selection.id}>
              <strong>{selection.courseCode || selection.subjectId} {selection.courseName}</strong>
              <span>{selection.academicYear} {t('universityErp.live.fields.semesterShort', { semester: selection.semester })} | {selection.credits} {t('universityErp.live.fields.credits')} | {selection.invoiceNumber || statusLabel(t, selection.status)}</span>
            </div>
          ))}
          {!selections.length ? <p className="muted-text">{t('universityErp.live.courseSelection.noSelections')}</p> : null}
        </div>
        <div className="erp-subsection">
          <h3>{t('universityErp.live.courseSelection.eligibilityTitle')}</h3>
          <form className="erp-stacked-form" onSubmit={submitPrerequisite}>
            <Field label={t('universityErp.live.courseSelection.course')}>
              <select value={prerequisiteSubjectId} onChange={(event) => setPrerequisiteSubjectId(event.target.value)} required>
                <option value="">{t('universityErp.live.courseSelection.selectCourse')}</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
              </select>
            </Field>
            <Field label={t('universityErp.live.courseSelection.prerequisite')}>
              <select value={prerequisiteRequiredId} onChange={(event) => setPrerequisiteRequiredId(event.target.value)} required>
                <option value="">{t('universityErp.live.courseSelection.selectPrerequisite')}</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
              </select>
            </Field>
            <Field label={t('universityErp.live.courseSelection.prerequisiteGroup')}>
              <input value={prerequisiteGroupCode} onChange={(event) => setPrerequisiteGroupCode(event.target.value)} placeholder={t('universityErp.live.courseSelection.prerequisiteGroupPlaceholder')} />
            </Field>
            <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.courseSelection.addPrerequisite')}</button>
          </form>
          <form className="erp-stacked-form" onSubmit={submitCorequisite}>
            <Field label={t('universityErp.live.courseSelection.corequisiteCourse')}>
              <select value={corequisiteSubjectId} onChange={(event) => setCorequisiteSubjectId(event.target.value)} required>
                <option value="">{t('universityErp.live.courseSelection.selectCourse')}</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
              </select>
            </Field>
            <Field label={t('universityErp.live.courseSelection.corequisiteRequired')}>
              <select value={corequisiteRequiredId} onChange={(event) => setCorequisiteRequiredId(event.target.value)} required>
                <option value="">{t('universityErp.live.courseSelection.selectCorequisite')}</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
              </select>
            </Field>
            <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.courseSelection.addCorequisite')}</button>
          </form>
          <div className="erp-selection-list">
            {corequisites.map((rule) => (
              <div className="erp-rule-row" key={rule.id}>
                <strong>{rule.subjectCode || rule.subjectId} {rule.subjectName}</strong>
                <span>{t('universityErp.live.courseSelection.corequisites')}: {rule.corequisiteSubjectCode || rule.corequisiteSubjectId} {rule.corequisiteSubjectName}</span>
              </div>
            ))}
            {!corequisites.length ? <p className="muted-text">{t('universityErp.live.courseSelection.noCorequisites')}</p> : null}
          </div>
          <form className="erp-stacked-form" onSubmit={submitAcademicRecord}>
            <Field label={t('universityErp.live.courseSelection.completedCourse')}>
              <select value={completedSubjectId} onChange={(event) => setCompletedSubjectId(event.target.value)} required>
                <option value="">{t('universityErp.live.courseSelection.selectCompletedCourse')}</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
              </select>
            </Field>
            <button type="submit" className="btn btn-secondary" disabled={saving || !studentId}>{t('universityErp.live.courseSelection.markCompleted')}</button>
          </form>
          <div className="erp-selection-list">
            {academicRecords.map((record) => (
              <div className="erp-rule-row" key={record.id}>
                <strong>{record.courseCode || record.subjectId} {record.courseName}</strong>
                <span>{statusLabel(t, record.status)} | {record.academicYear} {t('universityErp.live.fields.semesterShort', { semester: record.semester })}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="erp-subsection">
          <h3>{t('universityErp.live.courseSelection.policyTitle')}</h3>
          {academicPolicy ? (
            <div className="erp-request-meta">
              <span>{t('universityErp.live.courseSelection.maxCredits')}: <strong>{academicPolicy.maxTermCredits}</strong></span>
              <span>{t('universityErp.live.courseSelection.probationMaxCredits')}: <strong>{academicPolicy.probationMaxTermCredits}</strong></span>
              <span>{t('universityErp.live.courseSelection.goodStandingGrade')}: <strong>{academicPolicy.minAverageGradeGoodStanding}</strong></span>
              <span>{t('universityErp.live.courseSelection.repeatRule')}: <strong>{academicPolicy.allowRepeatCompletedCourses ? t('common.yes') : t('common.no')}</strong></span>
            </div>
          ) : null}
          <form className="erp-stacked-form" onSubmit={submitAcademicPolicy}>
            <Field label={t('universityErp.live.courseSelection.policyName')}>
              <input value={policyForm.policyName} onChange={(event) => setPolicyForm({ ...policyForm, policyName: event.target.value })} required />
            </Field>
            <div className="erp-live-form">
              <Field label={t('universityErp.live.courseSelection.minCredits')}>
                <input type="number" min="0" max="30" value={policyForm.minTermCredits} onChange={(event) => setPolicyForm({ ...policyForm, minTermCredits: event.target.value })} required />
              </Field>
              <Field label={t('universityErp.live.courseSelection.maxCredits')}>
                <input type="number" min="1" max="30" value={policyForm.maxTermCredits} onChange={(event) => setPolicyForm({ ...policyForm, maxTermCredits: event.target.value })} required />
              </Field>
              <Field label={t('universityErp.live.courseSelection.probationMaxCredits')}>
                <input type="number" min="1" max="30" value={policyForm.probationMaxTermCredits} onChange={(event) => setPolicyForm({ ...policyForm, probationMaxTermCredits: event.target.value })} required />
              </Field>
              <Field label={t('universityErp.live.courseSelection.goodStandingGrade')}>
                <input type="number" min="0" step="0.01" value={policyForm.minAverageGradeGoodStanding} onChange={(event) => setPolicyForm({ ...policyForm, minAverageGradeGoodStanding: event.target.value })} required />
              </Field>
            </div>
            <label className="erp-check-option">
              <input type="checkbox" checked={policyForm.blockRegistrationWhenProbation} onChange={(event) => setPolicyForm({ ...policyForm, blockRegistrationWhenProbation: event.target.checked })} />
              <span>{t('universityErp.live.courseSelection.blockProbation')}</span>
            </label>
            <label className="erp-check-option">
              <input type="checkbox" checked={policyForm.allowRepeatCompletedCourses} onChange={(event) => setPolicyForm({ ...policyForm, allowRepeatCompletedCourses: event.target.checked })} />
              <span>{t('universityErp.live.courseSelection.allowRepeats')}</span>
            </label>
            <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.courseSelection.updatePolicy')}</button>
          </form>
        </div>
        <div className="erp-subsection">
          <h3>{t('universityErp.live.courseSelection.degreeAuditTitle')}</h3>
          <form className="erp-stacked-form" onSubmit={submitProgramRequirement}>
            <div className="erp-live-form">
              <Field label={t('universityErp.live.courseSelection.programName')}>
                <input value={requirementForm.programName} onChange={(event) => setRequirementForm({ ...requirementForm, programName: event.target.value })} required />
              </Field>
              <Field label={t('universityErp.live.courseSelection.requirementName')}>
                <input value={requirementForm.requirementName} onChange={(event) => setRequirementForm({ ...requirementForm, requirementName: event.target.value })} required />
              </Field>
              <Field label={t('universityErp.live.courseSelection.requiredCourse')}>
                <select value={requirementForm.subjectId} onChange={(event) => setRequirementForm({ ...requirementForm, subjectId: event.target.value })}>
                  <option value="">{t('universityErp.live.courseSelection.creditBucket')}</option>
                  {courses.map((course) => <option key={course.id} value={course.id}>{course.courseCode || course.id} {course.courseName}</option>)}
                </select>
              </Field>
              <Field label={t('universityErp.live.courseSelection.requiredCredits')}>
                <input type="number" min="1" max="240" value={requirementForm.requiredCredits} onChange={(event) => setRequirementForm({ ...requirementForm, requiredCredits: event.target.value })} required />
              </Field>
            </div>
            <div className="erp-action-row">
              <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.courseSelection.addRequirement')}</button>
              <button type="button" className="btn btn-primary" disabled={saving || !studentId} onClick={refreshDegreeAudit}>{t('universityErp.live.courseSelection.runDegreeAudit')}</button>
            </div>
          </form>
          {degreeAudit ? (
            <div className="erp-request-meta">
              <span>{t('universityErp.live.courseSelection.progress')}: <strong>{degreeAudit.progressPercent}%</strong></span>
              <span>{t('universityErp.live.courseSelection.matchedCredits')}: <strong>{degreeAudit.matchedRequiredCredits}/{degreeAudit.totalRequiredCredits}</strong></span>
              <span>{t('universityErp.live.courseSelection.remainingCredits')}: <strong>{degreeAudit.remainingCredits}</strong></span>
              <span>{t('universityErp.live.courseSelection.graduationEligible')}: <strong>{degreeAudit.graduationEligible ? t('common.yes') : t('common.no')}</strong></span>
            </div>
          ) : null}
          <div className="erp-selection-list">
            {(degreeAudit?.requirements || programRequirements).map((requirement) => (
              <div className="erp-rule-row" key={requirement.requirementId || requirement.id}>
                <strong>{requirement.requirementName}</strong>
                <span>
                  {requirement.courseCode || requirement.subjectId || t('universityErp.live.courseSelection.creditBucket')} |
                  {' '}{requirement.completedCredits ?? 0}/{requirement.requiredCredits} {t('universityErp.live.fields.credits')} |
                  {' '}{requirement.satisfied ? t('universityErp.live.courseSelection.satisfied') : t('universityErp.live.courseSelection.remaining')}
                </span>
              </div>
            ))}
            {!programRequirements.length && !degreeAudit?.requirements?.length ? <p className="muted-text">{t('universityErp.live.courseSelection.noRequirements')}</p> : null}
          </div>
        </div>
      </SectionCard>
    </div>
  );
}

function ReportingDemo({ t }) {
  const [summary, setSummary] = useState(null);
  const [events, setEvents] = useState([]);
  const [reportDefinitions, setReportDefinitions] = useState([]);
  const [reportRuns, setReportRuns] = useState([]);
  const [reportFilters, setReportFilters] = useState({ academicYear: '', semester: '', status: '' });
  const [reportDetails, setReportDetails] = useState([]);
  const [activeReportKey, setActiveReportKey] = useState('');
  const [integrations, setIntegrations] = useState([]);
  const [integrationRuns, setIntegrationRuns] = useState([]);
  const [integrationConnections, setIntegrationConnections] = useState([]);
  const [connectionForm, setConnectionForm] = useState({
    integrationKey: 'lms',
    displayName: t('universityErp.live.reporting.integrationKeys.lms'),
    endpointUrl: 'mock://lms/roster',
    adapterMode: 'MOCK',
    authType: 'NONE',
    secretRef: '',
    enabled: true,
  });
  const [reportResult, setReportResult] = useState('');
  const [integrationResult, setIntegrationResult] = useState('');
  const [smokeTestResults, setSmokeTestResults] = useState([]);
  const [bankCallbackResult, setBankCallbackResult] = useState('');
  const [lmsExportResult, setLmsExportResult] = useState('');
  const [notificationDispatchResult, setNotificationDispatchResult] = useState('');
  const [governmentExportResult, setGovernmentExportResult] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadSummary = async () => {
    setLoading(true);
    setError('');
    try {
      const [summaryResponse, eventsResponse, reportDefinitionResponse, reportRunResponse, integrationResponse, integrationRunResponse, connectionResponse] = await Promise.all([
        getUniversityReportSummary(),
        getUniversityAuditEvents(),
        getUniversityReportDefinitions(),
        getUniversityReportRuns(),
        getUniversityIntegrations(),
        getUniversityIntegrationRuns(),
        getUniversityIntegrationConnections(),
      ]);
      setSummary(summaryResponse.data);
      setEvents(eventsResponse.data || []);
      setReportDefinitions(reportDefinitionResponse.data || []);
      setReportRuns(reportRunResponse.data || []);
      setIntegrations(integrationResponse.data || []);
      setIntegrationRuns(integrationRunResponse.data || []);
      setIntegrationConnections(connectionResponse.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSummary();
  }, []);

  const simulateIntegration = async (key) => {
    setError('');
    setIntegrationResult('');
    try {
      const response = await runUniversityIntegration(key);
      setIntegrationResult(t('universityErp.live.reporting.integrationRan', {
        key: response.data.key,
        status: response.data.status,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const simulateFailure = async (key) => {
    setError('');
    setIntegrationResult('');
    try {
      const response = await failUniversityIntegration(key);
      setIntegrationResult(t('universityErp.live.reporting.integrationFailed', {
        key: response.data.key,
        status: response.data.status,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const retryIntegrationRun = async (id) => {
    setError('');
    setIntegrationResult('');
    try {
      const response = await retryUniversityIntegrationRun(id);
      setIntegrationResult(t('universityErp.live.reporting.integrationRetried', {
        key: response.data.key,
        status: response.data.status,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const saveConnection = async (event) => {
    event.preventDefault();
    setError('');
    try {
      await saveUniversityIntegrationConnection(connectionForm);
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const runIntegrationSmokeTest = async () => {
    setError('');
    setIntegrationResult('');
    try {
      const response = await smokeTestUniversityIntegrations();
      setSmokeTestResults(response.data || []);
      setIntegrationResult(t('universityErp.live.reporting.smokeTestComplete'));
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const simulateBankCallback = async () => {
    setError('');
    setBankCallbackResult('');
    try {
      const response = await simulateUniversityBankPaymentCallback();
      setBankCallbackResult(t('universityErp.live.reporting.bankCallbackRecorded', {
        invoice: response.data.invoiceNumber,
        amount: formatMoney(response.data.paidAmount),
        status: response.data.invoiceStatus,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const exportLmsRoster = async () => {
    setError('');
    setLmsExportResult('');
    try {
      const response = await exportUniversityLmsRoster();
      setLmsExportResult(t('universityErp.live.reporting.lmsRosterExported', {
        rows: response.data.rosterRows,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const dispatchNotifications = async () => {
    setError('');
    setNotificationDispatchResult('');
    try {
      const response = await dispatchUniversityNotifications();
      setNotificationDispatchResult(t('universityErp.live.reporting.notificationsDispatched', {
        count: response.data.notifications,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const exportGovernmentReport = async () => {
    setError('');
    setGovernmentExportResult('');
    try {
      const response = await exportUniversityGovernmentReport();
      setGovernmentExportResult(t('universityErp.live.reporting.governmentReportExported', {
        period: response.data.reportPeriod,
        rows: response.data.reportRows,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const generateReport = async (reportKey) => {
    setError('');
    setReportResult('');
    try {
      const response = await runUniversityReport(reportKey);
      setReportResult(t('universityErp.live.reporting.reportGenerated', {
        name: response.data.reportName,
        status: response.data.status,
      }));
      await loadSummary();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const downloadReportCsv = async (reportKey) => {
    setError('');
    try {
      const params = Object.fromEntries(Object.entries(reportFilters).filter(([, value]) => value !== ''));
      const response = await exportUniversityReportCsv(reportKey, params);
      const blobUrl = URL.createObjectURL(response.data);
      const link = document.createElement('a');
      link.href = blobUrl;
      link.download = `${reportKey}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(blobUrl);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const loadReportDetails = async (reportKey) => {
    setError('');
    try {
      const response = await getUniversityReportDetails(reportKey, {
        academicYear: reportFilters.academicYear || undefined,
        semester: reportFilters.semester || undefined,
        status: reportFilters.status || undefined,
      });
      setActiveReportKey(reportKey);
      setReportDetails(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (error) return <ErrorState title={t('common.error')} description={error} retryLabel={t('common.retry')} onRetry={loadSummary} />;

  const metrics = [
    ['applicants', summary.applicants],
    ['acceptedApplicants', summary.acceptedApplicants],
    ['convertedStudents', summary.convertedStudents],
    ['selectedCourses', summary.selectedCourses],
    ['billedSelections', summary.billedSelections],
    ['serviceRequests', summary.serviceRequests],
    ['openServiceRequests', summary.openServiceRequests],
    ['heldServiceRequests', summary.heldServiceRequests],
    ['prerequisiteRules', summary.prerequisiteRules],
    ['academicRecords', summary.academicRecords],
    ['auditEvents', summary.auditEvents],
    ['financeInvoices', summary.financeInvoices],
    ['billedAmount', formatMoney(summary.billedAmount)],
    ['outstandingBalance', formatMoney(summary.outstandingBalance)],
  ];
  const breakdowns = [
    ['admissionsByStatus', summary.admissionsByStatus],
    ['serviceRequestsByStatus', summary.serviceRequestsByStatus],
    ['financeByStatus', summary.financeByStatus],
    ['serviceQueues', summary.serviceQueues],
    ['academicPolicyMetrics', summary.academicPolicyMetrics],
    ['programRequirementProgress', summary.programRequirementProgress],
  ];

  return (
    <SectionCard title={t('universityErp.live.reporting.title')} subtitle={t('universityErp.live.reporting.subtitle')}>
      <div className="erp-stat-strip">
        {metrics.map(([key, value]) => (
          <div className="erp-metric" key={key}>
            <span>{t(`universityErp.live.reporting.metrics.${key}`)}</span>
            <strong>{value}</strong>
          </div>
        ))}
      </div>
      <div className="erp-subsection">
        <h3>{t('universityErp.live.reporting.biTitle')}</h3>
        <div className="erp-bi-grid">
          {breakdowns.map(([key, rows]) => (
            <div className="erp-bi-panel" key={key}>
              <strong>{t(`universityErp.live.reporting.breakdowns.${key}`)}</strong>
              <div className="erp-selection-list">
                {(rows || []).map((row) => (
                  <div className="erp-bi-row" key={`${key}-${row.label}`}>
                    <span>{row.label}</span>
                    <strong>{row.amount != null ? `${row.count} | ${formatMoney(row.amount)}` : row.count}</strong>
                  </div>
                ))}
                {!(rows || []).length ? <p className="muted-text">{t('common.noData')}</p> : null}
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="erp-subsection">
        <h3>{t('universityErp.live.reporting.reportWorkspace')}</h3>
        {reportResult ? <div className="alert alert-success">{reportResult}</div> : null}
        <div className="erp-form-grid">
          <input
            value={reportFilters.academicYear}
            onChange={(event) => setReportFilters({ ...reportFilters, academicYear: event.target.value })}
            placeholder={t('universityErp.live.reporting.filterAcademicYear')}
          />
          <input
            type="number"
            min="1"
            value={reportFilters.semester}
            onChange={(event) => setReportFilters({ ...reportFilters, semester: event.target.value })}
            placeholder={t('universityErp.live.reporting.filterSemester')}
          />
          <input
            value={reportFilters.status}
            onChange={(event) => setReportFilters({ ...reportFilters, status: event.target.value })}
            placeholder={t('universityErp.live.reporting.filterStatus')}
          />
        </div>
        <div className="erp-governance-grid">
          {reportDefinitions.map((definition) => (
            <div className="erp-governance-item" key={definition.reportKey}>
              <strong>{definition.name}</strong>
              <span>{definition.category}</span>
              <span className="muted-text">{definition.description}</span>
              <span className="muted-text">{t('universityErp.live.reporting.visibleTo')}: {(definition.visibleToRoles || []).join(', ')}</span>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => generateReport(definition.reportKey)}>
                {t('universityErp.live.reporting.generateReport')}
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => loadReportDetails(definition.reportKey)}>
                {t('universityErp.live.reporting.viewDetails')}
              </button>
              <button type="button" className="btn btn-primary btn-sm" onClick={() => downloadReportCsv(definition.reportKey)}>
                {t('universityErp.live.reporting.exportCsv')}
              </button>
            </div>
          ))}
          {!reportDefinitions.length ? <p className="muted-text">{t('common.noData')}</p> : null}
        </div>
        <div className="erp-subsection">
          <h4>{t('universityErp.live.reporting.detailRows')}</h4>
          <div className="erp-selection-list">
            {reportDetails.map((row) => (
              <div className="erp-rule-row" key={`${row.entityType}-${row.entityId}`}>
                <strong>{row.primaryLabel} | {statusLabel(t, row.status)}</strong>
                <span>{row.secondaryLabel || row.entityType} | {row.amount != null ? formatMoney(row.amount) : row.entityId}</span>
                <span className="muted-text">{row.details || activeReportKey}</span>
              </div>
            ))}
            {!reportDetails.length ? <p className="muted-text">{t('universityErp.live.reporting.noDetailRows')}</p> : null}
          </div>
        </div>
        <div className="erp-subsection">
          <h4>{t('universityErp.live.reporting.reportHistory')}</h4>
          <div className="erp-selection-list">
            {reportRuns.map((run) => (
              <div className="erp-rule-row" key={run.id}>
                <strong>{run.reportName} | {statusLabel(t, run.status)}</strong>
                <span>{run.generatedAt || '-'} | {t('universityErp.live.reporting.rowCount')}: {run.rowCount}</span>
                <span className="muted-text">{run.snapshotPayload}</span>
              </div>
            ))}
            {!reportRuns.length ? <p className="muted-text">{t('universityErp.live.reporting.noReportRuns')}</p> : null}
          </div>
        </div>
      </div>
      <div className="erp-subsection">
        <h3>{t('universityErp.live.reporting.auditTitle')}</h3>
        <div className="erp-selection-list">
          {events.map((event) => (
            <div className="erp-rule-row" key={event.id}>
              <strong>{event.module} | {event.action}</strong>
              <span>{event.studentName || event.actorName || '-'} | {event.details || event.entityType}</span>
            </div>
          ))}
          {!events.length ? <p className="muted-text">{t('universityErp.live.reporting.noAuditEvents')}</p> : null}
        </div>
      </div>
      <div className="erp-subsection">
        <h3>{t('universityErp.live.reporting.integrationTitle')}</h3>
        {integrationResult ? <div className="alert alert-success">{integrationResult}</div> : null}
        {lmsExportResult ? <div className="alert alert-success">{lmsExportResult}</div> : null}
        {notificationDispatchResult ? <div className="alert alert-success">{notificationDispatchResult}</div> : null}
        {governmentExportResult ? <div className="alert alert-success">{governmentExportResult}</div> : null}
        {bankCallbackResult ? <div className="alert alert-success">{bankCallbackResult}</div> : null}
        <form className="erp-live-form" onSubmit={saveConnection}>
          <Field label={t('universityErp.live.reporting.connectionKey')}>
            <select value={connectionForm.integrationKey} onChange={(event) => {
              const connection = integrationConnections.find((item) => item.integrationKey === event.target.value);
              setConnectionForm(connection ? {
                integrationKey: connection.integrationKey,
                displayName: connection.displayName,
                endpointUrl: connection.endpointUrl || '',
                adapterMode: connection.adapterMode || 'MOCK',
                authType: connection.authType || 'NONE',
                secretRef: connection.secretRef || '',
                enabled: connection.enabled,
              } : { ...connectionForm, integrationKey: event.target.value, displayName: integrationKeyLabel(t, event.target.value) });
            }}>
              {['lms', 'bank', 'notification', 'government'].map((key) => <option key={key} value={key}>{integrationKeyLabel(t, key)}</option>)}
            </select>
          </Field>
          <Field label={t('universityErp.live.reporting.connectionName')}>
            <input value={connectionForm.displayName} onChange={(event) => setConnectionForm({ ...connectionForm, displayName: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.reporting.connectionEndpoint')}>
            <input value={connectionForm.endpointUrl} onChange={(event) => setConnectionForm({ ...connectionForm, endpointUrl: event.target.value })} />
          </Field>
          <Field label={t('universityErp.live.reporting.adapterMode')}>
            <select value={connectionForm.adapterMode} onChange={(event) => setConnectionForm({ ...connectionForm, adapterMode: event.target.value })}>
              {['MOCK', 'HTTP'].map((mode) => <option key={mode} value={mode}>{adapterModeLabel(t, mode)}</option>)}
            </select>
          </Field>
          <Field label={t('universityErp.live.reporting.authType')}>
            <select value={connectionForm.authType} onChange={(event) => setConnectionForm({ ...connectionForm, authType: event.target.value })}>
              {['NONE', 'API_KEY', 'BEARER_TOKEN', 'BASIC'].map((type) => <option key={type} value={type}>{authTypeLabel(t, type)}</option>)}
            </select>
          </Field>
          <Field label={t('universityErp.live.reporting.secretRef')}>
            <input value={connectionForm.secretRef} onChange={(event) => setConnectionForm({ ...connectionForm, secretRef: event.target.value })} />
          </Field>
          <Field label={t('universityErp.live.reporting.connectionEnabled')}>
            <select value={String(connectionForm.enabled)} onChange={(event) => setConnectionForm({ ...connectionForm, enabled: event.target.value === 'true' })}>
              <option value="true">{t('common.yes')}</option>
              <option value="false">{t('common.no')}</option>
            </select>
          </Field>
          <button type="submit" className="btn btn-secondary btn-sm">{t('universityErp.live.reporting.saveConnection')}</button>
        </form>
        <div className="erp-selection-list">
          {integrationConnections.map((connection) => (
            <div className="erp-rule-row" key={connection.integrationKey}>
              <strong>{connection.displayName} | {statusLabel(t, connection.lastStatus)}</strong>
              <span>{connection.endpointUrl || '-'} | {adapterModeLabel(t, connection.adapterMode || 'MOCK')} | {authTypeLabel(t, connection.authType || 'NONE')} | {connection.secretRef || '-'}</span>
            </div>
          ))}
        </div>
        <button type="button" className="btn btn-secondary btn-sm" onClick={runIntegrationSmokeTest}>
          {t('universityErp.live.reporting.runSmokeTest')}
        </button>
        {smokeTestResults.length ? (
          <div className="erp-selection-list">
            {smokeTestResults.map((result) => (
              <div className="erp-rule-row" key={result.key}>
                <strong>{integrationKeyLabel(t, result.key)} | {statusLabel(t, result.status)}</strong>
                <span>{adapterModeLabel(t, result.adapterMode)} | {authTypeLabel(t, result.authType)} | {result.secretResolved ? t('common.yes') : t('common.no')}</span>
                <span className="muted-text">{result.message}</span>
              </div>
            ))}
          </div>
        ) : null}
        <div className="erp-governance-grid">
          {integrations.map((integration) => (
            <div className="erp-governance-item" key={integration.key}>
              <strong>{integration.name}</strong>
              <span>{integration.direction}</span>
              <span>{statusLabel(t, integration.status)} | {integration.lastExchange}</span>
              <span className="muted-text">{integration.payload}</span>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => simulateIntegration(integration.key)}>
                {t('universityErp.live.reporting.runIntegration')}
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => simulateFailure(integration.key)}>
                {t('universityErp.live.reporting.simulateFailure')}
              </button>
              {integration.key === 'bank' ? (
                <button type="button" className="btn btn-primary btn-sm" onClick={simulateBankCallback}>
                  {t('universityErp.live.reporting.simulateBankCallback')}
                </button>
              ) : null}
              {integration.key === 'lms' ? (
                <button type="button" className="btn btn-primary btn-sm" onClick={exportLmsRoster}>
                  {t('universityErp.live.reporting.exportLmsRoster')}
                </button>
              ) : null}
              {integration.key === 'notification' ? (
                <button type="button" className="btn btn-primary btn-sm" onClick={dispatchNotifications}>
                  {t('universityErp.live.reporting.dispatchNotifications')}
                </button>
              ) : null}
              {integration.key === 'government' ? (
                <button type="button" className="btn btn-primary btn-sm" onClick={exportGovernmentReport}>
                  {t('universityErp.live.reporting.exportGovernmentReport')}
                </button>
              ) : null}
            </div>
          ))}
        </div>
        <div className="erp-subsection">
          <h4>{t('universityErp.live.reporting.integrationHistory')}</h4>
          <div className="erp-selection-list">
            {integrationRuns.map((run) => (
              <div className="erp-rule-row" key={run.id}>
                <strong>{run.name} | {statusLabel(t, run.status)}</strong>
                <span>{run.exchangedAt || '-'} | {run.resultMessage || '-'}</span>
                {run.errorMessage ? <span className="muted-text">{run.errorMessage}</span> : null}
                {run.status === 'FAILED' ? (
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => retryIntegrationRun(run.id)}>
                    {t('universityErp.live.reporting.retryRun')}
                  </button>
                ) : null}
                <span className="muted-text">{run.payload}</span>
              </div>
            ))}
            {!integrationRuns.length ? <p className="muted-text">{t('universityErp.live.reporting.noIntegrationRuns')}</p> : null}
          </div>
        </div>
      </div>
    </SectionCard>
  );
}

function HrFacultyDemo({ t }) {
  const [facultyUsers, setFacultyUsers] = useState([]);
  const [profiles, setProfiles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [workloads, setWorkloads] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [form, setForm] = useState(initialFacultyProfile);
  const [departmentForm, setDepartmentForm] = useState(initialDepartmentForm);
  const [workloadForm, setWorkloadForm] = useState(initialWorkloadForm);
  const [leaveForm, setLeaveForm] = useState(initialLeaveForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [facultyResponse, profileResponse, departmentResponse, workloadResponse, leaveResponse] = await Promise.all([
        getAllUsers({ role: 2, page: 1, pageSize: 100 }),
        getUniversityFacultyProfiles(),
        getUniversityDepartments(),
        getUniversityFacultyWorkloads(),
        getUniversityFacultyLeaveRequests(),
      ]);
      const facultyData = Array.isArray(facultyResponse.data?.items) ? facultyResponse.data.items : facultyResponse.data || [];
      const profileData = profileResponse.data || [];
      setFacultyUsers(facultyData);
      setProfiles(profileData);
      setDepartments(departmentResponse.data || []);
      setWorkloads(workloadResponse.data || []);
      setLeaveRequests(leaveResponse.data || []);
      setForm((current) => ({
        ...current,
        facultyUserId: current.facultyUserId || (facultyData[0]?.id ? String(facultyData[0].id) : ''),
      }));
      setWorkloadForm((current) => ({
        ...current,
        facultyProfileId: current.facultyProfileId || (profileData[0]?.id ? String(profileData[0].id) : ''),
      }));
      setLeaveForm((current) => ({
        ...current,
        facultyProfileId: current.facultyProfileId || (profileData[0]?.id ? String(profileData[0].id) : ''),
      }));
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const editProfile = (profile) => {
    setForm({
      facultyUserId: String(profile.facultyUserId),
      employeeNumber: profile.employeeNumber || '',
      department: profile.department || '',
      academicRank: profile.academicRank || '',
      employmentStatus: profile.employmentStatus || 'ACTIVE',
      hireDate: profile.hireDate || '',
      officeLocation: profile.officeLocation || '',
      workloadTargetCredits: profile.workloadTargetCredits ?? 12,
    });
  };

  const submitProfile = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await upsertUniversityFacultyProfile({
        facultyUserId: Number(form.facultyUserId),
        employeeNumber: form.employeeNumber,
        department: form.department,
        academicRank: form.academicRank,
        employmentStatus: form.employmentStatus,
        hireDate: form.hireDate || null,
        officeLocation: form.officeLocation,
        workloadTargetCredits: Number(form.workloadTargetCredits),
      });
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitDepartment = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityDepartment(departmentForm);
      setDepartmentForm(initialDepartmentForm);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitWorkload = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityFacultyWorkload({
        ...workloadForm,
        facultyProfileId: Number(workloadForm.facultyProfileId),
        semester: Number(workloadForm.semester),
        teachingCredits: Number(workloadForm.teachingCredits),
        advisingCredits: Number(workloadForm.advisingCredits),
        researchCredits: Number(workloadForm.researchCredits),
        committeeCredits: Number(workloadForm.committeeCredits),
      });
      setWorkloadForm({ ...initialWorkloadForm, facultyProfileId: workloadForm.facultyProfileId });
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const submitLeaveRequest = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createUniversityFacultyLeaveRequest({
        ...leaveForm,
        facultyProfileId: Number(leaveForm.facultyProfileId),
      });
      setLeaveForm({ ...initialLeaveForm, facultyProfileId: leaveForm.facultyProfileId });
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const decideLeaveRequest = async (id, status) => {
    setSaving(true);
    setError('');
    try {
      await decideUniversityFacultyLeaveRequest(id, {
        status,
        decisionNotes: t(`universityErp.live.hrFaculty.leaveDecision.${status}`),
      });
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (error && !profiles.length) return <ErrorState title={t('common.error')} description={error} retryLabel={t('common.retry')} onRetry={loadData} />;

  return (
    <SectionCard title={t('universityErp.live.hrFaculty.title')} subtitle={t('universityErp.live.hrFaculty.subtitle')}>
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="erp-subsection">
        <h3>{t('universityErp.live.hrFaculty.departments')}</h3>
        <form className="erp-live-form" onSubmit={submitDepartment}>
          <Field label={t('universityErp.live.hrFaculty.departmentCode')}>
            <input value={departmentForm.code} onChange={(event) => setDepartmentForm({ ...departmentForm, code: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.hrFaculty.departmentName')}>
            <input value={departmentForm.name} onChange={(event) => setDepartmentForm({ ...departmentForm, name: event.target.value })} required />
          </Field>
          <button type="submit" className="btn btn-secondary" disabled={saving}>{t('universityErp.live.hrFaculty.saveDepartment')}</button>
        </form>
        <div className="erp-selection-list">
          {departments.map((department) => (
            <div className="erp-rule-row" key={department.id}>
              <strong>{department.code} | {department.name}</strong>
              <span>{department.active ? t('common.active') : t('common.inactive')}</span>
            </div>
          ))}
        </div>
      </div>
      <form className="erp-live-form" onSubmit={submitProfile}>
        <Field label={t('universityErp.live.hrFaculty.faculty')}>
          <select value={form.facultyUserId} onChange={(event) => setForm({ ...form, facultyUserId: event.target.value })} required>
            {facultyUsers.map((faculty) => <option key={faculty.id} value={faculty.id}>{fullName(faculty)}</option>)}
          </select>
        </Field>
        <Field label={t('universityErp.live.hrFaculty.employeeNumber')}>
          <input value={form.employeeNumber} onChange={(event) => setForm({ ...form, employeeNumber: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.hrFaculty.department')}>
          <select value={form.department} onChange={(event) => setForm({ ...form, department: event.target.value })} required>
            {[...new Set([form.department, ...departments.map((department) => department.name)].filter(Boolean))].map((department) => (
              <option key={department} value={department}>{department}</option>
            ))}
          </select>
        </Field>
        <Field label={t('universityErp.live.hrFaculty.academicRank')}>
          <input value={form.academicRank} onChange={(event) => setForm({ ...form, academicRank: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.hrFaculty.employmentStatus')}>
          <select value={form.employmentStatus} onChange={(event) => setForm({ ...form, employmentStatus: event.target.value })}>
            {['ACTIVE', 'ON_LEAVE', 'ADJUNCT', 'INACTIVE'].map((status) => <option key={status} value={status}>{statusLabel(t, status)}</option>)}
          </select>
        </Field>
        <Field label={t('universityErp.live.hrFaculty.hireDate')}>
          <input type="date" value={form.hireDate} onChange={(event) => setForm({ ...form, hireDate: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.hrFaculty.officeLocation')}>
          <input value={form.officeLocation} onChange={(event) => setForm({ ...form, officeLocation: event.target.value })} />
        </Field>
        <Field label={t('universityErp.live.hrFaculty.workloadTarget')}>
          <input type="number" min="0" max="40" value={form.workloadTargetCredits} onChange={(event) => setForm({ ...form, workloadTargetCredits: event.target.value })} required />
        </Field>
        <button type="submit" className="btn btn-primary" disabled={saving || !form.facultyUserId}>{t('universityErp.live.hrFaculty.saveProfile')}</button>
      </form>

      <div className="desktop-table table-container erp-live-table">
        <table>
          <thead>
            <tr>
              <th>{t('universityErp.live.hrFaculty.faculty')}</th>
              <th>{t('universityErp.live.hrFaculty.department')}</th>
              <th>{t('universityErp.live.hrFaculty.academicRank')}</th>
              <th>{t('common.status')}</th>
              <th>{t('universityErp.live.hrFaculty.workload')}</th>
              <th>{t('common.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {profiles.map((profile) => (
              <tr key={profile.id}>
                <td>{profile.facultyName}<br /><small className="muted-text">{profile.email}</small></td>
                <td>{profile.department}</td>
                <td>{profile.academicRank || '-'}</td>
                <td><span className="badge badge-info">{statusLabel(t, profile.employmentStatus)}</span></td>
                <td>
                  {profile.assignedCredits}/{profile.workloadTargetCredits} {t('universityErp.live.fields.credits')}
                  <br /><small className="muted-text">{profile.activeTeachingAssignments} {t('universityErp.live.hrFaculty.assignments')}</small>
                </td>
                <td><button type="button" className="btn btn-secondary btn-sm" onClick={() => editProfile(profile)}>{t('common.update')}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!profiles.length ? <p className="muted-text">{t('universityErp.live.hrFaculty.noProfiles')}</p> : null}
      <div className="erp-subsection">
        <h3>{t('universityErp.live.hrFaculty.termWorkload')}</h3>
        <form className="erp-live-form" onSubmit={submitWorkload}>
          <Field label={t('universityErp.live.hrFaculty.faculty')}>
            <select value={workloadForm.facultyProfileId} onChange={(event) => setWorkloadForm({ ...workloadForm, facultyProfileId: event.target.value })} required>
              {profiles.map((profile) => <option key={profile.id} value={profile.id}>{profile.facultyName}</option>)}
            </select>
          </Field>
          <Field label={t('universityErp.live.fields.academicYear')}>
            <input value={workloadForm.academicYear} onChange={(event) => setWorkloadForm({ ...workloadForm, academicYear: event.target.value })} required />
          </Field>
          <Field label={t('universityErp.live.fields.semester')}>
            <input type="number" min="1" value={workloadForm.semester} onChange={(event) => setWorkloadForm({ ...workloadForm, semester: event.target.value })} required />
          </Field>
          {['teachingCredits', 'advisingCredits', 'researchCredits', 'committeeCredits'].map((key) => (
            <Field key={key} label={t(`universityErp.live.hrFaculty.${key}`)}>
              <input type="number" min="0" value={workloadForm[key]} onChange={(event) => setWorkloadForm({ ...workloadForm, [key]: event.target.value })} />
            </Field>
          ))}
          <Field label={t('universityErp.live.hrFaculty.notes')}>
            <input value={workloadForm.notes} onChange={(event) => setWorkloadForm({ ...workloadForm, notes: event.target.value })} />
          </Field>
          <button type="submit" className="btn btn-primary" disabled={saving || !workloadForm.facultyProfileId}>{t('universityErp.live.hrFaculty.saveWorkload')}</button>
        </form>
        <div className="erp-selection-list">
          {workloads.map((workload) => (
            <div className="erp-rule-row" key={workload.id}>
              <strong>{workload.facultyName} | {workload.academicYear} {t('universityErp.live.fields.semesterShort', { semester: workload.semester })}</strong>
              <span>{t('universityErp.live.hrFaculty.totalWorkload')}: {workload.totalCredits}</span>
              <span className="muted-text">{workload.notes || '-'}</span>
            </div>
          ))}
          {!workloads.length ? <p className="muted-text">{t('universityErp.live.hrFaculty.noWorkloads')}</p> : null}
        </div>
      </div>
    </SectionCard>
  );
}
