import { useTranslation } from 'react-i18next';
import { getAiValueLabel } from './aiI18n';

const CLASS_BY_VALUE = {
  LOW: 'badge-success',
  MEDIUM: 'badge-warning',
  HIGH: 'badge-danger',
  GENERATED: 'badge-info',
  REVIEWED: 'badge-success',
  INSUFFICIENT_DATA: 'badge-warning',
  FAILED: 'badge-danger',
  REQUESTED: 'badge-warning',
  GENERATING: 'badge-info',
  READY_FOR_REVIEW: 'badge-info',
  GENERATION_FAILED: 'badge-danger',
  APPROVED: 'badge-success',
  REJECTED: 'badge-danger',
  SUCCESS: 'badge-success',
  FAILURE: 'badge-danger',
  EMAIL: 'badge-info',
  SMS: 'badge-warning',
  PORTAL: 'badge-purple',
  ATTENDANCE: 'badge-warning',
  MISSING_WORK: 'badge-danger',
  GRADE_DECLINE: 'badge-danger',
  MIXED_CONCERN: 'badge-purple',
  POSITIVE_UPDATE: 'badge-success',
  GENERAL_FOLLOW_UP: 'badge-info',
  TRUE: 'badge-info',
  FALSE: 'badge-success',
};

export default function AiStatusBadge({ value }) {
  const { t } = useTranslation();
  const normalized = String(value ?? '').toUpperCase();
  return (
    <span className={`badge ${CLASS_BY_VALUE[normalized] || 'badge-purple'}`}>
      {getAiValueLabel(t, value)}
    </span>
  );
}
