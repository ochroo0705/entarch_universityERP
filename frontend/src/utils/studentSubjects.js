export const getSubjectLabel = (item) => {
  const raw =
    item?.subjectName
    || item?.subject
    || item?.subjectTitle
    || item?.teachingAssignment?.subject?.subjectName
    || item?.teachingAssignment?.subject?.name
    || item?.teachingAssignment?.subject?.subjectNameMn
    || '';

  return String(raw).trim();
};

export const buildSubjectSlug = (label) =>
  {
    const normalized = String(label || '')
      .normalize('NFKD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();

    const slug = normalized
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');

    if (slug) return slug;

    const fallback = Array.from(String(label || ''))
      .reduce((hash, char) => ((hash * 31) + char.charCodeAt(0)) >>> 0, 7)
      .toString(36);

    return `subject-${fallback}`;
  };

export function buildStudentSubjects({ schedule = [], homework = [], attendance = [] }) {
  const subjectMap = new Map();

  const ensureSubject = (label) => {
    const normalized = String(label || '').trim();
    if (!normalized) return null;

    if (!subjectMap.has(normalized)) {
      subjectMap.set(normalized, {
        name: normalized,
        slug: buildSubjectSlug(normalized),
        scheduleItems: [],
        homeworkItems: [],
        attendanceItems: [],
        teacherNames: new Set(),
        classNames: new Set(),
        roomNumbers: new Set(),
      });
    }

    return subjectMap.get(normalized);
  };

  schedule.forEach((item) => {
    const subject = ensureSubject(getSubjectLabel(item));
    if (!subject) return;

    subject.scheduleItems.push(item);
    if (item.teacher) subject.teacherNames.add(item.teacher);
    if (item.className) subject.classNames.add(item.className);
    if (item.roomNumber) subject.roomNumbers.add(item.roomNumber);
  });

  homework.forEach((item) => {
    const subject = ensureSubject(getSubjectLabel(item));
    if (!subject) return;
    subject.homeworkItems.push(item);
  });

  attendance.forEach((item) => {
    const subject = ensureSubject(getSubjectLabel(item));
    if (!subject) return;
    subject.attendanceItems.push(item);
  });

  return Array.from(subjectMap.values())
    .map((subject) => ({
      ...subject,
      teacherNames: Array.from(subject.teacherNames),
      classNames: Array.from(subject.classNames),
      roomNumbers: Array.from(subject.roomNumbers),
    }))
    .sort((a, b) => a.name.localeCompare(b.name));
}
