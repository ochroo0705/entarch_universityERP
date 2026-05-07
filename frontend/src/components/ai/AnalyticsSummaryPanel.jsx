import { useTranslation } from 'react-i18next';
import SectionCard from '../ui/SectionCard';
import { EmptyState } from '../ui/StateBlock';
import AnalyticsFreshnessBadge from './AnalyticsFreshnessBadge';
import { formatDateTime } from './aiI18n';

function SummaryList({ title, items }) {
  if (!items?.length) return null;
  return (
    <div className="analytics-summary-block">
      <h3>{title}</h3>
      <ul className="analytics-summary-list">
        {items.map((item, index) => <li key={`${title}-${index}`}>{item}</li>)}
      </ul>
    </div>
  );
}

function SummarySkeleton() {
  return (
    <div className="analytics-summary-stack" aria-hidden="true">
      <div className="analytics-summary-hero">
        <div className="parent-page-skeleton-stack">
          <div className="users-skeleton users-skeleton-card-subtitle" />
          <div className="users-skeleton users-skeleton-card-title" />
          <div className="users-skeleton users-skeleton-email" />
        </div>
        <div className="analytics-summary-meta">
          <div className="users-skeleton users-skeleton-id" />
          <div className="users-skeleton users-skeleton-text" />
        </div>
      </div>

      <div className="analytics-summary-meta-grid">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={`analytics-summary-meta-${index}`} className="risk-meta-item">
            <span className="users-skeleton users-skeleton-meta-label" />
            <strong className="users-skeleton users-skeleton-meta-value" />
          </div>
        ))}
      </div>

      <div className="analytics-summary-grid">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={`analytics-summary-list-${index}`} className="analytics-summary-block">
            <div className="users-skeleton users-skeleton-text" style={{ width: '40%', marginBottom: '0.75rem' }} />
            {Array.from({ length: 3 }, (_, itemIndex) => (
              <div key={`analytics-summary-item-${index}-${itemIndex}`} className="users-skeleton users-skeleton-text" style={{ marginBottom: '0.5rem' }} />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

export default function AnalyticsSummaryPanel({
  summary,
  loading,
  error,
  audience,
  enabled,
  onGenerate,
  onRefresh,
  refreshing,
}) {
  const { t, i18n } = useTranslation();
  const fallbackValue = t('ai.shared.notAvailable');
  const showGenerateAction = !loading && !summary && !enabled && onGenerate;

  return (
    <SectionCard
      title={audience === 'admin' ? t('ai.summary.adminTitle') : t('ai.summary.teacherTitle')}
      subtitle={t('ai.summary.subtitle')}
      action={summary ? (
        <div className="analytics-summary-actions">
          <AnalyticsFreshnessBadge summary={summary} />
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={onRefresh}
            disabled={refreshing}
          >
            {refreshing ? t('ai.summary.refreshing') : t('ai.summary.refresh')}
          </button>
        </div>
      ) : showGenerateAction ? (
        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={onGenerate}
        >
          {t('ai.summary.generate')}
        </button>
      ) : null}
    >
      {loading ? <SummarySkeleton /> : null}
      {!loading && error ? (
        <div className="alert alert-error">
          {t('ai.summary.error')}
        </div>
      ) : null}
      {!loading && !summary ? (
        <EmptyState
          title={enabled ? t('ai.summary.emptyTitle') : t('ai.summary.optionalTitle')}
          description={enabled ? t('ai.summary.emptyDescription') : t('ai.summary.optionalDescription')}
          action={!enabled && onGenerate ? (
            <button type="button" className="btn btn-primary" onClick={onGenerate}>
              {t('ai.summary.generate')}
            </button>
          ) : null}
        />
      ) : null}
      {!loading && summary ? (
        <div className="analytics-summary-stack">
          <div className="analytics-summary-hero">
            <div>
              <div className="page-kicker">{summary.scopeLabel || t('ai.summary.currentScope')}</div>
              <h3>{summary.headline || t('ai.summary.fallbackHeadline')}</h3>
              <p className="muted-copy">{summary.overallSummary || t('ai.summary.noText')}</p>
            </div>
            <div className="analytics-summary-meta">
              <strong>{formatDateTime(summary.generatedAt, i18n.language, t('ai.shared.pending'))}</strong>
              <span>{t('ai.summary.lastGenerated')}</span>
            </div>
          </div>

          <div className="analytics-summary-meta-grid">
            <div className="risk-meta-item">
              <span>{t('ai.summary.period')}</span>
              <strong>
                {summary.periodStart && summary.periodEnd
                  ? t('ai.summary.periodRange', { start: summary.periodStart, end: summary.periodEnd })
                  : fallbackValue}
              </strong>
            </div>
            <div className="risk-meta-item">
              <span>{t('ai.summary.provider')}</span>
              <strong>{summary.providerName || t('ai.summary.internalFallback')}</strong>
            </div>
          </div>

          <div className="analytics-summary-grid">
            <SummaryList title={t('ai.summary.keyObservations')} items={summary.keyObservations} />
            <SummaryList title={t('ai.summary.watchAreas')} items={summary.watchAreas} />
          </div>

          <SummaryList title={t('ai.summary.recommendedActions')} items={summary.recommendedActions} />

          {summary.confidenceNote ? (
            <div className="analytics-summary-note">
              <strong>{t('ai.summary.confidenceNote')}</strong>
              <p>{summary.confidenceNote}</p>
            </div>
          ) : null}
        </div>
      ) : null}
    </SectionCard>
  );
}
