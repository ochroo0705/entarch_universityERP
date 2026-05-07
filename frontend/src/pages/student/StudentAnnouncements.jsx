import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getAnnouncements } from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';

const priorityBadge = {
  low: 'badge-info',
  normal: 'badge-success',
  high: 'badge-warning',
  urgent: 'badge-danger',
};

const priorityLabel = (priority, t) => t(`common.priority.${(priority || 'normal').toLowerCase()}`);

export default function StudentAnnouncements() {
  const { t } = useTranslation();
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const { getField } = useEntityTranslations('announcement', announcements);

  useEffect(() => {
    getAnnouncements()
      .then((res) => {
        const all = res.data || [];
        const relevant = all.filter(
          (a) => !a.targetRoleFlags || (a.targetRoleFlags & 1) !== 0
        );
        setAnnouncements(relevant);
      })
      .catch(() => setAnnouncements([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading"><div className="spinner" />{t('common.loadingAnnouncements')}</div>;

  return (
    <div>
      <div className="page-header">
        <h1>{t('student.announcements.title')}</h1>
      </div>

      {announcements.length === 0 ? (
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>
            {t('student.announcements.noAnnouncements')}
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {announcements.map((a) => (
            <div key={a.id} className="card" style={{ cursor: 'pointer' }} onClick={() => setExpanded(expanded === a.id ? null : a.id)}>
              <div className="card-body">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.25rem' }}>
                      <span className={`badge ${priorityBadge[a.priority] || 'badge-info'}`}>
                        {priorityLabel(a.priority, t)}
                      </span>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {a.createdAt ? new Date(a.createdAt).toLocaleDateString() : ''}
                      </span>
                    </div>
                    <h3 style={{ fontSize: '1rem', marginBottom: '0.25rem' }}>{getField(a, 'title', a.title)}</h3>
                    {expanded !== a.id && (
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                        {(getField(a, 'content', a.content) || '').substring(0, 120)}{(getField(a, 'content', a.content) || '').length > 120 ? '...' : ''}
                      </div>
                    )}
                  </div>
                  <span style={{
                    color: 'var(--text-muted)', fontSize: '1.2rem',
                    transition: 'transform 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                    transform: expanded === a.id ? 'rotate(180deg)' : 'rotate(0deg)',
                    display: 'inline-block',
                  }}>
                    ▼
                  </span>
                </div>
                <div style={{
                  display: 'grid',
                  gridTemplateRows: expanded === a.id ? '1fr' : '0fr',
                  transition: 'grid-template-rows 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                }}>
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{
                      marginTop: '0.75rem', padding: '1rem', background: '#f8fafc',
                      borderRadius: '6px', whiteSpace: 'pre-wrap', lineHeight: 1.6, fontSize: '0.9rem',
                    }}>
                      {getField(a, 'content', a.content)}
                      {a.createdBy && (
                        <div style={{ marginTop: '0.75rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          — {a.createdBy.firstName} {a.createdBy.lastName}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
