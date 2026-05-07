import { useTranslation } from 'react-i18next';

export default function AnalyticsFreshnessBadge({ summary }) {
  const { t } = useTranslation();
  if (!summary) return null;

  const label = summary.status === 'FAILED'
    ? t('ai.values.freshness.failed')
    : summary.isStale
      ? t('ai.values.freshness.stale')
      : summary.status === 'GENERATING'
        ? t('ai.values.freshness.generating')
        : t('ai.values.freshness.fresh');

  const toneClass = summary.status === 'FAILED'
    ? 'badge-danger'
    : summary.isStale
      ? 'badge-warning'
      : 'badge-success';

  return <span className={`badge ${toneClass}`}>{label}</span>;
}
