import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createExamSchedule, getTeachingAssignments } from '../../api/endpoints';
import { AdminFormFieldSkeleton } from '../../components/ui/AdminPageSkeletons';
import SearchableSelect from '../../components/ui/SearchableSelect';

function formatAssignmentLabel(assignment) {
  const teacher = assignment.teacher ? `${assignment.teacher.firstName} ${assignment.teacher.lastName}` : 'Unknown';
  const subject = assignment.subject?.subjectName || assignment.subject?.name || 'Unknown';
  const className = assignment.classEntity?.className || 'Unknown';
  return `${teacher} - ${subject} - ${className}`;
}

export default function CreateExamSchedule() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState([]);
  const [assignmentsLoading, setAssignmentsLoading] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    teachingAssignmentId: '',
    examDate: '',
    startTime: '09:00',
    endTime: '10:30',
    roomNumber: '',
    title: '',
    notes: '',
    published: true,
  });

  useEffect(() => {
    getTeachingAssignments()
      .then((res) => setAssignments(Array.isArray(res.data) ? res.data : []))
      .catch(() => setAssignments([]))
      .finally(() => setAssignmentsLoading(false));
  }, []);

  const assignmentOptions = assignments.map((assignment) => ({
    value: assignment.id,
    label: formatAssignmentLabel(assignment),
  }));

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await createExamSchedule({
        teachingAssignmentId: Number(form.teachingAssignmentId),
        examDate: form.examDate,
        startTime: form.startTime,
        endTime: form.endTime,
        roomNumber: form.roomNumber || null,
        title: form.title,
        notes: form.notes || null,
        published: form.published,
      });
      navigate('/admin/exam-schedules');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create exam schedule.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createExamSchedule.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error ? <div className="alert alert-error">{error}</div> : null}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{t('admin.createExamSchedule.teachingAssignment')}</label>
              {assignmentsLoading ? (
                <AdminFormFieldSkeleton withLabel={false} />
              ) : (
                <SearchableSelect
                  options={assignmentOptions}
                  value={form.teachingAssignmentId}
                  onChange={(value) => setForm((current) => ({ ...current, teachingAssignmentId: String(value || '') }))}
                  placeholder={t('admin.createExamSchedule.selectAssignment')}
                  searchPlaceholder={t('common.search')}
                  emptyLabel="No assignments found"
                />
              )}
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createExamSchedule.examDate')}</label>
                <input type="date" name="examDate" className="form-control" value={form.examDate} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createExamSchedule.roomNumber')}</label>
                <input type="text" name="roomNumber" className="form-control" value={form.roomNumber} onChange={handleChange} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createExamSchedule.startTime')}</label>
                <input type="time" name="startTime" className="form-control" value={form.startTime} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createExamSchedule.endTime')}</label>
                <input type="time" name="endTime" className="form-control" value={form.endTime} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>{t('admin.createExamSchedule.titleLabel')}</label>
              <input type="text" name="title" className="form-control" value={form.title} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>{t('admin.createExamSchedule.notes')}</label>
              <textarea name="notes" className="form-control" rows="4" value={form.notes} onChange={handleChange} />
            </div>
            <label style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '1rem' }}>
              <input type="checkbox" name="published" checked={form.published} onChange={handleChange} />
              {t('admin.createExamSchedule.publishNow')}
            </label>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createExamSchedule.submit')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/exam-schedules')}>
                {t('common.cancel')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
