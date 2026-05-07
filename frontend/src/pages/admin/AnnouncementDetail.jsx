import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getAnnouncementById, deleteAnnouncement } from '../../api/endpoints';
import { AdminDetailSkeleton } from '../../components/ui/AdminPageSkeletons';
import useEntityTranslations from '../../hooks/useEntityTranslations';

const priorityBadge = {
  low: 'badge-info',
  normal: 'badge-success',
  high: 'badge-warning',
  urgent: 'badge-danger',
};

const priorityLabel = (priority, t) => t(`common.priority.${(priority || 'normal').toLowerCase()}`);

export default function AnnouncementDetail() {
  const { t } = useTranslation();

  const roleLabel = (flags) => {
    const roles = [];
    if (flags & 1) roles.push(t('roles.student'));
    if (flags & 2) roles.push(t('roles.teacher'));
    if (flags & 4) roles.push(t('roles.parent'));
    if (flags & 8) roles.push(t('roles.admin'));
    return roles.join(', ') || t('common.all');
  };

  const { id } = useParams();
  const navigate = useNavigate();
  const [announcement, setAnnouncement] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { getField } = useEntityTranslations('announcement', announcement ? [announcement] : []);

  useEffect(() => {
    getAnnouncementById(id)
      .then((res) => setAnnouncement(res.data))
      .catch(() => setError('Failed to load announcement.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = () => {
    if (!window.confirm(t('admin.announcementDetail.confirmDelete'))) return;
    deleteAnnouncement(id)
      .then(() => navigate('/admin/announcements'))
      .catch(() => alert('Failed to delete announcement.'));
  };

  if (loading) {
    return <AdminDetailSkeleton includeContent />;
  }

  if (error) {
    return <div className="alert alert-error">{error}</div>;
  }

  if (!announcement) {
    return <div className="alert alert-error">{t('admin.announcementDetail.notFound')}</div>;
  }

  const a = announcement;

  return (
    <div>
      <div className="page-header">
        <h1>{t('admin.announcementDetail.title')}</h1>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn btn-danger" onClick={handleDelete}>{t('common.delete')}</button>
          <Link to="/admin/announcements" className="btn btn-secondary">{t('common.back')}</Link>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="card-body">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <span className={`badge ${priorityBadge[a.priority] || 'badge-info'}`} style={{ fontSize: '0.75rem' }}>
              {priorityLabel(a.priority, t)}
            </span>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              ID: {a.id}
            </span>
          </div>

          <h2 style={{ fontSize: '1.35rem', marginBottom: '0.75rem' }}>{getField(a, 'title', a.title)}</h2>

          <div
            style={{
              background: '#f8fafc',
              border: '1px solid var(--border)',
              borderRadius: '8px',
              padding: '1.25rem',
              marginBottom: '1.5rem',
              lineHeight: 1.7,
              whiteSpace: 'pre-wrap',
            }}
          >
            {getField(a, 'content', a.content) || t('admin.announcementDetail.noContent')}
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: '1rem',
            }}
          >
            <InfoField label={t('admin.announcementDetail.targetAudience')} value={roleLabel(a.targetRoleFlags)} />
            <InfoField
              label={t('admin.announcementDetail.targetClass')}
              value={a.targetClass ? `${a.targetClass.className || a.targetClass.name || ''} (ID: ${a.targetClass.id})` : t('admin.announcementDetail.allClasses')}
            />
            <InfoField
              label={t('admin.announcementDetail.createdBy')}
              value={a.createdBy ? `${a.createdBy.firstName} ${a.createdBy.lastName}` : '-'}
            />
            <InfoField
              label={t('admin.announcementDetail.createdAt')}
              value={a.createdAt ? new Date(a.createdAt).toLocaleString() : '-'}
            />
            <InfoField
              label={t('admin.announcementDetail.expiresAt')}
              value={a.expiresAt ? new Date(a.expiresAt).toLocaleString() : t('admin.announcementDetail.never')}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

function InfoField({ label, value }) {
  return (
    <div>
      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', marginBottom: '0.15rem' }}>
        {label}
      </div>
      <div style={{ fontSize: '0.9rem' }}>{value || '-'}</div>
    </div>
  );
}
