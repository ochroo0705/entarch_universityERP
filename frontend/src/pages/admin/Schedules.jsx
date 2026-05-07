import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  deleteSchedule,
  generateSchedule,
  getClasses,
  getSchedules,
  getSubjects,
  getTeachers,
} from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

const SORTABLE_COLUMNS = {
  dayOfWeek: 'dayOfWeek',
  periodNumber: 'periodNumber',
  startTime: 'startTime',
  subject: 'subject',
  teacher: 'teacher',
  className: 'className',
  roomNumber: 'roomNumber',
};

function SortableHeader({ label, sortKey, sortBy, sortOrder, onSort }) {
  const isActive = sortBy === sortKey;
  const indicator = isActive ? (sortOrder === 'asc' ? '↑' : '↓') : '↕';

  return (
    <th>
      <button
        type="button"
        className={`table-sort-button${isActive ? ' is-active' : ''}`}
        onClick={() => onSort(sortKey)}
      >
        <span>{label}</span>
        <span className="table-sort-indicator" aria-hidden="true">{indicator}</span>
      </button>
    </th>
  );
}

function ScheduleSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`schedule-skeleton-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-button" /></td>
    </tr>
  ));
}

function ScheduleSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`schedule-card-skeleton-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
      <div className="data-card-header">
        <div>
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-card-subtitle" />
        </div>
        <div className="users-skeleton users-skeleton-pill" />
      </div>
      <div className="data-card-meta">
        <div className="data-card-meta-row">
          <span className="users-skeleton users-skeleton-meta-label" />
          <span className="users-skeleton users-skeleton-meta-value" />
        </div>
        <div className="data-card-meta-row">
          <span className="users-skeleton users-skeleton-meta-label" />
          <span className="users-skeleton users-skeleton-meta-value" />
        </div>
        <div className="data-card-meta-row">
          <span className="users-skeleton users-skeleton-meta-label" />
          <span className="users-skeleton users-skeleton-meta-value" />
        </div>
      </div>
      <div className="users-skeleton users-skeleton-card-button" />
    </article>
  ));
}

