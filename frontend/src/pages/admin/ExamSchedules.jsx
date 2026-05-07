import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { deleteExamSchedule, getClasses, getExamSchedules, getSubjects, getTeachers } from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

function ExamScheduleSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`exam-schedule-card-skeleton-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function ExamSchedules() {
  const { t } = useTranslation();
  const [items, setItems] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    teacherId: 'all',
    subjectId: 'all',
    classId: 'all',
    published: 'all',
    examDate: '',
  });
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    totalPages: 0,
  });
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
        setClasses(Array.isArray(classRes.data) ? classRes.data : classRes.data?.content ?? []);
      })
      .catch((err) => {
        console.error('Failed to load exam schedule filters', err);
      });
  }, []);

  useEffect(() => {
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getExamSchedules({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      teacherId: query.teacherId === 'all' ? undefined : Number(query.teacherId),
      subjectId: query.subjectId === 'all' ? undefined : Number(query.subjectId),
      classId: query.classId === 'all' ? undefined : Number(query.classId),
      published: query.published === 'all' ? undefined : query.published === 'published',
      examDate: query.examDate || undefined,
      sortBy: 'examDate',
      sortOrder: 'asc',
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setItems(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load exam schedules', err);
        setError(err.response?.data?.message || 'Unable to load exam schedules');
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

  const hasFilters = Boolean(debouncedSearch)
    || query.teacherId !== 'all'
    || query.subjectId !== 'all'
    || query.classId !== 'all'
    || query.published !== 'all'
    || Boolean(query.examDate);

  const paginationSummary = useMemo(() => {
    if (pagination.total === 0) return 'Showing 0-0 of 0';
    return `Showing ${(pagination.page - 1) * pagination.pageSize + 1}-${Math.min(pagination.page * pagination.pageSize, pagination.total)} of ${pagination.total}`;
  }, [pagination]);
  const teacherOptions = useMemo(() => [{ value: 'all', label: t('admin.examSchedules.teacher') }, ...teachers.map((teacher) => ({ value: String(teacher.id), label: [teacher.firstName, teacher.lastName].filter(Boolean).join(' ') || teacher.username }))], [teachers, t]);
  const subjectOptions = useMemo(() => [{ value: 'all', label: t('admin.examSchedules.subject') }, ...subjects.map((subject) => ({ value: String(subject.id), label: subject.subjectNameMn || subject.name }))], [subjects, t]);
  const classOptions = useMemo(() => [{ value: 'all', label: t('admin.examSchedules.class') }, ...classes.map((classItem) => ({ value: String(classItem.id), label: classItem.className }))], [classes, t]);
  const publishedOptions = useMemo(() => [
    { value: 'all', label: t('admin.examSchedules.allPublicationStates') },
    { value: 'published', label: t('admin.examSchedules.published') },
    { value: 'draft', label: t('admin.examSchedules.draft') },
  ], [t]);

  const handleFilterChange = (key) => (event) => {
    const value = key === 'pageSize' ? Number(event.target.value) : event.target.value;
    setQuery((current) => ({ ...current, [key]: value, page: 1 }));
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 1 || nextPage > pagination.totalPages || nextPage === query.page) return;
    setQuery((current) => ({ ...current, page: nextPage }));
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.examSchedules.confirmDelete'))) return;
    try {
      await deleteExamSchedule(id);
      setReloadToken((value) => value + 1);
    } catch (err) {
      window.alert(err.response?.data?.message || t('admin.examSchedules.failedDelete'));
    }
  };

  if (loading) {
    return <LoadingState label={t('common.loading')} />;
  }

  if (error) {
    return (
      <ErrorState
        title={t('admin.examSchedules.loadErrorTitle')}
        description={error}
        retryLabel={t('admin.users.retry')}
        onRetry={() => {
          setError('');
          setLoading(true);
          setReloadToken((value) => value + 1);
        }}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{t('admin.examSchedules.title')}</h1>
          <p className="page-summary">{t('admin.examSchedules.summary')}</p>
        </div>
        <Link to="/admin/exam-schedules/create" className="btn btn-primary">{t('admin.examSchedules.create')}</Link>
      </div>

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.examSchedules.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <input type="date" className="form-control" value={query.examDate} onChange={handleFilterChange('examDate')} />
        <SearchableSelect options={teacherOptions} value={query.teacherId} onChange={(value) => handleFilterChange('teacherId')({ target: { value } })} placeholder={t('admin.examSchedules.teacher')} searchPlaceholder={t('common.search')} emptyLabel="No teachers found" />
        <SearchableSelect options={subjectOptions} value={query.subjectId} onChange={(value) => handleFilterChange('subjectId')({ target: { value } })} placeholder={t('admin.examSchedules.subject')} searchPlaceholder={t('common.search')} emptyLabel="No subjects found" />
        <SearchableSelect options={classOptions} value={query.classId} onChange={(value) => handleFilterChange('classId')({ target: { value } })} placeholder={t('admin.examSchedules.class')} searchPlaceholder={t('common.search')} emptyLabel="No classes found" />
        <SelectMenu options={publishedOptions} value={query.published} onChange={(value) => handleFilterChange('published')({ target: { value } })} placeholder={t('admin.examSchedules.allPublicationStates')} />
      </div>

      <SectionCard title={t('admin.examSchedules.title')} subtitle={`Page ${pagination.page} of ${Math.max(pagination.totalPages, 1)}`}>
        <div className="content-stack" aria-busy={refreshing}>
          {!items.length ? (
            <EmptyState
              title={hasFilters ? t('admin.examSchedules.noMatches') : t('admin.examSchedules.emptyTitle')}
              description={hasFilters ? t('admin.examSchedules.noMatchesDescription') : t('admin.examSchedules.emptyDescription')}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <th>{t('admin.examSchedules.date')}</th>
                      <th>{t('admin.examSchedules.time')}</th>
                      <th>{t('admin.examSchedules.titleColumn')}</th>
                      <th>{t('admin.examSchedules.subject')}</th>
                      <th>{t('admin.examSchedules.teacher')}</th>
                      <th>{t('admin.examSchedules.class')}</th>
                      <th>{t('admin.examSchedules.room')}</th>
                      <th>{t('admin.examSchedules.status')}</th>
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.id}>
                        <td>{item.id}</td>
                        <td>{item.examDate}</td>
                        <td>{item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}</td>
                        <td>{item.title}</td>
                        <td>{item.subject}</td>
                        <td>{item.teacher}</td>
                        <td>{item.className}</td>
                        <td>{item.roomNumber || '-'}</td>
                        <td>
                          <span className={`badge ${item.published ? 'badge-success' : 'badge-warning'}`}>
                            {item.published ? t('admin.examSchedules.published') : t('admin.examSchedules.draft')}
                          </span>
                        </td>
                        <td>
                          <button type="button" className="btn btn-danger btn-sm" onClick={() => handleDelete(item.id)}>
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
                  ? <ExamScheduleSkeletonCards count={skeletonCardCount} />
                  : items.map((item) => (
                    <article key={item.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{item.title || '-'}</div>
                          <div className="muted-copy">{item.subject || '-'}</div>
                        </div>
                        <span className={`badge ${item.published ? 'badge-success' : 'badge-warning'}`}>
                          {item.published ? t('admin.examSchedules.published') : t('admin.examSchedules.draft')}
                        </span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('admin.examSchedules.date')}</span>
                          <span>{item.examDate || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.examSchedules.time')}</span>
                          <span>{item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.examSchedules.teacher')}</span>
                          <span>{item.teacher || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.examSchedules.class')}</span>
                          <span>{item.className || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.examSchedules.room')}</span>
                          <span>{item.roomNumber || '-'}</span>
                        </div>
                      </div>

                      <button type="button" className="btn btn-danger btn-block" onClick={() => handleDelete(item.id)}>
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
