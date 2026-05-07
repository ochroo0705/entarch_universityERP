import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { useChild } from '../../components/ChildSelector';
import { getAnnouncements, getMyChildrenDashboard } from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import AnnouncementList from '../../components/ui/AnnouncementList';
import { ErrorState, LoadingState } from '../../components/ui/StateBlock';

export default function ParentDashboard() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { children: kids, setSelectedChild } = useChild();
  const [dashboard, setDashboard] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { getField } = useEntityTranslations('announcement', announcements);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [dashRes, annRes] = await Promise.all([getMyChildrenDashboard(), getAnnouncements()]);
      setDashboard(dashRes.data || []);
      setAnnouncements(((annRes.data || []).filter((item) => !item.targetRoleFlags || (item.targetRoleFlags & 4))).slice(0, 5));
    } catch (err) {
      console.error('Failed to load parent dashboard', err);
      setError('dashboard');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const totals = useMemo(() => {
    const childCount = dashboard.length;
    const avgAttendance = childCount ? Math.round(dashboard.reduce((sum, item) => sum + (item.attendanceRatePercent || 0), 0) / childCount) : 0;
    const avgGpa = childCount ? (dashboard.reduce((sum, item) => sum + (item.overallGpa || 0), 0) / childCount).toFixed(1) : '-';
    const pendingHomework = dashboard.reduce((sum, item) => sum + ((item.homeworkTotal || 0) - (item.homeworkSubmitted || 0)), 0);
    return { childCount, avgAttendance, avgGpa, pendingHomework };
  }, [dashboard]);

  if (loading) return <LoadingState label={t('common.loadingDashboard')} />;

  if (error) {
    return (
      <ErrorState
        title="Unable to load the parent dashboard"
        description="The child overview or announcements could not be loaded."
        retryLabel="Retry"
        onRetry={loadDashboard}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Parent overview</div>
          <h1>{t('parent.dashboard.welcome', { name: user.firstName })}</h1>
          <p className="page-summary">Keep the active child in view, spot attendance or homework issues early, and jump straight to the next page you need.</p>
        </div>
      </div>
      <div className="stats-grid">
        <StatCard icon={'\u{1F468}\u200D\u{1F469}\u200D\u{1F467}\u200D\u{1F466}'} tone="classes" value={totals.childCount} label={totals.childCount === 1 ? t('parent.dashboard.child') : t('parent.dashboard.children')} />
        <StatCard icon={'\u2705'} tone="students" value={`${totals.avgAttendance}%`} label={t('parent.dashboard.avgAttendance')} />
        <StatCard icon={'\u{1F4CA}'} tone="teachers" value={totals.avgGpa} label={t('parent.dashboard.avgGPA')} />
        <StatCard icon={'\u{1F4DD}'} tone="assignments" value={totals.pendingHomework} label={t('parent.dashboard.pendingHomework')} />
      </div>

      <div className="panel-grid-two">
        <SectionCard title={t('parent.dashboard.childrenOverview')}>
          <div className="interactive-list">
            {dashboard.map((child) => {
              const pending = (child.homeworkTotal || 0) - (child.homeworkSubmitted || 0);
              return (
                <div key={child.studentId} className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                  <div className="interactive-card-main">
                    <div className="interactive-card-title">{child.firstName} {child.lastName}</div>
                    <div className="interactive-card-meta">
                      Attendance {child.attendanceRatePercent || 0}% / GPA {child.overallGpa ? child.overallGpa.toFixed(1) : '-'} / {child.classesToday || 0} classes today
                    </div>
                  </div>
                  <div className="parent-action-row">
                    <Link to="/parent/grades" className="btn btn-secondary btn-sm" onClick={() => {
                      const kid = kids.find((item) => item.id === child.studentId);
                      if (kid) setSelectedChild(kid);
                    }}>
                      Grades
                    </Link>
                    <Link to="/parent/attendance" className="btn btn-secondary btn-sm" onClick={() => {
                      const kid = kids.find((item) => item.id === child.studentId);
                      if (kid) setSelectedChild(kid);
                    }}>
                      Attendance
                    </Link>
                    <Link to="/parent/schedule" className="btn btn-secondary btn-sm" onClick={() => {
                      const kid = kids.find((item) => item.id === child.studentId);
                      if (kid) setSelectedChild(kid);
                    }}>
                      Schedule
                    </Link>
                    {pending > 0 ? <span className="badge badge-warning">{pending} pending</span> : null}
                  </div>
                </div>
              );
            })}
          </div>
        </SectionCard>

        <SectionCard
          title={t('parent.dashboard.recentAnnouncements')}
          action={<Link to="/parent/announcements" className="btn btn-ghost btn-sm">{t('parent.dashboard.viewAll')}</Link>}
        >
          <AnnouncementList
            items={announcements}
            linkBase=""
            getTitle={(item) => getField(item, 'title', item.title)}
            getContent={(item) => {
              const content = getField(item, 'content', item.content) || '';
              return `${content.substring(0, 100)}${content.length > 100 ? '...' : ''}`;
            }}
            emptyLabel={t('parent.dashboard.noAnnouncements')}
          />
        </SectionCard>
      </div>
    </div>
  );
}
