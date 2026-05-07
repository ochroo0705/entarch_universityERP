import { useEffect, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import ChildSelector, { useChild } from '../../components/ChildSelector';
import { getHomeworkForStudentId, downloadFileAuthenticated } from '../../api/endpoints';
import { ParentHomeworkSkeleton } from '../../components/ui/ParentPageSkeletons';
import HomeworkAttachmentViewer from '../../components/HomeworkAttachmentViewer';

const TYPE_BADGES = {
  HOMEWORK: 'badge-info',
  PROJECT: 'badge-warning',
  QUIZ: 'badge-success',
  TEST: 'badge-danger',
};

export default function ParentHomework() {
  const { t } = useTranslation();
  const { selectedChild, loading: childLoading } = useChild();
  const [homework, setHomework] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    if (!selectedChild) return;

    setLoading(true);
    getHomeworkForStudentId(selectedChild.id)
      .then((res) => setHomework(res.data || []))
      .catch(() => setHomework([]))
      .finally(() => setLoading(false));
  }, [selectedChild?.id]);

  const now = new Date();
  const { filtered, pendingCount, submittedCount, gradedCount } = useMemo(() => {
    const nextFiltered = homework.filter((item) => {
      const sub = item.submission;
      const isPastDue = new Date(item.dueDate) < now;
      if (filter === 'pending') return !sub && !isPastDue;
      if (filter === 'submitted') return sub && sub.status !== 'graded';
      if (filter === 'graded') return sub && sub.status === 'graded';
      if (filter === 'overdue') return !sub && isPastDue;
      return true;
    });

    const nextPendingCount = homework.filter((item) => !item.submission && new Date(item.dueDate) >= now).length;
    const nextSubmittedCount = homework.filter((item) => item.submission && item.submission.status !== 'graded').length;
    const nextGradedCount = homework.filter((item) => item.submission?.status === 'graded').length;

    return {
      filtered: nextFiltered,
      pendingCount: nextPendingCount,
      submittedCount: nextSubmittedCount,
      gradedCount: nextGradedCount,
    };
  }, [homework, filter, now]);

  if (childLoading) return <div className="loading"><div className="spinner" />{t('common.loading')}</div>;
  if (!selectedChild) return <div className="card"><div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>{t('parent.homework.noChildren')}</div></div>;

  const childName = `${selectedChild.firstName} ${selectedChild.lastName}`;

  return (
    <div>
      <div className="page-header"><h1>{t('parent.homework.title', { name: childName })}</h1></div>
      <ChildSelector />

      <div className="content-stack" aria-busy={loading}>
        {loading ? (
          <ParentHomeworkSkeleton />
        ) : (
          <>
            <div className="stats-grid" style={{ marginBottom: '1.5rem' }}>
              <div className="stat-card" onClick={() => setFilter('all')} style={{ cursor: 'pointer', outline: filter === 'all' ? '2px solid var(--primary)' : 'none' }}>
                <div className="stat-icon classes">{'\u{1F4CB}'}</div>
                <div className="stat-info"><h3>{homework.length}</h3><p>{t('parent.homework.total')}</p></div>
              </div>
              <div className="stat-card" onClick={() => setFilter('pending')} style={{ cursor: 'pointer', outline: filter === 'pending' ? '2px solid var(--primary)' : 'none' }}>
                <div className="stat-icon assignments">{'\u{1F4DD}'}</div>
                <div className="stat-info"><h3>{pendingCount}</h3><p>{t('parent.homework.pending')}</p></div>
              </div>
              <div className="stat-card" onClick={() => setFilter('submitted')} style={{ cursor: 'pointer', outline: filter === 'submitted' ? '2px solid var(--primary)' : 'none' }}>
                <div className="stat-icon teachers">{'\u{1F4E4}'}</div>
                <div className="stat-info"><h3>{submittedCount}</h3><p>{t('parent.homework.submitted')}</p></div>
              </div>
              <div className="stat-card" onClick={() => setFilter('graded')} style={{ cursor: 'pointer', outline: filter === 'graded' ? '2px solid var(--primary)' : 'none' }}>
                <div className="stat-icon students">{'\u2705'}</div>
                <div className="stat-info"><h3>{gradedCount}</h3><p>{t('parent.homework.graded')}</p></div>
              </div>
            </div>

            {filtered.length === 0 ? (
              <div className="card">
                <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>
                  {t('parent.homework.noHomework')}
                </div>
              </div>
            ) : (
              <div className="parent-stack">
                {filtered.map((item, index) => {
                  const sub = item.submission;
                  const due = new Date(item.dueDate);
                  const isPastDue = due < now;
                  const daysLeft = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
                  const isExpanded = expanded === item.id;

                  return (
                    <div key={item.id} className="card" style={{ animation: `fadeInUp 0.35s cubic-bezier(0.16,1,0.3,1) ${index * 0.04}s both` }}>
                      <div className="card-body">
                        <div
                          style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem', flexWrap: 'wrap' }}
                          onClick={() => setExpanded(isExpanded ? null : item.id)}
                        >
                          <div style={{ flex: 1 }}>
                            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.35rem', flexWrap: 'wrap' }}>
                              <span className={`badge ${TYPE_BADGES[item.type] || 'badge-info'}`}>
                                {item.type || 'HOMEWORK'}
                              </span>
                              {sub ? (
                                <span className={`badge ${sub.status === 'graded' ? 'badge-success' : 'badge-warning'}`}>
                                  {sub.status === 'graded' ? t('parent.homework.gradedScore', { score: `${sub.score}${item.maxScore ? `/${item.maxScore}` : ''}` }) : t('parent.homework.submitted')}
                                </span>
                              ) : isPastDue ? (
                                <span className="badge badge-danger">{t('parent.homework.overdue')}</span>
                              ) : (
                                <span className="badge" style={{ background: 'var(--primary-light)', color: 'var(--primary)' }}>
                                  {daysLeft <= 0 ? t('parent.homework.dueToday') : t('parent.homework.daysLeft', { count: daysLeft })}
                                </span>
                              )}
                            </div>
                            <h3 style={{ fontSize: '1rem', marginBottom: '0.2rem' }}>{item.title}</h3>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                              {t('parent.homework.due')}: {due.toLocaleDateString()}
                            </div>
                          </div>
                          <span
                            style={{
                              color: 'var(--text-muted)',
                              fontSize: '1.2rem',
                              transition: 'transform 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                              transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)',
                              display: 'inline-block',
                            }}
                          >
                            {'\u25BC'}
                          </span>
                        </div>

                        <div
                          style={{
                            display: 'grid',
                            gridTemplateRows: isExpanded ? '1fr' : '0fr',
                            transition: 'grid-template-rows 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                          }}
                        >
                          <div style={{ overflow: 'hidden' }}>
                            <div style={{ marginTop: '0.75rem', padding: '1rem', background: '#f8fafc', borderRadius: '6px' }}>
                              {item.description && (
                                <div style={{ marginBottom: '1rem', whiteSpace: 'pre-wrap', lineHeight: 1.6, fontSize: '0.9rem' }}>
                                  {item.description}
                                </div>
                              )}

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

                              {sub ? (
                                <div style={{ padding: '0.75rem', background: 'white', borderRadius: '6px', border: '1px solid var(--border)' }}>
                                  <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem', fontFamily: 'var(--font-heading)' }}>
                                    {t('parent.homework.submissionTitle', { name: childName })}
                                  </div>
                                  {sub.submissionText && (
                                    <div style={{ fontSize: '0.85rem', marginBottom: '0.5rem', whiteSpace: 'pre-wrap' }}>{sub.submissionText}</div>
                                  )}
                                  {sub.attachmentUrl && (
                                    <a
                                      href="#"
                                      onClick={(event) => {
                                        event.preventDefault();
                                        downloadFileAuthenticated(sub.attachmentUrl);
                                      }}
                                      style={{ fontSize: '0.8rem', color: 'var(--primary)', cursor: 'pointer' }}
                                    >
                                      {'\u{1F4CE}'} {t('parent.homework.attachedFile')}
                                    </a>
                                  )}
                                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                                    {t('parent.homework.submittedAt')}: {sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '-'}
                                  </div>
                                  {sub.status === 'graded' && (
                                    <div style={{ marginTop: '0.75rem', padding: '0.6rem', background: 'var(--primary-light)', borderRadius: '6px' }}>
                                      <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--primary)' }}>
                                        {t('parent.homework.score')}: {sub.score}{item.maxScore ? ` / ${item.maxScore}` : ''}
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
                                <div style={{ padding: '0.75rem', background: 'white', borderRadius: '6px', border: '1px solid var(--border)', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                                  {isPastDue
                                    ? t('parent.homework.notSubmittedOverdue', { name: childName })
                                    : t('parent.homework.notSubmitted', { name: childName })}
                                </div>
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
          </>
        )}
      </div>
    </div>
  );
}
