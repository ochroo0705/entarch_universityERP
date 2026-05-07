import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import {
  getExamScheduleRoster,
  getTeacherExamResults,
  getTeacherExamSchedules,
  updateExamResultPublishStatus,
  upsertExamResult,
} from '../../api/endpoints';
import ExamResultList from '../../components/ui/ExamResultList';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SectionCard from '../../components/ui/SectionCard';
import StatCard from '../../components/ui/StatCard';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

function toDraftMap(items) {
  return Object.fromEntries(
    items.map((item) => [
      item.studentId,
      {
        score: item.score ?? '',
        totalScore: item.totalScore ?? '',
        weighting: item.weighting ?? '',
        teacherComment: item.teacherComment ?? '',
        remarks: item.remarks ?? '',
        published: Boolean(item.published),
      },
    ])
  );
}

function ExamRosterCard({ draft, isSaving, row, t, onDraftChange, onSave, onPublishToggle }) {
  return (
    <article className="teacher-mobile-card">
      <div className="teacher-mobile-card-head">
        <div>
          <h3 className="teacher-mobile-card-title">{row.studentName}</h3>
          <div className="teacher-card-meta">
            {row.examResultId ? t('common.saved') : t('teacher.exams.entryTitle')}
          </div>
        </div>
        <span className={`badge ${draft.published ? 'badge-success' : 'badge-warning'}`}>
          {draft.published ? t('exams.results.published') : t('exams.results.unpublished')}
        </span>
      </div>

      <div className="teacher-mobile-card-grid">
        <label className="teacher-mobile-card-field">
          <span>{t('exams.results.score')}</span>
          <input className="form-control" type="number" min="0" step="0.01" value={draft.score ?? ''} onChange={(event) => onDraftChange(row.studentId, 'score', event.target.value)} />
        </label>
        <label className="teacher-mobile-card-field">
          <span>{t('exams.results.totalScore')}</span>
          <input className="form-control" type="number" min="0.01" step="0.01" value={draft.totalScore ?? ''} onChange={(event) => onDraftChange(row.studentId, 'totalScore', event.target.value)} />
        </label>
        <label className="teacher-mobile-card-field">
          <span>{t('exams.results.weighting')}</span>
          <input className="form-control" type="number" min="0" max="100" step="0.01" value={draft.weighting ?? ''} onChange={(event) => onDraftChange(row.studentId, 'weighting', event.target.value)} />
        </label>
        <label className="teacher-mobile-card-field">
          <span>{t('exams.results.publish')}</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', minHeight: '42px' }}>
            <input type="checkbox" checked={Boolean(draft.published)} onChange={(event) => onDraftChange(row.studentId, 'published', event.target.checked)} />
            <span>{draft.published ? t('exams.results.published') : t('exams.results.unpublished')}</span>
          </div>
        </label>
      </div>

      <label className="teacher-mobile-card-field">
        <span>{t('exams.results.teacherComment')}</span>
        <input className="form-control" type="text" value={draft.teacherComment ?? ''} onChange={(event) => onDraftChange(row.studentId, 'teacherComment', event.target.value)} />
      </label>

      <div className="teacher-action-row">
        <button className="btn btn-primary btn-sm" type="button" disabled={isSaving} onClick={() => onSave(row.studentId)}>
          {t('teacher.exams.save')}
        </button>
        <button className="btn btn-secondary btn-sm" type="button" disabled={isSaving || !row.examResultId} onClick={() => onPublishToggle(row)}>
          {row.published ? t('teacher.exams.unpublish') : t('teacher.exams.publish')}
        </button>
      </div>
    </article>
  );
}

