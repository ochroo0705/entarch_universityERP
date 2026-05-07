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

export default function ExamScheduleList({ exams, emptyLabel, renderMeta }) {
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
        <article key={exam.id} className="card">
          <div className="card-body" style={{ display: 'grid', gap: '0.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', alignItems: 'flex-start', flexWrap: 'wrap' }}>
              <div>
                <div className={`badge ${exam.published ? 'badge-success' : 'badge-warning'}`} style={{ marginBottom: '0.5rem' }}>
                  {exam.published ? t('admin.examSchedules.published') : t('admin.examSchedules.draft')}
                </div>
                <h3 style={{ marginBottom: '0.25rem' }}>{exam.title}</h3>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>{exam.subject}</div>
              </div>
              <div style={{ textAlign: 'right', minWidth: '160px' }}>
                <div style={{ fontWeight: 700 }}>{formatDate(exam.examDate)}</div>
                <div style={{ color: 'var(--text-muted)' }}>
                  {formatTime(exam.startTime)} - {formatTime(exam.endTime)}
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gap: '0.25rem', color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              <div>{renderMeta?.(exam)}</div>
              {exam.notes ? <div style={{ whiteSpace: 'pre-wrap' }}>{exam.notes}</div> : null}
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
