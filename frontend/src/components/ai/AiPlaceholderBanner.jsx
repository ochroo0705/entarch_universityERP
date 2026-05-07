import { useTranslation } from 'react-i18next';

export default function AiPlaceholderBanner({ titleKey = 'ai.shared.placeholderTitle', descriptionKey = 'ai.shared.placeholderDescription' }) {
  const { t } = useTranslation();

  return (
    <div
      className="card"
      style={{
        marginBottom: '1rem',
        border: '1px solid rgba(58, 123, 204, 0.18)',
        background: 'linear-gradient(135deg, rgba(58, 123, 204, 0.08), rgba(26, 107, 92, 0.08))',
      }}
    >
      <div className="card-body" style={{ padding: '1rem 1.1rem' }}>
        <div className="badge badge-info" style={{ marginBottom: '0.65rem' }}>{t('ai.shared.phase')}</div>
        <h3 style={{ marginBottom: '0.35rem' }}>{t(titleKey)}</h3>
        <p style={{ color: 'var(--text-muted)' }}>{t(descriptionKey)}</p>
      </div>
    </div>
  );
}