export default function TeacherExams() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [results, setResults] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [selectedScheduleId, setSelectedScheduleId] = useState('');
  const [roster, setRoster] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [rosterLoading, setRosterLoading] = useState(false);
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    Promise.all([
      getTeacherExamResults(user.userId),
      getTeacherExamSchedules(user.userId),
    ])
      .then(([resultsRes, schedulesRes]) => {
        const nextResults = resultsRes.data || [];
        const nextSchedules = schedulesRes.data || [];
        setResults(nextResults);
        setSchedules(nextSchedules);
        if (nextSchedules.length) {
          setSelectedScheduleId(String(nextSchedules[0].id));
        }
      })
      .catch(() => {
        setResults([]);
        setSchedules([]);
      })
      .finally(() => setLoading(false));
  }, [user.userId]);

  useEffect(() => {
    if (!selectedScheduleId) {
      setRoster([]);
      setDrafts({});
      return;
    }
    setRosterLoading(true);
    getExamScheduleRoster(selectedScheduleId)
      .then((res) => {
        const items = res.data || [];
        setRoster(items);
        setDrafts(toDraftMap(items));
      })
      .catch(() => {
        setRoster([]);
        setDrafts({});
      })
      .finally(() => setRosterLoading(false));
  }, [selectedScheduleId]);

  const items = useMemo(
    () => [...results].sort((a, b) => `${b.examDate} ${b.startTime}`.localeCompare(`${a.examDate} ${a.startTime}`)),
    [results]
  );

  const uniqueClasses = new Set(items.map((exam) => exam.className)).size;
  const uniqueSubjects = new Set(items.map((exam) => exam.subject)).size;
  const scheduleOptions = useMemo(
    () => schedules.map((schedule) => ({
      value: String(schedule.id),
      label: `${schedule.title} • ${schedule.className} • ${schedule.examDate}`,
    })),
    [schedules]
  );

  const handleDraftChange = (studentId, field, value) => {
    setDrafts((prev) => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        [field]: value,
      },
    }));
  };

  const refreshResults = async () => {
    const [resultsRes, rosterRes] = await Promise.all([
      getTeacherExamResults(user.userId),
      selectedScheduleId ? getExamScheduleRoster(selectedScheduleId) : Promise.resolve({ data: [] }),
    ]);
    setResults(resultsRes.data || []);
    if (selectedScheduleId) {
      const nextRoster = rosterRes.data || [];
      setRoster(nextRoster);
      setDrafts(toDraftMap(nextRoster));
    }
  };

  const handleSave = async (studentId) => {
    const draft = drafts[studentId];
    const schedule = schedules.find((item) => String(item.id) === String(selectedScheduleId));
    if (!schedule) return;

    setSavingId(studentId);
    try {
      await upsertExamResult({
        examScheduleId: Number(selectedScheduleId),
        studentId,
        score: Number(draft.score),
        totalScore: Number(draft.totalScore),
        weighting: draft.weighting === '' ? null : Number(draft.weighting),
        teacherComment: draft.teacherComment || null,
        remarks: draft.remarks || null,
        published: Boolean(draft.published),
      });
      await refreshResults();
    } catch (error) {
      window.alert(error.response?.data?.message || t('teacher.exams.saveFailed'));
    } finally {
      setSavingId(null);
    }
  };

  const handlePublishToggle = async (row) => {
    if (!row.examResultId) {
      window.alert(t('teacher.exams.saveBeforePublish'));
      return;
    }
    setSavingId(row.studentId);
    try {
      await updateExamResultPublishStatus(row.examResultId, !row.published);
      await refreshResults();
    } catch (error) {
      window.alert(error.response?.data?.message || t('teacher.exams.publishFailed'));
    } finally {
      setSavingId(null);
    }
  };

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (!schedules.length && !items.length) return <EmptyState title={t('teacher.exams.title')} description={t('teacher.exams.empty')} />;

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('teacher.exams.kicker')}</div>
          <h1>{t('teacher.exams.title')}</h1>
          <p className="page-summary">{t('teacher.exams.summary')}</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F4C5}'} tone="teachers" value={items.length} label={t('teacher.exams.total')} />
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('teacher.exams.subjects')} />
        <StatCard icon={'\u{1F3EB}'} tone="classes" value={uniqueClasses} label={t('teacher.exams.classes')} />
      </div>

      <SectionCard title={t('teacher.exams.entryTitle')} subtitle={t('teacher.exams.entrySummary')}>
        <div className="teacher-section-stack">
          <div className="teacher-inline-controls">
            <div className="teacher-control-wide">
              <label style={{ display: 'block', marginBottom: '0.4rem', fontWeight: 600 }}>{t('teacher.exams.selectExam')}</label>
              <SearchableSelect
                options={scheduleOptions}
                value={selectedScheduleId}
                onChange={(value) => setSelectedScheduleId(String(value || ''))}
                placeholder={t('teacher.exams.selectExam')}
                searchPlaceholder={t('common.search')}
                emptyLabel="No exams found"
              />
            </div>
          </div>

          {rosterLoading ? (
            <LoadingState label={t('common.loading')} />
          ) : !roster.length ? (
            <EmptyState title={t('teacher.exams.entryTitle')} description={t('teacher.exams.noRoster')} />
          ) : (
            <>
              <div className="table-container desktop-table">
                <table className="table">
                  <thead>
                    <tr>
                      <th>{t('teacher.exams.student')}</th>
                      <th>{t('exams.results.score')}</th>
                      <th>{t('exams.results.totalScore')}</th>
                      <th>{t('exams.results.weighting')}</th>
                      <th>{t('exams.results.teacherComment')}</th>
                      <th>{t('exams.results.publish')}</th>
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((row) => {
                      const draft = drafts[row.studentId] || {};
                      const isSaving = savingId === row.studentId;
                      return (
                        <tr key={`${row.examScheduleId}-${row.studentId}`}>
                          <td>{row.studentName}</td>
                          <td>
                            <input className="form-control" type="number" min="0" step="0.01" value={draft.score ?? ''} onChange={(event) => handleDraftChange(row.studentId, 'score', event.target.value)} />
                          </td>
                          <td>
                            <input className="form-control" type="number" min="0.01" step="0.01" value={draft.totalScore ?? ''} onChange={(event) => handleDraftChange(row.studentId, 'totalScore', event.target.value)} />
                          </td>
                          <td>
                            <input className="form-control" type="number" min="0" max="100" step="0.01" value={draft.weighting ?? ''} onChange={(event) => handleDraftChange(row.studentId, 'weighting', event.target.value)} />
                          </td>
                          <td>
                            <input className="form-control" type="text" value={draft.teacherComment ?? ''} onChange={(event) => handleDraftChange(row.studentId, 'teacherComment', event.target.value)} />
                          </td>
                          <td>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                              <input type="checkbox" checked={Boolean(draft.published)} onChange={(event) => handleDraftChange(row.studentId, 'published', event.target.checked)} />
                              <span>{draft.published ? t('exams.results.published') : t('exams.results.unpublished')}</span>
                            </label>
                          </td>
                          <td>
                            <div className="teacher-action-row">
                              <button className="btn btn-primary btn-sm" type="button" disabled={isSaving} onClick={() => handleSave(row.studentId)}>
                                {t('teacher.exams.save')}
                              </button>
                              <button className="btn btn-secondary btn-sm" type="button" disabled={isSaving || !row.examResultId} onClick={() => handlePublishToggle(row)}>
                                {row.published ? t('teacher.exams.unpublish') : t('teacher.exams.publish')}
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              <div className="teacher-mobile-card-list">
                {roster.map((row) => (
                  <ExamRosterCard
                    key={`${row.examScheduleId}-${row.studentId}`}
                    draft={drafts[row.studentId] || {}}
                    isSaving={savingId === row.studentId}
                    row={row}
                    t={t}
                    onDraftChange={handleDraftChange}
                    onSave={handleSave}
                    onPublishToggle={handlePublishToggle}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      </SectionCard>

      <SectionCard title={t('teacher.exams.recordedResults')}>
        <ExamResultList
          exams={items}
          emptyLabel={t('teacher.exams.empty')}
          showStudentName
          renderMeta={(exam) => `${exam.className}${exam.roomNumber ? ` • ${exam.roomNumber}` : ''}`}
        />
      </SectionCard>
    </div>
  );
}
