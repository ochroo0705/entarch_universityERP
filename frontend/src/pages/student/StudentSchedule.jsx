import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getStudentSchedule } from '../../api/endpoints';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import ResponsiveSchedule from '../../components/ui/ResponsiveSchedule';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

export default function StudentSchedule() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const dayNames = useMemo(() => ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday')], [t]);
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStudentSchedule(user.userId)
      .then((res) => setSchedule(res.data || []))
      .catch(() => setSchedule([]))
      .finally(() => setLoading(false));
  }, [user.userId]);

  const totalPeriods = schedule.length;
  const uniqueSubjects = new Set(schedule.map((item) => item.subject)).size;

  if (loading) return <LoadingState label={t('common.loadingSchedule')} />;

  if (!schedule.length) {
    return <EmptyState title={t('student.schedule.title')} description={t('student.schedule.noSchedule')} />;
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Weekly view</div>
          <h1>{t('student.schedule.title')}</h1>
          <p className="page-summary">Use the timetable on larger screens or switch to the day-by-day mobile agenda when you're on the go.</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F5D3}\uFE0F'} tone="teachers" value={totalPeriods} label={t('student.schedule.totalPeriods')} />
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('student.schedule.subjects')} />
      </div>

      <SectionCard title={t('student.schedule.title')} subtitle="Desktop shows the week grid; mobile switches to a focused daily agenda.">
        <ResponsiveSchedule
          schedule={schedule}
          dayNames={dayNames}
          periodLabel={t('student.schedule.period')}
          renderMeta={(slot) => `${slot.teacher || ''}${slot.roomNumber ? ` / ${slot.roomNumber}` : ''}`}
        />
      </SectionCard>
    </div>
  );
}
