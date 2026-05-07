import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import ChildSelector, { useChild } from '../../components/ChildSelector';
import { getStudentExamResults } from '../../api/endpoints';
import ExamResultList from '../../components/ui/ExamResultList';
import SectionCard from '../../components/ui/SectionCard';
import StatCard from '../../components/ui/StatCard';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

export default function ParentExams() {
  const { t } = useTranslation();
  const { selectedChild, loading: childLoading } = useChild();
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedChild) return;
    setLoading(true);
    getStudentExamResults(selectedChild.id)
      .then((res) => setExams(res.data || []))
      .catch(() => setExams([]))
      .finally(() => setLoading(false));
  }, [selectedChild?.id]);

  const items = useMemo(
    () => [...exams].sort((a, b) => `${b.examDate} ${b.startTime}`.localeCompare(`${a.examDate} ${a.startTime}`)),
    [exams]
  );

  if (childLoading) return <LoadingState label={t('common.loading')} />;
  if (!selectedChild) return <EmptyState title={t('parent.exams.titleFallback')} description={t('parent.exams.noChildren')} />;

  const childName = `${selectedChild.firstName} ${selectedChild.lastName}`;
  const uniqueSubjects = new Set(items.map((exam) => exam.subject)).size;
  const average = items.length
    ? Math.round((items.reduce((sum, item) => sum + Number(item.percentage || 0), 0) / items.length) * 100) / 100
    : 0;

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('parent.exams.kicker')}</div>
          <h1>{t('parent.exams.title', { name: childName })}</h1>
          <p className="page-summary">{t('parent.exams.summary')}</p>
        </div>
      </div>

      <ChildSelector />

      {loading ? (
        <LoadingState label={t('common.loading')} />
      ) : !items.length ? (
        <EmptyState title={t('parent.exams.title', { name: childName })} description={t('parent.exams.empty')} />
      ) : (
        <>
          <div className="stats-grid">
            <StatCard icon={'\u{1F4C5}'} tone="teachers" value={items.length} label={t('parent.exams.total')} />
            <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('parent.exams.subjects')} />
            <StatCard icon={'\u{1F4AF}'} tone="success" value={`${average}%`} label={t('parent.exams.average')} />
          </div>

          <SectionCard title={t('parent.exams.title', { name: childName })}>
            <ExamResultList
              exams={items}
              emptyLabel={t('parent.exams.empty')}
              renderMeta={(exam) => `${exam.teacherName}${exam.roomNumber ? ` • ${exam.roomNumber}` : ''}`}
            />
          </SectionCard>
        </>
      )}
    </div>
  );
}
