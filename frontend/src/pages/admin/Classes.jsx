import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { deactivateClass, getClasses } from '../../api/endpoints';
import { AdminFilterRowSkeleton, AdminPageHeaderSkeleton, AdminTableSkeleton } from '../../components/ui/AdminPageSkeletons';
import SelectMenu from '../../components/ui/SelectMenu';

export default function Classes() {
  const { t } = useTranslation();
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [gradeFilter, setGradeFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');

  const load = async () => {
    try {
      const res = await getClasses();
      const data = res.data;
      setClasses(Array.isArray(data) ? data : data.content ?? []);
    } catch (err) {
      console.error('Failed to load classes', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleDeactivate = async (id) => {
    if (!window.confirm(t('admin.classes.confirmDeactivate'))) return;
    try {
      await deactivateClass(id);
      load();
    } catch {
      alert('Failed to deactivate class');
    }
  };

  const grades = [...new Set(classes.map((item) => item.grade))].sort((a, b) => a - b);
  const gradeOptions = [{ value: 'all', label: t('admin.classes.allGrades') }, ...grades.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))];
  const statusOptions = [
    { value: 'all', label: t('common.allStatus') },
    { value: 'active', label: t('common.active') },
    { value: 'inactive', label: t('common.inactive') },
  ];

  const filtered = classes.filter((item) => {
    if (gradeFilter !== 'all' && item.grade !== parseInt(gradeFilter, 10)) return false;
    if (statusFilter === 'active' && item.isActive === false) return false;
    if (statusFilter === 'inactive' && item.isActive !== false) return false;
    if (search.trim()) {
      const query = search.toLowerCase();
      return (
        item.className?.toLowerCase().includes(query) ||
        item.section?.toLowerCase().includes(query) ||
        item.roomNumber?.toLowerCase().includes(query) ||
        item.academicYear?.toLowerCase().includes(query)
      );
    }
    return true;
  });

  if (loading) {
    return (
      <div className="content-stack">
        <AdminPageHeaderSkeleton />
        <AdminFilterRowSkeleton fields={3} />
        <AdminTableSkeleton columns={8} rows={6} mobileCards={4} />
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>{t('admin.classes.title')}</h1>
        <Link to="/admin/classes/create" className="btn btn-primary">{t('admin.classes.createClass')}</Link>
      </div>

      <div className="list-filter-row">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.classes.searchPlaceholder')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <div className="list-filter-select">
          <SelectMenu options={gradeOptions} value={gradeFilter} onChange={setGradeFilter} placeholder={t('admin.classes.allGrades')} />
        </div>
        <div className="list-filter-select">
          <SelectMenu options={statusOptions} value={statusFilter} onChange={setStatusFilter} placeholder={t('common.allStatus')} />
        </div>
        <span className="list-filter-count">
          {t('common.class', { count: filtered.length })}
        </span>
      </div>

      <div className="card">
        <div className="table-container desktop-table">
          <table>
            <thead>
              <tr>
                <th>{t('common.id')}</th>
                <th>{t('admin.classes.className')}</th>
                <th>{t('common.grade')}</th>
                <th>{t('admin.classes.section')}</th>
                <th>{t('admin.classes.room')}</th>
                <th>{t('admin.classes.academicYear')}</th>
                <th>{t('admin.classes.students')}</th>
                <th>{t('common.status')}</th>
                <th>{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr><td colSpan={9} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('admin.classes.noClasses')}</td></tr>
              ) : (
                filtered.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.className}</td>
                    <td>{item.grade}</td>
                    <td>{item.section}</td>
                    <td>{item.roomNumber}</td>
                    <td>{item.academicYear}</td>
                    <td>{item.studentCount ?? 0}</td>
                    <td>
                      <span className={`badge ${item.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
                        {item.isActive !== false ? t('common.active') : t('common.inactive')}
                      </span>
                    </td>
                    <td>
                      {item.isActive !== false ? (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDeactivate(item.id)}>
                          {t('common.deactivate')}
                        </button>
                      ) : null}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="card-body mobile-card-list">
          {filtered.length === 0 ? (
            <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('admin.classes.noClasses')}</div>
          ) : filtered.map((item) => (
            <article key={`mobile-${item.id}`} className="data-card">
              <div className="data-card-header">
                <div>
                  <div className="data-card-title">{item.className}</div>
                  <div className="muted-copy">{t('admin.classes.gradeN', { n: item.grade })} - {t('admin.classes.section')} {item.section}</div>
                </div>
                <span className={`badge ${item.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
                  {item.isActive !== false ? t('common.active') : t('common.inactive')}
                </span>
              </div>

              <div className="data-card-meta">
                <div className="data-card-meta-row">
                  <span>{t('common.id')}</span>
                  <strong>{item.id}</strong>
                </div>
                <div className="data-card-meta-row">
                  <span>{t('admin.classes.room')}</span>
                  <strong>{item.roomNumber || '-'}</strong>
                </div>
                <div className="data-card-meta-row">
                  <span>{t('admin.classes.academicYear')}</span>
                  <strong>{item.academicYear || '-'}</strong>
                </div>
                <div className="data-card-meta-row">
                  <span>{t('admin.classes.students')}</span>
                  <strong>{item.studentCount ?? 0}</strong>
                </div>
              </div>

              {item.isActive !== false ? (
                <button className="btn btn-danger btn-block" onClick={() => handleDeactivate(item.id)}>
                  {t('common.deactivate')}
                </button>
              ) : null}
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
