import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getAiRiskConfig, updateAiRiskConfig } from '../../api/endpoints';
import { AdminRiskConfigSkeleton } from '../ui/AdminPageSkeletons';
import SectionCard from '../ui/SectionCard';
import StatCard from '../ui/StatCard';
import { ErrorState } from '../ui/StateBlock';

const EMPTY_FORM = {
  configVersion: '',
  attendanceWeight: '0.35',
  latenessWeight: '0.15',
  homeworkWeight: '0.25',
  gradeWeight: '0.25',
  lowMaxScore: '34',
  mediumMaxScore: '64',
  attendanceWindowDays: '45',
  homeworkWindowDays: '30',
  gradeWindowDays: '90',
};

function toFormState(config) {
  return {
    configVersion: config?.configVersion ?? '',
    attendanceWeight: String(config?.attendanceWeight ?? '0.35'),
    latenessWeight: String(config?.latenessWeight ?? '0.15'),
    homeworkWeight: String(config?.homeworkWeight ?? '0.25'),
    gradeWeight: String(config?.gradeWeight ?? '0.25'),
    lowMaxScore: String(config?.lowMaxScore ?? '34'),
    mediumMaxScore: String(config?.mediumMaxScore ?? '64'),
    attendanceWindowDays: String(config?.attendanceWindowDays ?? '45'),
    homeworkWindowDays: String(config?.homeworkWindowDays ?? '30'),
    gradeWindowDays: String(config?.gradeWindowDays ?? '90'),
  };
}

export default function RiskConfigPage() {
  const { t } = useTranslation();
  const [activeConfig, setActiveConfig] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadConfig = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getAiRiskConfig();
      setActiveConfig(response.data);
      setForm(toFormState(response.data));
    } catch (err) {
      console.error('Failed to load risk config', err);
      setError('load');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadConfig();
  }, []);

  const weightTotal = useMemo(() => (
    Number(form.attendanceWeight || 0)
    + Number(form.latenessWeight || 0)
    + Number(form.homeworkWeight || 0)
    + Number(form.gradeWeight || 0)
  ).toFixed(2), [form]);

  const handleChange = (key, value) => {
    setForm((current) => ({ ...current, [key]: value }));
    setSuccess('');
  };

  const handleReset = () => {
    setForm(toFormState(activeConfig));
    setSuccess('');
    setError('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        configVersion: form.configVersion.trim(),
        attendanceWeight: Number(form.attendanceWeight),
        latenessWeight: Number(form.latenessWeight),
        homeworkWeight: Number(form.homeworkWeight),
        gradeWeight: Number(form.gradeWeight),
        lowMaxScore: Number(form.lowMaxScore),
        mediumMaxScore: Number(form.mediumMaxScore),
        attendanceWindowDays: Number(form.attendanceWindowDays),
        homeworkWindowDays: Number(form.homeworkWindowDays),
        gradeWindowDays: Number(form.gradeWindowDays),
        activate: true,
      };
      const response = await updateAiRiskConfig('DEFAULT', payload);
      setActiveConfig(response.data);
      setForm(toFormState(response.data));
      setSuccess(t('ai.config.saveSuccess'));
    } catch (err) {
      console.error('Failed to save risk config', err);
      setError('save');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <AdminRiskConfigSkeleton />;
  }

  if (error === 'load') {
    return (
      <ErrorState
        title={t('ai.config.loadErrorTitle')}
        description={t('ai.config.loadErrorDescription')}
        retryLabel={t('admin.users.retry')}
        onRetry={loadConfig}
      />
    );
  }

  return (
    <div className="content-stack">
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('ai.shared.adminKicker')}</div>
          <h1>{t('ai.config.title')}</h1>
          <p className="page-summary">{t('ai.config.summary')}</p>
        </div>
      </div>

      {error === 'save' ? (
        <div className="alert alert-error">
          {t('ai.config.saveError')}
        </div>
      ) : null}
      {success ? (
        <div className="alert alert-success">
          {success}
        </div>
      ) : null}

      <div className="stats-grid">
        <StatCard icon="V" tone="classes" value={activeConfig?.configVersion || t('ai.shared.notAvailable')} label={t('ai.config.activeVersion')} />
        <StatCard icon="W" tone="students" value={weightTotal} label={t('ai.config.weightTotal')} hint={t('ai.config.weightHint')} />
        <StatCard icon="L" tone="students" value={activeConfig?.lowMaxScore ?? t('ai.shared.notAvailable')} label={t('ai.config.lowRiskCeiling')} />
        <StatCard icon="M" tone="assignments" value={activeConfig?.mediumMaxScore ?? t('ai.shared.notAvailable')} label={t('ai.config.mediumRiskCeiling')} />
      </div>

      <form onSubmit={handleSubmit} className="grid-dashboard-bottom">
        <SectionCard title={t('ai.config.weightsTitle')} subtitle={t('ai.config.weightsDescription')}>
          <div className="risk-filters-grid">
            <div>
              <label className="form-group label-like">{t('ai.config.fields.configVersion')}</label>
              <input className="input" value={form.configVersion} onChange={(event) => handleChange('configVersion', event.target.value)} required />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.attendanceWeight')}</label>
              <input className="input" type="number" min="0" step="0.01" value={form.attendanceWeight} onChange={(event) => handleChange('attendanceWeight', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.latenessWeight')}</label>
              <input className="input" type="number" min="0" step="0.01" value={form.latenessWeight} onChange={(event) => handleChange('latenessWeight', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.homeworkWeight')}</label>
              <input className="input" type="number" min="0" step="0.01" value={form.homeworkWeight} onChange={(event) => handleChange('homeworkWeight', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.gradeWeight')}</label>
              <input className="input" type="number" min="0" step="0.01" value={form.gradeWeight} onChange={(event) => handleChange('gradeWeight', event.target.value)} />
            </div>
          </div>
          <div className="muted-copy" style={{ marginTop: '0.75rem' }}>
            {t('ai.config.currentWeightTotal')} <strong>{weightTotal}</strong>
          </div>
        </SectionCard>

        <SectionCard title={t('ai.config.thresholdsTitle')} subtitle={t('ai.config.thresholdsDescription')}>
          <div className="risk-filters-grid">
            <div>
              <label className="form-group label-like">{t('ai.config.fields.lowMaxScore')}</label>
              <input className="input" type="number" min="0" max="100" value={form.lowMaxScore} onChange={(event) => handleChange('lowMaxScore', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.mediumMaxScore')}</label>
              <input className="input" type="number" min="0" max="100" value={form.mediumMaxScore} onChange={(event) => handleChange('mediumMaxScore', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.attendanceWindowDays')}</label>
              <input className="input" type="number" min="1" value={form.attendanceWindowDays} onChange={(event) => handleChange('attendanceWindowDays', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.homeworkWindowDays')}</label>
              <input className="input" type="number" min="1" value={form.homeworkWindowDays} onChange={(event) => handleChange('homeworkWindowDays', event.target.value)} />
            </div>
            <div>
              <label className="form-group label-like">{t('ai.config.fields.gradeWindowDays')}</label>
              <input className="input" type="number" min="1" value={form.gradeWindowDays} onChange={(event) => handleChange('gradeWindowDays', event.target.value)} />
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? t('common.saving') : t('ai.config.saveAndActivate')}
            </button>
            <button type="button" className="btn btn-secondary" onClick={handleReset} disabled={saving}>
              {t('ai.config.reset')}
            </button>
          </div>
        </SectionCard>
      </form>
    </div>
  );
}
