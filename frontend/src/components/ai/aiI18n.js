const VALUE_KEY_BY_CODE = {
  LOW: 'ai.values.risk.low',
  MEDIUM: 'ai.values.risk.medium',
  HIGH: 'ai.values.risk.high',
  GENERATED: 'ai.values.snapshotStatus.generated',
  REVIEWED: 'ai.values.snapshotStatus.reviewed',
  INSUFFICIENT_DATA: 'ai.values.snapshotStatus.insufficientData',
  FAILED: 'ai.values.generic.failed',
  REQUESTED: 'ai.values.draftStatus.requested',
  GENERATING: 'ai.values.draftStatus.generating',
  READY_FOR_REVIEW: 'ai.values.draftStatus.readyForReview',
  GENERATION_FAILED: 'ai.values.draftStatus.generationFailed',
  APPROVED: 'ai.values.draftStatus.approved',
  REJECTED: 'ai.values.draftStatus.rejected',
  SUCCESS: 'ai.values.actionStatus.success',
  FAILURE: 'ai.values.actionStatus.failure',
  EMAIL: 'ai.values.channel.email',
  SMS: 'ai.values.channel.sms',
  PORTAL: 'ai.values.channel.portal',
  ATTENDANCE: 'ai.values.issueType.attendance',
  MISSING_WORK: 'ai.values.issueType.missingWork',
  GRADE_DECLINE: 'ai.values.issueType.gradeDecline',
  MIXED_CONCERN: 'ai.values.issueType.mixedConcern',
  POSITIVE_UPDATE: 'ai.values.issueType.positiveUpdate',
  GENERAL_FOLLOW_UP: 'ai.values.issueType.generalFollowUp',
  TRUE: 'common.yes',
  FALSE: 'common.no',
};

export function getAiValueKey(value) {
  const normalized = String(value ?? '').toUpperCase();
  return VALUE_KEY_BY_CODE[normalized] || '';
}

export function getAiValueLabel(t, value, fallback = 'ai.shared.notAvailable') {
  if (value === null || value === undefined || value === '') {
    return t(fallback);
  }

  const key = getAiValueKey(value);
  return key ? t(key) : String(value);
}

export function formatDateTime(value, locale, fallback) {
  if (!value) return fallback;

  return new Date(value).toLocaleString(locale?.startsWith('mn') ? 'mn-MN' : 'en-US');
}

export function formatDate(value, locale, fallback) {
  if (!value) return fallback;

  return new Date(value).toLocaleDateString(locale?.startsWith('mn') ? 'mn-MN' : 'en-US');
}
