import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  getAiAdminAnalyticsSummary,
  getAiAdminRiskDashboardBundle,
  getAiRiskAccessScope,
  getAiRiskDashboard,
  getAiRiskDashboardDetail,
  getAiTeacherAnalyticsSummary,
  recalculateAiRiskScope,
  recalculateAiRiskStudent,
  refreshAiAnalyticsSummary,
} from '../../api/endpoints';
import SectionCard from '../ui/SectionCard';
import StatCard from '../ui/StatCard';
import { EmptyState, ErrorState, LoadingState } from '../ui/StateBlock';
import AiStatusBadge from './AiStatusBadge';
import AnalyticsSummaryPanel from './AnalyticsSummaryPanel';
import SearchableSelect from '../ui/SearchableSelect';
import SelectMenu from '../ui/SelectMenu';
import { formatDateTime, getAiValueLabel } from './aiI18n';

const DEFAULT_PAGE_SIZE = 20;

const DEFAULT_FILTERS = {
  classId: '',
  gradeLevel: '',
  riskLevel: '',
  search: '',
};

function RiskTableSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`risk-skeleton-row-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-id" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-email" /></td>
      <td><div className="users-skeleton users-skeleton-button" /></td>
    </tr>
  ));
}

function RiskMobileSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`risk-skeleton-card-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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
      </div>

      <div className="users-skeleton users-skeleton-card-button" />
    </article>
  ));
}

function RiskDetailSkeleton() {
  return (
    <div className="risk-detail-stack" aria-hidden="true">
      <div className="risk-detail-hero">
        <div className="parent-page-skeleton-stack">
          <div className="users-skeleton users-skeleton-card-subtitle" />
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-email" />
        </div>
        <div className="risk-detail-score">
          <div className="users-skeleton users-skeleton-pill" />
          <div className="users-skeleton users-skeleton-id" style={{ width: '54px', height: '20px' }} />
          <div className="users-skeleton users-skeleton-text" />
        </div>
      </div>

      <div className="risk-detail-meta-grid">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={`risk-meta-skeleton-${index}`} className="risk-meta-item">
            <span className="users-skeleton users-skeleton-meta-label" />
            <strong className="users-skeleton users-skeleton-meta-value" />
          </div>
        ))}
      </div>
    </div>
  );
}

