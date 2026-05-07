import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createSchedule, getTeachingAssignments } from '../../api/endpoints';
import { AdminFormFieldSkeleton } from '../../components/ui/AdminPageSkeletons';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';

export default function CreateSchedule() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState([]);
  const [assignmentsLoading, setAssignmentsLoading] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    teachingAssignmentId: '',
    dayOfWeek: '1',
    periodNumber: '1',
    startTime: '08:00',
    endTime: '08:45',
    roomNumber: '',
  });

  useEffect(() => {
    getTeachingAssignments()
      .then((res) => setAssignments(res.data))
      .catch(() => setAssignments([]))
      .finally(() => setAssignmentsLoading(false));
  }, []);

  const assignmentOptions = assignments.map((assignment) => ({
    value: assignment.id,
    label: getAssignmentLabel(assignment),
  }));
  const dayOptions = [
    { value: '1', label: t('days.monday') },
    { value: '2', label: t('days.tuesday') },
    { value: '3', label: t('days.wednesday') },
    { value: '4', label: t('days.thursday') },
    { value: '5', label: t('days.friday') },
    { value: '6', label: t('days.saturday') },
    { value: '7', label: t('days.sunday') },
  ];

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await createSchedule({
        teachingAssignmentId: parseInt(form.teachingAssignmentId),
        dayOfWeek: parseInt(form.dayOfWeek),
        periodNumber: parseInt(form.periodNumber),
        startTime: form.startTime,
        endTime: form.endTime,
        roomNumber: form.roomNumber,
      });
      navigate('/admin/schedules');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create schedule.');
    } finally {
      setLoading(false);
    }
  };

  const getAssignmentLabel = (a) => {
    const teacher = a.teacher ? `${a.teacher.firstName} ${a.teacher.lastName}` : 'Unknown';
    const subject = a.subject?.subjectName || 'Unknown';
    const cls = a.classEntity?.className || 'Unknown';
    return `${teacher} - ${subject} - ${cls}`;
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createSchedule.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{t('admin.createSchedule.teachingAssignment')} *</label>
              {assignmentsLoading ? (
                <AdminFormFieldSkeleton withLabel={false} />
              ) : (
                <SearchableSelect options={assignmentOptions} value={form.teachingAssignmentId} onChange={(value) => setForm({ ...form, teachingAssignmentId: String(value || '') })} placeholder={t('admin.createSchedule.selectAssignment')} searchPlaceholder={t('common.search')} emptyLabel="No assignments found" />
              )}
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createSchedule.dayOfWeek')} *</label>
                <SelectMenu options={dayOptions} value={form.dayOfWeek} onChange={(value) => setForm({ ...form, dayOfWeek: value })} placeholder={t('admin.createSchedule.dayOfWeek')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createSchedule.periodNumber')} *</label>
                <input name="periodNumber" type="number" className="form-control" value={form.periodNumber} onChange={handleChange} required min="1" max="10" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createSchedule.startTime')} *</label>
                <input name="startTime" type="time" className="form-control" value={form.startTime} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createSchedule.endTime')} *</label>
                <input name="endTime" type="time" className="form-control" value={form.endTime} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>{t('admin.createSchedule.roomNumber')}</label>
              <input name="roomNumber" className="form-control" value={form.roomNumber} onChange={handleChange} placeholder={t('admin.createClass.roomPlaceholder')} />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createSchedule.title')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/schedules')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
