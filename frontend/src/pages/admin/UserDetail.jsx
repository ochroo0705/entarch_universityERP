import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getUserById, getTeacherSchedule, getStudentSchedule } from '../../api/endpoints';
import { AdminDetailSkeleton, AdminScheduleSectionSkeleton } from '../../components/ui/AdminPageSkeletons';
import { primaryRoleBadgeClass, roleName } from '../../utils/roles';

const DAY_NAMES = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

const SLOT_COLORS = [
  '#4f46e5', '#0891b2', '#7c3aed', '#059669', '#d97706',
  '#dc2626', '#2563eb', '#9333ea', '#ca8a04', '#0d9488',
  '#be185d', '#6d28d9', '#ea580c', '#0369a1', '#4338ca',
];

function getSubjectColor(subject, colorMap) {
  if (!colorMap.has(subject)) {
    colorMap.set(subject, SLOT_COLORS[colorMap.size % SLOT_COLORS.length]);
  }
  return colorMap.get(subject);
}

function WeeklyScheduleGrid({ scheduleData, isTeacher }) {
  if (!scheduleData || scheduleData.length === 0) {
    return (
      <div style={{ color: 'var(--text-muted)', padding: '2rem', textAlign: 'center' }}>
        No schedule data available.
      </div>
    );
  }

  // Determine all periods and days present
  const periods = [...new Set(scheduleData.map((s) => s.periodNumber))].sort((a, b) => a - b);
  const days = [...new Set(scheduleData.map((s) => s.dayOfWeek))].sort((a, b) => a - b);

  // Build lookup: day -> period -> slot
  const lookup = {};
  scheduleData.forEach((s) => {
    const key = `${s.dayOfWeek}-${s.periodNumber}`;
    lookup[key] = s;
  });

  // Get time ranges per period from data
  const periodTimes = {};
  scheduleData.forEach((s) => {
    if (!periodTimes[s.periodNumber]) {
      periodTimes[s.periodNumber] = { start: s.startTime, end: s.endTime };
    }
  });

  const colorMap = new Map();

  return (
    <div style={{ overflowX: 'auto' }}>
      <table className="schedule-grid">
        <thead>
          <tr>
            <th style={{ width: '100px', textAlign: 'center' }}>Period</th>
            {days.map((d) => (
              <th key={d} style={{ textAlign: 'center', minWidth: '140px' }}>
                {DAY_NAMES[d] || `Day ${d}`}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {periods.map((p) => {
            const time = periodTimes[p];
            return (
              <tr key={p}>
                <td style={{ textAlign: 'center', fontWeight: 600, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  <div>P{p}</div>
                  {time && (
                    <div style={{ fontSize: '0.7rem', opacity: 0.7 }}>
                      {time.start?.slice(0, 5)} – {time.end?.slice(0, 5)}
                    </div>
                  )}
                </td>
                {days.map((d) => {
                  const slot = lookup[`${d}-${p}`];
                  if (!slot) {
                    return (
                      <td key={d} style={{ textAlign: 'center', background: '#f8fafc', color: '#cbd5e1' }}>
                        —
                      </td>
                    );
                  }
                  const bg = getSubjectColor(slot.subject, colorMap);
                  return (
                    <td key={d} style={{ padding: 0 }}>
                      <div
                        style={{
                          background: bg,
                          color: 'white',
                          borderRadius: '6px',
                          padding: '0.5rem 0.4rem',
                          margin: '3px',
                          textAlign: 'center',
                          fontSize: '0.8rem',
                          lineHeight: 1.3,
                        }}
                      >
                        <div style={{ fontWeight: 700 }}>{slot.subject}</div>
                        {isTeacher ? (
                          <div style={{ fontSize: '0.7rem', opacity: 0.85, marginTop: '2px' }}>
                            {slot.className}
                            {slot.roomNumber ? ` • ${slot.roomNumber}` : ''}
                          </div>
                        ) : (
                          <div style={{ fontSize: '0.7rem', opacity: 0.85, marginTop: '2px' }}>
                            {slot.teacher}
                            {slot.roomNumber ? ` • ${slot.roomNumber}` : ''}
                          </div>
                        )}
                      </div>
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default function UserDetail() {
  const { t } = useTranslation();
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [schedule, setSchedule] = useState(null);
  const [loading, setLoading] = useState(true);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [error, setError] = useState('');

  const isTeacher = user && (user.roleFlags & 2) !== 0;
  const isStudent = user && (user.roleFlags & 1) !== 0;
  const hasSchedule = isTeacher || isStudent;

  useEffect(() => {
    setLoading(true);
    getUserById(id)
      .then((res) => setUser(res.data))
      .catch(() => setError('Failed to load user details.'))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!user) return;
    if (!hasSchedule) return;

    setScheduleLoading(true);
    const fetchSchedule = isTeacher ? getTeacherSchedule(id) : getStudentSchedule(id);
    fetchSchedule
      .then((res) => setSchedule(res.data))
      .catch(() => setSchedule(null))
      .finally(() => setScheduleLoading(false));
  }, [user, id, isTeacher, hasSchedule]);

  if (loading) {
    return <AdminDetailSkeleton includeContent={false} includeSchedule />;
  }

  if (error) {
    return <div className="alert alert-error">{error}</div>;
  }

  if (!user) {
    return <div className="alert alert-error">User not found.</div>;
  }

  return (
    <div>
      <div className="page-header">
        <h1>{t('admin.userDetail.title')}</h1>
        <Link to="/admin/users" className="btn btn-secondary">{t('admin.userDetail.backToUsers')}</Link>
      </div>

      {/* User info card */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="card-body">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginBottom: '1.5rem' }}>
            <div
              style={{
                width: 64,
                height: 64,
                borderRadius: '50%',
                background: 'var(--primary)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem',
                fontWeight: 700,
                flexShrink: 0,
              }}
            >
              {(user.firstName?.[0] || '').toUpperCase()}
              {(user.lastName?.[0] || '').toUpperCase()}
            </div>
            <div>
              <h2 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>
                {user.firstName} {user.lastName}
              </h2>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <span className={`badge ${primaryRoleBadgeClass(user.roleFlags)}`}>
                  {roleName(user.roleFlags, t)}
                </span>
                <span className={`badge ${user.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
                  {user.isActive !== false ? t('common.active') : t('common.inactive')}
                </span>
              </div>
            </div>
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '1rem',
            }}
          >
            <InfoField label={t('admin.userDetail.id')} value={user.id} />
            <InfoField label={t('admin.userDetail.username')} value={user.username} />
            <InfoField label={t('admin.userDetail.email')} value={user.email} />
            <InfoField label={t('admin.userDetail.phone')} value={user.phone} />
            <InfoField label={t('admin.userDetail.gender')} value={user.gender} />
            <InfoField label={t('admin.userDetail.dateOfBirth')} value={user.dateOfBirth} />
            <InfoField label={t('admin.userDetail.address')} value={user.address} />
            {user.teacherSubjects && (
              <InfoField label={t('admin.userDetail.subjects')} value={user.teacherSubjects} />
            )}
          </div>
        </div>
      </div>

      {/* Schedule section */}
      {hasSchedule && (
        <div className="card">
          <div className="card-body">
            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>
              {t('admin.userDetail.schedule')}
            </h3>
            {scheduleLoading ? (
              <AdminScheduleSectionSkeleton />
            ) : (
              <WeeklyScheduleGrid scheduleData={schedule} isTeacher={isTeacher} />
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function InfoField({ label, value }) {
  if (!value && value !== 0) return null;
  return (
    <div>
      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', marginBottom: '0.15rem' }}>
        {label}
      </div>
      <div style={{ fontSize: '0.9rem' }}>{value}</div>
    </div>
  );
}