export default function Schedules() {
  const { t } = useTranslation();
  const dayNames = ['', t('days.monday'), t('days.tuesday'), t('days.wednesday'), t('days.thursday'), t('days.friday'), t('days.saturday'), t('days.sunday')];
  const [schedules, setSchedules] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [generating, setGenerating] = useState(false);
  const [showGenModal, setShowGenModal] = useState(false);
  const [clearExisting, setClearExisting] = useState(true);
  const [genResult, setGenResult] = useState(null);
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    dayOfWeek: 'all',
    teacherId: 'all',
    subjectId: 'all',
    classId: 'all',
    grade: 'all',
    sortBy: 'dayOfWeek',
    sortOrder: 'asc',
  });
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    totalPages: 0,
  });
  const skeletonRowCount = Math.max(Math.min(Number(query.pageSize) || DEFAULT_PAGE_SIZE, 10), 5);
  const skeletonCardCount = Math.max(Math.min(Number(query.pageSize) || DEFAULT_PAGE_SIZE, 6), 3);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedSearch(searchInput.trim());
      setQuery((current) => ({ ...current, page: 1 }));
    }, 350);

    return () => window.clearTimeout(timeoutId);
  }, [searchInput]);

  useEffect(() => {
    Promise.all([getTeachers(), getSubjects(), getClasses()])
      .then(([teacherRes, subjectRes, classRes]) => {
        setTeachers(Array.isArray(teacherRes.data) ? teacherRes.data : []);
        setSubjects(Array.isArray(subjectRes.data) ? subjectRes.data : []);
        const classData = Array.isArray(classRes.data) ? classRes.data : classRes.data?.content ?? [];
        setClasses(classData);
      })
      .catch((err) => {
        console.error('Failed to load schedule filters', err);
      });
  }, []);

  useEffect(() => {
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getSchedules({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      dayOfWeek: query.dayOfWeek === 'all' ? undefined : Number(query.dayOfWeek),
      teacherId: query.teacherId === 'all' ? undefined : Number(query.teacherId),
      subjectId: query.subjectId === 'all' ? undefined : Number(query.subjectId),
      classId: query.classId === 'all' ? undefined : Number(query.classId),
      grade: query.grade === 'all' ? undefined : Number(query.grade),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setSchedules(Array.isArray(data.items) ? data.items : []);
        setPagination({
          page: data.page ?? 1,
          pageSize: data.pageSize ?? query.pageSize,
          total: data.total ?? 0,
          totalPages: data.totalPages ?? 0,
        });
        setError('');
      })
      .catch((err) => {
        if (ignore) return;
        console.error('Failed to load schedules', err);
        setError(err.response?.data?.message || 'Unable to load schedules');
      })
      .finally(() => {
        if (ignore) return;
        setLoading(false);
        setRefreshing(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedSearch, query, reloadToken]);

  const gradeOptions = useMemo(
    () => [...new Set(classes.map((item) => item.grade).filter(Boolean))].sort((a, b) => a - b),
    [classes]
  );
  const dayOptions = useMemo(() => [
    { value: 'all', label: t('admin.schedules.allDays') },
    { value: '1', label: t('days.monday') },
    { value: '2', label: t('days.tuesday') },
    { value: '3', label: t('days.wednesday') },
    { value: '4', label: t('days.thursday') },
    { value: '5', label: t('days.friday') },
    { value: '6', label: t('days.saturday') },
    { value: '7', label: t('days.sunday') },
  ], [t]);
  const teacherOptions = useMemo(() => [{ value: 'all', label: t('admin.schedules.teacher') }, ...teachers.map((teacher) => ({ value: String(teacher.id), label: [teacher.firstName, teacher.lastName].filter(Boolean).join(' ') || teacher.username }))], [teachers, t]);
  const subjectOptions = useMemo(() => [{ value: 'all', label: t('admin.schedules.subject') }, ...subjects.map((subject) => ({ value: String(subject.id), label: subject.subjectNameMn || subject.name }))], [subjects, t]);
  const classOptions = useMemo(() => [{ value: 'all', label: t('admin.schedules.class') }, ...classes.map((classItem) => ({ value: String(classItem.id), label: classItem.className }))], [classes, t]);
  const gradeFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.classes.allGrades') }, ...gradeOptions.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))], [gradeOptions, t]);
  const pageSizeOptions = useMemo(() => [10, 20, 50, 100].map((size) => ({ value: String(size), label: `${size} / page` })), []);

  const hasFilters = Boolean(debouncedSearch)
    || query.dayOfWeek !== 'all'
    || query.teacherId !== 'all'
    || query.subjectId !== 'all'
    || query.classId !== 'all'
    || query.grade !== 'all';

  const handleFilterChange = (key) => (event) => {
    const rawValue = event.target.value;
    const value = key === 'pageSize' ? Number(rawValue) : rawValue;
    setQuery((current) => ({
      ...current,
      [key]: value,
      page: 1,
    }));
  };

  const handleSort = (sortKey) => {
    if (!(sortKey in SORTABLE_COLUMNS)) return;
    setQuery((current) => ({
      ...current,
      sortBy: sortKey,
      sortOrder: current.sortBy === sortKey && current.sortOrder === 'asc' ? 'desc' : 'asc',
      page: 1,
    }));
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 1 || nextPage > pagination.totalPages || nextPage === query.page) return;
    setQuery((current) => ({ ...current, page: nextPage }));
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.schedules.confirmDelete'))) return;
    try {
      await deleteSchedule(id);
      setReloadToken((value) => value + 1);
    } catch (err) {
      window.alert(err.response?.data?.message || 'Failed to delete schedule');
    }
  };

  const handleGenerate = async () => {
    setGenerating(true);
    setGenResult(null);
    try {
      const res = await generateSchedule(clearExisting);
      setGenResult({ success: true, count: Array.isArray(res.data) ? res.data.length : 0 });
      setShowGenModal(false);
      setReloadToken((value) => value + 1);
    } catch (err) {
      const message = err.response?.data?.message || err.response?.data || 'Failed to generate schedule';
      setGenResult({ success: false, message: String(message) });
    } finally {
      setGenerating(false);
    }
  };

  const retryLoad = () => {
    setError('');
    setLoading(true);
    setReloadToken((value) => value + 1);
  };

  const paginationSummary = pagination.total === 0
    ? 'Showing 0-0 of 0'
    : `Showing ${(pagination.page - 1) * pagination.pageSize + 1}-${Math.min(pagination.page * pagination.pageSize, pagination.total)} of ${pagination.total}`;

  if (loading) {
    return <LoadingState label={t('common.loading')} />;
  }

  if (error) {
    return (
      <ErrorState
        title="Unable to load schedules"
        description={error}
        retryLabel={t('admin.users.retry')}
        onRetry={retryLoad}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{t('admin.schedules.title')}</h1>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button type="button" className="btn btn-success" onClick={() => setShowGenModal(true)}>
            {t('admin.schedules.autoGenerate')}
          </button>
          <Link to="/admin/schedules/create" className="btn btn-primary">{t('admin.schedules.createSchedule')}</Link>
        </div>
      </div>

      {genResult ? (
        <div style={{
          padding: '0.75rem 1rem',
          marginBottom: '1rem',
          borderRadius: '8px',
          background: genResult.success ? 'var(--success-bg, #d4edda)' : 'var(--danger-bg, #f8d7da)',
          color: genResult.success ? '#155724' : '#721c24',
          border: `1px solid ${genResult.success ? '#c3e6cb' : '#f5c6cb'}`,
        }}
        >
          {genResult.success
            ? t('admin.schedules.genSuccess', { count: genResult.count })
            : genResult.message}
          <button type="button" onClick={() => setGenResult(null)} style={{ float: 'right', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>x</button>
        </div>
      ) : null}

      {showGenModal ? (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
        }}
        >
          <div className="card" style={{ maxWidth: '480px', width: '90%', padding: '1.5rem' }}>
            <h2 style={{ marginTop: 0 }}>{t('admin.schedules.autoGenerate')}</h2>
            <p style={{ color: 'var(--text-muted)' }}>{t('admin.schedules.genDescription')}</p>
            <p><strong>{t('admin.schedules.algorithm')}:</strong></p>
            <ul style={{ fontSize: '0.9rem', color: 'var(--text-muted)', lineHeight: '1.6' }}>
              <li>{t('admin.schedules.genRule1')}</li>
              <li>{t('admin.schedules.genRule2')}</li>
              <li>{t('admin.schedules.genRule3')}</li>
              <li>{t('admin.schedules.genRule4')}</li>
              <li>{t('admin.schedules.genRule5')}</li>
            </ul>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', cursor: 'pointer' }}>
              <input type="checkbox" checked={clearExisting} onChange={(event) => setClearExisting(event.target.checked)} />
              {t('admin.schedules.clearExisting')}
            </label>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button type="button" className="btn" onClick={() => setShowGenModal(false)} disabled={generating}>
                {t('common.cancel')}
              </button>
              <button type="button" className="btn btn-success" onClick={handleGenerate} disabled={generating}>
                {generating ? t('admin.schedules.generating') : t('admin.schedules.generateSchedule')}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.schedules.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu options={dayOptions} value={query.dayOfWeek} onChange={(value) => handleFilterChange('dayOfWeek')({ target: { value } })} placeholder={t('admin.schedules.allDays')} />
        <SearchableSelect options={teacherOptions} value={query.teacherId} onChange={(value) => handleFilterChange('teacherId')({ target: { value } })} placeholder={t('admin.schedules.teacher')} searchPlaceholder={t('common.search')} emptyLabel="No teachers found" />
        <SearchableSelect options={subjectOptions} value={query.subjectId} onChange={(value) => handleFilterChange('subjectId')({ target: { value } })} placeholder={t('admin.schedules.subject')} searchPlaceholder={t('common.search')} emptyLabel="No subjects found" />
        <SearchableSelect options={classOptions} value={query.classId} onChange={(value) => handleFilterChange('classId')({ target: { value } })} placeholder={t('admin.schedules.class')} searchPlaceholder={t('common.search')} emptyLabel="No classes found" />
        <SelectMenu options={gradeFilterOptions} value={query.grade} onChange={(value) => handleFilterChange('grade')({ target: { value } })} placeholder={t('admin.classes.allGrades')} />
        <SelectMenu options={pageSizeOptions} value={String(query.pageSize)} onChange={(value) => handleFilterChange('pageSize')({ target: { value } })} placeholder="Page size" />
        <span className="filter-result-count">{pagination.total} schedules</span>
      </div>

      <SectionCard title={t('admin.schedules.title')} subtitle={`Page ${pagination.page} of ${Math.max(pagination.totalPages, 1)}`}>
        <div className="content-stack" aria-busy={refreshing}>
          {schedules.length === 0 ? (
            <EmptyState
              title={hasFilters ? 'No schedules match these filters' : t('admin.schedules.noSchedules')}
              description={hasFilters ? 'Try clearing a filter or widening the search.' : 'Schedules will appear here after they are created or generated.'}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <SortableHeader label={t('admin.schedules.day')} sortKey="dayOfWeek" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.period')} sortKey="periodNumber" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.time')} sortKey="startTime" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.subject')} sortKey="subject" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.teacher')} sortKey="teacher" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.class')} sortKey="className" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.schedules.room')} sortKey="roomNumber" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing
                      ? <ScheduleSkeletonRows count={skeletonRowCount} />
                      : schedules.map((schedule) => (
                        <tr key={schedule.id}>
                          <td>{schedule.id}</td>
                          <td>{dayNames[schedule.dayOfWeek] || schedule.dayOfWeek}</td>
                          <td>{schedule.periodNumber}</td>
                          <td>{schedule.startTime} - {schedule.endTime}</td>
                          <td>{schedule.subject}</td>
                          <td>{schedule.teacher}</td>
                          <td>{schedule.className}</td>
                          <td>{schedule.roomNumber || '-'}</td>
                          <td>
                            <button type="button" className="btn btn-danger btn-sm" onClick={() => handleDelete(schedule.id)}>
                              {t('common.delete')}
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing
                  ? <ScheduleSkeletonCards count={skeletonCardCount} />
                  : schedules.map((schedule) => (
                    <article key={schedule.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{schedule.subject || '-'}</div>
                          <div className="muted-copy">{schedule.className || '-'}</div>
                        </div>
                        <span className="badge badge-info">{dayNames[schedule.dayOfWeek] || schedule.dayOfWeek}</span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('admin.schedules.period')}</span>
                          <span>{schedule.periodNumber ?? '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.schedules.time')}</span>
                          <span>{schedule.startTime} - {schedule.endTime}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.schedules.teacher')}</span>
                          <span>{schedule.teacher || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.schedules.room')}</span>
                          <span>{schedule.roomNumber || '-'}</span>
                        </div>
                      </div>

                      <button type="button" className="btn btn-danger btn-block" onClick={() => handleDelete(schedule.id)}>
                        {t('common.delete')}
                      </button>
                    </article>
                  ))}
              </div>
            </>
          )}

          <div className="pagination-toolbar">
            <div className="pagination-summary">{paginationSummary}</div>
            <div className="pagination-actions">
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(query.page - 1)} disabled={query.page <= 1}>
                {t('common.prev')}
              </button>
              <span className="pagination-page-label">Page {pagination.page} / {Math.max(pagination.totalPages, 1)}</span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => handlePageChange(query.page + 1)}
                disabled={pagination.totalPages === 0 || query.page >= pagination.totalPages}
              >
                {t('common.next')}
              </button>
            </div>
          </div>
        </div>
      </SectionCard>
    </div>
  );
}
