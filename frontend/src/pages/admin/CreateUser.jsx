import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { createTeacher, createStudent, createParent } from '../../api/endpoints';
import SelectMenu from '../../components/ui/SelectMenu';

export default function CreateUser() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [role, setRole] = useState('TEACHER');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    username: '',
    email: '',
    passwordHash: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    gender: 'M',
    teacherSubjects: '',
  });
  const roleOptions = [
    { value: 'TEACHER', label: t('roles.teacher') },
    { value: 'STUDENT', label: t('roles.student') },
    { value: 'PARENT', label: t('roles.parent') },
  ];
  const genderOptions = [
    { value: 'M', label: t('admin.createUser.male') },
    { value: 'F', label: t('admin.createUser.female') },
    { value: 'Other', label: t('admin.createUser.other') },
  ];

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const getRoleFlags = () => {
    switch (role) {
      case 'TEACHER': return 2;
      case 'STUDENT': return 1;
      case 'PARENT': return 4;
      default: return 0;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const payload = {
      ...form,
      roleFlags: getRoleFlags(),
      isActive: true,
    };

    try {
      if (role === 'TEACHER') await createTeacher(payload);
      else if (role === 'STUDENT') await createStudent(payload);
      else await createParent(payload);

      setSuccess(`${role} created successfully!`);
      setForm({
        username: '', email: '', passwordHash: '', firstName: '',
        lastName: '', phone: '', address: '', dateOfBirth: '', gender: 'M', teacherSubjects: '',
      });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to create user.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1>{t('admin.createUser.title')}</h1>
      </div>

      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{t('admin.createUser.userRole')}</label>
              <SelectMenu options={roleOptions} value={role} onChange={setRole} placeholder={t('admin.createUser.userRole')} />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createUser.firstName')}</label>
                <input name="firstName" className="form-control" value={form.firstName} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createUser.lastName')}</label>
                <input name="lastName" className="form-control" value={form.lastName} onChange={handleChange} required />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createUser.username')}</label>
                <input name="username" className="form-control" value={form.username} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createUser.password')}</label>
                <input name="passwordHash" type="password" className="form-control" value={form.passwordHash} onChange={handleChange} required />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createUser.email')}</label>
                <input name="email" type="email" className="form-control" value={form.email} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>{t('admin.createUser.phone')}</label>
                <input name="phone" className="form-control" value={form.phone} onChange={handleChange} />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.createUser.dateOfBirth')}</label>
                <input name="dateOfBirth" type="date" className="form-control" value={form.dateOfBirth} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>{t('admin.createUser.gender')}</label>
                <SelectMenu options={genderOptions} value={form.gender} onChange={(value) => setForm({ ...form, gender: value })} placeholder={t('admin.createUser.gender')} />
              </div>
            </div>

            <div className="form-group">
              <label>{t('admin.createUser.address')}</label>
              <input name="address" className="form-control" value={form.address} onChange={handleChange} />
            </div>

            {role === 'TEACHER' && (
              <div className="form-group">
                <label>{t('admin.createUser.subjects')}</label>
                <input name="teacherSubjects" className="form-control" value={form.teacherSubjects} onChange={handleChange} placeholder={t('admin.createUser.subjectsPlaceholder')} />
              </div>
            )}

            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? t('common.creating') : t('admin.createUser.createRole', { role })}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/users')}>
                {t('common.cancel')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
