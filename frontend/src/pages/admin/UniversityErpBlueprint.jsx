import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import SectionCard from '../../components/ui/SectionCard';
import { seedUniversityErpDemoData } from '../../api/endpoints';

const moduleRows = [
  ['admissions', 'demo'],
  ['studentRegistration', 'partial'],
  ['academicManagement', 'partial'],
  ['courseSelection', 'demo'],
  ['assessment', 'partial'],
  ['finance', 'partial'],
  ['studentServices', 'demo'],
  ['hrFaculty', 'demo'],
  ['reporting', 'demo'],
  ['accessManagement', 'partial'],
];

const valueStream = [
  'applicant',
  'admission',
  'registration',
  'courseSelection',
  'learning',
  'assessment',
  'progress',
  'graduation',
  'alumni',
];

const demoModules = ['admissions', 'course-selection', 'student-services', 'hr-faculty', 'reporting'];

export default function UniversityErpBlueprint() {
  const { t } = useTranslation();
  const [seedResult, setSeedResult] = useState(null);
  const [seedError, setSeedError] = useState('');
  const [seeding, setSeeding] = useState(false);

  const runSeed = async () => {
    setSeeding(true);
    setSeedError('');
    try {
      const response = await seedUniversityErpDemoData();
      setSeedResult(response.data);
    } catch (err) {
      setSeedError(err.response?.data?.message || err.message);
    } finally {
      setSeeding(false);
    }
  };

  return (
    <div className="erp-page">
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('universityErp.kicker')}</div>
          <h1>{t('universityErp.blueprint.title')}</h1>
          <p className="page-summary">{t('universityErp.blueprint.summary')}</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={runSeed} disabled={seeding}>
          {seeding ? t('universityErp.blueprint.seed.running') : t('universityErp.blueprint.seed.action')}
        </button>
      </div>

      {seedResult ? (
        <div className="alert alert-success">
          {t('universityErp.blueprint.seed.success', { name: seedResult.studentName })}
        </div>
      ) : null}
      {seedError ? <div className="alert alert-danger">{seedError}</div> : null}

      <div className="erp-stat-strip">
        <div className="erp-metric">
          <span>{t('universityErp.blueprint.metrics.method')}</span>
            <strong>{t('universityErp.method.togafAdm')}</strong>
        </div>
        <div className="erp-metric">
          <span>{t('universityErp.blueprint.metrics.architecture')}</span>
          <strong>{t('universityErp.blueprint.metrics.modularMonolith')}</strong>
        </div>
        <div className="erp-metric">
          <span>{t('universityErp.blueprint.metrics.modules')}</span>
          <strong>10</strong>
        </div>
        <div className="erp-metric">
          <span>{t('universityErp.blueprint.metrics.scope')}</span>
          <strong>{t('universityErp.blueprint.metrics.demoScope')}</strong>
        </div>
      </div>

      <SectionCard title={t('universityErp.blueprint.valueStreamTitle')} subtitle={t('universityErp.blueprint.valueStreamSubtitle')}>
        <div className="erp-value-stream">
          {valueStream.map((step, index) => (
            <div className="erp-value-step" key={step}>
              <span>{String(index + 1).padStart(2, '0')}</span>
              <strong>{t(`universityErp.valueStream.${step}`)}</strong>
            </div>
          ))}
        </div>
      </SectionCard>

      <div className="erp-two-column">
        <SectionCard title={t('universityErp.blueprint.moduleMapTitle')} subtitle={t('universityErp.blueprint.moduleMapSubtitle')}>
          <div className="desktop-table table-container">
            <table>
              <thead>
                <tr>
                  <th>{t('universityErp.common.module')}</th>
                  <th>{t('common.status')}</th>
                  <th>{t('universityErp.common.businessProcess')}</th>
                </tr>
              </thead>
              <tbody>
                {moduleRows.map(([moduleKey, status]) => (
                  <tr key={moduleKey}>
                    <td>{t(`universityErp.modules.${moduleKey}.name`)}</td>
                    <td><span className={`badge erp-status-${status}`}>{t(`universityErp.status.${status}`)}</span></td>
                    <td>{t(`universityErp.modules.${moduleKey}.process`)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </SectionCard>

        <SectionCard title={t('universityErp.blueprint.demoPagesTitle')} subtitle={t('universityErp.blueprint.demoPagesSubtitle')}>
          <div className="erp-module-link-list">
            {demoModules.map((slug) => (
              <Link className="erp-module-link" to={`/admin/university-erp/${slug}`} key={slug}>
                <span>{t(`universityErp.demo.${slug}.short`)}</span>
                <strong>{t(`universityErp.demo.${slug}.title`)}</strong>
                <small>{t(`universityErp.demo.${slug}.summary`)}</small>
              </Link>
            ))}
          </div>
        </SectionCard>
      </div>

      <SectionCard title={t('universityErp.blueprint.governanceTitle')} subtitle={t('universityErp.blueprint.governanceSubtitle')}>
        <div className="erp-governance-grid">
          {['rbac', 'audit', 'privacy', 'integration'].map((item) => (
            <div className="erp-governance-item" key={item}>
              <strong>{t(`universityErp.governance.${item}.title`)}</strong>
              <span>{t(`universityErp.governance.${item}.body`)}</span>
            </div>
          ))}
        </div>
      </SectionCard>
    </div>
  );
}
