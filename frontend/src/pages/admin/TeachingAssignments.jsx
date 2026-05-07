import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  deactivateTeachingAssignment,
  getClasses,
  getSubjects,
  getTeachers,
  getTeachingAssignments,
} from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const DEFAULT_PAGE_SIZE = 20;

const SORTABLE_COLUMNS = {
  teacher: 'teacher',
  subject: 'subject',
  className: 'className',
  academicYear: 'academicYear',
  semester: 'semester',
  isActive: 'isActive',
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

function AssignmentSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`assignment-skeleton-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-button" /></td>
    </tr>
  ));
}

function AssignmentSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`assignment-card-skeleton-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function TeachingAssignments() {
  const { t } = useTranslation();
  const [assignments, setAssignments] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
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
    status: 'all',
    teacherId: 'all',
    subjectId: 'all',
    classId: 'all',
    semester: 'all',
    academicYear: 'all',
    grade: 'all',
    sortBy: 'id',
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
    Promise.all([getTeachers(), getSubjects(), getClasses()])
      .then(([teacherRes, subjectRes, classRes]) => {
        setTeachers(Array.isArray(teacherRes.data) ? teacherRes.data : []);
        setSubjects(Array.isArray(subjectRes.data) ? subjectRes.data : []);
        const classData = Array.isArray(classRes.data) ? classRes.data : classRes.data?.content ?? [];
        setClasses(classData);
      })
      .catch((err) => {
        console.error('Failed to load assignment filters', err);
      });
  }, []);

  useEffect(() => {
    let ignore = false;
    const initialLoad = loading;

    if (!initialLoad) {
      setRefreshing(true);
    }

    getTeachingAssignments({
      page: query.page,
      pageSize: query.pageSize,
      search: debouncedSearch || undefined,
      status: query.status === 'all' ? undefined : query.status === 'active',
      teacherId: query.teacherId === 'all' ? undefined : Number(query.teacherId),
      subjectId: query.subjectId === 'all' ? undefined : Number(query.subjectId),
      classId: query.classId === 'all' ? undefined : Number(query.classId),
      semester: query.semester === 'all' ? undefined : Number(query.semester),
      academicYear: query.academicYear === 'all' ? undefined : query.academicYear,
      grade: query.grade === 'all' ? undefined : Number(query.grade),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
      .then((res) => {
        if (ignore) return;
        const data = res.data ?? {};
        setAssignments(Array.isArray(data.items) ? data.items : []);
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
        console.error('Failed to load teaching assignments', err);
        setError(err.response?.data?.message || 'Unable to load teaching assignments');
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
  const academicYearOptions = useMemo(
    () => [...new Set(classes.map((item) => item.academicYear).filter(Boolean))].sort((a, b) => b.localeCompare(a)),
    [classes]
  );
  const teacherFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.teachingAssignments.teacher') }, ...teachers.map((teacher) => ({ value: String(teacher.id), label: [teacher.firstName, teacher.lastName].filter(Boolean).join(' ') || teacher.username }))], [teachers, t]);
  const subjectFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.teachingAssignments.subject') }, ...subjects.map((subject) => ({ value: String(subject.id), label: subject.subjectNameMn || subject.name }))], [subjects, t]);
  const classFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.teachingAssignments.class') }, ...classes.map((classItem) => ({ value: String(classItem.id), label: classItem.className }))], [classes, t]);
  const statusOptions = useMemo(() => [
    { value: 'all', label: t('common.allStatus') },
    { value: 'active', label: t('common.active') },
    { value: 'inactive', label: t('common.inactive') },
  ], [t]);
  const semesterOptions = useMemo(() => [
    { value: 'all', label: t('admin.teachingAssignments.semester') },
    { value: '1', label: '1' },
    { value: '2', label: '2' },
  ], [t]);
  const yearOptions = useMemo(() => [{ value: 'all', label: t('admin.teachingAssignments.year') }, ...academicYearOptions.map((year) => ({ value: year, label: year }))], [academicYearOptions, t]);
  const gradeFilterOptions = useMemo(() => [{ value: 'all', label: t('admin.classes.allGrades') }, ...gradeOptions.map((grade) => ({ value: String(grade), label: t('admin.classes.gradeN', { n: grade }) }))], [gradeOptions, t]);
  const pageSizeOptions = useMemo(() => [10, 20, 50, 100].map((size) => ({ value: String(size), label: `${size} / page` })), []);

  const hasFilters = Boolean(debouncedSearch)
    || query.status !== 'all'
    || query.teacherId !== 'all'
    || query.subjectId !== 'all'
    || query.classId !== 'all'
    || query.semester !== 'all'
    || query.academicYear !== 'all'
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

  const handleDeactivate = async (id) => {
    if (!window.confirm(t('admin.teachingAssignments.confirmDeactivate'))) return;
    try {
      await deactivateTeachingAssignment(id);
      setReloadToken((value) => value + 1);
    } catch (err) {
      window.alert(err.response?.data?.message || 'Failed to deactivate assignment');
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
        title="Unable to load teaching assignments"
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
          <h1>{t('admin.teachingAssignments.title')}</h1>
        </div>
        <Link to="/admin/teaching-assignments/create" className="btn btn-primary">{t('admin.teachingAssignments.assignTeacher')}</Link>
      </div>

      <div className="filter-toolbar users-filter-toolbar">
        <input
          type="text"
          className="form-control"
          placeholder={t('admin.teachingAssignments.searchPlaceholder')}
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
        />
        <SelectMenu options={statusOptions} value={query.status} onChange={(value) => handleFilterChange('status')({ target: { value } })} placeholder={t('common.allStatus')} />
        <SearchableSelect options={teacherFilterOptions} value={query.teacherId} onChange={(value) => handleFilterChange('teacherId')({ target: { value } })} placeholder={t('admin.teachingAssignments.teacher')} searchPlaceholder={t('common.search')} emptyLabel="No teachers found" />
        <SearchableSelect options={subjectFilterOptions} value={query.subjectId} onChange={(value) => handleFilterChange('subjectId')({ target: { value } })} placeholder={t('admin.teachingAssignments.subject')} searchPlaceholder={t('common.search')} emptyLabel="No subjects found" />
        <SearchableSelect options={classFilterOptions} value={query.classId} onChange={(value) => handleFilterChange('classId')({ target: { value } })} placeholder={t('admin.teachingAssignments.class')} searchPlaceholder={t('common.search')} emptyLabel="No classes found" />
        <SelectMenu options={semesterOptions} value={query.semester} onChange={(value) => handleFilterChange('semester')({ target: { value } })} placeholder={t('admin.teachingAssignments.semester')} />
        <SelectMenu options={yearOptions} value={query.academicYear} onChange={(value) => handleFilterChange('academicYear')({ target: { value } })} placeholder={t('admin.teachingAssignments.year')} />
        <SelectMenu options={gradeFilterOptions} value={query.grade} onChange={(value) => handleFilterChange('grade')({ target: { value } })} placeholder={t('admin.classes.allGrades')} />
        <SelectMenu options={pageSizeOptions} value={String(query.pageSize)} onChange={(value) => handleFilterChange('pageSize')({ target: { value } })} placeholder="Page size" />
        <span className="filter-result-count">{pagination.total} assignments</span>
      </div>

      <SectionCard
        title={t('admin.teachingAssignments.title')}
        subtitle={`Page ${pagination.page} of ${Math.max(pagination.totalPages, 1)}`}
      >
        <div className="content-stack" aria-busy={refreshing}>
          {assignments.length === 0 ? (
            <EmptyState
              title={hasFilters ? 'No assignments match these filters' : t('admin.teachingAssignments.noAssignments')}
              description={hasFilters ? 'Try widening your search or clearing one of the filters.' : 'Assignments will appear here after they are created.'}
            />
          ) : (
            <>
              <div className="desktop-table table-container">
                <table>
                  <thead>
                    <tr>
                      <th>{t('common.id')}</th>
                      <SortableHeader label={t('admin.teachingAssignments.teacher')} sortKey="teacher" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.teachingAssignments.subject')} sortKey="subject" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.teachingAssignments.class')} sortKey="className" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.teachingAssignments.year')} sortKey="academicYear" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('admin.teachingAssignments.semester')} sortKey="semester" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <SortableHeader label={t('common.status')} sortKey="isActive" sortBy={query.sortBy} sortOrder={query.sortOrder} onSort={handleSort} />
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {refreshing
                      ? <AssignmentSkeletonRows count={skeletonRowCount} />
                      : assignments.map((assignment) => (
                        <tr key={assignment.id}>
                          <td>{assignment.id}</td>
                          <td>{[assignment.teacher?.firstName, assignment.teacher?.lastName].filter(Boolean).join(' ') || '-'}</td>
                          <td>{assignment.subject?.subjectNameMn || assignment.subject?.name || '-'}</td>
                          <td>{assignment.classInfo?.className || '-'}</td>
                          <td>{assignment.academicYear || '-'}</td>
                          <td>{assignment.semester || '-'}</td>
                          <td>
                            <span className={`badge ${assignment.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
                              {assignment.isActive !== false ? t('common.active') : t('common.inactive')}
                            </span>
                          </td>
                          <td>
                            {assignment.isActive !== false ? (
                              <button type="button" className="btn btn-danger btn-sm" onClick={() => handleDeactivate(assignment.id)}>
                                {t('common.deactivate')}
                              </button>
                            ) : null}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              <div className="mobile-card-list">
                {refreshing
                  ? <AssignmentSkeletonCards count={skeletonCardCount} />
                  : assignments.map((assignment) => (
                    <article key={assignment.id} className="data-card">
                      <div className="data-card-header">
                        <div>
                          <div className="data-card-title">{assignment.subject?.subjectNameMn || assignment.subject?.name || '-'}</div>
                          <div className="muted-copy">{[assignment.teacher?.firstName, assignment.teacher?.lastName].filter(Boolean).join(' ') || '-'}</div>
                        </div>
                        <span className={`badge ${assignment.isActive !== false ? 'badge-success' : 'badge-danger'}`}>
                          {assignment.isActive !== false ? t('common.active') : t('common.inactive')}
                        </span>
                      </div>

                      <div className="data-card-meta">
                        <div className="data-card-meta-row">
                          <span>{t('admin.teachingAssignments.class')}</span>
                          <span>{assignment.classInfo?.className || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.teachingAssignments.year')}</span>
                          <span>{assignment.academicYear || '-'}</span>
                        </div>
                        <div className="data-card-meta-row">
                          <span>{t('admin.teachingAssignments.semester')}</span>
                          <span>{assignment.semester || '-'}</span>
                        </div>
                      </div>

                      {assignment.isActive !== false ? (
                        <button type="button" className="btn btn-danger btn-block" onClick={() => handleDeactivate(assignment.id)}>
                          {t('common.deactivate')}
                        </button>
                      ) : null}
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
