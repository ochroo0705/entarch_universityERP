import { useTranslation } from 'react-i18next';

function formatDate(dateValue) {
  if (!dateValue) return '';
  const date = new Date(`${dateValue}T00:00:00`);
  if (Number.isNaN(date.getTime())) return dateValue;
  return date.toLocaleDateString();
}

function formatTime(timeValue) {
  if (!timeValue) return '';
  return timeValue.slice(0, 5);
}

function formatScore(score, totalScore) {
  if (score == null && totalScore == null) return '—';
  return `${score ?? '—'} / ${totalScore ?? '—'}`;
}

export default function ExamResultList({ exams, emptyLabel, renderMeta, renderActions, showStudentName = false }) {
  const { t } = useTranslation();

  if (!exams.length) {
    return (
      <div className="card">
        <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2.5rem' }}>
          {emptyLabel}
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      {exams.map((exam) => (
        <article key={exam.id ?? `${exam.examScheduleId}-${exam.studentId}`} className="card">
          <div className="card-body" style={{ display: 'grid', gap: '0.9rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', alignItems: 'flex-start', flexWrap: 'wrap' }}>
              <div>
                <div className={`badge ${exam.published ? 'badge-success' : 'badge-warning'}`} style={{ marginBottom: '0.5rem' }}>
                  {exam.published ? t('exams.results.published') : t('exams.results.unpublished')}
                </div>
                <h3 style={{ marginBottom: '0.25rem' }}>{exam.examTitle}</h3>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>{exam.subject}</div>
                {showStudentName ? (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.25rem' }}>{exam.studentName}</div>
                ) : null}
              </div>
              <div style={{ textAlign: 'right', minWidth: '170px' }}>
                <div style={{ fontWeight: 700 }}>{formatDate(exam.examDate)}</div>
                <div style={{ color: 'var(--text-muted)' }}>
                  {formatTime(exam.startTime)} - {formatTime(exam.endTime)}
                </div>
              </div>
            </div>

            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '0.75rem' }}>
              <div className="card" style={{ background: 'var(--surface-2)' }}>
                <div className="card-body" style={{ padding: '0.85rem' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{t('exams.results.score')}</div>
                  <div style={{ fontWeight: 700 }}>{formatScore(exam.score, exam.totalScore)}</div>
                </div>
              </div>
              <div className="card" style={{ background: 'var(--surface-2)' }}>
                <div className="card-body" style={{ padding: '0.85rem' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{t('exams.results.percentage')}</div>
                  <div style={{ fontWeight: 700 }}>{exam.percentage != null ? `${exam.percentage}%` : '—'}</div>
                </div>
              </div>
              <div className="card" style={{ background: 'var(--surface-2)' }}>
                <div className="card-body" style={{ padding: '0.85rem' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{t('exams.results.weighting')}</div>
                  <div style={{ fontWeight: 700 }}>{exam.weighting != null ? `${exam.weighting}%` : '—'}</div>
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              <div>{renderMeta?.(exam)}</div>
              {exam.teacherComment ? <div><strong>{t('exams.results.teacherComment')}:</strong> {exam.teacherComment}</div> : null}
              {exam.remarks ? <div><strong>{t('exams.results.remarks')}:</strong> {exam.remarks}</div> : null}
              {exam.notes ? <div style={{ whiteSpace: 'pre-wrap' }}>{exam.notes}</div> : null}
            </div>

            {renderActions ? <div>{renderActions(exam)}</div> : null}
          </div>
        </article>
      ))}
    </div>
  );
}
