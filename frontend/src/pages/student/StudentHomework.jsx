import { useEffect, useState, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import {
  getHomeworkForStudent,
  submitHomework,
  downloadFileAuthenticated,
} from '../../api/endpoints';
import HomeworkAttachmentViewer from '../../components/HomeworkAttachmentViewer';
import { isHomeworkGraded, isHomeworkSubmitted } from '../../utils/studentProgress';

const TYPE_BADGES = {
  HOMEWORK: 'badge-info',
  PROJECT: 'badge-warning',
  QUIZ: 'badge-success',
  TEST: 'badge-danger',
};

export default function StudentHomework() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [homework, setHomework] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const [submitting, setSubmitting] = useState(null);
  const [submitText, setSubmitText] = useState('');
  const [submitFile, setSubmitFile] = useState(null);
  const [filter, setFilter] = useState('all');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    getHomeworkForStudent()
      .then((res) => setHomework(res.data || []))
      .catch(() => setHomework([]))
      .finally(() => setLoading(false));
  }, [user.userId]);

  // Auto-expand homework from query param
  useEffect(() => {
    const hwId = searchParams.get('hw');
    if (hwId && homework.length > 0 && !loading) {
      setExpanded(parseInt(hwId));
      setSearchParams({}, { replace: true });
    }
  }, [homework, loading, searchParams, setSearchParams]);

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
      setSuccessMsg('Homework submitted successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit homework');
    } finally {
      setSubmitting(null);
    }
  };

  const now = new Date();
  const { filtered, pendingCount, submittedCount, gradedCount } = useMemo(() => {
    const filtered = homework.filter((h) => {
      const sub = h.submission;
      const isPastDue = new Date(h.dueDate) < now;
      const hasSubmission = isHomeworkSubmitted(sub);
      if (filter === 'pending') return !hasSubmission && !isPastDue;
      if (filter === 'submitted') return hasSubmission && !isHomeworkGraded(sub);
      if (filter === 'graded') return isHomeworkGraded(sub);
      if (filter === 'overdue') return !hasSubmission && isPastDue;
      return true;
    });
    const pendingCount = homework.filter((h) => !isHomeworkSubmitted(h.submission) && new Date(h.dueDate) >= now).length;
    const submittedCount = homework.filter((h) => isHomeworkSubmitted(h.submission) && !isHomeworkGraded(h.submission)).length;
    const gradedCount = homework.filter((h) => isHomeworkGraded(h.submission)).length;
    return { filtered, pendingCount, submittedCount, gradedCount };
  }, [homework, filter, now]);

  if (loading) return <div className="loading"><div className="spinner" />{t('common.loadingHomework')}</div>;

  return (
    <div>
      <div className="page-header">
        <h1>{t('student.homework.title')}</h1>
      </div>

      {successMsg && (
        <div className="alert alert-success" style={{ marginBottom: '1rem', animation: 'fadeInUp 0.3s ease' }}>
          {successMsg}
        </div>
      )}

      <div className="stats-grid" style={{ marginBottom: '1.5rem' }}>
        <div className="stat-card" onClick={() => setFilter('all')} style={{ cursor: 'pointer', outline: filter === 'all' ? '2px solid var(--primary)' : 'none' }}>
          <div className="stat-icon classes">📋</div>
          <div className="stat-info"><h3>{homework.length}</h3><p>{t('student.homework.total')}</p></div>
        </div>
        <div className="stat-card" onClick={() => setFilter('pending')} style={{ cursor: 'pointer', outline: filter === 'pending' ? '2px solid var(--primary)' : 'none' }}>
          <div className="stat-icon assignments">📝</div>
          <div className="stat-info"><h3>{pendingCount}</h3><p>{t('student.homework.pending')}</p></div>
        </div>
        <div className="stat-card" onClick={() => setFilter('submitted')} style={{ cursor: 'pointer', outline: filter === 'submitted' ? '2px solid var(--primary)' : 'none' }}>
          <div className="stat-icon teachers">📤</div>
          <div className="stat-info"><h3>{submittedCount}</h3><p>{t('student.homework.submitted')}</p></div>
        </div>
        <div className="stat-card" onClick={() => setFilter('graded')} style={{ cursor: 'pointer', outline: filter === 'graded' ? '2px solid var(--primary)' : 'none' }}>
          <div className="stat-icon students">✅</div>
          <div className="stat-info"><h3>{gradedCount}</h3><p>{t('student.homework.graded')}</p></div>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>
            {t('student.homework.noHomework')}
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {filtered.map((h, i) => {
            const sub = h.submission;
            const due = new Date(h.dueDate);
            const isPastDue = due < now;
            const daysLeft = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
            const isExpanded = expanded === h.id;

            return (
              <div
                key={h.id}
                className="card"
                style={{ animation: `fadeInUp 0.35s cubic-bezier(0.16,1,0.3,1) ${i * 0.04}s both` }}
              >
                <div className="card-body">
                  <div
                    style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}
                    onClick={() => setExpanded(isExpanded ? null : h.id)}
                  >
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.35rem', flexWrap: 'wrap' }}>
                        <span className={`badge ${TYPE_BADGES[h.type] || 'badge-info'}`}>
                          {h.type || 'HOMEWORK'}
                        </span>
                        {isHomeworkSubmitted(sub) ? (
                          <span className={`badge ${isHomeworkGraded(sub) ? 'badge-success' : 'badge-warning'}`}>
                            {isHomeworkGraded(sub) ? `Graded: ${sub.score}${h.maxScore ? `/${h.maxScore}` : ''}` : 'Submitted'}
                          </span>
                        ) : isPastDue ? (
                          <span className="badge badge-danger">{t('student.homework.overdue')}</span>
                        ) : (
                          <span className="badge" style={{ background: 'var(--primary-light)', color: 'var(--primary)' }}>
                            {daysLeft <= 0 ? t('student.dashboard.dueToday') : t('student.dashboard.daysLeft', { count: daysLeft })}
                          </span>
                        )}
                      </div>
                      <h3 style={{ fontSize: '1rem', marginBottom: '0.2rem' }}>{h.title}</h3>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        Due: {due.toLocaleDateString()}
                      </div>
                    </div>
                    <span style={{
                      color: 'var(--text-muted)', fontSize: '1.2rem',
                      transition: 'transform 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                      transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)',
                      display: 'inline-block',
                    }}>▼</span>
                  </div>

                  <div style={{
                    display: 'grid',
                    gridTemplateRows: isExpanded ? '1fr' : '0fr',
                    transition: 'grid-template-rows 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                  }}>
                    <div style={{ overflow: 'hidden' }}>
                      <div style={{ marginTop: '0.75rem', padding: '1rem', background: '#f8fafc', borderRadius: '6px' }}>
                        {h.description && (
                          <div style={{ marginBottom: '1rem', whiteSpace: 'pre-wrap', lineHeight: 1.6, fontSize: '0.9rem' }}>
                            {h.description}
                          </div>
                        )}

                        <HomeworkAttachmentViewer
                          attachments={h.attachments?.length ? h.attachments : (h.attachmentUrl ? [{
                            id: null,
                            originalFilename: 'Attachment',
                            mimeType: 'application/octet-stream',
                            size: null,
                            uploadedAt: h.createdAt,
                            downloadUrl: h.attachmentUrl,
                            previewUrl: null,
                            previewable: false,
                            kind: 'legacy-fallback',
                          }] : [])}
                          compact
                        />

                        {/* Submission display */}
                        {isHomeworkSubmitted(sub) ? (
                          <div style={{ padding: '0.75rem', background: 'white', borderRadius: '6px', border: '1px solid var(--border)' }}>
                            <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem', fontFamily: 'var(--font-heading)' }}>
                              Your Submission
                            </div>
                            {sub.submissionText && (
                              <div style={{ fontSize: '0.85rem', marginBottom: '0.5rem', whiteSpace: 'pre-wrap' }}>{sub.submissionText}</div>
                            )}
                            {sub.attachmentUrl && (
                              <a href="#" onClick={(e) => { e.preventDefault(); downloadFileAuthenticated(sub.attachmentUrl); }}
                                 style={{ fontSize: '0.8rem', color: 'var(--primary)', cursor: 'pointer' }}>
                                📎 Your attached file
                              </a>
                            )}
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                              Submitted: {sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '—'}
                            </div>
                            {isHomeworkGraded(sub) && (
                              <div style={{ marginTop: '0.75rem', padding: '0.6rem', background: 'var(--primary-light)', borderRadius: '6px' }}>
                                <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--primary)' }}>
                                  Score: {sub.score}{h.maxScore ? ` / ${h.maxScore}` : ''}
                                </div>
                                {sub.feedback && (
                                  <div style={{ fontSize: '0.85rem', marginTop: '0.4rem', whiteSpace: 'pre-wrap' }}>
                                    {sub.feedback}
                                  </div>
                                )}
                              </div>
                            )}
                          </div>
                        ) : (
                          /* Submission form */
                          !isPastDue && (
                            <div style={{ marginTop: '0.5rem' }}>
                              <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem', fontFamily: 'var(--font-heading)' }}>
                                {t('student.homework.submitYourWork')}
                              </div>
                              <textarea
                                className="form-control"
                                placeholder={t('student.homework.typeAnswer')}
                                value={submitting === h.id ? submitText : (expanded === h.id ? submitText : '')}
                                onChange={(e) => setSubmitText(e.target.value)}
                                rows={4}
                                style={{ marginBottom: '0.5rem', fontSize: '0.85rem' }}
                              />
                              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                                <label style={{ fontSize: '0.8rem', cursor: 'pointer', color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                                  📎 {t('student.homework.attachFile')}
                                  <input
                                    type="file"
                                    style={{ display: 'none' }}
                                    onChange={(e) => setSubmitFile(e.target.files[0])}
                                  />
                                </label>
                                {submitFile && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{submitFile.name}</span>}
                                <div style={{ flex: 1 }} />
                                <button
                                  className="btn btn-primary btn-sm"
                                  onClick={() => handleSubmit(h.id)}
                                  disabled={submitting === h.id || (!submitText.trim() && !submitFile)}
                                >
                                  {submitting === h.id ? t('common.submitting') : t('common.submit')}
                                </button>
                              </div>
                            </div>
                          )
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
