import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getParentStudents } from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

function ParentStudentSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`parent-student-skeleton-row-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
    </tr>
  ));
}

function ParentStudentSkeletonCards({ count = 6 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`parent-student-skeleton-card-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function ParentStudents() {
  const { t } = useTranslation();
  const [links, setLinks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    relationship: 'all',
  });
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    totalPages: 0,
  });

  const relLabel = useMemo(() => ({
    FATHER: t('admin.parentStudents.father'),
    father: t('admin.parentStudents.father'),
    MOTHER: t('admin.parentStudents.mother'),
    mother: t('admin.parentStudents.mother'),
    GUARDIAN: t('admin.parentStudents.guardian'),
    guardian: t('admin.parentStudents.guardian'),
    OTHER: t('admin.parentStudents.other'),
    other: t('admin.parentStudents.other'),
  }), [t]);

  const relationshipOptions = useMemo(() => ([
    { value: 'all', label: t('admin.parentStudents.allRelationships') },
    { value: 'father', label: t('admin.parentStudents.father') },
    { value: 'mother', label: t('admin.parentStudents.mother') },
    { value: 'guardian', label: t('admin.parentStudents.guardian') },
    { value: 'other', label: t('admin.parentStudents.other') },
  ]), [t]);

  const pageSizeOptions = useMemo(
    () => [10, 20, 50, 100].map((size) => ({ value: String(size), label: t('admin.users.pageSizeOption', { count: size }) })),
    [t]
  );

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

    getParentStudents({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      relationship: query.relationship === 'all' ? undefined : query.relationship,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setLinks(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load parent-student links', err);
        setError('parentStudents');
      })
      .finally(() => {
        if (ignore) return;
        setLoading(false);
        setRefreshing(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedSearch, query.page, query.pageSize, query.relationship, reloadToken]);

  const skeletonRowCount = Math.max(Math.min(Number(query.pageSize) || DEFAULT_PAGE_SIZE, 10), 5);
  const skeletonCardCount = Math.max(Math.min(Number(query.pageSize) || DEFAULT_PAGE_SIZE, 6), 3);
  const hasFilters = query.relationship !== 'all' || Boolean(debouncedSearch);
  const total = pagination.total;
  const totalPages = Math.max(pagination.totalPages, 1);
  const currentPage = pagination.page || query.page;

  const handleFilterChange = (key) => (event) => {
    const rawValue = event.target.value;
    const value = key === 'pageSize' ? Number(rawValue) : rawValue;
    setQuery((current) => ({
      ...current,
      [key]: value,
      page: 1,
    }));
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 1 || nextPage > totalPages || nextPage === currentPage) return;
    setQuery((current) => ({ ...current, page: nextPage }));
  };

  const retryLoad = () => {
    setError('');
    setLoading(true);
    setReloadToken((value) => value + 1);
  };

  if (loading) {
    return <LoadingState label={t('common.loading')} />;
  }

  if (error) {
    return (
      <ErrorState
        title={t('admin.parentStudents.title')}
        description={t('common.noData')}
        retryLabel={t('admin.users.retry')}
        onRetry={retryLoad}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{t('admin.parentStudents.title')}</h1>
          <p className="page-summary">{t('admin.parentStudents.searchPlaceholder')}</p>
        </div>
        <Link to="/admin/parent-students/link" className="btn btn-primary">{t('admin.parentStudents.linkParentStudent')}</Link>
      </div>

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.parentStudents.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu
          options={relationshipOptions}
          value={query.relationship}
          onChange={(value) => handleFilterChange('relationship')({ target: { value } })}
          placeholder={t('admin.parentStudents.allRelationships')}
        />
        <SelectMenu
          options={pageSizeOptions}
          value={String(query.pageSize)}
          onChange={(value) => handleFilterChange('pageSize')({ target: { value } })}
          placeholder={t('ai.shared.pageSize')}
        />
        <span className="filter-result-count">{t('common.link', { count: total })}</span>
      </div>

      <SectionCard
        title={t('admin.parentStudents.title')}
        subtitle={`${t('common.link', { count: total })} • ${t('admin.users.pageLabel', { page: currentPage, totalPages })}`}
      >
        <div className="content-stack" aria-busy={refreshing}>
          {links.length === 0 ? (
            <EmptyState
              title={t('admin.parentStudents.noLinks')}
              description={hasFilters ? t('admin.parentStudents.searchPlaceholder') : t('common.noData')}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <th>{t('admin.parentStudents.parentCol')}</th>
                      <th>{t('admin.parentStudents.studentCol')}</th>
                      <th>{t('admin.parentStudents.relationship')}</th>
                      <th>{t('admin.parentStudents.primaryContact')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing ? (
                      <ParentStudentSkeletonRows count={skeletonRowCount} />
                    ) : (
                      links.map((link) => (
                        <tr key={link.id}>
                          <td>{link.id}</td>
                          <td>{link.parent?.firstName} {link.parent?.lastName} ({link.parent?.username})</td>
                          <td>{link.student?.firstName} {link.student?.lastName} ({link.student?.username})</td>
                          <td>{relLabel[link.relationship] || link.relationship}</td>
                          <td>
                            <span className={`badge ${link.isPrimaryContact ? 'badge-success' : 'badge-info'}`}>
                              {link.isPrimaryContact ? t('common.yes') : t('common.no')}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing ? (
                  <ParentStudentSkeletonCards count={skeletonCardCount} />
                ) : (
                  links.map((link) => (
                    <article key={`mobile-${link.id}`} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{link.parent?.firstName} {link.parent?.lastName}</div>
                          <div className="muted-copy">@{link.parent?.username}</div>
                        </div>
                        <span className={`badge ${link.isPrimaryContact ? 'badge-success' : 'badge-info'}`}>
                          {link.isPrimaryContact ? t('common.yes') : t('common.no')}
                        </span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('common.id')}</span>
                          <strong>{link.id}</strong>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.parentStudents.studentCol')}</span>
                          <strong>{link.student?.firstName} {link.student?.lastName} ({link.student?.username})</strong>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.parentStudents.relationship')}</span>
                          <strong>{relLabel[link.relationship] || link.relationship}</strong>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.parentStudents.primaryContact')}</span>
                          <strong>{link.isPrimaryContact ? t('common.yes') : t('common.no')}</strong>
                        </div>
                      </div>
                    </article>
                  ))
                )}
              </div>
            </>
          )}

          <div className="pagination-toolbar">
            <div className="pagination-summary">
              {t('admin.users.paginationSummary', {
                start: total === 0 ? 0 : (currentPage - 1) * query.pageSize + 1,
                end: total === 0 ? 0 : Math.min(currentPage * query.pageSize, total),
                total,
              })}
            </div>
            <div className="pagination-actions">
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage <= 1}>
                {t('common.prev')}
              </button>
              <span className="pagination-page-label">
                {t('admin.users.pageLabel', {
                  page: currentPage,
                  totalPages,
                })}
              </span>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage >= totalPages}>
                {t('common.next')}
              </button>
            </div>
          </div>
        </div>
      </SectionCard>
    </div>
  );
}
