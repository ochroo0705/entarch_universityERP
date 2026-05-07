import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getAiAuditLogs } from '../../api/endpoints';
import { AdminAuditLogSkeleton } from '../ui/AdminPageSkeletons';
import SectionCard from '../ui/SectionCard';
import { EmptyState, ErrorState } from '../ui/StateBlock';
import AiPlaceholderBanner from './AiPlaceholderBanner';
import AiStatusBadge from './AiStatusBadge';
import SelectMenu from '../ui/SelectMenu';
import { formatDateTime } from './aiI18n';

const DEFAULT_PAGE_SIZE = 20;

function AuditLogSkeletonRows({ count = 8 }) {
  return Array.from({ length: count }, (_, index) => (
    <tr key={`audit-skeleton-row-${index}`} className="users-skeleton-row" aria-hidden="true">
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-text" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-name" /></td>
      <td><div className="users-skeleton users-skeleton-pill" /></td>
      <td><div className="users-skeleton users-skeleton-email" /></td>
    </tr>
  ));
}

function AuditLogMobileSkeletonCards({ count = 4 }) {
  return Array.from({ length: count }, (_, index) => (
    <article key={`audit-skeleton-card-${index}`} className="data-card users-skeleton-card" aria-hidden="true">
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

export default function AuditLogPage() {
  const { t, i18n } = useTranslation();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [query, setQuery] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
  });
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    totalPages: 0,
  });

  useEffect(() => {
    let ignore = false;

    const run = async () => {
      if (loading) {
        setLoading(true);
      } else {
        setRefreshing(true);
      }

      setError('');

      try {
        const response = await getAiAuditLogs({
          page: query.page,
          pageSize: query.pageSize,
        });
        if (ignore) return;

        const data = response.data ?? {};
        setLogs(Array.isArray(data.items) ? data.items : []);
        setPagination({
          page: data.page ?? query.page,
          pageSize: data.pageSize ?? query.pageSize,
          total: data.total ?? 0,
          totalPages: data.totalPages ?? 0,
        });
      } catch (err) {
        if (ignore) return;
        console.error('Failed to load AI audit logs', err);
        setError('audit');
      } finally {
        if (ignore) return;
        setLoading(false);
        setRefreshing(false);
      }
    };

    run();

    return () => {
      ignore = true;
    };
  }, [query.page, query.pageSize]);

  const retryLoad = () => {
    setLoading(true);
    setError('');
    setQuery((current) => ({ ...current }));
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 1 || nextPage > pagination.totalPages || nextPage === query.page) return;
    setQuery((current) => ({ ...current, page: nextPage }));
  };

  const handlePageSizeChange = (value) => {
    setQuery({
      page: 1,
      pageSize: Number(value),
    });
  };

  const pageSizeOptions = [10, 20, 50, 100].map((size) => ({
    value: String(size),
    label: t('ai.shared.pageSizeOption', { count: size }),
  }));

  if (loading) return <AdminAuditLogSkeleton />;

  if (error) {
    return (
      <ErrorState
        title={t('ai.audit.loadErrorTitle')}
        description={t('ai.audit.loadErrorDescription')}
        retryLabel={t('admin.users.retry')}
        onRetry={retryLoad}
      />
    );
  }

  return (
    <div className="audit-log-page">
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('ai.shared.adminKicker')}</div>
          <h1>{t('ai.audit.title')}</h1>
          <p className="page-summary">{t('ai.audit.summary')}</p>
        </div>
      </div>

      <AiPlaceholderBanner titleKey="ai.audit.placeholderTitle" descriptionKey="ai.audit.placeholderDescription" />

      <SectionCard
        title={t('ai.audit.tableTitle')}
        subtitle={t('ai.audit.resultsSummary', {
          page: pagination.page,
          totalPages: Math.max(pagination.totalPages, 1),
          total: pagination.total,
        })}
      >
        <div className="filter-toolbar users-filter-toolbar audit-log-toolbar">
          <SelectMenu
            options={pageSizeOptions}
            value={String(query.pageSize)}
            onChange={handlePageSizeChange}
            placeholder={t('ai.audit.pageSize')}
          />
          <span className="filter-result-count">{t('ai.audit.totalLogs', { count: pagination.total })}</span>
        </div>

        {logs.length ? (
          <>
            <div className="table-container desktop-table">
              <table>
                <thead>
                  <tr>
                    <th>{t('ai.audit.event')}</th>
                    <th>{t('ai.audit.entity')}</th>
                    <th>{t('ai.audit.actor')}</th>
                    <th>{t('ai.risk.student')}</th>
                    <th>{t('common.status')}</th>
                    <th>{t('ai.audit.createdAt')}</th>
                  </tr>
                </thead>
                <tbody>
                  {refreshing ? <AuditLogSkeletonRows count={Math.max(Math.min(query.pageSize, 8), 5)} /> : logs.map((log) => (
                    <tr key={log.id}>
                      <td>{log.eventType}</td>
                      <td>{log.entityType}</td>
                      <td>{log.actorUserName}</td>
                      <td>{log.targetStudentName || t('ai.shared.notAvailable')}</td>
                      <td><AiStatusBadge value={log.actionStatus} /></td>
                      <td>{formatDateTime(log.createdAt, i18n.language, t('ai.shared.notAvailable'))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="mobile-card-list audit-log-mobile-list">
              {refreshing ? <AuditLogMobileSkeletonCards count={Math.max(Math.min(query.pageSize, 4), 3)} /> : logs.map((log) => (
                <article key={log.id} className="data-card audit-log-card">
                  <div className="data-card-header audit-log-card-header">
                    <div>
                      <div className="data-card-title">{log.eventType}</div>
                      <div className="muted-copy">{log.entityType}</div>
                    </div>
                    <AiStatusBadge value={log.actionStatus} />
                  </div>

                  <div className="data-card-meta audit-log-card-meta">
                    <div className="data-card-meta-row audit-log-card-row">
                      <span>{t('ai.audit.actor')}</span>
                      <strong>{log.actorUserName}</strong>
                    </div>
                    <div className="data-card-meta-row audit-log-card-row">
                      <span>{t('ai.risk.student')}</span>
                      <strong>{log.targetStudentName || t('ai.shared.notAvailable')}</strong>
                    </div>
                    <div className="data-card-meta-row audit-log-card-row">
                      <span>{t('ai.audit.createdAt')}</span>
                      <strong>{formatDateTime(log.createdAt, i18n.language, t('ai.shared.notAvailable'))}</strong>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            <div className="pagination-toolbar audit-log-pagination">
              <div className="pagination-summary">
                {t('ai.audit.paginationSummary', {
                  start: pagination.total === 0 ? 0 : (pagination.page - 1) * pagination.pageSize + 1,
                  end: pagination.total === 0 ? 0 : Math.min(pagination.page * pagination.pageSize, pagination.total),
                  total: pagination.total,
                })}
              </div>
              <div className="pagination-actions audit-log-pagination-actions">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(query.page - 1)} disabled={query.page <= 1}>
                  {t('common.prev')}
                </button>
                <span className="pagination-page-label">
                  {t('ai.audit.pageLabel', {
                    page: pagination.page,
                    totalPages: Math.max(pagination.totalPages, 1),
                  })}
                </span>
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => handlePageChange(query.page + 1)} disabled={pagination.totalPages === 0 || query.page >= pagination.totalPages}>
                  {t('common.next')}
                </button>
              </div>
            </div>
          </>
        ) : (
          <EmptyState title={t('ai.audit.emptyTitle')} description={t('ai.audit.emptyDescription')} />
        )}
      </SectionCard>
    </div>
  );
}
