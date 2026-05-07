import { useEffect, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import ChildSelector, { useChild } from '../../components/ChildSelector';
import { getStudentAttendance } from '../../api/endpoints';
import { ParentAttendanceSkeleton } from '../../components/ui/ParentPageSkeletons';

export default function ParentAttendance() {
  const { t } = useTranslation();
  const { selectedChild, loading: childLoading } = useChild();
  const [attendance, setAttendance] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMonth, setViewMonth] = useState(new Date());

  useEffect(() => {
    if (!selectedChild) return;

    const year = viewMonth.getFullYear();
    const month = viewMonth.getMonth();
    const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month + 1, 0).getDate();
    const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

    setLoading(true);
    getStudentAttendance(selectedChild.id, startDate, endDate)
      .then((res) => setAttendance(res.data || []))
      .catch(() => setAttendance([]))
      .finally(() => setLoading(false));
  }, [selectedChild?.id, viewMonth]);

  const statusConfig = useMemo(() => ({
    present: { label: t('parent.attendance.present'), color: 'var(--success)', icon: '\u2705', badge: 'badge-success' },
    absent: { label: t('parent.attendance.absent'), color: 'var(--danger)', icon: '\u274C', badge: 'badge-danger' },
    late: { label: t('parent.attendance.late'), color: 'var(--warning)', icon: '\u23F0', badge: 'badge-warning' },
    excused: { label: t('parent.attendance.excused'), color: 'var(--info)', icon: '\u{1F4CB}', badge: 'badge-info' },
    sick: { label: t('parent.attendance.sick'), color: '#9333ea', icon: '\u{1F912}', badge: 'badge-warning' },
  }), [t]);

  const monthNames = useMemo(() => [
    t('days.january'), t('days.february'), t('days.march'), t('days.april'),
    t('days.may'), t('days.june'), t('days.july'), t('days.august'),
    t('days.september'), t('days.october'), t('days.november'), t('days.december'),
  ], [t]);
  const dayLabels = useMemo(() => [
    t('days.sun'), t('days.mon'), t('days.tue'), t('days.wed'), t('days.thu'), t('days.fri'), t('days.sat'),
  ], [t]);

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
      const status = item.status?.toLowerCase();
      if (nextCounts[status] !== undefined) nextCounts[status] += 1;
    });
    const total = attendance.length;
    const nextAttendanceRate = total > 0 ? Math.round(((nextCounts.present + nextCounts.late + nextCounts.excused) / total) * 100) : 100;
    return { counts: nextCounts, attendanceRate: nextAttendanceRate };
  }, [attendance]);

  const sortedAttendance = useMemo(
    () => [...attendance].sort((a, b) => (b.attendanceDate || b.date || '').localeCompare(a.attendanceDate || a.date || '')),
    [attendance]
  );

  if (childLoading) return <div className="loading"><div className="spinner" />{t('common.loading')}</div>;
  if (!selectedChild) return <div className="card"><div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>{t('parent.attendance.noChildren')}</div></div>;

  const childName = `${selectedChild.firstName} ${selectedChild.lastName}`;

  return (
    <div>
      <div className="page-header"><h1>{t('parent.attendance.title', { name: childName })}</h1></div>
      <ChildSelector />

      <div className="content-stack" aria-busy={loading}>
        {loading ? (
          <ParentAttendanceSkeleton />
        ) : (
          <>
            <div className="stats-grid" style={{ marginBottom: '1.5rem' }}>
              <div className="stat-card">
                <div className="stat-icon students">{'\u2705'}</div>
                <div className="stat-info">
                  <h3 style={{ color: attendanceRate >= 90 ? 'var(--success)' : attendanceRate >= 75 ? 'var(--warning)' : 'var(--danger)' }}>
                    {attendanceRate}%
                  </h3>
                  <p>{t('parent.attendance.attendanceRate')}</p>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon classes">{'\u{1F4C5}'}</div>
                <div className="stat-info"><h3>{counts.present}</h3><p>{t('parent.attendance.present')}</p></div>
              </div>
              <div className="stat-card">
                <div className="stat-icon assignments">{'\u274C'}</div>
                <div className="stat-info"><h3>{counts.absent}</h3><p>{t('parent.attendance.absent')}</p></div>
              </div>
              <div className="stat-card">
                <div className="stat-icon teachers">{'\u23F0'}</div>
                <div className="stat-info"><h3>{counts.late}</h3><p>{t('parent.attendance.late')}</p></div>
              </div>
            </div>

            <div className="card" style={{ marginBottom: '1.5rem' }}>
              <div className="card-body parent-calendar-shell">
                <div className="parent-calendar-toolbar">
                  <button className="btn btn-secondary btn-sm" onClick={() => changeMonth(-1)}>{t('parent.attendance.prev')}</button>
                  <h3 style={{ fontSize: '1.05rem', fontFamily: 'var(--font-heading)', fontWeight: 700, textAlign: 'center' }}>
                    {monthNames[month]} {year}
                  </h3>
                  <button className="btn btn-secondary btn-sm" onClick={() => changeMonth(1)}>{t('parent.attendance.next')}</button>
                </div>

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
                    const primaryStatus = dayRecords.length > 0 ? dayRecords[0].status?.toLowerCase() : null;
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
                    {t('parent.attendance.recordsThisMonth')}
                  </h3>
                  <div className="table-container desktop-table">
                    <table>
                      <thead>
                        <tr>
                          <th>{t('parent.attendance.date')}</th>
                          <th>{t('parent.attendance.subject')}</th>
                          <th>{t('parent.attendance.period')}</th>
                          <th>{t('parent.attendance.status')}</th>
                          <th>{t('parent.attendance.remarks')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {sortedAttendance.map((item, index) => {
                          const status = item.status?.toLowerCase();
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
                      const status = item.status?.toLowerCase();
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
                              <span>{t('parent.attendance.period')}</span>
                              <strong>P{item.periodNumber || '-'}</strong>
                            </div>
                            <div className="parent-mobile-card-field">
                              <span>{t('parent.attendance.status')}</span>
                              <strong>{cfg.label}</strong>
                            </div>
                          </div>

                          <div className="parent-mobile-card-field">
                            <span>{t('parent.attendance.remarks')}</span>
                            <div className="parent-mobile-card-copy">{item.remarks || '-'}</div>
                          </div>
                        </article>
                      );
                    })}
                  </div>
                </div>
              </div>
            ) : null}
          </>
        )}
      </div>
    </div>
  );
}
