import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import ChildSelector, { useChild } from '../../components/ChildSelector';
import { getStudentSchedule } from '../../api/endpoints';
import StatCard from '../../components/ui/StatCard';
import SectionCard from '../../components/ui/SectionCard';
import ResponsiveSchedule from '../../components/ui/ResponsiveSchedule';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';
import { ParentScheduleSkeleton } from '../../components/ui/ParentPageSkeletons';

export default function ParentSchedule() {
  const { t } = useTranslation();
  const { selectedChild, loading: childLoading } = useChild();
  const dayNames = useMemo(() => ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday')], [t]);
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedChild) return;
    setLoading(true);
    getStudentSchedule(selectedChild.id)
      .then((res) => setSchedule(res.data || []))
      .catch(() => setSchedule([]))
      .finally(() => setLoading(false));
  }, [selectedChild?.id]);

  if (childLoading) return <LoadingState label={t('common.loading')} />;
  if (!selectedChild) return <EmptyState title="Child schedule" description={t('parent.schedule.noChildren')} />;

  const childName = `${selectedChild.firstName} ${selectedChild.lastName}`;
  const totalPeriods = schedule.length;
  const uniqueSubjects = new Set(schedule.map((item) => item.subject)).size;

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">Child schedule</div>
          <h1>{t('parent.schedule.title', { name: childName })}</h1>
          <p className="page-summary">Switch children at any time and keep the active weekly plan available in both grid and agenda formats.</p>
        </div>
      </div>

      <ChildSelector />

      <div className="content-stack" aria-busy={loading}>
        {loading ? (
          <ParentScheduleSkeleton />
        ) : !schedule.length ? (
          <EmptyState title={t('parent.schedule.title', { name: childName })} description={t('parent.schedule.noSchedule')} />
        ) : (
          <>
            <div className="stats-grid">
              <StatCard icon={'\u{1F5D3}\uFE0F'} tone="teachers" value={totalPeriods} label={t('parent.schedule.totalPeriods')} />
              <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('parent.schedule.subjects')} />
            </div>

            <SectionCard title={t('parent.schedule.title', { name: childName })}>
              <ResponsiveSchedule
                schedule={schedule}
                dayNames={dayNames}
                periodLabel={t('parent.schedule.period')}
                renderMeta={(slot) => `${slot.teacher || ''}${slot.roomNumber ? ` / ${slot.roomNumber}` : ''}`}
              />
            </SectionCard>
          </>
        )}
      </div>
    </div>
  );
}
