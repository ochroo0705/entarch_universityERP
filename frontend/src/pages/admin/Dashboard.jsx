import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getAnnouncements, getStats } from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';
import SectionCard from '../../components/ui/SectionCard';
import StatCard from '../../components/ui/StatCard';
import { AdminDashboardSkeleton } from '../../components/ui/AdminPageSkeletons';
import QuickActionTile from '../../components/ui/QuickActionTile';
import AnnouncementList from '../../components/ui/AnnouncementList';
import { EmptyState, ErrorState } from '../../components/ui/StateBlock';

export default function Dashboard() {
  const { t } = useTranslation();
  const [stats, setStats] = useState(null);
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { getField } = useEntityTranslations('announcement', announcements);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [statsRes, annRes] = await Promise.all([getStats(), getAnnouncements()]);
      setStats(statsRes.data);
      setAnnouncements((annRes.data || []).slice(0, 5));
    } catch (err) {
      console.error('Failed to load dashboard', err);
      setError('dashboard');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  if (loading) return <AdminDashboardSkeleton />;

  if (error) {
    return (
      <ErrorState
        title="Unable to load the admin dashboard"
        description="The overview widgets or announcements could not be fetched. Try again to refresh the page data."
        retryLabel="Retry"
        onRetry={loadDashboard}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Admin overview</div>
          <h1>{t('admin.dashboard.title')}</h1>
          <p className="page-summary">Monitor core school activity, jump into frequent actions, and keep the latest announcements within reach.</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F468}\u200D\u{1F3EB}'} tone="teachers" value={stats?.teacherCount ?? 0} label={t('admin.dashboard.teachers')} />
        <StatCard icon={'\u{1F393}'} tone="students" value={stats?.studentCount ?? 0} label={t('admin.dashboard.students')} />
        <StatCard icon={'\u{1F3EB}'} tone="classes" value={stats?.classCount ?? 0} label={t('admin.dashboard.classes')} />
        <StatCard icon={'\u{1F4CB}'} tone="assignments" value={stats?.teachingAssignmentCount ?? 0} label={t('admin.dashboard.teachingAssignments')} />
      </div>

      <div className="panel-grid-two">
        <SectionCard title={t('admin.dashboard.quickActions')} subtitle="Create or update the most common school records from one place.">
          <div className="quick-action-grid">
            <QuickActionTile to="/admin/users/create" icon={'\u{1F464}'} label={t('admin.dashboard.createUser')} tone="#1A6B5C" />
            <QuickActionTile to="/admin/classes/create" icon={'\u{1F3EB}'} label={t('admin.dashboard.createClass')} tone="#2D9F6F" />
            <QuickActionTile to="/admin/teaching-assignments/create" icon={'\u{1F4CB}'} label={t('admin.dashboard.assignTeacher')} tone="#3A7BCC" />
            <QuickActionTile to="/admin/schedules/create" icon={'\u{1F5D3}\uFE0F'} label={t('admin.dashboard.newSchedule')} tone="#D4972E" />
          </div>
          <div style={{ marginTop: '0.75rem' }}>
            <Link to="/admin/announcements/create" className="btn btn-primary btn-block">New Announcement</Link>
          </div>
        </SectionCard>

        <SectionCard title={t('admin.dashboard.recentAnnouncements')} action={<Link to="/admin/announcements" className="btn btn-ghost btn-sm">{t('common.viewAll')}</Link>}>
          {announcements.length ? (
            <AnnouncementList
              items={announcements}
              linkBase="/admin/announcements"
              getTitle={(item) => getField(item, 'title', item.title)}
              getContent={(item) => {
                const content = getField(item, 'content', item.content) || '';
                return `${content.substring(0, 90)}${content.length > 90 ? '...' : ''}`;
              }}
              emptyLabel={t('admin.dashboard.noAnnouncements')}
            />
          ) : (
            <EmptyState title={t('admin.dashboard.noAnnouncements')} />
          )}
        </SectionCard>
      </div>
    </div>
  );
}
