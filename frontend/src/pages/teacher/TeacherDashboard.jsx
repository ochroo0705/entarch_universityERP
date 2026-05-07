import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getAnnouncements, getMyTeachingAssignments, getTeacherSchedule } from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import AnnouncementList from '../../components/ui/AnnouncementList';
import { ErrorState, LoadingState } from '../../components/ui/StateBlock';

export default function TeacherDashboard() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const dayNames = useMemo(() => ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday')], [t]);
  const [schedule, setSchedule] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { getField } = useEntityTranslations('announcement', announcements);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [schedRes, taRes, annRes] = await Promise.all([
        getTeacherSchedule(user.userId),
        getMyTeachingAssignments(),
        getAnnouncements(),
      ]);
      setSchedule(schedRes.data || []);
      setAssignments(taRes.data || []);
      setAnnouncements(((annRes.data || []).filter((item) => !item.targetRoleFlags || (item.targetRoleFlags & 2))).slice(0, 5));
    } catch (err) {
      console.error('Failed to load teacher dashboard', err);
      setError('dashboard');
    } finally {
      setLoading(false);
    }
  }, [user.userId]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const todayIndex = useMemo(() => {
    const currentDay = new Date().getDay();
    return currentDay === 0 ? 1 : currentDay;
  }, []);
  const todaySchedule = useMemo(
    () => schedule.filter((item) => item.dayOfWeek === todayIndex).sort((a, b) => a.periodNumber - b.periodNumber),
    [schedule, todayIndex]
  );
  const uniqueClasses = useMemo(() => {
    const classMap = new Map();
    assignments.forEach((item) => {
      const classInfo = item.classInfo;
      if (classInfo && !classMap.has(classInfo.id)) classMap.set(classInfo.id, classInfo);
    });
    return [...classMap.values()];
  }, [assignments]);
  const subjectCount = useMemo(() => new Set(assignments.map((item) => item.subject?.name).filter(Boolean)).size, [assignments]);

  if (loading) return <LoadingState label={t('common.loadingDashboard')} />;

  if (error) {
    return (
      <ErrorState
        title="Unable to load the teacher dashboard"
        description="Your classes, schedule, or announcements could not be loaded."
        retryLabel="Retry"
        onRetry={loadDashboard}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{t('teacher.dashboard.welcome', { name: user.firstName })}</h1>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F3EB}'} tone="classes" value={uniqueClasses.length} label={t('teacher.dashboard.classes')} />
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={subjectCount} label={t('teacher.dashboard.subjects')} />
        <StatCard icon={'\u{1F5D3}\uFE0F'} tone="teachers" value={schedule.length} label={t('teacher.dashboard.weeklyPeriods')} />
        <StatCard icon={'\u{1F4CB}'} tone="students" value={todaySchedule.length} label={t('teacher.dashboard.todaysClasses')} />
      </div>

      <div className="panel-grid-two">
        <SectionCard
          title={`Today's Schedule - ${dayNames[todayIndex] || t('teacher.dashboard.today')}`}
          action={<Link to="/teacher/schedule" className="btn btn-ghost btn-sm">{t('teacher.dashboard.viewAll')}</Link>}
        >
          {todaySchedule.length ? (
            <div className="interactive-list">
              {todaySchedule.map((item) => (
                <Link key={item.scheduleId} to="/teacher/schedule" className="interactive-card-link">
                  <div className="interactive-card-main">
                    <div className="interactive-card-title">{item.subject}</div>
                    <div className="interactive-card-meta">
                      {item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)} / {item.className}{item.roomNumber ? ` / ${t('teacher.classDetail.room')} ${item.roomNumber}` : ''}
                    </div>
                  </div>
                  <span className="badge badge-info">{t('teacher.dashboard.periodBadge', { number: item.periodNumber })}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="muted-copy">{t('teacher.dashboard.noClassesToday')}</p>
          )}
        </SectionCard>

        <SectionCard
          title={t('teacher.dashboard.myClasses')}
          action={<Link to="/teacher/classes" className="btn btn-primary btn-sm">{t('teacher.dashboard.viewAll')}</Link>}
        >
          {uniqueClasses.length ? (
            <div className="interactive-list">
              {uniqueClasses.map((item) => (
                <Link key={item.id} to={`/teacher/classes/${item.id}`} className="interactive-card-link">
                  <div className="interactive-card-main">
                    <div className="interactive-card-title">{item.className}</div>
                    <div className="interactive-card-meta">{t('admin.classes.gradeN', { n: item.grade })} / {item.section}</div>
                  </div>
                  <span className="badge badge-info">{t('common.open')}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="muted-copy">{t('teacher.dashboard.noClassesAssigned')}</p>
          )}
        </SectionCard>
      </div>

      <div style={{ marginTop: '1rem' }}>
        <SectionCard
          title={t('teacher.dashboard.recentAnnouncements')}
          action={<Link to="/teacher/announcements" className="btn btn-ghost btn-sm">{t('teacher.dashboard.viewAll')}</Link>}
        >
          <AnnouncementList
            items={announcements}
            linkBase=""
            getTitle={(item) => getField(item, 'title', item.title)}
            getContent={(item) => {
              const content = getField(item, 'content', item.content) || '';
              return `${content.substring(0, 100)}${content.length > 100 ? '...' : ''}`;
            }}
            emptyLabel={t('teacher.dashboard.noAnnouncements')}
          />
        </SectionCard>
      </div>
    </div>
  );
}
