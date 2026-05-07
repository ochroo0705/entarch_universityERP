import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  approveAiMessageDraft,
  createAiMessageDraft,
  getAiAccessibleStudents,
  getAiMessageDraftAuditLogs,
  getAiMessageDraftById,
  getAiMessageDrafts,
  getAiStudentRiskSnapshots,
  rejectAiMessageDraft,
  retryAiMessageDraftGeneration,
  updateAiMessageDraft,
} from '../../api/endpoints';
import SectionCard from '../ui/SectionCard';
import SearchableSelect from '../ui/SearchableSelect';
import SelectMenu from '../ui/SelectMenu';
import { EmptyState, ErrorState } from '../ui/StateBlock';
import AiStatusBadge from './AiStatusBadge';
import { formatDate, formatDateTime, getAiValueLabel } from './aiI18n';

const ISSUE_TYPES = [
  'ATTENDANCE',
  'MISSING_WORK',
  'GRADE_DECLINE',
  'MIXED_CONCERN',
  'POSITIVE_UPDATE',
  'GENERAL_FOLLOW_UP',
];

const CHANNELS = ['EMAIL', 'SMS', 'PORTAL'];
const TONES = ['supportive', 'direct', 'encouraging', 'neutral'];

function parseJson(value) {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function DraftAuditTimeline({ logs }) {
  const { t, i18n } = useTranslation();

  if (!logs.length) {
    return <div className="muted-copy">{t('ai.drafts.noDraftAudit')}</div>;
  }

  return (
    <div className="draft-audit-timeline">
      {logs.map((log) => (
        <article key={log.id} className="draft-audit-item">
          <div className="draft-audit-head">
            <strong>{log.eventType}</strong>
            <AiStatusBadge value={log.actionStatus} />
          </div>
          <div className="draft-audit-meta">
            <span>{log.actorUserName}</span>
            <span>{formatDateTime(log.createdAt, i18n.language, t('ai.shared.notAvailable'))}</span>
          </div>
          {log.reasonCode ? <p>{log.reasonCode}</p> : null}
          {log.providerName ? (
            <div className="muted-copy">
              {log.providerName}
              {log.providerModel ? ` • ${log.providerModel}` : ''}
            </div>
          ) : null}
        </article>
      ))}
    </div>
  );
}

function DraftContextSummaryCard({ draft }) {
  const { t } = useTranslation();
  const context = parseJson(draft?.generationInputRedactedJson);

  if (!context) {
    return <div className="muted-copy">{t('ai.drafts.contextUnavailable')}</div>;
  }

  return (
    <div className="draft-context-grid">
      {Object.entries(context).map(([key, value]) => (
        <div key={key} className="draft-context-item">
          <span>{key.replaceAll('_', ' ')}</span>
          <strong>{Array.isArray(value) ? value.join(', ') || t('ai.shared.none') : String(value || t('ai.shared.notAvailable'))}</strong>
        </div>
      ))}
    </div>
  );
}

function DraftGeneratedVsEditedPanel({ draft, onChange, canEdit }) {
  const { t } = useTranslation();

  return (
    <div className="draft-review-grid">
      <div className="draft-review-pane">
        <div className="draft-review-label">{t('ai.drafts.originalGeneratedDraft')}</div>
        <label className="draft-field-label">
          {t('ai.drafts.subject')}
          <input className="input" value={draft.generatedSubject || ''} readOnly />
        </label>
        <label className="draft-field-label">
          {t('ai.drafts.message')}
          <textarea className="textarea" rows={10} value={draft.generatedMessageBody || ''} readOnly />
        </label>
      </div>
      <div className="draft-review-pane">
        <div className="draft-review-label">{t('ai.drafts.teacherReviewCopy')}</div>
        <label className="draft-field-label">
          {t('ai.drafts.subject')}
          <input
            className="input"
            value={draft.currentSubject || ''}
            readOnly={!canEdit}
            onChange={(event) => onChange('currentSubject', event.target.value)}
          />
        </label>
        <label className="draft-field-label">
          {t('ai.drafts.message')}
          <textarea
            className="textarea"
            rows={10}
            value={draft.currentMessageBody || ''}
            readOnly={!canEdit}
            onChange={(event) => onChange('currentMessageBody', event.target.value)}
          />
        </label>
      </div>
    </div>
  );
}

function DraftTableSkeletonRows({ count = 6 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`draft-skeleton-row-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-email" /></td>
      <td><div className="users-skeleton users-skeleton-button" /></td>
    </tr>
  ));
}

function DraftMobileSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`draft-skeleton-card-${index}`} className="teacher-mobile-card users-skeleton-card" aria-hidden="true">
      <div className="teacher-mobile-card-head">
        <div>
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-card-subtitle" />
        </div>
        <div className="users-skeleton users-skeleton-pill" />
      </div>

      <div className="draft-badge-row">
        <div className="users-skeleton users-skeleton-pill" />
        <div className="users-skeleton users-skeleton-pill" />
      </div>

      <div className="teacher-mobile-card-grid">
        <div className="teacher-mobile-card-field">
          <span className="users-skeleton users-skeleton-meta-label" />
          <strong className="users-skeleton users-skeleton-meta-value" />
        </div>
        <div className="teacher-mobile-card-field">
          <span className="users-skeleton users-skeleton-meta-label" />
          <strong className="users-skeleton users-skeleton-meta-value" />
        </div>
      </div>

      <div className="users-skeleton users-skeleton-card-button" />
    </article>
  ));
}

function DraftReviewSkeleton() {
  return (
    <div className="draft-detail-stack" aria-hidden="true">
      <div className="draft-badge-row">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={`draft-review-pill-${index}`} className="users-skeleton users-skeleton-pill" />
        ))}
      </div>

      <div className="draft-meta-grid">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={`draft-meta-skeleton-${index}`}>
            <span className="users-skeleton users-skeleton-meta-label" />
            <strong className="users-skeleton users-skeleton-meta-value" />
          </div>
        ))}
      </div>

      <div className="draft-review-grid">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={`draft-pane-skeleton-${index}`} className="draft-review-pane">
            <div className="users-skeleton users-skeleton-card-subtitle" />
            <div className="users-skeleton users-skeleton-email" />
            <div className="users-skeleton users-skeleton-email" />
            <div className="users-skeleton users-skeleton-email" style={{ width: '100%', height: '180px', borderRadius: '14px' }} />
          </div>
        ))}
      </div>
    </div>
  );
}

function DraftManagementPageSkeleton({ audience }) {
  const showRequestForm = audience === 'teacher' || audience === 'admin';

  return (
    <div className="draft-workspace" aria-hidden="true">
      <div className="page-header">
        <div className="parent-page-skeleton-stack">
          <div className="users-skeleton users-skeleton-card-subtitle" />
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-email" />
        </div>
      </div>

      <div className="panel-grid-two">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={`draft-top-skeleton-${index}`} className="card">
            <div className="card-body">
              <div className="parent-page-skeleton-stack">
                <div className="users-skeleton users-skeleton-card-subtitle" />
                <div className="users-skeleton users-skeleton-card-title" />
              </div>
              <div className="draft-form-grid" style={{ marginTop: '1rem' }}>
                {index === 0 ? Array.from({ length: showRequestForm ? 9 : 4 }, (_, itemIndex) => (
                  <div key={`draft-field-skeleton-${itemIndex}`} className="draft-field-label">
                    <span className="users-skeleton users-skeleton-meta-label" />
                    <div className="users-skeleton users-skeleton-email" style={{ width: '100%', height: '42px', borderRadius: '12px' }} />
                  </div>
                )) : <DraftReviewSkeleton />}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-body">
          <div className="parent-page-skeleton-stack">
            <div className="users-skeleton users-skeleton-card-subtitle" />
            <div className="users-skeleton users-skeleton-card-title" />
          </div>
          <div className="table-container desktop-table" style={{ marginTop: '1rem' }}>
            <table>
              <tbody>
                <DraftTableSkeletonRows />
              </tbody>
            </table>
          </div>
          <div className="teacher-mobile-card-list" style={{ marginTop: '1rem' }}>
            <DraftMobileSkeletonCards />
          </div>
        </div>
      </div>
    </div>
  );
}

export default function DraftManagementPage({ audience = 'admin' }) {
  const { t, i18n } = useTranslation();
  const hasLoadedRef = useRef(false);
  const [students, setStudents] = useState([]);
  const [drafts, setDrafts] = useState([]);
  const [riskSnapshots, setRiskSnapshots] = useState([]);
  const [selectedDraft, setSelectedDraft] = useState(null);
  const [draftAuditLogs, setDraftAuditLogs] = useState([]);
  const [showDraftAudit, setShowDraftAudit] = useState(false);
  const [auditLoading, setAuditLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [filters, setFilters] = useState({ status: '', provider: '' });
  const [form, setForm] = useState({
    studentId: '',
    parentUserId: '',
    riskSnapshotId: '',
    issueType: 'GENERAL_FOLLOW_UP',
    teacherNote: '',
    toneLabel: 'supportive',
    languageCode: 'mn',
    goalLabel: '',
    channel: 'EMAIL',
  });
  const [loading, setLoading] = useState(true);
  const [refreshingDrafts, setRefreshingDrafts] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const canRequestDrafts = audience === 'teacher' || audience === 'admin';
  const canApproveDrafts = audience === 'teacher';
  const selectedStudent = useMemo(
    () => students.find((student) => String(student.studentId) === String(form.studentId)),
    [students, form.studentId]
  );

  const loadDraftAudit = useCallback(async (draftId) => {
    setAuditLoading(true);
    try {
      const response = await getAiMessageDraftAuditLogs(draftId, { page: 1, pageSize: 10 });
      setDraftAuditLogs(response.data?.items || []);
    } catch (err) {
      console.error('Failed to load draft audit log', err);
      setDraftAuditLogs([]);
    } finally {
      setAuditLoading(false);
    }
  }, []);

  const loadDraftDetail = useCallback(async (draftId) => {
    setDetailLoading(true);
    try {
      const response = await getAiMessageDraftById(draftId);
      setSelectedDraft(response.data);
      setShowDraftAudit(false);
      setDraftAuditLogs([]);
    } catch (err) {
      console.error('Failed to load draft detail', err);
      setError('draft-detail');
    } finally {
      setDetailLoading(false);
    }
  }, []);

  const loadData = useCallback(async ({ showGlobalLoading = true } = {}) => {
    if (showGlobalLoading) {
      setLoading(true);
    } else {
      setRefreshingDrafts(true);
    }
    setError('');
    try {
      const [studentsRes, draftsRes] = await Promise.all([
        getAiAccessibleStudents({ forceRefresh: true }),
        getAiMessageDrafts({
          ...(filters.status ? { status: filters.status } : {}),
          ...(filters.provider ? { provider: filters.provider } : {}),
        }),
      ]);
      setStudents(studentsRes.data || []);
      setDrafts(draftsRes.data || []);
    } catch (err) {
      console.error('Failed to load AI drafts', err);
      setError('drafts');
    } finally {
      if (showGlobalLoading) {
        setLoading(false);
      } else {
        setRefreshingDrafts(false);
      }
    }
  }, [filters.provider, filters.status]);

  useEffect(() => {
    const showGlobalLoading = !hasLoadedRef.current;
    loadData({ showGlobalLoading }).finally(() => {
      hasLoadedRef.current = true;
    });
  }, [loadData]);

  useEffect(() => {
    if (!selectedStudent) {
      setRiskSnapshots([]);
      return;
    }

    let ignore = false;
    getAiStudentRiskSnapshots(selectedStudent.studentId)
      .then((response) => {
        if (!ignore) {
          setRiskSnapshots(response.data || []);
        }
      })
      .catch((err) => {
        console.error('Failed to load student risk snapshots', err);
        if (!ignore) {
          setRiskSnapshots([]);
        }
      });

    return () => {
      ignore = true;
    };
  }, [selectedStudent]);

  useEffect(() => {
    if (!selectedStudent) return;
    const nextParentId = selectedStudent.parents?.[0]?.parentUserId ?? '';
    setForm((current) => ({
      ...current,
      parentUserId: nextParentId,
      riskSnapshotId: current.riskSnapshotId || '',
    }));
  }, [selectedStudent]);

  const providerValues = useMemo(
    () => Array.from(new Set(drafts.map((draft) => draft.generationProvider).filter(Boolean))),
    [drafts]
  );

  const studentOptions = useMemo(
    () => students.map((student) => ({ value: String(student.studentId), label: student.studentName })),
    [students]
  );

  const parentOptions = useMemo(
    () => (selectedStudent?.parents || []).map((parent) => ({ value: String(parent.parentUserId), label: parent.parentName })),
    [selectedStudent]
  );

  const riskSnapshotOptions = useMemo(
    () => [
      { value: '', label: t('ai.drafts.useLatestContext') },
      ...riskSnapshots.map((snapshot) => ({
        value: String(snapshot.id),
        label: `${getAiValueLabel(t, snapshot.riskLevel)} • ${formatDate(snapshot.calculatedAt, i18n.language, t('ai.shared.noDate'))}`,
      })),
    ],
    [i18n.language, riskSnapshots, t]
  );

  const issueTypeOptions = useMemo(
    () => ISSUE_TYPES.map((type) => ({ value: type, label: getAiValueLabel(t, type) })),
    [t]
  );

  const toneOptions = useMemo(
    () => TONES.map((tone) => ({ value: tone, label: t(`ai.values.tone.${tone}`) })),
    [t]
  );

  const channelOptions = useMemo(
    () => CHANNELS.map((channel) => ({ value: channel, label: getAiValueLabel(t, channel) })),
    [t]
  );

  const statusFilterOptions = useMemo(
    () => [{ value: '', label: t('ai.drafts.allStatuses') }, ...['REQUESTED', 'GENERATING', 'READY_FOR_REVIEW', 'GENERATION_FAILED', 'APPROVED', 'REJECTED'].map((status) => ({ value: status, label: getAiValueLabel(t, status) }))],
    [t]
  );

  const providerFilterOptions = useMemo(
    () => [{ value: '', label: t('ai.drafts.allProviders') }, ...providerValues.map((provider) => ({ value: provider, label: provider }))],
    [providerValues, t]
  );

  const handleCreate = async () => {
    if (!form.studentId || !form.parentUserId) return;
    setSaving(true);
    try {
      const response = await createAiMessageDraft({
        ...form,
        studentId: Number(form.studentId),
        parentUserId: Number(form.parentUserId),
        riskSnapshotId: form.riskSnapshotId ? Number(form.riskSnapshotId) : null,
      });
      setSelectedDraft(response.data);
      setShowDraftAudit(false);
      setDraftAuditLogs([]);
      setForm((current) => ({ ...current, teacherNote: '', goalLabel: '' }));
      await loadData();
    } catch (err) {
      console.error('Failed to create AI draft', err);
      setError('drafts');
    } finally {
      setSaving(false);
    }
  };

  const handleUpdate = async () => {
    if (!selectedDraft) return;
    setSaving(true);
    try {
      const response = await updateAiMessageDraft(selectedDraft.id, {
        currentSubject: selectedDraft.currentSubject,
        currentMessageBody: selectedDraft.currentMessageBody,
      });
      setSelectedDraft(response.data);
      await loadData();
      if (showDraftAudit) {
        await loadDraftAudit(selectedDraft.id);
      }
    } catch (err) {
      console.error('Failed to update AI draft', err);
      setError('drafts');
    } finally {
      setSaving(false);
    }
  };

  const handleDecision = async (kind) => {
    if (!selectedDraft) return;
    const note = window.prompt(kind === 'approve' ? t('ai.drafts.approvalPrompt') : t('ai.drafts.rejectionPrompt')) ?? '';
    setSaving(true);
    try {
      const action = kind === 'approve' ? approveAiMessageDraft : rejectAiMessageDraft;
      const response = await action(selectedDraft.id, { note });
      setSelectedDraft(response.data);
      await loadData();
      if (showDraftAudit) {
        await loadDraftAudit(selectedDraft.id);
      }
    } catch (err) {
      console.error(`Failed to ${kind} AI draft`, err);
      setError('drafts');
    } finally {
      setSaving(false);
    }
  };

  const handleRetry = async () => {
    if (!selectedDraft) return;
    setSaving(true);
    try {
      const response = await retryAiMessageDraftGeneration(selectedDraft.id);
      setSelectedDraft(response.data);
      await loadData();
      if (showDraftAudit) {
        await loadDraftAudit(selectedDraft.id);
      }
    } catch (err) {
      console.error('Failed to retry draft generation', err);
      setError('drafts');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleDraftAudit = async () => {
    if (!selectedDraft) return;
    if (showDraftAudit) {
      setShowDraftAudit(false);
      return;
    }
    setShowDraftAudit(true);
    await loadDraftAudit(selectedDraft.id);
  };

  if (loading) return <DraftManagementPageSkeleton audience={audience} />;

  if (error) {
    return (
      <ErrorState
        title={t('ai.drafts.loadErrorTitle')}
        description={t('ai.drafts.loadErrorDescription')}
        retryLabel={t('admin.users.retry')}
        onRetry={loadData}
      />
    );
  }

  return (
    <div className="draft-workspace">
      <div className="page-header">
        <div>
          <div className="page-kicker">{audience === 'admin' ? t('ai.drafts.adminKicker') : t('ai.drafts.teacherKicker')}</div>
          <h1>{t('ai.drafts.pageTitle')}</h1>
          <p className="page-summary">{t('ai.drafts.pageSummary')}</p>
        </div>
      </div>

      <div className="panel-grid-two">
        <SectionCard
          title={canRequestDrafts ? t('ai.drafts.requestTitle') : t('ai.drafts.oversightTitle')}
          subtitle={canRequestDrafts ? t('ai.drafts.requestDescription') : t('ai.drafts.oversightDescription')}
        >
          <div className="draft-form-grid">
            <label className="draft-field-label">
              {t('ai.drafts.student')}
              <SearchableSelect
                options={studentOptions}
                value={form.studentId}
                onChange={(value) => setForm((current) => ({ ...current, studentId: String(value || ''), parentUserId: '', riskSnapshotId: '' }))}
                placeholder={t('ai.drafts.selectStudent')}
                searchPlaceholder={t('ai.drafts.searchStudents')}
                emptyLabel={t('ai.drafts.noStudentsFound')}
                disabled={!canRequestDrafts}
              />
            </label>
            <label className="draft-field-label">
              {t('ai.drafts.parent')}
              <SelectMenu
                options={parentOptions}
                value={form.parentUserId}
                onChange={(value) => setForm((current) => ({ ...current, parentUserId: String(value || '') }))}
                placeholder={t('ai.drafts.selectParent')}
                disabled={!canRequestDrafts || !selectedStudent}
              />
            </label>
            {canRequestDrafts ? (
              <>
                <label className="draft-field-label">
                  {t('ai.drafts.riskSnapshot')}
                  <SearchableSelect
                    options={riskSnapshotOptions}
                    value={form.riskSnapshotId}
                    onChange={(value) => setForm((current) => ({ ...current, riskSnapshotId: String(value || '') }))}
                    placeholder={t('ai.drafts.useLatestContext')}
                    searchPlaceholder={t('ai.drafts.searchSnapshots')}
                    emptyLabel={t('ai.drafts.noSnapshotsFound')}
                  />
                </label>
                <label className="draft-field-label">
                  {t('ai.drafts.issueType')}
                  <SelectMenu options={issueTypeOptions} value={form.issueType} onChange={(value) => setForm((current) => ({ ...current, issueType: value }))} placeholder={t('ai.drafts.issueType')} />
                </label>
                <label className="draft-field-label">
                  {t('ai.drafts.tone')}
                  <SelectMenu options={toneOptions} value={form.toneLabel} onChange={(value) => setForm((current) => ({ ...current, toneLabel: value }))} placeholder={t('ai.drafts.tone')} />
                </label>
                <label className="draft-field-label">
                  {t('ai.drafts.channel')}
                  <SelectMenu options={channelOptions} value={form.channel} onChange={(value) => setForm((current) => ({ ...current, channel: value }))} placeholder={t('ai.drafts.channel')} />
                </label>
                <label className="draft-field-label">
                  {t('ai.drafts.goal')}
                  <input className="input" value={form.goalLabel} onChange={(event) => setForm((current) => ({ ...current, goalLabel: event.target.value }))} placeholder={t('ai.drafts.goalPlaceholder')} />
                </label>
                <label className="draft-field-label">
                  {t('ai.drafts.teacherNote')}
                  <textarea className="textarea" rows={5} value={form.teacherNote} onChange={(event) => setForm((current) => ({ ...current, teacherNote: event.target.value }))} placeholder={t('ai.drafts.teacherNotePlaceholder')} />
                </label>
                <button type="button" className="btn btn-primary" disabled={!form.studentId || !form.parentUserId || saving} onClick={handleCreate}>
                  {saving ? t('ai.drafts.generatingDraft') : t('ai.drafts.generateDraft')}
                </button>
              </>
            ) : null}

            <div className="draft-filter-row">
              <label className="draft-field-label">
                {t('ai.drafts.statusFilter')}
                <SelectMenu options={statusFilterOptions} value={filters.status} onChange={(value) => setFilters((current) => ({ ...current, status: value }))} placeholder={t('ai.drafts.allStatuses')} />
              </label>
              <label className="draft-field-label">
                {t('ai.drafts.providerFilter')}
                <SelectMenu options={providerFilterOptions} value={filters.provider} onChange={(value) => setFilters((current) => ({ ...current, provider: value }))} placeholder={t('ai.drafts.allProviders')} />
              </label>
            </div>
          </div>
        </SectionCard>

        <SectionCard title={t('ai.drafts.reviewTitle')} subtitle={t('ai.drafts.reviewDescription')}>
          {detailLoading ? (
            <DraftReviewSkeleton />
          ) : selectedDraft ? (
            <div className="draft-detail-stack">
              <div className="draft-badge-row">
                <AiStatusBadge value={selectedDraft.draftStatus} />
                <AiStatusBadge value={selectedDraft.channel} />
                <AiStatusBadge value={selectedDraft.issueType} />
                {selectedDraft.generationProvider ? <AiStatusBadge value={selectedDraft.generationProvider} /> : null}
              </div>

              <div className="draft-meta-grid">
                <div>
                  <span>{t('ai.drafts.student')}</span>
                  <strong>{selectedDraft.studentName}</strong>
                </div>
                <div>
                  <span>{t('ai.drafts.parent')}</span>
                  <strong>{selectedDraft.parentName}</strong>
                </div>
                <div>
                  <span>{t('ai.drafts.requestedBy')}</span>
                  <strong>{selectedDraft.createdByUserName}</strong>
                </div>
                <div>
                  <span>{t('ai.drafts.generated')}</span>
                  <strong>{formatDateTime(selectedDraft.generatedAt, i18n.language, t('ai.shared.notAvailable'))}</strong>
                </div>
              </div>

              <DraftGeneratedVsEditedPanel
                draft={selectedDraft}
                canEdit={Boolean(selectedDraft.canEdit && canRequestDrafts)}
                onChange={(key, value) => setSelectedDraft((current) => ({ ...current, [key]: value }))}
              />

              <div className="draft-action-row">
                {selectedDraft.canEdit && canRequestDrafts ? (
                  <button type="button" className="btn btn-secondary" disabled={saving} onClick={handleUpdate}>
                    {saving ? t('common.saving') : t('ai.drafts.saveReviewedCopy')}
                  </button>
                ) : null}
                {selectedDraft.canApprove && canApproveDrafts ? (
                  <button type="button" className="btn btn-primary" disabled={saving} onClick={() => handleDecision('approve')}>
                    {t('ai.drafts.approveDraft')}
                  </button>
                ) : null}
                {selectedDraft.canApprove && canApproveDrafts ? (
                  <button type="button" className="btn btn-danger" disabled={saving} onClick={() => handleDecision('reject')}>
                    {t('ai.drafts.rejectDraft')}
                  </button>
                ) : null}
                {selectedDraft.canRetryGeneration && canRequestDrafts ? (
                  <button type="button" className="btn btn-secondary" disabled={saving} onClick={handleRetry}>
                    {t('ai.drafts.retryGeneration')}
                  </button>
                ) : null}
              </div>

              <SectionCard title={t('ai.drafts.contextTitle')} subtitle={t('ai.drafts.contextDescription')}>
                <DraftContextSummaryCard draft={selectedDraft} />
              </SectionCard>

              <div className="draft-audit-toggle-row">
                <button type="button" className="btn btn-secondary btn-sm" onClick={handleToggleDraftAudit}>
                  {showDraftAudit ? t('ai.drafts.hideAuditTrail') : t('ai.drafts.showAuditTrail')}
                </button>
              </div>

              {showDraftAudit ? (
                <SectionCard title={t('ai.drafts.auditTrailTitle')} subtitle={t('ai.drafts.auditTrailDescription')}>
                  {auditLoading ? <div className="muted-copy">{t('ai.drafts.loadingAuditTrail')}</div> : <DraftAuditTimeline logs={draftAuditLogs} />}
                </SectionCard>
              ) : null}
            </div>
          ) : (
            <EmptyState title={t('ai.drafts.noSelectionTitle')} description={t('ai.drafts.noSelectionDescription')} />
          )}
        </SectionCard>
      </div>

      <SectionCard title={audience === 'admin' ? t('ai.drafts.schoolWideTitle') : t('ai.drafts.myDraftsTitle')} subtitle={t('ai.drafts.listSectionDescription')}>
        {drafts.length || refreshingDrafts ? (
          <>
            <div className="table-container desktop-table">
              <table>
                <thead>
                  <tr>
                    <th>{t('ai.drafts.student')}</th>
                    <th>{t('ai.drafts.parent')}</th>
                    <th>{t('common.status')}</th>
                    <th>{t('ai.drafts.issue')}</th>
                    <th>{t('ai.drafts.provider')}</th>
                    <th>{t('ai.drafts.updated')}</th>
                    <th>{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {refreshingDrafts ? <DraftTableSkeletonRows /> : drafts.map((draft) => (
                    <tr key={draft.id}>
                      <td>{draft.studentName}</td>
                      <td>{draft.parentName}</td>
                      <td><AiStatusBadge value={draft.draftStatus} /></td>
                      <td><AiStatusBadge value={draft.issueType} /></td>
                      <td>{draft.generationProvider || t('ai.shared.pending')}</td>
                      <td>{formatDateTime(draft.updatedAt, i18n.language, t('ai.shared.notAvailable'))}</td>
                      <td>
                        <button type="button" className="btn btn-secondary btn-sm" onClick={() => loadDraftDetail(draft.id)}>
                          {t('common.open')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="teacher-mobile-card-list">
              {refreshingDrafts ? <DraftMobileSkeletonCards /> : drafts.map((draft) => (
                <article key={draft.id} className="teacher-mobile-card">
                  <div className="teacher-mobile-card-head">
                    <div>
                      <h3 className="teacher-mobile-card-title">{draft.studentName}</h3>
                      <div className="teacher-card-meta">{draft.parentName}</div>
                    </div>
                    <AiStatusBadge value={draft.draftStatus} />
                  </div>

                  <div className="draft-badge-row">
                    <AiStatusBadge value={draft.issueType} />
                    <AiStatusBadge value={draft.generationProvider || t('ai.shared.pending')} />
                  </div>

                  <div className="teacher-mobile-card-grid">
                    <div className="teacher-mobile-card-field">
                      <span>{t('ai.drafts.provider')}</span>
                      <strong>{draft.generationProvider || t('ai.shared.pending')}</strong>
                    </div>
                    <div className="teacher-mobile-card-field">
                      <span>{t('ai.drafts.updated')}</span>
                      <strong>{formatDateTime(draft.updatedAt, i18n.language, t('ai.shared.notAvailable'))}</strong>
                    </div>
                  </div>

                  <div className="teacher-action-row">
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => loadDraftDetail(draft.id)}>
                      {t('common.open')}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </>
        ) : (
          <EmptyState title={t('ai.drafts.emptyTitle')} description={t('ai.drafts.emptyDescription')} />
        )}
      </SectionCard>
    </div>
  );
}
