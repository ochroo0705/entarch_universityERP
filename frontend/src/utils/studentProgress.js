export const ATTENDED_ATTENDANCE_STATUSES = new Set(['present', 'late', 'excused', 'sick']);
export const COMPLETED_HOMEWORK_STATUSES = new Set(['submitted', 'late', 'graded']);

export const normalizeStatus = (value) => String(value || '').toLowerCase();

export const isAttendanceCountedAsPresent = (status) =>
  ATTENDED_ATTENDANCE_STATUSES.has(normalizeStatus(status));

export const isHomeworkSubmitted = (submission) => {
  if (!submission) return false;
  if (submission.submittedAt) return true;
  return COMPLETED_HOMEWORK_STATUSES.has(normalizeStatus(submission.status));
};

export const isHomeworkGraded = (submission) =>
  normalizeStatus(submission?.status) === 'graded';
