import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getTeacherSchedule } from '../../api/endpoints';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import ResponsiveSchedule from '../../components/ui/ResponsiveSchedule';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

export default function TeacherSchedule() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const dayNames = useMemo(() => ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday')], [t]);
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getTeacherSchedule(user.userId)
      .then((res) => setSchedule(res.data || []))
      .catch(() => setSchedule([]))
      .finally(() => setLoading(false));
  }, [user.userId]);

  const totalPeriods = schedule.length;
  const uniqueSubjects = new Set(schedule.map((item) => item.subject)).size;
  const uniqueClasses = new Set(schedule.map((item) => item.className)).size;

  if (loading) return <LoadingState label={t('common.loadingSchedule')} />;

  if (!schedule.length) {
    return <EmptyState title={t('teacher.schedule.title')} description={t('teacher.schedule.noSchedule')} />;
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Teaching week</div>
          <h1>{t('teacher.schedule.title')}</h1>
          <p className="page-summary">The same schedule now adapts cleanly between a weekly timetable and a mobile-friendly day agenda.</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F5D3}\uFE0F'} tone="teachers" value={totalPeriods} label={t('teacher.schedule.totalPeriods')} />
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('teacher.schedule.subjects')} />
        <StatCard icon={'\u{1F3E0}'} tone="classes" value={uniqueClasses} label={t('teacher.schedule.classes')} />
      </div>

      <SectionCard title={t('teacher.schedule.title')} subtitle="Review every period by day on desktop or switch to a compact daily teaching list on small screens.">
        <ResponsiveSchedule
          schedule={schedule}
          dayNames={dayNames}
          periodLabel={t('teacher.schedule.period')}
          renderMeta={(slot) => `${slot.className}${slot.roomNumber ? ` / ${slot.roomNumber}` : ''}`}
        />
      </SectionCard>
    </div>
  );
}
