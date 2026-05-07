import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getHomeworkForStudent, getStudentAttendance, getStudentSchedule } from '../../api/endpoints';
import { buildStudentSubjects } from '../../utils/studentSubjects';
import { isAttendanceCountedAsPresent, isHomeworkSubmitted } from '../../utils/studentProgress';
import StatCard from '../../components/ui/StatCard';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

export default function StudentSubjects() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [schedule, setSchedule] = useState([]);
  const [homework, setHomework] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [loading, setLoading] = useState(true);

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

  const now = new Date();
  const todayIndex = now.getDay() === 0 ? 1 : now.getDay();
  const totalPendingHomework = useMemo(
    () => homework.filter((item) => new Date(item.dueDate) >= now && !isHomeworkSubmitted(item.submission)).length,
    [homework, now]
  );
  const attendanceRate = useMemo(() => {
    const attended = attendance.filter((item) => isAttendanceCountedAsPresent(item.status)).length;
    return attendance.length ? Math.round((attended / attendance.length) * 100) : 100;
  }, [attendance]);
  const todaysLessons = useMemo(
    () => schedule.filter((item) => item.dayOfWeek === todayIndex).length,
    [schedule, todayIndex]
  );

  if (loading) return <LoadingState label={t('common.loadingSubjects')} />;

  if (!subjects.length) {
    return <EmptyState title={t('student.subjects.title')} description={t('student.subjects.empty')} />;
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('sidebar.subjects')}</div>
          <h1>{t('student.subjects.title')}</h1>
          <p className="page-summary">{t('student.subjects.summary')}</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={subjects.length} label={t('student.schedule.subjects')} />
        <StatCard icon={'\u{1F4DD}'} tone="teachers" value={totalPendingHomework} label={t('student.subjects.pendingHomework')} />
        <StatCard icon={'\u2705'} tone="students" value={`${attendanceRate}%`} label={t('student.subjects.attendanceRate')} />
        <StatCard icon={'\u{1F4C5}'} tone="classes" value={todaysLessons} label={t('student.subjects.todayLessons')} />
      </div>

      <div className="content-stack">
        {subjects.map((subject, index) => {
          const subjectAttendanceRate = subject.attendanceItems.length
            ? Math.round((subject.attendanceItems.filter((item) => isAttendanceCountedAsPresent(item.status)).length / subject.attendanceItems.length) * 100)
            : 100;
          const pendingHomework = subject.homeworkItems.filter(
            (item) => new Date(item.dueDate) >= now && !isHomeworkSubmitted(item.submission)
          ).length;
          const nextDue = [...subject.homeworkItems]
            .filter((item) => new Date(item.dueDate) >= now)
            .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))[0];
          const todayLessonsForSubject = subject.scheduleItems.filter((item) => item.dayOfWeek === todayIndex);

          return (
            <article
              key={subject.slug || subject.name}
              className="card"
              style={{ animation: `fadeInUp 0.35s cubic-bezier(0.16,1,0.3,1) ${index * 0.05}s both` }}
            >
              <div className="card-body">
                <div className="section-header">
                  <div>
                    <h2 className="section-title">{subject.name}</h2>
                    <p className="section-subtitle">
                      {subject.teacherNames[0] || subject.classNames[0] || t('student.subjects.noClassesToday')}
                    </p>
                  </div>
                  <Link to={`/student/subjects/${subject.slug}`} className="btn btn-primary btn-sm">
                    {t('student.subjects.open')}
                  </Link>
                </div>

                <div className="stats-grid" style={{ marginBottom: '1rem' }}>
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
                    <div className="stat-info"><h3>{subjectAttendanceRate}%</h3><p>{t('student.subjects.attendanceRate')}</p></div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-icon classes">{'\u{1F4CB}'}</div>
                    <div className="stat-info"><h3>{pendingHomework}</h3><p>{t('student.subjects.pendingHomework')}</p></div>
                  </div>
                </div>

                <div className="panel-grid-two">
                  <div className="card" style={{ marginBottom: 0 }}>
                    <div className="card-body" style={{ padding: '1rem' }}>
                      <h3 style={{ fontSize: '0.95rem', marginBottom: '0.75rem' }}>{t('student.subjects.todayLessons')}</h3>
                      {todayLessonsForSubject.length ? (
                        <div className="interactive-list">
                          {todayLessonsForSubject.map((item) => (
                            <div key={item.scheduleId || `${item.dayOfWeek}-${item.periodNumber}`} className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                              <div className="interactive-card-main">
                                <div className="interactive-card-title">
                                  {t('student.schedule.period')} {item.periodNumber}
                                </div>
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

                  <div className="card" style={{ marginBottom: 0 }}>
                    <div className="card-body" style={{ padding: '1rem' }}>
                      <h3 style={{ fontSize: '0.95rem', marginBottom: '0.75rem' }}>{t('student.subjects.nextDue')}</h3>
                      {nextDue ? (
                        <div className="interactive-card-link is-static" style={{ cursor: 'default' }}>
                          <div className="interactive-card-main">
                            <div className="interactive-card-title">{nextDue.title}</div>
                            <div className="interactive-card-meta">{new Date(nextDue.dueDate).toLocaleDateString()}</div>
                          </div>
                        </div>
                      ) : (
                        <p className="muted-copy">{t('student.subjects.noHomework')}</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </article>
          );
        })}
      </div>
    </div>
  );
}
