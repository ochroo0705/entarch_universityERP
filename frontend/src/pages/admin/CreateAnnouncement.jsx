import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createAnnouncement, getClasses, setTranslationsBulk } from '../../api/endpoints';
import { AdminFormFieldSkeleton } from '../../components/ui/AdminPageSkeletons';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';

export default function CreateAnnouncement() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [classes, setClasses] = useState([]);
  const [classesLoading, setClassesLoading] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '',
    content: '',
    titleEn: '',
    contentEn: '',
    priority: 'normal',
    targetClassId: '',
    targetStudents: true,
    targetTeachers: true,
    targetParents: true,
    targetAdmins: true,
  });

  useEffect(() => {
    getClasses()
      .then((res) => setClasses(res.data))
      .catch(() => setClasses([]))
      .finally(() => setClassesLoading(false));
  }, []);

  const priorityOptions = [
    { value: 'low', label: t('admin.announcements.low') },
    { value: 'normal', label: t('admin.announcements.normal') },
    { value: 'high', label: t('admin.announcements.high') },
    { value: 'urgent', label: t('admin.announcements.urgent') },
  ];
  const classOptions = classes.map((classItem) => ({
    value: classItem.id,
    label: classItem.className,
    meta: [classItem.grade ? `Grade ${classItem.grade}` : null, classItem.section].filter(Boolean).join(' • '),
  }));

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    let targetRoleFlags = 0;
    if (form.targetStudents) targetRoleFlags |= 1;
    if (form.targetTeachers) targetRoleFlags |= 2;
    if (form.targetParents) targetRoleFlags |= 4;
    if (form.targetAdmins) targetRoleFlags |= 8;

    const payload = {
      title: form.title,
      content: form.content,
      priority: form.priority,
      targetRoleFlags,
    };
    if (form.targetClassId) {
      payload.targetClass = { id: parseInt(form.targetClassId) };
    }

    try {
      const res = await createAnnouncement(payload);
      const announcementId = res.data?.id;
      if (announcementId) {
        const translations = [];
        if (form.titleEn.trim()) {
          translations.push({ entityType: 'announcement', entityId: announcementId, fieldName: 'title', locale: 'en', value: form.titleEn });
        }
        if (form.contentEn.trim()) {
          translations.push({ entityType: 'announcement', entityId: announcementId, fieldName: 'content', locale: 'en', value: form.contentEn });
        }
        if (translations.length > 0) {
          await setTranslationsBulk(translations).catch(() => {});
        }
      }
      navigate('/admin/announcements');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create announcement.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.createAnnouncement.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{t('admin.createAnnouncement.announcementTitle')} *</label>
              <input name="title" className="form-control" value={form.title} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>{t('admin.createAnnouncement.announcementTitle')} ({t('common.english')})</label>
              <input name="titleEn" className="form-control" value={form.titleEn} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>{t('admin.createAnnouncement.content')} *</label>
              <textarea name="content" className="form-control" rows={4} value={form.content} onChange={handleChange} required style={{ resize: 'vertical' }} />
            </div>
            <div className="form-group">
              <label>{t('admin.createAnnouncement.content')} ({t('common.english')})</label>
              <textarea name="contentEn" className="form-control" rows={4} value={form.contentEn} onChange={handleChange} style={{ resize: 'vertical' }} />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createAnnouncement.priority')}</label>
                <SelectMenu options={priorityOptions} value={form.priority} onChange={(value) => setForm({ ...form, priority: value })} placeholder={t('admin.createAnnouncement.priority')} />
              </div>
              <div className="form-group">
                <label>{t('admin.createAnnouncement.targetClass')}</label>
                {classesLoading ? (
                  <AdminFormFieldSkeleton withLabel={false} />
                ) : (
                  <SearchableSelect
                    options={classOptions}
                    value={form.targetClassId}
                    onChange={(value) => setForm({ ...form, targetClassId: String(value || '') })}
                    placeholder={t('admin.createAnnouncement.allClasses')}
                    searchPlaceholder={t('common.search')}
                    emptyLabel="No classes found"
                  />
                )}
              </div>
            </div>
            <div className="form-group">
              <label>{t('admin.createAnnouncement.targetAudience')}</label>
              <div style={{ display: 'flex', gap: '1.5rem', marginTop: '0.5rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 400 }}>
                  <input type="checkbox" name="targetStudents" checked={form.targetStudents} onChange={handleChange} /> {t('roles.student')}
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 400 }}>
                  <input type="checkbox" name="targetTeachers" checked={form.targetTeachers} onChange={handleChange} /> {t('roles.teacher')}
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 400 }}>
                  <input type="checkbox" name="targetParents" checked={form.targetParents} onChange={handleChange} /> {t('roles.parent')}
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 400 }}>
                  <input type="checkbox" name="targetAdmins" checked={form.targetAdmins} onChange={handleChange} /> {t('roles.admin')}
                </label>
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createAnnouncement.createAnnouncement')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/announcements')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
