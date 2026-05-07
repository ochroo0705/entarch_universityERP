import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { assignTeaching, getTeachers, getSubjects, getClasses } from '../../api/endpoints';
import { AdminFormFieldSkeleton } from '../../components/ui/AdminPageSkeletons';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';

export default function CreateTeachingAssignment() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [classes, setClasses] = useState([]);
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    teacherId: '',
    subjectId: '',
    classId: '',
    academicYear: '',
    semester: '1',
    isActive: true,
  });

  useEffect(() => {
    Promise.all([getTeachers(), getSubjects(), getClasses()])
      .then(([tc, s, c]) => {
        setTeachers(tc.data);
        setSubjects(s.data);
        setClasses(c.data);
      })
      .catch(() => {
        setTeachers([]);
        setSubjects([]);
        setClasses([]);
      })
      .finally(() => setOptionsLoading(false));
  }, []);

  const teacherOptions = teachers.map((teacher) => ({
    value: teacher.id,
    label: [teacher.firstName, teacher.lastName].filter(Boolean).join(' ') || teacher.username,
    meta: teacher.username ? `@${teacher.username}` : null,
  }));
  const subjectOptions = subjects.map((subject) => ({
    value: subject.id,
    label: subject.subjectName || subject.subjectNameMn || subject.name,
    meta: subject.subjectCode || null,
  }));
  const classOptions = classes.map((classItem) => ({
    value: classItem.id,
    label: classItem.className,
    meta: [classItem.grade ? `Grade ${classItem.grade}` : null, classItem.section].filter(Boolean).join(' • '),
  }));
  const semesterOptions = [
    { value: '1', label: t('admin.createTeachingAssignment.semester1') },
    { value: '2', label: t('admin.createTeachingAssignment.semester2') },
  ];

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await assignTeaching({
        teacherId: parseInt(form.teacherId),
        subjectId: parseInt(form.subjectId),
        classId: parseInt(form.classId),
        academicYear: form.academicYear,
        semester: parseInt(form.semester),
        isActive: true,
      });
      navigate('/admin/teaching-assignments');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create assignment.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createTeachingAssignment.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{t('admin.createTeachingAssignment.teacher')} *</label>
              {optionsLoading ? (
                <AdminFormFieldSkeleton withLabel={false} />
              ) : (
                <SearchableSelect options={teacherOptions} value={form.teacherId} onChange={(value) => setForm({ ...form, teacherId: String(value || '') })} placeholder={t('admin.createClass.selectTeacher')} searchPlaceholder={t('common.search')} emptyLabel="No teachers found" />
              )}
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createTeachingAssignment.subject')} *</label>
                {optionsLoading ? (
                  <AdminFormFieldSkeleton withLabel={false} />
                ) : (
                  <SearchableSelect options={subjectOptions} value={form.subjectId} onChange={(value) => setForm({ ...form, subjectId: String(value || '') })} placeholder={t('admin.createTeachingAssignment.selectSubject')} searchPlaceholder={t('common.search')} emptyLabel="No subjects found" />
                )}
              </div>
              <div className="form-group">
                <label>{t('admin.createTeachingAssignment.class')} *</label>
                {optionsLoading ? (
                  <AdminFormFieldSkeleton withLabel={false} />
                ) : (
                  <SearchableSelect options={classOptions} value={form.classId} onChange={(value) => setForm({ ...form, classId: String(value || '') })} placeholder={t('admin.createTeachingAssignment.selectClass')} searchPlaceholder={t('common.search')} emptyLabel="No classes found" />
                )}
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createTeachingAssignment.academicYear')} *</label>
                <input name="academicYear" className="form-control" value={form.academicYear} onChange={handleChange} required placeholder={t('admin.createClass.yearPlaceholder')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createTeachingAssignment.semester')} *</label>
                <SelectMenu options={semesterOptions} value={form.semester} onChange={(value) => setForm({ ...form, semester: value })} placeholder={t('admin.createTeachingAssignment.semester')} />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('admin.createTeachingAssignment.assigning') : t('admin.createTeachingAssignment.assignTeacher')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/teaching-assignments')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
