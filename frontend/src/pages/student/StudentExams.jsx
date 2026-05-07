import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getStudentExamResults } from '../../api/endpoints';
import ExamResultList from '../../components/ui/ExamResultList';
import SectionCard from '../../components/ui/SectionCard';
import StatCard from '../../components/ui/StatCard';
import { EmptyState, LoadingState } from '../../components/ui/StateBlock';

export default function StudentExams() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStudentExamResults(user.userId)
      .then((res) => setExams(res.data || []))
      .catch(() => setExams([]))
      .finally(() => setLoading(false));
  }, [user.userId]);

  const items = useMemo(
    () => [...exams].sort((a, b) => `${b.examDate} ${b.startTime}`.localeCompare(`${a.examDate} ${a.startTime}`)),
    [exams]
  );

  const uniqueSubjects = new Set(items.map((exam) => exam.subject)).size;
  const average = items.length
    ? Math.round((items.reduce((sum, item) => sum + Number(item.percentage || 0), 0) / items.length) * 100) / 100
    : 0;

  if (loading) return <LoadingState label={t('common.loading')} />;
  if (!items.length) return <EmptyState title={t('student.exams.title')} description={t('student.exams.empty')} />;

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('student.exams.kicker')}</div>
          <h1>{t('student.exams.title')}</h1>
          <p className="page-summary">{t('student.exams.summary')}</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={'\u{1F4C5}'} tone="teachers" value={items.length} label={t('student.exams.total')} />
        <StatCard icon={'\u{1F4DA}'} tone="assignments" value={uniqueSubjects} label={t('student.exams.subjects')} />
        <StatCard icon={'\u{1F4AF}'} tone="success" value={`${average}%`} label={t('student.exams.average')} />
      </div>

      <SectionCard title={t('student.exams.title')}>
        <ExamResultList
          exams={items}
          emptyLabel={t('student.exams.empty')}
          renderMeta={(exam) => `${exam.teacherName}${exam.roomNumber ? ` • ${exam.roomNumber}` : ''}`}
        />
      </SectionCard>
    </div>
  );
}
