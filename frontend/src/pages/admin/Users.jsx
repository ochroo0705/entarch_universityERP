import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getAllUsers, getClasses } from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';
import { ROLE_FLAGS, primaryRoleBadgeClass, roleLabelKey, roleName } from '../../utils/roles';

const DEFAULT_PAGE_SIZE = 20;

const USER_ROLE_FILTERS = [
  'ADMIN',
  'TEACHER',
  'STUDENT',
  'PARENT',
  'COUNSELOR',
  'NURSE',
  'FINANCE_STAFF',
  'LIBRARIAN',
  'TRANSPORT_COORDINATOR',
  'ADMISSIONS_STAFF',
  'CAFETERIA_STAFF',
];

const SORTABLE_COLUMNS = {
  username: 'username',
  name: 'name',
  email: 'email',
  roleFlags: 'roleFlags',
  createdAt: 'createdAt',
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

function UserTableRow({ user, t, navigate }) {
  return (
    <tr key={user.id}>
      <td>{user.id}</td>
      <td>{user.username}</td>
      <td>{[user.firstName, user.lastName].filter(Boolean).join(' ') || '-'}</td>
      <td>{user.email || '-'}</td>
      <td><span className={`badge ${primaryRoleBadgeClass(user.roleFlags)}`}>{roleName(user.roleFlags, t)}</span></td>
      <td>{user.grade ? `${t('common.grade')} ${user.grade}${user.section ? `-${user.section}` : ''}` : '-'}</td>
      <td>
        <span className={`badge ${user.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
          {user.isActive !== false ? t('common.active') : t('common.inactive')}
        </span>
      </td>
      <td>
        <button type="button" className="btn btn-secondary btn-sm" onClick={() => navigate(`/admin/users/${user.id}`)}>
          {t('admin.users.open')}
        </button>
      </td>
    </tr>
  );
}

function UserTableSkeletonRows({ count = 10 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`skeleton-row-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-email" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-grade" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-button" /></td>
    </tr>
  ));
}

function UserMobileSkeletonCards({ count = 6 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`skeleton-card-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function Users() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    role: 'all',
    status: 'all',
    grade: 'all',
    section: 'all',
    sortBy: 'createdAt',
    sortOrder: 'desc',
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
    getClasses()
      .then((res) => {
        const data = Array.isArray(res.data) ? res.data : res.data?.content ?? [];
        setClasses(data);
      })
      .catch((err) => {
        console.error('Failed to load class filters', err);
      });
  }, []);

  useEffect(() => {
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getAllUsers({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      role: query.role === 'all' ? undefined : Number(query.role),
      status: query.status === 'all' ? undefined : query.status === 'active',
      grade: query.grade === 'all' ? undefined : Number(query.grade),
      section: query.section === 'all' ? undefined : query.section,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setUsers(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load users', err);
        setError('users');
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

  const sectionOptions = useMemo(
    () => [...new Set(classes.map((item) => item.section).filter(Boolean))].sort((a, b) => a.localeCompare(b)),
    [classes]
  );
  const roleOptions = useMemo(() => ([
    { value: 'all', label: t('admin.users.allRoles') },
    ...USER_ROLE_FILTERS.map((code) => ({
      value: String(ROLE_FLAGS[code]),
      label: t(roleLabelKey(code)),
    })),
  ]), [t]);
  const statusOptions = useMemo(() => ([
    { value: 'all', label: t('common.allStatus') },
    { value: 'active', label: t('common.active') },
    { value: 'inactive', label: t('common.inactive') },
  ]), [t]);
  const gradeFilterOptions = useMemo(
    () => [{ value: 'all', label: t('admin.users.allGrades') }, ...gradeOptions.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))],
    [gradeOptions, t]
  );
  const sectionFilterOptions = useMemo(
    () => [{ value: 'all', label: t('admin.users.allSections') }, ...sectionOptions.map((section) => ({ value: section, label: section }))],
    [sectionOptions, t]
  );
  const pageSizeOptions = useMemo(
    () => [10, 20, 50, 100].map((size) => ({ value: String(size), label: t('admin.users.pageSizeOption', { count: size }) })),
    [t]
  );

  const hasFilters = query.role !== 'all'
    || query.status !== 'all'
    || query.grade !== 'all'
    || query.section !== 'all'
    || Boolean(debouncedSearch);

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
    if (!SORTABLE_COLUMNS[sortKey]) return;

    setQuery((current) => ({
      ...current,
      sortBy: sortKey,
      sortOrder: current.sortBy === sortKey && current.sortOrder === 'asc' ? 'desc' : 'asc',
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

  if (loading) {
    return <LoadingState label={t('common.loadingUsers')} />;
  }

  if (error) {
    return (
      <ErrorState
        title={t('admin.users.loadErrorTitle')}
        description={t('admin.users.loadErrorDescription')}
        retryLabel={t('admin.users.retry')}
        onRetry={retryLoad}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('admin.users.kicker')}</div>
          <h1>{t('admin.users.title')}</h1>
          <p className="page-summary">{t('admin.users.summary')}</p>
        </div>
        <Link to="/admin/users/create" className="btn btn-primary">{t('admin.users.createUser')}</Link>
      </div>

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.users.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu options={roleOptions} value={query.role} onChange={(value) => handleFilterChange('role')({ target: { value } })} placeholder={t('admin.users.allRoles')} />
        <SelectMenu options={statusOptions} value={query.status} onChange={(value) => handleFilterChange('status')({ target: { value } })} placeholder={t('common.allStatus')} />
        <SelectMenu options={gradeFilterOptions} value={query.grade} onChange={(value) => handleFilterChange('grade')({ target: { value } })} placeholder={t('admin.users.allGrades')} />
        <SelectMenu options={sectionFilterOptions} value={query.section} onChange={(value) => handleFilterChange('section')({ target: { value } })} placeholder={t('admin.users.allSections')} />
        <SelectMenu options={pageSizeOptions} value={String(query.pageSize)} onChange={(value) => handleFilterChange('pageSize')({ target: { value } })} placeholder={t('common.pageSize')} />
        <span className="filter-result-count">{t('admin.users.totalUsers', { count: pagination.total })}</span>
      </div>

      <SectionCard
        title={t('admin.users.title')}
        subtitle={t('admin.users.resultsSummary', {
          page: pagination.page,
          totalPages: Math.max(pagination.totalPages, 1),
          total: pagination.total,
        })}
      >
        <div className="content-stack" aria-busy={refreshing}>
          {users.length === 0 ? (
            <EmptyState
              title={hasFilters ? t('admin.users.noResultsTitle') : t('admin.users.emptyTitle')}
              description={hasFilters ? t('admin.users.noResultsDescription') : t('admin.users.emptyDescription')}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <SortableHeader label={t('admin.users.username')} sortKey="username" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.users.name')} sortKey="name" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.users.email')} sortKey="email" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.users.role')} sortKey="roleFlags" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <th>{t('admin.users.gradeSection')}</th>
                      <th>{t('common.status')}</th>
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing
                      ? <UserTableSkeletonRows count={skeletonRowCount} />
                      : users.map((user) => (
                        <UserTableRow key={user.id} user={user} t={t} navigate={navigate} />
                      ))}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing
                  ? <UserMobileSkeletonCards count={skeletonCardCount} />
                  : users.map((user) => (
                    <article key={user.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{[user.firstName, user.lastName].filter(Boolean).join(' ') || user.username}</div>
                          <div className="muted-copy">@{user.username}</div>
                        </div>
                        <span className={`badge ${primaryRoleBadgeClass(user.roleFlags)}`}>{roleName(user.roleFlags, t)}</span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('common.id')}</span>
                          <span>{user.id}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.users.email')}</span>
                          <span>{user.email || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.users.gradeSection')}</span>
                          <span>{user.grade ? `${t('common.grade')} ${user.grade}${user.section ? `-${user.section}` : ''}` : '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('common.status')}</span>
                          <span>{user.isActive !== false ? t('common.active') : t('common.inactive')}</span>
                        </div>
                      </div>

                      <button type="button" className="btn btn-primary btn-block" onClick={() => navigate(`/admin/users/${user.id}`)}>
                        {t('admin.users.open')}
                      </button>
                    </article>
                  ))}
              </div>
            </>
          )}

          <div className="pagination-toolbar">
            <div className="pagination-summary">
              {t('admin.users.paginationSummary', {
                start: pagination.total === 0 ? 0 : (pagination.page - 1) * pagination.pageSize + 1,
                end: pagination.total === 0 ? 0 : Math.min(pagination.page * pagination.pageSize, pagination.total),
                total: pagination.total,
              })}
            </div>
            <div className="pagination-actions">
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(query.page - 1)} disabled={query.page <= 1}>
                {t('common.prev')}
              </button>
              <span className="pagination-page-label">
                {t('admin.users.pageLabel', {
                  page: pagination.page,
                  totalPages: Math.max(pagination.totalPages, 1),
                })}
              </span>
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
