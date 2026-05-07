import { useEffect, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getStudentAttendance } from '../../api/endpoints';
import { isAttendanceCountedAsPresent, normalizeStatus } from '../../utils/studentProgress';

export default function StudentAttendance() {
  const { t } = useTranslation();
  const { user } = useAuth();

  const statusConfig = useMemo(() => ({
    present: { label: t('student.attendance.present'), color: 'var(--success)', icon: '\u2705', badge: 'badge-success' },
    absent: { label: t('student.attendance.absent'), color: 'var(--danger)', icon: '\u274C', badge: 'badge-danger' },
    late: { label: t('student.attendance.late'), color: 'var(--warning)', icon: '\u23F0', badge: 'badge-warning' },
    excused: { label: t('student.attendance.excused'), color: 'var(--info)', icon: '\u{1F4CB}', badge: 'badge-info' },
    sick: { label: t('teacher.classDetail.sick'), color: '#9333ea', icon: '\u{1F912}', badge: 'badge-warning' },
  }), [t]);
  const monthNames = useMemo(() => [t('days.january'), t('days.february'), t('days.march'), t('days.april'), t('days.may'), t('days.june'), t('days.july'), t('days.august'), t('days.september'), t('days.october'), t('days.november'), t('days.december')], [t]);
  const dayLabels = useMemo(() => [t('days.sun'), t('days.mon'), t('days.tue'), t('days.wed'), t('days.thu'), t('days.fri'), t('days.sat')], [t]);
  const [attendance, setAttendance] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMonth, setViewMonth] = useState(new Date());

  useEffect(() => {
    const year = viewMonth.getFullYear();
    const month = viewMonth.getMonth();
    const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month + 1, 0).getDate();
    const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

    setLoading(true);
    getStudentAttendance(user.userId, startDate, endDate)
      .then((res) => setAttendance(res.data || []))
      .catch(() => setAttendance([]))
      .finally(() => setLoading(false));
  }, [user.userId, viewMonth]);

  const changeMonth = useCallback((delta) => {
    setViewMonth((prev) => {
      const nextDate = new Date(prev);
      nextDate.setMonth(nextDate.getMonth() + delta);
      return nextDate;
    });
  }, []);

  const year = viewMonth.getFullYear();
  const month = viewMonth.getMonth();
  const firstDayOfMonth = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  const attendanceByDate = useMemo(() => {
    const result = {};
    attendance.forEach((item) => {
      const date = item.attendanceDate || item.date;
      if (!date) return;
      if (!result[date]) result[date] = [];
      result[date].push(item);
    });
    return result;
  }, [attendance]);

  const { counts, attendanceRate } = useMemo(() => {
    const nextCounts = { present: 0, absent: 0, late: 0, excused: 0, sick: 0 };
    attendance.forEach((item) => {
      const status = normalizeStatus(item.status);
      if (nextCounts[status] !== undefined) nextCounts[status] += 1;
    });
    const total = attendance.length;
    const attendedCount = attendance.filter((item) => isAttendanceCountedAsPresent(item.status)).length;
    const nextAttendanceRate = total > 0 ? Math.round((attendedCount / total) * 100) : 100;
    return { counts: nextCounts, attendanceRate: nextAttendanceRate };
  }, [attendance]);

  const sortedAttendance = useMemo(
    () => [...attendance].sort((a, b) => (b.attendanceDate || b.date || '').localeCompare(a.attendanceDate || a.date || '')),
    [attendance]
  );

  return (
    <div>
      <div className="page-header">
        <h1>{t('student.attendance.title')}</h1>
      </div>

      <div className="stats-grid" style={{ marginBottom: '1.5rem' }}>
        <div className="stat-card">
          <div className="stat-icon students">{'\u2705'}</div>
          <div className="stat-info">
            <h3 style={{ color: attendanceRate >= 90 ? 'var(--success)' : attendanceRate >= 75 ? 'var(--warning)' : 'var(--danger)' }}>
              {attendanceRate}%
            </h3>
            <p>{t('student.attendance.attendanceRate')}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon classes">{'\u{1F4C5}'}</div>
          <div className="stat-info"><h3>{counts.present}</h3><p>{t('student.attendance.present')}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon assignments">{'\u274C'}</div>
          <div className="stat-info"><h3>{counts.absent}</h3><p>{t('student.attendance.absent')}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teachers">{'\u23F0'}</div>
          <div className="stat-info"><h3>{counts.late}</h3><p>{t('student.attendance.late')}</p></div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="card-body parent-calendar-shell">
          <div className="parent-calendar-toolbar">
            <button className="btn btn-secondary btn-sm" onClick={() => changeMonth(-1)}>{t('common.prev')}</button>
            <h3 style={{ fontSize: '1.05rem', fontFamily: 'var(--font-heading)', fontWeight: 700, textAlign: 'center' }}>
              {monthNames[month]} {year}
            </h3>
            <button className="btn btn-secondary btn-sm" onClick={() => changeMonth(1)}>{t('common.next')}</button>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Loading...</div>
          ) : (
            <div className="parent-calendar-grid">
              {dayLabels.map((day) => (
                <div key={day} className="parent-calendar-label">{day}</div>
              ))}
              {Array.from({ length: firstDayOfMonth }, (_, index) => (
                <div key={`empty-${index}`} style={{ padding: '0.4rem' }} />
              ))}
              {Array.from({ length: daysInMonth }, (_, index) => {
                const day = index + 1;
                const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                const dayRecords = attendanceByDate[dateStr] || [];
                const primaryStatus = dayRecords.length > 0 ? normalizeStatus(dayRecords[0].status) : null;
                const config = primaryStatus ? statusConfig[primaryStatus] : null;
                const isToday = dateStr === new Date().toISOString().split('T')[0];

                return (
                  <div
                    key={day}
                    title={dayRecords.map((record) => `${record.subjectName || 'Class'}: ${record.status}`).join('\n')}
                    className="parent-calendar-day"
                    style={{
                      fontWeight: isToday ? 700 : 400,
                      background: config ? `${config.color}15` : 'transparent',
                      border: isToday ? '2px solid var(--primary)' : config ? `1px solid ${config.color}40` : '1px solid transparent',
                    }}
                  >
                    <div>{day}</div>
                    {config ? <div style={{ fontSize: '0.65rem', marginTop: '1px' }}>{config.icon}</div> : null}
                    {dayRecords.length > 1 ? <div style={{ fontSize: '0.6rem', color: 'var(--text-muted)' }}>+{dayRecords.length - 1}</div> : null}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="card-body" style={{ padding: '0.75rem 1rem' }}>
          <div className="parent-legend">
            {Object.entries(statusConfig).map(([key, cfg]) => (
              <div key={key} className="parent-legend-item">
                <span>{cfg.icon}</span>
                <span style={{ color: cfg.color, fontWeight: 600 }}>{cfg.label}</span>
                <span style={{ color: 'var(--text-muted)' }}>({counts[key]})</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {attendance.length > 0 ? (
        <div className="card">
          <div className="card-body">
            <h3 style={{ fontSize: '1.05rem', marginBottom: '1rem', fontFamily: 'var(--font-heading)', fontWeight: 700 }}>
              {t('student.attendance.recordsThisMonth')}
            </h3>
            <div className="table-container desktop-table">
              <table>
                <thead>
                  <tr>
                    <th>{t('student.attendance.date')}</th>
                    <th>{t('teacher.classDetail.subject')}</th>
                    <th>{t('teacher.classDetail.period')}</th>
                    <th>{t('common.status')}</th>
                    <th>{t('student.attendance.remarks')}</th>
                  </tr>
                </thead>
                <tbody>
                  {sortedAttendance.map((item, index) => {
                    const status = normalizeStatus(item.status);
                    const cfg = statusConfig[status] || statusConfig.present;
                    return (
                      <tr key={item.id || index}>
                        <td>{(item.attendanceDate || item.date) ? new Date(item.attendanceDate || item.date).toLocaleDateString() : '-'}</td>
                        <td>{item.subjectName || '-'}</td>
                        <td>P{item.periodNumber || '-'}</td>
                        <td><span className={`badge ${cfg.badge}`}>{cfg.label}</span></td>
                        <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{item.remarks || '-'}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="parent-mobile-card-list">
              {sortedAttendance.map((item, index) => {
                const status = normalizeStatus(item.status);
                const cfg = statusConfig[status] || statusConfig.present;
                return (
                  <article key={`mobile-${item.id || index}`} className="parent-mobile-card">
                    <div className="parent-mobile-card-head">
                      <div>
                        <h3 className="parent-mobile-card-title">
                          {(item.attendanceDate || item.date) ? new Date(item.attendanceDate || item.date).toLocaleDateString() : '-'}
                        </h3>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{item.subjectName || '-'}</div>
                      </div>
                      <span className={`badge ${cfg.badge}`}>{cfg.label}</span>
                    </div>

                    <div className="parent-mobile-card-grid">
                      <div className="parent-mobile-card-field">
                        <span>{t('teacher.classDetail.period')}</span>
                        <strong>P{item.periodNumber || '-'}</strong>
                      </div>
                      <div className="parent-mobile-card-field">
                        <span>{t('common.status')}</span>
                        <strong>{cfg.label}</strong>
                      </div>
                    </div>

                    <div className="parent-mobile-card-field">
                      <span>{t('student.attendance.remarks')}</span>
                      <div className="parent-mobile-card-copy">{item.remarks || '-'}</div>
                    </div>
                  </article>
                );
              })}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
