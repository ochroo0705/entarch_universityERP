import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createSubject, setTranslationsBulk } from '../../api/endpoints';

export default function CreateSubject() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    subjectName: '',
    nameEn: '',
    subjectCode: '',
    gradeLevel: '',
    hoursPerWeek: '',
    isMandatory: true,
  });

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? e.target.checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await createSubject({
        subjectName: form.subjectName,
        subjectNameMn: form.subjectName,
        subjectCode: form.subjectCode,
        gradeLevel: form.gradeLevel ? parseInt(form.gradeLevel) : null,
        hoursPerWeek: form.hoursPerWeek ? parseInt(form.hoursPerWeek) : null,
        isMandatory: form.isMandatory,
      });
      const subjectId = res.data?.id;
      if (subjectId && form.nameEn.trim()) {
        await setTranslationsBulk([
          { entityType: 'subject', entityId: subjectId, fieldName: 'name', locale: 'en', value: form.nameEn },
        ]).catch(() => {});
      }
      navigate('/admin/subjects');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create subject.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createSubject.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createSubject.subjectName')} *</label>
                <input name="subjectName" className="form-control" value={form.subjectName} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createSubject.subjectNameEn')}</label>
                <input name="nameEn" className="form-control" value={form.nameEn} onChange={handleChange} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createSubject.subjectCode')} *</label>
                <input name="subjectCode" className="form-control" value={form.subjectCode} onChange={handleChange} required placeholder={t('admin.createSubject.codePlaceholder')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createSubject.gradeLevel')}</label>
                <input name="gradeLevel" type="number" className="form-control" value={form.gradeLevel} onChange={handleChange} min="1" max="12" />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createSubject.hoursPerWeek')}</label>
                <input name="hoursPerWeek" type="number" className="form-control" value={form.hoursPerWeek} onChange={handleChange} min="1" />
              </div>
              <div className="form-group" style={{ display: 'flex', alignItems: 'end', gap: '0.5rem', paddingBottom: '0.25rem' }}>
                <input type="checkbox" name="isMandatory" checked={form.isMandatory} onChange={handleChange} id="mandatory" />
                <label htmlFor="mandatory" style={{ margin: 0 }}>{t('admin.createSubject.mandatorySubject')}</label>
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createSubject.title')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/subjects')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
