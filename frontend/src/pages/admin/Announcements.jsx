import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { deleteAnnouncement, getAnnouncements } from '../../api/endpoints';
import SelectMenu from '../../components/ui/SelectMenu';
import { AdminFilterRowSkeleton, AdminPageHeaderSkeleton, AdminTableSkeleton } from '../../components/ui/AdminPageSkeletons';
import useEntityTranslations from '../../hooks/useEntityTranslations';

const priorityBadge = {
  low: 'badge-info',
  normal: 'badge-success',
  high: 'badge-warning',
  urgent: 'badge-danger',
};

const priorityLabel = (priority, t) => t(`common.priority.${(priority || 'normal').toLowerCase()}`);

export default function Announcements() {
  const { t } = useTranslation();

  const roleLabel = useCallback((flags) => {
    const roles = [];
    if (flags & 1) roles.push(t('roles.student'));
    if (flags & 2) roles.push(t('roles.teacher'));
    if (flags & 4) roles.push(t('roles.parent'));
    if (flags & 8) roles.push(t('roles.admin'));
    return roles.join(', ') || t('common.all');
  }, [t]);

  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('all');
  const navigate = useNavigate();
  const { getField } = useEntityTranslations('announcement', announcements);
  const priorityOptions = [
    { value: 'all', label: t('admin.announcements.allPriorities') },
    { value: 'low', label: t('admin.announcements.low') },
    { value: 'normal', label: t('admin.announcements.normal') },
    { value: 'high', label: t('admin.announcements.high') },
    { value: 'urgent', label: t('admin.announcements.urgent') },
  ];

  useEffect(() => {
    getAnnouncements()
      .then((res) => setAnnouncements(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const filtered = announcements.filter((announcement) => {
    if (priorityFilter !== 'all' && announcement.priority !== priorityFilter) return false;
    if (search.trim()) {
      const query = search.toLowerCase();
      const title = getField(announcement, 'title', announcement.title);
      const content = getField(announcement, 'content', announcement.content);
      return title?.toLowerCase().includes(query) || content?.toLowerCase().includes(query);
    }
    return true;
  });

  if (loading) {
    return (
      <div className="content-stack">
        <AdminPageHeaderSkeleton />
        <AdminFilterRowSkeleton fields={2} />
        <AdminTableSkeleton columns={5} rows={6} mobileCards={4} />
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>{t('admin.announcements.title')}</h1>
        <Link to="/admin/announcements/create" className="btn btn-primary">{t('admin.announcements.newAnnouncement')}</Link>
      </div>

      <div className="list-filter-row">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.announcements.searchPlaceholder')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <div className="list-filter-select">
          <SelectMenu options={priorityOptions} value={priorityFilter} onChange={setPriorityFilter} placeholder={t('admin.announcements.allPriorities')} />
        </div>
        <span className="list-filter-count">
          {t('common.announcement', { count: filtered.length })}
        </span>
      </div>

      <div className="card">
        <div className="table-container desktop-table">
          <table>
            <thead>
              <tr>
                <th>{t('common.id')}</th>
                <th>{t('admin.announcements.titleCol')}</th>
                <th>{t('admin.announcements.priority')}</th>
                <th>{t('admin.announcements.targetAudience')}</th>
                <th>{t('admin.announcements.created')}</th>
                <th style={{ width: '80px' }}>{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('admin.announcements.noAnnouncements')}</td></tr>
              ) : (
                filtered.map((announcement) => (
                  <tr key={announcement.id} onClick={() => navigate(`/admin/announcements/${announcement.id}`)} style={{ cursor: 'pointer' }}>
                    <td>{announcement.id}</td>
                    <td>
                      <div style={{ fontWeight: 600 }}>{getField(announcement, 'title', announcement.title)}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {(getField(announcement, 'content', announcement.content) || '').substring(0, 100)}{(getField(announcement, 'content', announcement.content) || '').length > 100 ? '...' : ''}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${priorityBadge[announcement.priority] || 'badge-info'}`}>
                        {priorityLabel(announcement.priority, t)}
                      </span>
                    </td>
                    <td>{roleLabel(announcement.targetRoleFlags)}</td>
                    <td>{announcement.createdAt ? new Date(announcement.createdAt).toLocaleDateString() : '-'}</td>
                    <td>
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (window.confirm(t('admin.announcements.confirmDelete'))) {
                            deleteAnnouncement(announcement.id).then(() => {
                              setAnnouncements((prev) => prev.filter((item) => item.id !== announcement.id));
                            }).catch(() => alert('Failed to delete'));
                          }
                        }}
                      >
                        {t('common.delete')}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="card-body mobile-card-list">
          {filtered.length === 0 ? (
            <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('admin.announcements.noAnnouncements')}</div>
          ) : filtered.map((announcement) => (
            <article key={`mobile-${announcement.id}`} className="data-card" onClick={() => navigate(`/admin/announcements/${announcement.id}`)} style={{ cursor: 'pointer' }}>
              <div className="data-card-header">
                <div>
                  <div className="data-card-title">{getField(announcement, 'title', announcement.title)}</div>
                  <div className="muted-copy">{announcement.createdAt ? new Date(announcement.createdAt).toLocaleDateString() : '-'}</div>
                </div>
                <span className={`badge ${priorityBadge[announcement.priority] || 'badge-info'}`}>
                  {priorityLabel(announcement.priority, t)}
                </span>
              </div>

              <div className="data-card-meta">
                <div className="data-card-meta-row">
                  <span>{t('common.id')}</span>
                  <strong>{announcement.id}</strong>
                </div>
                <div className="data-card-meta-row">
                  <span>{t('admin.announcements.targetAudience')}</span>
                  <strong>{roleLabel(announcement.targetRoleFlags)}</strong>
                </div>
                <div className="data-card-meta-row">
                  <span>{t('admin.announcements.titleCol')}</span>
                  <strong>{(getField(announcement, 'content', announcement.content) || '').substring(0, 120)}{(getField(announcement, 'content', announcement.content) || '').length > 120 ? '...' : ''}</strong>
                </div>
              </div>

              <button
                className="btn btn-danger btn-block"
                onClick={(e) => {
                  e.stopPropagation();
                  if (window.confirm(t('admin.announcements.confirmDelete'))) {
                    deleteAnnouncement(announcement.id).then(() => {
                      setAnnouncements((prev) => prev.filter((item) => item.id !== announcement.id));
                    }).catch(() => alert('Failed to delete'));
                  }
                }}
              >
                {t('common.delete')}
              </button>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
