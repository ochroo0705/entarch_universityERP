import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createClass, getTeachers } from '../../api/endpoints';
import { AdminFormFieldSkeleton } from '../../components/ui/AdminPageSkeletons';
import SearchableSelect from '../../components/ui/SearchableSelect';

export default function CreateClass() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [teachers, setTeachers] = useState([]);
  const [teachersLoading, setTeachersLoading] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    className: '',
    grade: '',
    section: '',
    roomNumber: '',
    academicYear: '',
    homeroomTeacherId: '',
  });

  useEffect(() => {
    getTeachers()
      .then((res) => setTeachers(res.data))
      .catch(() => setTeachers([]))
      .finally(() => setTeachersLoading(false));
  }, []);

  const teacherOptions = teachers.map((teacher) => ({
    value: teacher.id,
    label: [teacher.firstName, teacher.lastName].filter(Boolean).join(' ') || teacher.username,
    meta: teacher.username ? `@${teacher.username}` : null,
  }));

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await createClass({
        ...form,
        grade: parseInt(form.grade),
        homeroomTeacherId: form.homeroomTeacherId ? parseInt(form.homeroomTeacherId) : null,
      });
      navigate('/admin/classes');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create class.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createClass.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createClass.className')} *</label>
                <input name="className" className="form-control" value={form.className} onChange={handleChange} required placeholder={t('admin.createClass.classNamePlaceholder')} />
              </div>
              <div className="form-group">
                <label>{t('common.grade')} *</label>
                <input name="grade" type="number" className="form-control" value={form.grade} onChange={handleChange} required min="1" max="12" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createClass.section')}</label>
                <input name="section" className="form-control" value={form.section} onChange={handleChange} placeholder={t('admin.createClass.sectionPlaceholder')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createClass.roomNumber')}</label>
                <input name="roomNumber" className="form-control" value={form.roomNumber} onChange={handleChange} placeholder={t('admin.createClass.roomPlaceholder')} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createClass.academicYear')} *</label>
                <input name="academicYear" className="form-control" value={form.academicYear} onChange={handleChange} required placeholder={t('admin.createClass.yearPlaceholder')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createClass.homeroomTeacher')}</label>
                {teachersLoading ? (
                  <AdminFormFieldSkeleton withLabel={false} />
                ) : (
                  <SearchableSelect
                    options={teacherOptions}
                    value={form.homeroomTeacherId}
                    onChange={(nextValue) => setForm({ ...form, homeroomTeacherId: String(nextValue || '') })}
                    placeholder={t('admin.createClass.selectTeacher')}
                    searchPlaceholder={t('common.search')}
                    emptyLabel="No teachers found"
                  />
                )}
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createClass.title')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/classes')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
