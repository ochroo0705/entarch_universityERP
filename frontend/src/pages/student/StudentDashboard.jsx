import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import {
  getAnnouncements,
  getEnrollments,
  getHomeworkForStudent,
  getStudentAttendance,
  getStudentGrades,
  getStudentSchedule,
} from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import AnnouncementList from '../../components/ui/AnnouncementList';
import { ErrorState, LoadingState } from '../../components/ui/StateBlock';
import { isAttendanceCountedAsPresent, isHomeworkSubmitted } from '../../utils/studentProgress';
import { buildStudentSubjects } from '../../utils/studentSubjects';

export default function StudentDashboard() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const dayNames = useMemo(() => ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday')], [t]);
  const [schedule, setSchedule] = useState([]);
  const [homework, setHomework] = useState([]);
  const [grades, setGrades] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { getField } = useEntityTranslations('announcement', announcements);

  const loadDashboard = useCallback(async () => {
    const now = new Date();
    const yearStart = `${now.getFullYear()}-01-01`;
    const yearEnd = `${now.getFullYear()}-12-31`;

    setLoading(true);
    setError('');
    try {
      const [schedRes, hwRes, gradeRes, attRes, annRes, enrRes] = await Promise.all([
        getStudentSchedule(user.userId),
        getHomeworkForStudent(),
        getStudentGrades(user.userId),
        getStudentAttendance(user.userId, yearStart, yearEnd),
        getAnnouncements(),
        getEnrollments(),
      ]);

      const homeworkItems = hwRes.data || [];
      setSchedule(schedRes.data || []);
      setHomework(homeworkItems);
      setGrades(gradeRes.data || []);
      setAttendance(attRes.data || []);
      setAnnouncements(((annRes.data || []).filter((item) => !item.targetRoleFlags || (item.targetRoleFlags & 1))).slice(0, 5));
      setEnrollments((enrRes.data || []).filter((item) => item.student?.id === user.userId && item.status?.toLowerCase() === 'active'));
    } catch (err) {
      console.error('Failed to load student dashboard', err);
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

  const now = new Date();
  const todaySchedule = useMemo(
    () => schedule.filter((item) => item.dayOfWeek === todayIndex).sort((a, b) => a.periodNumber - b.periodNumber),
    [schedule, todayIndex]
  );
  const pendingHomework = useMemo(
    () => homework
      .filter((item) => new Date(item.dueDate) >= now && !isHomeworkSubmitted(item.submission))
      .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
      .slice(0, 5),
    [homework, now]
  );
  const subjects = useMemo(
    () => buildStudentSubjects({ schedule, homework, attendance }),
    [schedule, homework, attendance]
  );
  const attendanceRate = useMemo(() => {
    const attendedCount = attendance.filter((item) => isAttendanceCountedAsPresent(item.status)).length;
    return attendance.length ? Math.round((attendedCount / attendance.length) * 100) : 100;
  }, [attendance]);
  const avgGrade = useMemo(() => {
    const values = grades.filter((item) => item.gradeValue != null).map((item) => item.gradeValue);
    return values.length ? Math.round(values.reduce((total, value) => total + value, 0) / values.length) : null;
  }, [grades]);

  if (loading) return <LoadingState label={t('common.loadingDashboard')} />;

  if (error) {
    return (
      <ErrorState
        title="Unable to load the student dashboard"
        description="Your schedule, homework, grades, or announcements could not be loaded."
        retryLabel="Retry"
        onRetry={loadDashboard}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Student overview</div>
          <h1>{t('student.dashboard.welcome', { name: user.firstName })}</h1>
          <p className="page-summary">See today's lessons first, then keep an eye on urgent homework, attendance, and school updates.</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F3EB}'} tone="classes" value={enrollments.length} label={t('student.dashboard.enrolledClasses')} />
        <StatCard icon={'\u{1F4DD}'} tone="assignments" value={pendingHomework.length} label={t('student.dashboard.pendingHomework')} />
        <StatCard icon={'\u2705'} tone="students" value={`${attendanceRate}%`} label={t('student.dashboard.attendanceRate')} />
        <StatCard icon={'\u{1F4CA}'} tone="teachers" value={avgGrade != null ? avgGrade : '-'} label={t('student.dashboard.avgGrade')} />
      </div>

      <div className="panel-grid-two">
        <SectionCard
          title={`Today's Schedule - ${dayNames[todayIndex] || t('student.dashboard.today')}`}
          action={<Link to="/student/schedule" className="btn btn-ghost btn-sm">{t('common.viewAll')}</Link>}
        >
          {todaySchedule.length ? (
            <div className="interactive-list">
              {todaySchedule.map((item) => (
                <Link key={item.scheduleId || `${item.dayOfWeek}-${item.periodNumber}`} to="/student/schedule" className="interactive-card-link">
                  <div className="interactive-card-main">
                    <div className="interactive-card-title">{item.subject}</div>
                    <div className="interactive-card-meta">
                      {item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)} / {item.className}{item.roomNumber ? ` / Room ${item.roomNumber}` : ''}
                    </div>
                  </div>
                  <span className="badge badge-info">P{item.periodNumber}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="muted-copy">{t('student.dashboard.noClassesToday')}</p>
          )}
        </SectionCard>

        <SectionCard
          title={t('student.dashboard.upcomingHomework')}
          action={<Link to="/student/homework" className="btn btn-ghost btn-sm">{t('common.viewAll')}</Link>}
        >
          {pendingHomework.length ? (
            <div className="interactive-list">
              {pendingHomework.map((item) => {
                const due = new Date(item.dueDate);
                const daysLeft = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
                return (
                  <Link key={item.id} to={`/student/homework?hw=${item.id}`} className={`interactive-card-link${daysLeft <= 1 ? ' is-urgent' : ''}`}>
                    <div className="interactive-card-main">
                      <div className="interactive-card-title">{item.title}</div>
                      <div className="interactive-card-meta">Due {due.toLocaleDateString()}</div>
                    </div>
                    <span className={`badge ${daysLeft <= 1 ? 'badge-danger' : daysLeft <= 3 ? 'badge-warning' : 'badge-info'}`}>
                      {daysLeft <= 0 ? t('student.dashboard.dueToday') : t('student.dashboard.daysLeft', { count: daysLeft })}
                    </span>
                  </Link>
                );
              })}
            </div>
          ) : (
            <p className="muted-copy">{t('student.dashboard.noPendingHomework')}</p>
          )}
        </SectionCard>
      </div>

      <div className="panel-grid-two" style={{ marginTop: '1rem' }}>
        <SectionCard
          title={t('student.subjects.title')}
          action={<Link to="/student/subjects" className="btn btn-ghost btn-sm">{t('common.viewAll')}</Link>}
        >
          {subjects.length ? (
            <div className="interactive-list">
              {subjects.map((subject) => {
                const pendingSubjectHomework = subject.homeworkItems.filter(
                  (item) => new Date(item.dueDate) >= now && !isHomeworkSubmitted(item.submission)
                ).length;

                return (
                  <Link
                    key={subject.slug}
                    to={`/student/subjects/${subject.slug}`}
                    className={`interactive-card-link${pendingSubjectHomework > 0 ? ' is-urgent' : ''}`}
                  >
                    <div className="interactive-card-main">
                      <div className="interactive-card-title">{subject.name}</div>
                      <div className="interactive-card-meta">
                        {subject.teacherNames[0] || subject.classNames[0] || t('student.subjects.noClassesToday')}
                      </div>
                    </div>
                    <span className={`badge ${pendingSubjectHomework > 0 ? 'badge-warning' : 'badge-info'}`}>
                      {pendingSubjectHomework}
                    </span>
                  </Link>
                );
              })}
            </div>
          ) : (
            <p className="muted-copy">{t('student.subjects.empty')}</p>
          )}
        </SectionCard>

        <SectionCard
          title={t('student.dashboard.recentAnnouncements')}
          action={<Link to="/student/announcements" className="btn btn-ghost btn-sm">{t('common.viewAll')}</Link>}
        >
          <AnnouncementList
            items={announcements}
            linkBase=""
            getTitle={(item) => getField(item, 'title', item.title)}
            getContent={(item) => {
              const content = getField(item, 'content', item.content) || '';
              return `${content.substring(0, 100)}${content.length > 100 ? '...' : ''}`;
            }}
            emptyLabel={t('student.dashboard.noAnnouncements')}
          />
        </SectionCard>
      </div>
    </div>
  );
}