function RiskDashboardPageSkeleton({ audience }) {
  return (
    <div className="content-stack" aria-hidden="true">
      <div className="page-header">
        <div className="parent-page-skeleton-stack">
          <div className="users-skeleton users-skeleton-card-subtitle" />
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-email" />
        </div>
        {audience === 'admin' ? <div className="users-skeleton users-skeleton-button risk-button-skeleton" /> : null}
      </div>

      <div className="stats-grid">
        {Array.from({ length: 4 }, (_, index) => (
          <article key={`risk-stat-skeleton-${index}`} className="stat-card">
            <div className="users-skeleton users-skeleton-pill" style={{ width: '48px', height: '48px' }} />
            <div className="stat-info">
              <div className="users-skeleton users-skeleton-id" style={{ width: '72px' }} />
              <div className="users-skeleton users-skeleton-text" />
            </div>
          </article>
        ))}
      </div>

      <div className="card">
        <div className="card-body">
          <div className="parent-page-skeleton-stack">
            <div className="users-skeleton users-skeleton-card-subtitle" />
            <div className="users-skeleton users-skeleton-card-title" />
            <div className="users-skeleton users-skeleton-email" />
          </div>
        </div>
      </div>

      <div className="grid-dashboard-bottom">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={`risk-top-card-skeleton-${index}`} className="card">
            <div className="card-body">
              <div className="parent-page-skeleton-stack">
                <div className="users-skeleton users-skeleton-card-subtitle" />
                <div className="users-skeleton users-skeleton-card-title" />
              </div>
              <div className="risk-filters-grid" style={{ marginTop: '1rem' }}>
                {Array.from({ length: 4 }, (_, itemIndex) => (
                  <div key={`risk-filter-skeleton-${index}-${itemIndex}`} className="users-skeleton users-skeleton-email" />
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid-student-detail has-detail">
        <div className="card">
          <div className="card-body">
            <div className="parent-page-skeleton-stack">
              <div className="users-skeleton users-skeleton-card-subtitle" />
              <div className="users-skeleton users-skeleton-card-title" />
            </div>
            <div className="table-container" style={{ marginTop: '1rem' }}>
              <table>
                <tbody>
                  <RiskTableSkeletonRows />
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="parent-page-skeleton-stack">
              <div className="users-skeleton users-skeleton-card-subtitle" />
              <div className="users-skeleton users-skeleton-card-title" />
            </div>
            <div style={{ marginTop: '1rem' }}>
              <RiskDetailSkeleton />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function SummaryBar({ label, value, max, toneClass }) {
  const width = max > 0 ? `${Math.max(8, (value / max) * 100)}%` : '0%';
  return (
    <div className="risk-summary-bar-row">
      <div className="risk-summary-bar-label">
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
      <div className="risk-summary-bar-track">
        <div className={`risk-summary-bar-fill ${toneClass}`} style={{ width }} />
      </div>
    </div>
  );
}

function IndicatorCard({ indicator }) {
  const { t } = useTranslation();

  return (
    <div className="risk-indicator-card">
      <div className="risk-indicator-top">
        <strong>{indicator.indicatorCode}</strong>
        <span>{indicator.weightedContribution}</span>
      </div>
      <div className="risk-indicator-meta">
        <span>{t('ai.risk.indicatorRaw', { value: indicator.rawValue ?? t('ai.shared.notAvailable') })}</span>
        <span>{t('ai.risk.indicatorRisk', { value: indicator.normalizedRiskValue ?? t('ai.shared.notAvailable') })}</span>
        <span>{t('ai.risk.indicatorDataPoints', { count: indicator.dataPointsCount ?? 0 })}</span>
      </div>
      {indicator.isMissingData ? (
        <div className="muted-copy">{t('ai.risk.indicatorMissingData')}</div>
      ) : null}
    </div>
  );
}

export default function RiskDashboardPage({ audience = 'admin' }) {
  const { t, i18n } = useTranslation();
  const dashboardRequestIdRef = useRef(0);
  const summaryRequestIdRef = useRef(0);
  const detailPanelRef = useRef(null);
  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const [scope, setScope] = useState({ classes: [], students: [] });
  const [items, setItems] = useState([]);
  const [summary, setSummary] = useState(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [selectedStudentId, setSelectedStudentId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [summaryEnabled, setSummaryEnabled] = useState(false);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryRefreshing, setSummaryRefreshing] = useState(false);
  const [analyticsSummary, setAnalyticsSummary] = useState(null);
  const [refreshingDashboard, setRefreshingDashboard] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [error, setError] = useState('');
  const [summaryError, setSummaryError] = useState('');
  const [recalculatingStudent, setRecalculatingStudent] = useState(null);
  const [recalculatingAll, setRecalculatingAll] = useState(false);

  const fallback = t('ai.shared.notAvailable');

  const loadDetail = async (studentId) => {
    if (!studentId) {
      setDetail(null);
      return;
    }

    setLoadingDetail(true);
    try {
      const response = await getAiRiskDashboardDetail(studentId);
      setDetail(response.data);
    } catch (err) {
      console.error('Failed to load AI risk detail', err);
    } finally {
      setLoadingDetail(false);
    }
  };

  const loadAnalyticsSummary = async () => {
    const requestId = ++summaryRequestIdRef.current;
    setSummaryLoading(true);
    setSummaryError('');

    try {
      const languageCode = i18n.language?.startsWith('en') ? 'en' : 'mn';
      const response = audience === 'admin'
        ? await getAiAdminAnalyticsSummary({ ...(filters.gradeLevel ? { gradeLevel: Number(filters.gradeLevel) } : {}), languageCode })
        : await getAiTeacherAnalyticsSummary({ ...(filters.classId ? { classId: Number(filters.classId) } : {}), languageCode });

      if (summaryRequestIdRef.current !== requestId) return;
      setAnalyticsSummary(response.data?.summary || null);
    } catch (err) {
      if (summaryRequestIdRef.current !== requestId) return;
      console.error('Failed to load AI analytics summary', err);
      setSummaryError('summary');
      setAnalyticsSummary(null);
    } finally {
      if (summaryRequestIdRef.current === requestId) {
        setSummaryLoading(false);
      }
    }
  };

  const loadDashboard = async ({ preserveSelection = true, showGlobalLoading = true } = {}) => {
    const requestId = ++dashboardRequestIdRef.current;

    if (showGlobalLoading) {
      setLoading(true);
    } else {
      setRefreshingDashboard(true);
    }

    setError('');
    if (summaryEnabled) {
      loadAnalyticsSummary();
    } else {
      setSummaryLoading(false);
      setSummaryError('');
      setAnalyticsSummary(null);
    }

    try {
      const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''));
      const [scopeRes, dashboardRes] = await Promise.all([
        getAiRiskAccessScope(),
        audience === 'admin'
          ? getAiAdminRiskDashboardBundle(params)
          : getAiRiskDashboard(params),
      ]);

      if (dashboardRequestIdRef.current !== requestId) return;

      const nextItems = audience === 'admin'
        ? dashboardRes.data?.items || []
        : dashboardRes.data || [];

      setScope(scopeRes.data || { classes: [], students: [] });
      setItems(nextItems);
      setSummary(audience === 'admin' ? dashboardRes.data?.summary || null : null);

      const nextSelected = preserveSelection && selectedStudentId
        ? nextItems.find((item) => item.studentId === selectedStudentId)
        : nextItems[0];

      if (nextSelected) {
        setSelectedStudentId(nextSelected.studentId);
        await loadDetail(nextSelected.studentId);
      } else {
        setSelectedStudentId(null);
        setDetail(null);
      }
    } catch (err) {
      if (dashboardRequestIdRef.current !== requestId) return;
      console.error('Failed to load AI risk dashboard', err);
      setError('dashboard');
    } finally {
      if (dashboardRequestIdRef.current === requestId) {
        if (showGlobalLoading) {
          setLoading(false);
        } else {
          setRefreshingDashboard(false);
        }
      }
    }
  };

  useEffect(() => {
    loadDashboard({ preserveSelection: false });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [i18n.language]);

  const handleGenerateSummary = async () => {
    setSummaryEnabled(true);
    setAnalyticsSummary(null);
    await loadAnalyticsSummary();
  };

  const summaryStats = useMemo(() => {
    if (summary) {
      return {
        total: summary.totalStudents || 0,
        low: summary.lowRiskCount || 0,
        medium: summary.mediumRiskCount || 0,
        high: summary.highRiskCount || 0,
      };
    }

    return items.reduce((acc, item) => {
      acc.total += 1;
      if (item.riskLevel === 'LOW') acc.low += 1;
      if (item.riskLevel === 'MEDIUM') acc.medium += 1;
      if (item.riskLevel === 'HIGH') acc.high += 1;
      return acc;
    }, { total: 0, low: 0, medium: 0, high: 0 });
  }, [items, summary]);

  const topSummaryMax = Math.max(summaryStats.low, summaryStats.medium, summaryStats.high, 1);
  const totalPages = Math.max(Math.ceil(items.length / pageSize), 1);

  const classOptions = [
    { value: '', label: t('ai.risk.allClasses') },
    ...scope.classes.map((classItem) => ({
      value: String(classItem.classId),
      label: classItem.gradeLevel
        ? t('ai.risk.classOption', { className: classItem.className, gradeLevel: classItem.gradeLevel })
        : classItem.className,
    })),
  ];

  const gradeOptions = [
    { value: '', label: t('ai.risk.allGrades') },
    ...[...new Set(scope.classes.map((item) => item.gradeLevel).filter(Boolean))].map((gradeLevel) => ({
      value: String(gradeLevel),
      label: t('ai.risk.gradeLabel', { gradeLevel }),
    })),
  ];

  const riskLevelOptions = [
    { value: '', label: t('ai.risk.allLevels') },
    { value: 'LOW', label: t('ai.values.risk.low') },
    { value: 'MEDIUM', label: t('ai.values.risk.medium') },
    { value: 'HIGH', label: t('ai.values.risk.high') },
  ];

  const pageSizeOptions = [10, 20, 50, 100].map((size) => ({
    value: String(size),
    label: t('ai.shared.pageSizeOption', { count: size }),
  }));

  const pagedItems = useMemo(() => {
    const start = (page - 1) * pageSize;
    return items.slice(start, start + pageSize);
  }, [items, page, pageSize]);

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const handleFilterChange = (key, value) => {
    setFilters((current) => ({ ...current, [key]: value }));
  };

  const applyFilters = async () => {
    setPage(1);
    await loadDashboard({ showGlobalLoading: false });
  };

  const clearFilters = async () => {
    setFilters(DEFAULT_FILTERS);
    setPage(1);
    setTimeout(() => loadDashboard({ preserveSelection: false, showGlobalLoading: false }), 0);
  };

  const handleSelectStudent = async (studentId) => {
    setSelectedStudentId(studentId);
    await loadDetail(studentId);

    if (window.matchMedia('(max-width: 768px)').matches) {
      window.requestAnimationFrame(() => {
        detailPanelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    }
  };

  const handleRecalculateStudent = async (studentId) => {
    setRecalculatingStudent(studentId);
    try {
      await recalculateAiRiskStudent(studentId);
      await loadDashboard();
    } catch (err) {
      console.error('Failed to recalculate risk for student', err);
      setError('recalculate');
    } finally {
      setRecalculatingStudent(null);
    }
  };

  const handleRecalculateAll = async () => {
    setRecalculatingAll(true);
    setError('');
    try {
      await recalculateAiRiskScope({});
      await new Promise((resolve) => {
        window.setTimeout(resolve, 2000);
      });
      await loadDashboard({ preserveSelection: false, showGlobalLoading: false });
    } catch (err) {
      console.error('Failed to recalculate risk scope', err);
      setError('recalculate');
    } finally {
      setRecalculatingAll(false);
    }
  };

  const handleRefreshSummary = async () => {
    if (!analyticsSummary?.id) return;
    setSummaryRefreshing(true);
    setSummaryError('');
    try {
      const languageCode = i18n.language?.startsWith('en') ? 'en' : 'mn';
      const response = await refreshAiAnalyticsSummary(analyticsSummary.id, { languageCode });
      setAnalyticsSummary(response.data);
    } catch (err) {
      console.error('Failed to refresh analytics summary', err);
      setSummaryError('summary');
    } finally {
      setSummaryRefreshing(false);
    }
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 1 || nextPage > totalPages || nextPage === page) return;
    setPage(nextPage);
  };

  const handlePageSizeChange = (value) => {
    setPageSize(Number(value));
    setPage(1);
  };

  const isSectionRefreshing = recalculatingAll || refreshingDashboard;

  if (loading) return <RiskDashboardPageSkeleton audience={audience} />;

  if (error && !items.length && !detail) {
    return (
      <ErrorState
        title={t('ai.risk.loadErrorTitle')}
        description={t('ai.risk.loadErrorDescription')}
        retryLabel={t('admin.users.retry')}
        onRetry={() => loadDashboard({ preserveSelection: false })}
      />
    );
  }

  return (
    <div className="content-stack">
      <div className="page-header">
        <div>
          <div className="page-kicker">{audience === 'admin' ? t('ai.shared.adminKicker') : t('ai.shared.teacherKicker')}</div>
          <h1>{t('ai.risk.dashboardTitle')}</h1>
          <p className="page-summary">{t('ai.risk.dashboardSummary')}</p>
        </div>
        {audience === 'admin' ? (
          isSectionRefreshing ? (
            <div className="users-skeleton users-skeleton-button risk-button-skeleton" aria-hidden="true" />
          ) : (
            <button type="button" className="btn btn-primary" onClick={handleRecalculateAll} disabled={recalculatingAll}>
              {t('ai.risk.recalculateSchool')}
            </button>
          )
        ) : null}
      </div>

      {error ? (
        <div className="alert alert-error">
          {t('ai.risk.actionError')}
        </div>
      ) : null}

      <div className="stats-grid">
        <StatCard icon="S" tone="students" value={summaryStats.total} label={t('ai.risk.studentsWithSnapshots')} />
        <StatCard icon="L" tone="students" value={summaryStats.low} label={t('ai.values.risk.low')} hint={t('ai.risk.lowRiskHint')} />
        <StatCard icon="M" tone="classes" value={summaryStats.medium} label={t('ai.values.risk.medium')} hint={t('ai.risk.mediumRiskHint')} />
        <StatCard icon="H" tone="assignments" value={summaryStats.high} label={t('ai.values.risk.high')} hint={t('ai.risk.highRiskHint')} />
      </div>

      <AnalyticsSummaryPanel
        summary={analyticsSummary}
        enabled={summaryEnabled}
        loading={summaryLoading}
        error={summaryError}
        audience={audience}
        onGenerate={handleGenerateSummary}
        onRefresh={handleRefreshSummary}
        refreshing={summaryRefreshing}
      />

      <div className="grid-dashboard-bottom">
        <SectionCard className="risk-filters-card" title={t('ai.risk.filtersTitle')} subtitle={t('ai.risk.filtersDescription')}>
          <div className="risk-filters-grid">
            <SearchableSelect
              options={classOptions}
              value={filters.classId}
              onChange={(value) => handleFilterChange('classId', String(value || ''))}
              placeholder={t('ai.risk.allClasses')}
              searchPlaceholder={t('ai.risk.searchClasses')}
              emptyLabel={t('ai.risk.noClassesFound')}
            />
            <SelectMenu options={gradeOptions} value={filters.gradeLevel} onChange={(value) => handleFilterChange('gradeLevel', String(value || ''))} placeholder={t('ai.risk.allGrades')} />
            <SelectMenu options={riskLevelOptions} value={filters.riskLevel} onChange={(value) => handleFilterChange('riskLevel', String(value || ''))} placeholder={t('ai.risk.allLevels')} />
            <input
              className="input"
              value={filters.search}
              onChange={(event) => handleFilterChange('search', event.target.value)}
              placeholder={t('ai.risk.searchStudents')}
            />
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-primary" onClick={applyFilters}>{t('ai.risk.applyFilters')}</button>
            <button type="button" className="btn btn-secondary" onClick={clearFilters}>{t('ai.risk.clearFilters')}</button>
          </div>
        </SectionCard>

        <SectionCard title={t('ai.risk.distributionTitle')} subtitle={t('ai.risk.distributionDescription')}>
          {isSectionRefreshing ? (
            <div className="risk-summary-bars">
              {Array.from({ length: 3 }, (_, index) => (
                <div key={`summary-skeleton-${index}`} className="risk-summary-bar-row" aria-hidden="true">
                  <div className="risk-summary-bar-label">
                    <span className="users-skeleton users-skeleton-text" />
                    <strong className="users-skeleton users-skeleton-id" />
                  </div>
                  <div className="risk-summary-bar-track">
                    <div className="users-skeleton risk-summary-skeleton-fill" />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="risk-summary-bars">
              <SummaryBar label={t('ai.values.risk.low')} value={summaryStats.low} max={topSummaryMax} toneClass="is-low" />
              <SummaryBar label={t('ai.values.risk.medium')} value={summaryStats.medium} max={topSummaryMax} toneClass="is-medium" />
              <SummaryBar label={t('ai.values.risk.high')} value={summaryStats.high} max={topSummaryMax} toneClass="is-high" />
            </div>
          )}

          {!isSectionRefreshing && summary?.classBreakdown?.length ? (
            <div className="risk-breakdown-list">
              {summary.classBreakdown.slice(0, 5).map((bucket) => (
                <div key={bucket.classId ?? `unknown-${bucket.className}`} className="interactive-card-link">
                  <div className="interactive-card-main">
                    <div className="interactive-card-title">{bucket.className}</div>
                    <div className="interactive-card-meta">
                      {bucket.gradeLevel ? t('ai.risk.breakdownGrade', { gradeLevel: bucket.gradeLevel }) : t('ai.risk.noGrade')}
                      {' • '}
                      {t('ai.risk.breakdownStudents', { count: bucket.totalStudents })}
                      {' • '}
                      {t('ai.risk.breakdownHighRisk', { count: bucket.highRiskCount })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : isSectionRefreshing ? (
            <div className="risk-breakdown-list" aria-hidden="true">
              {Array.from({ length: 3 }, (_, index) => (
                <div key={`bucket-skeleton-${index}`} className="interactive-card-link">
                  <div className="interactive-card-main">
                    <div className="users-skeleton users-skeleton-name" />
                    <div className="users-skeleton users-skeleton-email" />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="muted-copy">{t('ai.risk.noClassSummary')}</div>
          )}
        </SectionCard>
      </div>

      <div className="grid-student-detail has-detail">
        <SectionCard
          className="risk-results-card"
          title={t('ai.risk.latestStudentRiskList')}
          subtitle={t('ai.risk.resultsSummary', { page, totalPages, total: items.length })}
        >
          <div className="filter-toolbar users-filter-toolbar">
            <SelectMenu options={pageSizeOptions} value={String(pageSize)} onChange={handlePageSizeChange} placeholder={t('ai.risk.pageSize')} />
            <span className="filter-result-count">{t('ai.risk.totalStudents', { count: items.length })}</span>
          </div>

          {items.length ? (
            <div className="table-container desktop-table">
              <table>
                <thead>
                  <tr>
                    <th>{t('ai.risk.student')}</th>
                    <th>{t('ai.risk.class')}</th>
                    <th>{t('ai.risk.level')}</th>
                    <th>{t('ai.risk.score')}</th>
                    <th>{t('ai.risk.topSignals')}</th>
                    <th>{t('ai.risk.updated')}</th>
                    <th>{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {isSectionRefreshing ? <RiskTableSkeletonRows count={Math.max(Math.min(pageSize, 8), 5)} /> : pagedItems.map((item) => (
                    <tr key={item.snapshotId}>
                      <td>
                        <strong>{item.studentName}</strong>
                        {item.isStale ? <div className="muted-copy">{t('ai.risk.snapshotStale')}</div> : null}
                      </td>
                      <td>{item.className || t('ai.shared.unassigned')}</td>
                      <td><AiStatusBadge value={item.riskLevel} /></td>
                      <td>{item.riskScore}</td>
                      <td>{item.topIndicators?.join(', ') || fallback}</td>
                      <td>{formatDateTime(item.calculatedAt, i18n.language, fallback)}</td>
                      <td>
                        <div className="table-action-row">
                          <button type="button" className="btn btn-secondary btn-sm" onClick={() => handleSelectStudent(item.studentId)}>
                            {t('common.open')}
                          </button>
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            disabled={recalculatingStudent === item.studentId}
                            onClick={() => handleRecalculateStudent(item.studentId)}
                          >
                            {recalculatingStudent === item.studentId ? t('ai.shared.running') : t('ai.risk.recalculate')}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState title={t('ai.risk.emptyTitle')} description={t('ai.risk.emptyRealData')} />
          )}

          {items.length ? (
            <div className="mobile-card-list">
              {isSectionRefreshing ? <RiskMobileSkeletonCards count={Math.max(Math.min(pageSize, 4), 3)} /> : pagedItems.map((item) => (
                <div key={item.snapshotId} className="data-card">
                  <div className="data-card-header">
                    <div>
                      <div className="data-card-title">{item.studentName}</div>
                      <div className="muted-copy">{item.className || t('ai.shared.unassigned')}</div>
                    </div>
                    <AiStatusBadge value={item.riskLevel} />
                  </div>
                  <div className="data-card-meta">
                    <div className="data-card-meta-row"><span>{t('ai.risk.score')}</span><strong>{item.riskScore}</strong></div>
                    <div className="data-card-meta-row"><span>{t('ai.risk.signals')}</span><strong>{item.topIndicators?.join(', ') || fallback}</strong></div>
                  </div>
                  <div className="form-actions">
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => handleSelectStudent(item.studentId)}>{t('common.open')}</button>
                    <button type="button" className="btn btn-primary btn-sm" onClick={() => handleRecalculateStudent(item.studentId)}>{t('ai.risk.recalculate')}</button>
                  </div>
                </div>
              ))}
            </div>
          ) : null}

          <div className="pagination-toolbar">
            <div className="pagination-summary">
              {t('ai.risk.paginationSummary', {
                start: items.length === 0 ? 0 : (page - 1) * pageSize + 1,
                end: items.length === 0 ? 0 : Math.min(page * pageSize, items.length),
                total: items.length,
              })}
            </div>
            <div className="pagination-actions">
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(page - 1)} disabled={page <= 1}>
                {t('common.prev')}
              </button>
              <span className="pagination-page-label">{t('ai.risk.pageLabel', { page, totalPages })}</span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => handlePageChange(page + 1)}
                disabled={items.length === 0 || page >= totalPages}
              >
                {t('common.next')}
              </button>
            </div>
          </div>
        </SectionCard>

        <div ref={detailPanelRef} className="risk-student-detail-panel">
          <SectionCard
            className="risk-student-detail-card"
            title={t('ai.risk.studentDetailTitle')}
            subtitle={t('ai.risk.studentDetailDescription')}
            action={detail ? (
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={recalculatingStudent === detail.studentId}
                onClick={() => handleRecalculateStudent(detail.studentId)}
              >
                {recalculatingStudent === detail.studentId ? t('ai.shared.running') : t('ai.risk.recalculateStudent')}
              </button>
            ) : null}
          >
            {loadingDetail ? (
              <LoadingState label={t('ai.risk.loadingStudentDetail')} inline />
            ) : isSectionRefreshing ? (
              <RiskDetailSkeleton />
            ) : detail ? (
              <div className="risk-detail-stack">
                <div className="risk-detail-hero">
                  <div>
                    <div className="page-kicker">{detail.className || t('ai.risk.unassignedClass')}</div>
                    <h3>{detail.studentName}</h3>
                    <p className="muted-copy">{detail.reasonSummary || fallback}</p>
                  </div>
                  <div className="risk-detail-score">
                    <AiStatusBadge value={detail.riskLevel} />
                    <strong>{detail.riskScore}</strong>
                    <span>{t('ai.risk.score')}</span>
                  </div>
                </div>

                <div className="risk-detail-meta-grid">
                  <div className="risk-meta-item"><span>{t('ai.risk.config')}</span><strong>{detail.scoringConfigVersion || fallback}</strong></div>
                  <div className="risk-meta-item"><span>{t('ai.risk.model')}</span><strong>{detail.modelVersionLabel || fallback}</strong></div>
                  <div className="risk-meta-item"><span>{t('ai.risk.lastCalculated')}</span><strong>{formatDateTime(detail.calculatedAt, i18n.language, fallback)}</strong></div>
                  <div className="risk-meta-item"><span>{t('common.status')}</span><strong>{getAiValueLabel(t, detail.snapshotStatus)}</strong></div>
                </div>

                <SectionCard title={t('ai.risk.indicatorBreakdownTitle')} subtitle={t('ai.risk.indicatorBreakdownDescription')}>
                  <div className="risk-indicator-grid">
                    {(detail.indicators || []).map((indicator) => (
                      <IndicatorCard key={indicator.indicatorCode} indicator={indicator} />
                    ))}
                  </div>
                </SectionCard>

                <SectionCard title={t('ai.risk.recommendedAction')} subtitle={t('ai.risk.recommendedActionDescription')}>
                  <p className="muted-copy">{detail.recommendedAction || fallback}</p>
                </SectionCard>

                <SectionCard title={t('ai.risk.recentHistoryTitle')} subtitle={t('ai.risk.recentHistoryDescription')}>
                  {(detail.history || []).length ? (
                    <div className="risk-history-list">
                      {detail.history.map((historyItem) => (
                        <div key={historyItem.id} className="interactive-card-link">
                          <div className="interactive-card-main">
                            <div className="interactive-card-title">
                              {formatDateTime(historyItem.calculatedAt, i18n.language, t('ai.risk.unknownTimestamp'))}
                            </div>
                            <div className="interactive-card-meta">
                              {t('ai.risk.historyMeta', {
                                score: historyItem.riskScore,
                                indicators: historyItem.topIndicators?.join(', ') || t('ai.risk.noIndicators'),
                              })}
                            </div>
                          </div>
                          <AiStatusBadge value={historyItem.riskLevel} />
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="muted-copy">{t('ai.risk.noHistory')}</div>
                  )}
                </SectionCard>
              </div>
            ) : (
              <EmptyState title={t('ai.risk.selectStudentTitle')} description={t('ai.risk.selectStudentDescription')} />
            )}
          </SectionCard>
        </div>
      </div>
    </div>
  );
}
