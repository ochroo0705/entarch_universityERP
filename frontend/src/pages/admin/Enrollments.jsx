import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { createEnrollment, getAllUsers, getClasses, getEnrollments } from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

const statusBadge = {
  ACTIVE: 'badge-success',
  active: 'badge-success',
  GRADUATED: 'badge-info',
  graduated: 'badge-info',
  TRANSFERRED: 'badge-warning',
  transferred: 'badge-warning',
  DROPPED: 'badge-danger',
  dropped: 'badge-danger',
};

const SORTABLE_COLUMNS = {
  enrollmentDate: 'enrollmentDate',
  studentName: 'studentName',
  studentNumber: 'studentNumber',
  className: 'className',
  status: 'status',
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

function EnrollmentSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`enrollment-skeleton-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
    </tr>
  ));
}

function EnrollmentSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`enrollment-card-skeleton-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function Enrollments() {
  const { t } = useTranslation();
  const [enrollments, setEnrollments] = useState([]);
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [students, setStudents] = useState([]);
  const [studentsLoading, setStudentsLoading] = useState(false);
  const [studentSearchInput, setStudentSearchInput] = useState('');
  const [debouncedStudentSearch, setDebouncedStudentSearch] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState({
    studentId: '',
    classId: '',
    enrollmentDate: '',
    studentNumber: '',
    status: 'active',
  });
  const [creating, setCreating] = useState(false);
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    status: 'all',
    classId: 'all',
    grade: 'all',
    section: 'all',
    sortBy: 'enrollmentDate',
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
    const timeoutId = window.setTimeout(() => {
      setDebouncedStudentSearch(studentSearchInput.trim());
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [studentSearchInput]);

  useEffect(() => {
    Promise.allSettled([
      getClasses(),
    ]).then(([classesResult]) => {
      if (classesResult.status === 'fulfilled') {
        const classData = Array.isArray(classesResult.value.data)
          ? classesResult.value.data
          : classesResult.value.data?.content ?? [];
        setClasses(classData);
      } else {
        console.error('Failed to load classes for enrollments', classesResult.reason);
        setClasses([]);
      }
    });
  }, []);

  useEffect(() => {
    if (!showCreateForm) {
      setStudents([]);
      setStudentsLoading(false);
      return undefined;
    }

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
        const studentData = Array.isArray(response.data?.items)
          ? response.data.items
          : [];
        setStudents(studentData);
      })
      .catch((err) => {
        if (ignore) return;
        console.error('Failed to load students for enrollments', err);
        setStudents([]);
      })
      .finally(() => {
        if (ignore) return;
        setStudentsLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedStudentSearch, showCreateForm]);

  useEffect(() => {
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getEnrollments({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      status: query.status === 'all' ? undefined : query.status,
      classId: query.classId === 'all' ? undefined : Number(query.classId),
      grade: query.grade === 'all' ? undefined : Number(query.grade),
      section: query.section === 'all' ? undefined : query.section,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setEnrollments(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load enrollments', err);
        setError(err.response?.data?.message || 'Unable to load enrollments');
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

  const hasFilters = Boolean(debouncedSearch)
    || query.status !== 'all'
    || query.classId !== 'all'
    || query.grade !== 'all'
    || query.section !== 'all';

  const studentSelectOptions = useMemo(
    () => students.map((student) => ({
      value: student.id,
      label: [student.firstName, student.lastName].filter(Boolean).join(' ') || student.username,
      meta: student.username ? `@${student.username}` : null,
    })),
    [students]
  );

  const selectedStudentOption = useMemo(
    () => studentSelectOptions.find((option) => String(option.value) === String(createForm.studentId)) || null,
    [createForm.studentId, studentSelectOptions]
  );

  const searchableStudentOptions = useMemo(() => {
    if (!selectedStudentOption) {
      return studentSelectOptions;
    }

    const exists = studentSelectOptions.some((option) => String(option.value) === String(selectedStudentOption.value));
    return exists ? studentSelectOptions : [selectedStudentOption, ...studentSelectOptions];
  }, [selectedStudentOption, studentSelectOptions]);

  const classSelectOptions = useMemo(
    () => classes.map((classItem) => ({
      value: classItem.id,
      label: classItem.className,
      meta: [classItem.grade ? `Grade ${classItem.grade}` : null, classItem.section].filter(Boolean).join(' • '),
    })),
    [classes]
  );
  const createStatusOptions = useMemo(() => ([
    { value: 'active', label: t('common.active') },
    { value: 'ACTIVE', label: 'ACTIVE' },
    { value: 'GRADUATED', label: t('admin.enrollments.graduated') },
    { value: 'TRANSFERRED', label: t('admin.enrollments.transferred') },
    { value: 'DROPPED', label: t('admin.enrollments.dropped') },
  ]), [t]);
  const statusFilterOptions = useMemo(() => ([
    { value: 'all', label: t('common.allStatus') },
    { value: 'active', label: t('common.active') },
    { value: 'graduated', label: t('admin.enrollments.graduated') },
    { value: 'transferred', label: t('admin.enrollments.transferred') },
    { value: 'dropped', label: t('admin.enrollments.dropped') },
  ]), [t]);
  const filterClassOptions = useMemo(() => [{ value: 'all', label: t('admin.enrollments.class') }, ...classSelectOptions.map((option) => ({ ...option, value: String(option.value) }))], [classSelectOptions, t]);
  const gradeFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.classes.allGrades') }, ...gradeOptions.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))], [gradeOptions, t]);
  const sectionFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.users.allSections') }, ...sectionOptions.map((section) => ({ value: section, label: section }))], [sectionOptions, t]);
  const pageSizeOptions = useMemo(() => [10, 20, 50, 100].map((size) => ({ value: String(size), label: `${size} / page` })), []);

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

  const handleCreateEnrollment = async () => {
    if (!createForm.studentId || !createForm.classId) return;
    setCreating(true);
    try {
      await createEnrollment({
        studentId: Number(createForm.studentId),
        classId: Number(createForm.classId),
        enrollmentDate: createForm.enrollmentDate || null,
        studentNumber: createForm.studentNumber || null,
        status: createForm.status || 'active',
      });
      setCreateForm({
        studentId: '',
        classId: '',
        enrollmentDate: '',
        studentNumber: '',
        status: 'active',
      });
      setStudentSearchInput('');
      setDebouncedStudentSearch('');
      setShowCreateForm(false);
      setReloadToken((value) => value + 1);
    } catch (err) {
      console.error('Failed to create enrollment', err);
      setError(err.response?.data?.message || 'Unable to create enrollment');
    } finally {
      setCreating(false);
    }
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
        title="Unable to load enrollments"
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
          <h1>{t('admin.enrollments.title')}</h1>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setShowCreateForm((current) => !current)}
        >
          {showCreateForm ? 'Hide Create Enrollment' : 'Create Enrollment'}
        </button>
      </div>

      {showCreateForm ? (
        <SectionCard title="Create Enrollment" subtitle="Admins can assign a student to a class directly from this page.">
          <div className="draft-form-grid">
            <div className="draft-filter-row">
              <label className="draft-field-label">
                Student
                <SearchableSelect
                  options={searchableStudentOptions}
                  value={createForm.studentId}
                  onChange={(nextValue) => setCreateForm((current) => ({ ...current, studentId: String(nextValue) }))}
                  searchValue={studentSearchInput}
                  onSearchChange={setStudentSearchInput}
                  placeholder="Select a student"
                  searchPlaceholder="Search students"
                  emptyLabel="No students found"
                  loadingLabel="Searching students..."
                  isLoading={studentsLoading}
                />
              </label>
              <label className="draft-field-label">
                Class
                <SearchableSelect
                  options={classSelectOptions}
                  value={createForm.classId}
                  onChange={(nextValue) => setCreateForm((current) => ({ ...current, classId: String(nextValue) }))}
                  placeholder="Select a class"
                  searchPlaceholder="Search classes"
                  emptyLabel="No classes found"
                />
              </label>
            </div>
            <div className="draft-filter-row">
              <label className="draft-field-label">
                Enrollment Date
                <input type="date" className="form-control" value={createForm.enrollmentDate} onChange={(event) => setCreateForm((current) => ({ ...current, enrollmentDate: event.target.value }))} />
              </label>
              <label className="draft-field-label">
                Student Number
                <input className="form-control" value={createForm.studentNumber} onChange={(event) => setCreateForm((current) => ({ ...current, studentNumber: event.target.value }))} placeholder="Optional student number" />
              </label>
            </div>
            <div className="draft-filter-row">
              <label className="draft-field-label">
                Status
                <SelectMenu options={createStatusOptions} value={createForm.status} onChange={(value) => setCreateForm((current) => ({ ...current, status: value }))} placeholder={t('common.status')} />
              </label>
            </div>
            <div className="draft-action-row">
              <button type="button" className="btn btn-secondary" onClick={() => setShowCreateForm(false)} disabled={creating}>
                Cancel
              </button>
              <button type="button" className="btn btn-primary" disabled={creating || !createForm.studentId || !createForm.classId} onClick={handleCreateEnrollment}>
                {creating ? 'Creating...' : 'Create Enrollment'}
              </button>
            </div>
          </div>
        </SectionCard>
      ) : null}

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.enrollments.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu options={statusFilterOptions} value={query.status} onChange={(value) => handleFilterChange('status')({ target: { value } })} placeholder={t('common.allStatus')} />
        <SearchableSelect options={filterClassOptions} value={query.classId} onChange={(value) => handleFilterChange('classId')({ target: { value } })} placeholder={t('admin.enrollments.class')} searchPlaceholder={t('common.search')} emptyLabel="No classes found" />
        <SelectMenu options={gradeFilterOptions} value={query.grade} onChange={(value) => handleFilterChange('grade')({ target: { value } })} placeholder={t('admin.classes.allGrades')} />
        <SelectMenu options={sectionFilterOptions} value={query.section} onChange={(value) => handleFilterChange('section')({ target: { value } })} placeholder={t('admin.users.allSections')} />
        <SelectMenu options={pageSizeOptions} value={String(query.pageSize)} onChange={(value) => handleFilterChange('pageSize')({ target: { value } })} placeholder="Page size" />
        <span className="filter-result-count">{pagination.total} enrollments</span>
      </div>

      <SectionCard title={t('admin.enrollments.title')} subtitle={`Page ${pagination.page} of ${Math.max(pagination.totalPages, 1)}`}>
        <div className="content-stack" aria-busy={refreshing}>
          {enrollments.length === 0 ? (
            <EmptyState
              title={hasFilters ? 'No enrollments match these filters' : t('admin.enrollments.noEnrollments')}
              description={hasFilters ? 'Try clearing a filter or broadening the search terms.' : 'Enrollments will appear here after students are assigned to classes.'}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <SortableHeader label={t('admin.enrollments.student')} sortKey="studentName" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.enrollments.class')} sortKey="className" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.enrollments.studentNumber')} sortKey="studentNumber" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.enrollments.enrollmentDate')} sortKey="enrollmentDate" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('common.status')} sortKey="status" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing
                      ? <EnrollmentSkeletonRows count={skeletonRowCount} />
                      : enrollments.map((enrollment) => (
                        <tr key={enrollment.id}>
                          <td>{enrollment.id}</td>
                          <td>{[enrollment.student?.firstName, enrollment.student?.lastName].filter(Boolean).join(' ') || '-'} ({enrollment.student?.username || '-'})</td>
                          <td>{enrollment.classEntity?.className || '-'}</td>
                          <td><span className="badge badge-purple">{enrollment.studentNumber || '-'}</span></td>
                          <td>{enrollment.enrollmentDate || '-'}</td>
                          <td>
                            <span className={`badge ${statusBadge[enrollment.status] || 'badge-info'}`}>
                              {enrollment.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing
                  ? <EnrollmentSkeletonCards count={skeletonCardCount} />
                  : enrollments.map((enrollment) => (
                    <article key={enrollment.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{[enrollment.student?.firstName, enrollment.student?.lastName].filter(Boolean).join(' ') || '-'}</div>
                          <div className="muted-copy">@{enrollment.student?.username || '-'}</div>
                        </div>
                        <span className={`badge ${statusBadge[enrollment.status] || 'badge-info'}`}>
                          {enrollment.status || '-'}
                        </span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('common.id')}</span>
                          <span>{enrollment.id}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.enrollments.class')}</span>
                          <span>{enrollment.classEntity?.className || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.enrollments.studentNumber')}</span>
                          <span>{enrollment.studentNumber || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.enrollments.enrollmentDate')}</span>
                          <span>{enrollment.enrollmentDate || '-'}</span>
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
