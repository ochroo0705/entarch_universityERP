import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getSubjects } from '../../api/endpoints';
import useEntityTranslations from '../../hooks/useEntityTranslations';
import SectionCard from '../../components/ui/SectionCard';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

const SORTABLE_COLUMNS = {
  subjectName: 'subjectName',
  subjectCode: 'subjectCode',
  gradeLevel: 'gradeLevel',
  hoursPerWeek: 'hoursPerWeek',
  isMandatory: 'isMandatory',
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

function SubjectSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`subject-skeleton-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
    </tr>
  ));
}

function SubjectSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`subject-card-skeleton-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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
    </article>
  ));
}

export default function Subjects() {
  const { t } = useTranslation();
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    gradeLevel: 'all',
    isMandatory: 'all',
    sortBy: 'createdAt',
    sortOrder: 'desc',
  });
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    totalPages: 0,
  });
  const { getField } = useEntityTranslations('subject', subjects);
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
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getSubjects({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      gradeLevel: query.gradeLevel === 'all' ? undefined : Number(query.gradeLevel),
      isMandatory: query.isMandatory === 'all' ? undefined : query.isMandatory === 'yes',
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setSubjects(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load subjects', err);
        setError(err.response?.data?.message || 'Unable to load subjects');
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

  const grades = Array.from({ length: 12 }, (_, index) => index + 1);
  const gradeOptions = [{ value: 'all', label: t('admin.classes.allGrades') }, ...grades.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))];
  const mandatoryOptions = [
    { value: 'all', label: t('admin.subjects.allTypes') },
    { value: 'yes', label: t('admin.subjects.mandatory') },
    { value: 'no', label: t('admin.subjects.elective') },
  ];
  const pageSizeOptions = [10, 20, 50, 100].map((size) => ({ value: String(size), label: `${size} / page` }));

  const hasFilters = Boolean(debouncedSearch) || query.gradeLevel !== 'all' || query.isMandatory !== 'all';

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

  const retryLoad = () => {
    setError('');
    setLoading(true);
    setReloadToken((value) => value + 1);
  };

  const paginationSummary = pagination.total === 0
    ? 'Showing 0-0 of 0'
    : `Showing ${(pagination.page - 1) * pagination.pageSize + 1}-${Math.min(pagination.page * pagination.pageSize, pagination.total)} of ${pagination.total}`;

  if (loading) {
    return <LoadingState label={t('common.loadingSubjects')} />;
  }

  if (error) {
    return (
      <ErrorState
        title="Unable to load subjects"
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
          <h1>{t('admin.subjects.title')}</h1>
        </div>
        <Link to="/admin/subjects/create" className="btn btn-primary">{t('admin.subjects.createSubject')}</Link>
      </div>

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.subjects.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu options={gradeOptions} value={query.gradeLevel} onChange={(value) => handleFilterChange('gradeLevel')({ target: { value } })} placeholder={t('admin.classes.allGrades')} />
        <SelectMenu options={mandatoryOptions} value={query.isMandatory} onChange={(value) => handleFilterChange('isMandatory')({ target: { value } })} placeholder={t('admin.subjects.allTypes')} />
        <SelectMenu options={pageSizeOptions} value={String(query.pageSize)} onChange={(value) => handleFilterChange('pageSize')({ target: { value } })} placeholder="Page size" />
        <span className="filter-result-count">{pagination.total} subjects</span>
      </div>

      <SectionCard title={t('admin.subjects.title')} subtitle={`Page ${pagination.page} of ${Math.max(pagination.totalPages, 1)}`}>
        <div className="content-stack" aria-busy={refreshing}>
          {subjects.length === 0 ? (
            <EmptyState
              title={hasFilters ? 'No subjects match these filters' : t('admin.subjects.noSubjects')}
              description={hasFilters ? 'Try broadening the search terms or removing a filter.' : 'Subjects will appear here after they are created.'}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <SortableHeader label={t('admin.subjects.subjectName')} sortKey="subjectName" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.subjects.subjectCode')} sortKey="subjectCode" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.subjects.gradeLevel')} sortKey="gradeLevel" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.subjects.hoursPerWeek')} sortKey="hoursPerWeek" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.subjects.mandatory')} sortKey="isMandatory" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing
                      ? <SubjectSkeletonRows count={skeletonRowCount} />
                      : subjects.map((subject) => (
                        <tr key={subject.id}>
                          <td>{subject.id}</td>
                          <td>{getField(subject, 'name', subject.subjectNameMn || subject.name)}</td>
                          <td><span className="badge badge-info">{subject.subjectCode}</span></td>
                          <td>{subject.gradeLevel}</td>
                          <td>{subject.hoursPerWeek}</td>
                          <td>
                            <span className={`badge ${subject.isMandatory !== false ? 'badge-success' : 'badge-warning'}`}>
                              {subject.isMandatory !== false ? t('common.yes') : t('common.no')}
                            </span>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing
                  ? <SubjectSkeletonCards count={skeletonCardCount} />
                  : subjects.map((subject) => (
                    <article key={subject.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{getField(subject, 'name', subject.subjectNameMn || subject.name)}</div>
                          <div className="muted-copy">{subject.subjectCode || '-'}</div>
                        </div>
                        <span className={`badge ${subject.isMandatory !== false ? 'badge-success' : 'badge-warning'}`}>
                          {subject.isMandatory !== false ? t('common.yes') : t('common.no')}
                        </span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('common.id')}</span>
                          <span>{subject.id}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.subjects.gradeLevel')}</span>
                          <span>{subject.gradeLevel ?? '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.subjects.hoursPerWeek')}</span>
                          <span>{subject.hoursPerWeek ?? '-'}</span>
                        </div>
                      </div>
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
