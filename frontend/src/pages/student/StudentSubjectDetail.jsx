import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import SelectMenu from '../../components/ui/SelectMenu';
import HomeworkAttachmentViewer from '../../components/HomeworkAttachmentViewer';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';
import {
  downloadFileAuthenticated,
  getHomeworkForStudent,
  getStudentAttendance,
  getStudentSchedule,
  submitHomework,
} from '../../api/endpoints';
import { buildStudentSubjects } from '../../utils/studentSubjects';
import { isAttendanceCountedAsPresent, isHomeworkGraded, isHomeworkSubmitted, normalizeStatus } from '../../utils/studentProgress';

const TYPE_BADGES = {
  HOMEWORK: 'badge-info',
  PROJECT: 'badge-warning',
  QUIZ: 'badge-success',
  TEST: 'badge-danger',
};

const DAY_KEYS = {
  1: 'days.monday',
  2: 'days.tuesday',
  3: 'days.wednesday',
  4: 'days.thursday',
  5: 'days.friday',
  6: 'days.saturday',
  7: 'days.sunday',
};

export default function StudentSubjectDetail() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { subjectSlug } = useParams();
  const [schedule, setSchedule] = useState([]);
  const [homework, setHomework] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [expanded, setExpanded] = useState(null);
  const [submitting, setSubmitting] = useState(null);
  const [submitText, setSubmitText] = useState('');
  const [submitFile, setSubmitFile] = useState(null);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    const now = new Date();
    const yearStart = `${now.getFullYear()}-01-01`;
    const yearEnd = `${now.getFullYear()}-12-31`;

    setLoading(true);
    Promise.all([
      getStudentSchedule(user.userId),
      getHomeworkForStudent(),
      getStudentAttendance(user.userId, yearStart, yearEnd),
    ])
      .then(([scheduleRes, homeworkRes, attendanceRes]) => {
        setSchedule(scheduleRes.data || []);
        setHomework(homeworkRes.data || []);
        setAttendance(attendanceRes.data || []);
      })
      .catch(() => {
        setSchedule([]);
        setHomework([]);
        setAttendance([]);
      })
      .finally(() => setLoading(false));
  }, [user.userId]);

  const subjects = useMemo(
    () => buildStudentSubjects({ schedule, homework, attendance }),
    [schedule, homework, attendance]
  );

  const subject = useMemo(
    () => subjects.find((item) => item.slug === subjectSlug),
    [subjectSlug, subjects]
  );

  const tabOptions = useMemo(
    () => [
      { value: 'overview', label: t('student.subjectDetail.tabs.overview') },
      { value: 'homework', label: t('student.subjectDetail.tabs.homework') },
      { value: 'attendance', label: t('student.subjectDetail.tabs.attendance') },
    ],
    [t]
  );

  const statusConfig = useMemo(() => ({
    present: { label: t('student.attendance.present'), badge: 'badge-success' },
    absent: { label: t('student.attendance.absent'), badge: 'badge-danger' },
    late: { label: t('student.attendance.late'), badge: 'badge-warning' },
    excused: { label: t('student.attendance.excused'), badge: 'badge-info' },
    sick: { label: t('student.attendance.sick'), badge: 'badge-warning' },
  }), [t]);

  const now = new Date();
  const todayIndex = now.getDay() === 0 ? 1 : now.getDay();
  const todaySchedule = subject?.scheduleItems.filter((item) => item.dayOfWeek === todayIndex) || [];
  const sortedSchedule = useMemo(
    () => [...(subject?.scheduleItems || [])].sort((a, b) => (a.dayOfWeek - b.dayOfWeek) || (a.periodNumber - b.periodNumber)),
    [subject]
  );
  const sortedHomework = useMemo(
    () => [...(subject?.homeworkItems || [])].sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate)),
    [subject]
  );
  const sortedAttendance = useMemo(
    () => [...(subject?.attendanceItems || [])].sort((a, b) => (b.attendanceDate || b.date || '').localeCompare(a.attendanceDate || a.date || '')),
    [subject]
  );
  const attendanceRate = useMemo(() => {
    if (!subject?.attendanceItems.length) return 100;
    const attended = subject.attendanceItems.filter((item) => isAttendanceCountedAsPresent(item.status)).length;
    return Math.round((attended / subject.attendanceItems.length) * 100);
  }, [subject]);

  const handleSubmit = async (homeworkId) => {
    if (!submitText.trim() && !submitFile) return;
    setSubmitting(homeworkId);
    try {
      const res = await submitHomework(homeworkId, { submissionText: submitText, file: submitFile });
      setHomework((prev) => prev.map((item) => (
        item.id === homeworkId ? { ...item, submission: res.data } : item
      )));
      setSubmitText('');
      setSubmitFile(null);
      setExpanded(null);
      setSuccessMsg(t('student.subjectDetail.submitSuccess'));
      window.setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit homework');
    } finally {
      setSubmitting(null);
    }
  };

  if (loading) return <LoadingState label={t('student.subjectDetail.loading')} />;

  if (!subject) {
    return (
      <EmptyState
        title={t('student.subjectDetail.notFoundTitle')}
        description={t('student.subjectDetail.notFoundDescription')}
        action={<Link to="/student/subjects" className="btn btn-primary">{t('student.subjectDetail.backToSubjects')}</Link>}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('sidebar.subjects')}</div>
          <h1>{subject.name}</h1>
          <p className="page-summary">
            {[...subject.teacherNames, ...subject.classNames].join(' / ') || t('student.subjects.summary')}
          </p>
        </div>
        <Link to="/student/subjects" className="btn btn-secondary">{t('student.subjectDetail.backToSubjects')}</Link>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon teachers">{'\u{1F5D3}\uFE0F'}</div>
          <div className="stat-info"><h3>{subject.scheduleItems.length}</h3><p>{t('student.subjects.weeklyPeriods')}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon assignments">{'\u{1F4DD}'}</div>
          <div className="stat-info"><h3>{subject.homeworkItems.length}</h3><p>{t('student.subjects.homeworkItems')}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon students">{'\u2705'}</div>
          <div className="stat-info"><h3>{attendanceRate}%</h3><p>{t('student.subjects.attendanceRate')}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon classes">{'\u{1F4C5}'}</div>
          <div className="stat-info"><h3>{todaySchedule.length}</h3><p>{t('student.subjects.todayLessons')}</p></div>
        </div>
      </div>

      {successMsg ? <div className="alert alert-success" style={{ marginBottom: '1rem' }}>{successMsg}</div> : null}

      <div className="teacher-tab-select">
        <SelectMenu
          options={tabOptions}
          value={activeTab}
          onChange={(value) => setActiveTab(value)}
          placeholder={t('student.subjectDetail.tabs.overview')}
        />
      </div>

      <div className="teacher-tabs">
        {tabOptions.map((tab) => (
          <button
            key={tab.value}
            type="button"
            className={`teacher-tab${activeTab === tab.value ? ' active' : ''}`}
            onClick={() => setActiveTab(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'overview' ? (
        <div className="panel-grid-two">
          <div className="content-stack">
            <div className="card">
              <div className="card-body">
                <h3 style={{ fontSize: '1rem', marginBottom: '0.75rem' }}>{t('student.subjectDetail.todaySchedule')}</h3>
                {todaySchedule.length ? (
                  <div className="interactive-list">
                    {todaySchedule.map((item) => (
                      <div key={item.scheduleId || `${item.dayOfWeek}-${item.periodNumber}`} className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                        <div className="interactive-card-main">
                          <div className="interactive-card-title">{t('student.schedule.period')} {item.periodNumber}</div>
                          <div className="interactive-card-meta">
                            {item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}
                            {item.roomNumber ? ` / ${t('student.subjects.room')} ${item.roomNumber}` : ''}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="muted-copy">{t('student.subjects.noClassesToday')}</p>
                )}
              </div>
            </div>

            <div className="card">
              <div className="card-body">
                <h3 style={{ fontSize: '1rem', marginBottom: '0.75rem' }}>{t('student.subjectDetail.recentAttendance')}</h3>
                {sortedAttendance.length ? (
                  <div className="interactive-list">
                    {sortedAttendance.slice(0, 5).map((item, index) => {
                      const status = normalizeStatus(item.status);
                      const cfg = statusConfig[status] || statusConfig.present;
                      return (
                        <div key={item.id || index} className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                          <div className="interactive-card-main">
                            <div className="interactive-card-title">{new Date(item.attendanceDate || item.date).toLocaleDateString()}</div>
                            <div className="interactive-card-meta">
                              {t('student.attendance.period')} {item.periodNumber || '-'}
                              {item.remarks ? ` / ${item.remarks}` : ''}
                            </div>
                          </div>
                          <span className={`badge ${cfg.badge}`}>{cfg.label}</span>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <p className="muted-copy">{t('student.subjectDetail.noAttendance')}</p>
                )}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-body">
              <h3 style={{ fontSize: '1rem', marginBottom: '0.75rem' }}>{t('student.subjectDetail.weeklySchedule')}</h3>
              {sortedSchedule.length ? (
                <div className="interactive-list">
                  {sortedSchedule.map((item) => (
                    <div key={item.scheduleId || `${item.dayOfWeek}-${item.periodNumber}`} className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                      <div className="interactive-card-main">
                        <div className="interactive-card-title">{t(DAY_KEYS[item.dayOfWeek] || 'days.monday')}</div>
                        <div className="interactive-card-meta">
                          {t('student.schedule.period')} {item.periodNumber} / {item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}
                        </div>
                      </div>
                      {item.roomNumber ? <span className="badge badge-info">{t('student.subjects.room')} {item.roomNumber}</span> : null}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="muted-copy">{t('student.schedule.noSchedule')}</p>
              )}
            </div>
          </div>
        </div>
      ) : null}

      {activeTab === 'homework' ? (
        <div className="card">
          <div className="card-body">
            <div className="section-header">
              <div>
                <h2 className="section-title">{t('student.subjectDetail.homeworkList')}</h2>
                <p className="section-subtitle">{t('student.subjectDetail.homeworkSummary')}</p>
              </div>
            </div>

            {sortedHomework.length ? (
              <div className="content-stack">
                {sortedHomework.map((item, index) => {
                  const submission = item.submission;
                  const due = new Date(item.dueDate);
                  const isPastDue = due < now;
                  const isExpanded = expanded === item.id;
                  const daysLeft = Math.ceil((due - now) / (1000 * 60 * 60 * 24));

                  return (
                    <div key={item.id} className="card" style={{ marginBottom: 0, animation: `fadeInUp 0.35s cubic-bezier(0.16,1,0.3,1) ${index * 0.04}s both` }}>
                      <div className="card-body">
                        <div
                          style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}
                          onClick={() => setExpanded(isExpanded ? null : item.id)}
                        >
                          <div style={{ flex: 1 }}>
                            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap', marginBottom: '0.35rem' }}>
                              <span className={`badge ${TYPE_BADGES[item.type] || 'badge-info'}`}>{item.type || 'HOMEWORK'}</span>
                              {isHomeworkSubmitted(submission) ? (
                                <span className={`badge ${isHomeworkGraded(submission) ? 'badge-success' : 'badge-warning'}`}>
                                  {isHomeworkGraded(submission)
                                    ? t('student.subjectDetail.statusLabels.graded', { score: `${submission.score}${item.maxScore ? `/${item.maxScore}` : ''}` })
                                    : t('student.subjectDetail.statusLabels.submitted')}
                                </span>
                              ) : isPastDue ? (
                                <span className="badge badge-danger">{t('student.subjectDetail.statusLabels.overdue')}</span>
                              ) : (
                                <span className="badge" style={{ background: 'var(--primary-light)', color: 'var(--primary)' }}>
                                  {daysLeft <= 0
                                    ? t('student.subjectDetail.statusLabels.dueToday')
                                    : t('student.subjectDetail.statusLabels.daysLeft', { count: daysLeft })}
                                </span>
                              )}
                            </div>
                            <h3 style={{ fontSize: '1rem', marginBottom: '0.2rem' }}>{item.title}</h3>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                              {t('student.homework.due')}: {due.toLocaleDateString()}
                            </div>
                          </div>
                          <span style={{ color: 'var(--text-muted)', transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }}>▼</span>
                        </div>

                        {isExpanded ? (
                          <div style={{ marginTop: '0.75rem', padding: '1rem', background: '#f8fafc', borderRadius: '6px' }}>
                            {item.description ? (
                              <div style={{ marginBottom: '1rem', whiteSpace: 'pre-wrap', lineHeight: 1.6, fontSize: '0.9rem' }}>
                                {item.description}
                              </div>
                            ) : null}

                            <HomeworkAttachmentViewer
                              attachments={item.attachments?.length ? item.attachments : (item.attachmentUrl ? [{
                                id: null,
                                originalFilename: 'Attachment',
                                mimeType: 'application/octet-stream',
                                size: null,
                                uploadedAt: item.createdAt,
                                downloadUrl: item.attachmentUrl,
                                previewUrl: null,
                                previewable: false,
                                kind: 'legacy-fallback',
                              }] : [])}
                              compact
                            />

                            {isHomeworkSubmitted(submission) ? (
                              <div style={{ padding: '0.75rem', background: 'white', borderRadius: '6px', border: '1px solid var(--border)' }}>
                                <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem' }}>{t('student.subjectDetail.yourSubmission')}</div>
                                {submission.submissionText ? (
                                  <div style={{ fontSize: '0.85rem', marginBottom: '0.5rem', whiteSpace: 'pre-wrap' }}>{submission.submissionText}</div>
                                ) : null}
                                {submission.attachmentUrl ? (
                                  <a
                                    href="#"
                                    onClick={(event) => {
                                      event.preventDefault();
                                      downloadFileAuthenticated(submission.attachmentUrl);
                                    }}
                                    style={{ fontSize: '0.8rem', color: 'var(--primary)', cursor: 'pointer' }}
                                  >
                                    {t('student.subjectDetail.attachedFile')}
                                  </a>
                                ) : null}
                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                                  {t('student.subjectDetail.submittedAt')}: {submission.submittedAt ? new Date(submission.submittedAt).toLocaleString() : '-'}
                                </div>
                                {isHomeworkGraded(submission) ? (
                                  <div style={{ marginTop: '0.75rem', padding: '0.6rem', background: 'var(--primary-light)', borderRadius: '6px' }}>
                                    <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--primary)' }}>
                                      {t('student.subjectDetail.score')}: {submission.score}{item.maxScore ? ` / ${item.maxScore}` : ''}
                                    </div>
                                    {submission.feedback ? <div style={{ fontSize: '0.85rem', marginTop: '0.4rem', whiteSpace: 'pre-wrap' }}>{submission.feedback}</div> : null}
                                  </div>
                                ) : null}
                              </div>
                            ) : !isPastDue ? (
                              <div style={{ marginTop: '0.5rem' }}>
                                <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem' }}>{t('student.homework.submitYourWork')}</div>
                                <textarea
                                  className="form-control"
                                  placeholder={t('student.homework.typeAnswer')}
                                  value={expanded === item.id ? submitText : ''}
                                  onChange={(event) => setSubmitText(event.target.value)}
                                  rows={4}
                                  style={{ marginBottom: '0.5rem', fontSize: '0.85rem' }}
                                />
                                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                                  <label style={{ fontSize: '0.8rem', cursor: 'pointer', color: 'var(--primary)' }}>
                                    {t('student.homework.attachFile')}
                                    <input type="file" style={{ display: 'none' }} onChange={(event) => setSubmitFile(event.target.files?.[0] || null)} />
                                  </label>
                                  {submitFile ? <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{submitFile.name}</span> : null}
                                  <div style={{ flex: 1 }} />
                                  <button
                                    className="btn btn-primary btn-sm"
                                    onClick={() => handleSubmit(item.id)}
                                    disabled={submitting === item.id || (!submitText.trim() && !submitFile)}
                                  >
                                    {submitting === item.id ? t('common.submitting') : t('common.submit')}
                                  </button>
                                </div>
                              </div>
                            ) : null}
                          </div>
                        ) : null}
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <EmptyState title={t('student.subjectDetail.homeworkList')} description={t('student.subjectDetail.noHomework')} />
            )}
          </div>
        </div>
      ) : null}

      {activeTab === 'attendance' ? (
        <div className="card">
          <div className="card-body">
            <div className="section-header">
              <div>
                <h2 className="section-title">{t('student.subjectDetail.attendanceRecords')}</h2>
                <p className="section-subtitle">{t('student.subjectDetail.attendanceSummary')}</p>
              </div>
            </div>

            {sortedAttendance.length ? (
              <>
                <div className="table-container desktop-table">
                  <table>
                    <thead>
                      <tr>
                        <th>{t('student.attendance.date')}</th>
                        <th>{t('student.attendance.period')}</th>
                        <th>{t('student.attendance.status')}</th>
                        <th>{t('student.attendance.remarks')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sortedAttendance.map((item, index) => {
                        const status = normalizeStatus(item.status);
                        const cfg = statusConfig[status] || statusConfig.present;
                        return (
                          <tr key={item.id || index}>
                            <td>{(item.attendanceDate || item.date) ? new Date(item.attendanceDate || item.date).toLocaleDateString() : '-'}</td>
                            <td>{item.periodNumber || '-'}</td>
                            <td><span className={`badge ${cfg.badge}`}>{cfg.label}</span></td>
                            <td>{item.remarks || '-'}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                <div className="parent-mobile-card-list">
                  {sortedAttendance.map((item, index) => {
                    const status = normalizeStatus(item.status);
                    const cfg = statusConfig[status] || statusConfig.present;
                    return (
                      <article key={`mobile-${item.id || index}`} className="parent-mobile-card">
                        <div className="parent-mobile-card-head">
                          <div>
                            <h3 className="parent-mobile-card-title">
                              {(item.attendanceDate || item.date) ? new Date(item.attendanceDate || item.date).toLocaleDateString() : '-'}
                            </h3>
                            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                              {t('student.attendance.period')} {item.periodNumber || '-'}
                            </div>
                          </div>
                          <span className={`badge ${cfg.badge}`}>{cfg.label}</span>
                        </div>
                        <div className="parent-mobile-card-field">
                          <span>{t('student.attendance.remarks')}</span>
                          <div className="parent-mobile-card-copy">{item.remarks || '-'}</div>
                        </div>
                      </article>
                    );
                  })}
                </div>
              </>
            ) : (
              <EmptyState title={t('student.subjectDetail.attendanceRecords')} description={t('student.subjectDetail.noAttendance')} />
            )}
          </div>
        </div>
      ) : null}
    </div>
  );
}
