import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getAllUsers, linkParentStudent } from '../../api/endpoints';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';

export default function LinkParentStudent() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [parents, setParents] = useState([]);
  const [students, setStudents] = useState([]);
  const [parentsLoading, setParentsLoading] = useState(false);
  const [studentsLoading, setStudentsLoading] = useState(false);
  const [parentSearchInput, setParentSearchInput] = useState('');
  const [studentSearchInput, setStudentSearchInput] = useState('');
  const [debouncedParentSearch, setDebouncedParentSearch] = useState('');
  const [debouncedStudentSearch, setDebouncedStudentSearch] = useState('');
  const [selectedParentOption, setSelectedParentOption] = useState(null);
  const [selectedStudentOption, setSelectedStudentOption] = useState(null);
  const [form, setForm] = useState({
    parentId: '',
    studentId: '',
    relationship: 'FATHER',
    isPrimaryContact: false,
  });
  const relationshipOptions = [
    { value: 'FATHER', label: t('admin.parentStudents.father') },
    { value: 'MOTHER', label: t('admin.parentStudents.mother') },
    { value: 'GUARDIAN', label: t('admin.parentStudents.guardian') },
    { value: 'OTHER', label: t('admin.parentStudents.other') },
  ];

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedParentSearch(parentSearchInput.trim());
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [parentSearchInput]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedStudentSearch(studentSearchInput.trim());
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [studentSearchInput]);

  useEffect(() => {
    let ignore = false;
    setParentsLoading(true);

    getAllUsers({
      page: 1,
      pageSize: 25,
      role: 4,
      sortBy: 'name',
      sortOrder: 'asc',
      search: debouncedParentSearch || undefined,
    })
      .then((response) => {
        if (ignore) return;
        setParents(Array.isArray(response.data?.items) ? response.data.items : []);
      })
      .catch((err) => {
        if (ignore) return;
        console.error('Failed to load parents for parent-student link form', err);
        setParents([]);
      })
      .finally(() => {
        if (ignore) return;
        setParentsLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedParentSearch]);

  useEffect(() => {
    let ignore = false;
    setStudentsLoading(true);

    getAllUsers({
      page: 1,
      pageSize: 25,
      role: 1,
      sortBy: 'name',
      sortOrder: 'asc',
      search: debouncedStudentSearch || undefined,
    })
      .then((response) => {
        if (ignore) return;
        setStudents(Array.isArray(response.data?.items) ? response.data.items : []);
      })
      .catch((err) => {
        if (ignore) return;
        console.error('Failed to load students for parent-student link form', err);
        setStudents([]);
      })
      .finally(() => {
        if (ignore) return;
        setStudentsLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedStudentSearch]);

  const buildUserOption = (user) => ({
    value: user.id,
    label: [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || user.email || `#${user.id}`,
    meta: [user.username ? `@${user.username}` : null, user.email || null].filter(Boolean).join(' - ') || null,
  });

  const parentOptions = useMemo(() => parents.map(buildUserOption), [parents]);
  const studentOptions = useMemo(() => students.map(buildUserOption), [students]);

  useEffect(() => {
    if (!form.parentId) {
      setSelectedParentOption(null);
      return;
    }

    const matchingParent = parentOptions.find((option) => String(option.value) === String(form.parentId));
    if (matchingParent) {
      setSelectedParentOption(matchingParent);
    }
  }, [form.parentId, parentOptions]);

  useEffect(() => {
    if (!form.studentId) {
      setSelectedStudentOption(null);
      return;
    }

    const matchingStudent = studentOptions.find((option) => String(option.value) === String(form.studentId));
    if (matchingStudent) {
      setSelectedStudentOption(matchingStudent);
    }
  }, [form.studentId, studentOptions]);

  const availableParentOptions = useMemo(() => {
    if (!selectedParentOption) {
      return parentOptions;
    }

    return parentOptions.some((option) => String(option.value) === String(selectedParentOption.value))
      ? parentOptions
      : [selectedParentOption, ...parentOptions];
  }, [parentOptions, selectedParentOption]);
  const availableStudentOptions = useMemo(() => {
    if (!selectedStudentOption) {
      return studentOptions;
    }

    return studentOptions.some((option) => String(option.value) === String(selectedStudentOption.value))
      ? studentOptions
      : [selectedStudentOption, ...studentOptions];
  }, [selectedStudentOption, studentOptions]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await linkParentStudent({
        parentId: parseInt(form.parentId, 10),
        studentId: parseInt(form.studentId, 10),
        relationship: form.relationship,
        isPrimaryContact: form.isPrimaryContact,
      });
      navigate('/admin/parent-students');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to link.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header"><h1>{t('admin.linkParentStudent.title')}</h1></div>
      <div className="card">
        <div className="card-body">
          {error && <div className="alert alert-error">{typeof error === 'string' ? error : JSON.stringify(error)}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.parentStudents.parent')} *</label>
                <SearchableSelect
                  options={availableParentOptions}
                  value={form.parentId}
                  onChange={(value, option) => {
                    setForm((current) => ({ ...current, parentId: String(value || '') }));
                    setSelectedParentOption(option || null);
                  }}
                  searchValue={parentSearchInput}
                  onSearchChange={setParentSearchInput}
                  placeholder={t('admin.linkParentStudent.selectParent')}
                  searchPlaceholder={t('common.search')}
                  emptyLabel={t('admin.linkParentStudent.noParentsFound')}
                  loadingLabel={t('admin.linkParentStudent.loadingParents')}
                  isLoading={parentsLoading}
                  disabled={loading}
                />
              </div>
              <div className="form-group">
                <label>{t('admin.parentStudents.student')} *</label>
                <SearchableSelect
                  options={availableStudentOptions}
                  value={form.studentId}
                  onChange={(value, option) => {
                    setForm((current) => ({ ...current, studentId: String(value || '') }));
                    setSelectedStudentOption(option || null);
                  }}
                  searchValue={studentSearchInput}
                  onSearchChange={setStudentSearchInput}
                  placeholder={t('admin.linkParentStudent.selectStudent')}
                  searchPlaceholder={t('common.search')}
                  emptyLabel={t('admin.linkParentStudent.noStudentsFound')}
                  loadingLabel={t('admin.linkParentStudent.loadingStudents')}
                  isLoading={studentsLoading}
                  disabled={loading}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('admin.linkParentStudent.relationship')} *</label>
                <SelectMenu options={relationshipOptions} value={form.relationship} onChange={(value) => setForm({ ...form, relationship: value })} placeholder={t('admin.linkParentStudent.relationship')} />
              </div>
              <div className="form-group" style={{ display: 'flex', alignItems: 'end', gap: '0.5rem', paddingBottom: '0.25rem' }}>
                <input type="checkbox" name="isPrimaryContact" checked={form.isPrimaryContact} onChange={handleChange} id="primary" />
                <label htmlFor="primary" style={{ margin: 0 }}>{t('admin.linkParentStudent.primaryContact')}</label>
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading || !form.parentId || !form.studentId}>
                {loading ? t('admin.linkParentStudent.linking') : t('admin.linkParentStudent.linkButton')}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/parent-students')}>{t('common.cancel')}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
