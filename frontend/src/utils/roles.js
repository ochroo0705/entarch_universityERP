export const ROLE_FLAGS = {
  ADMIN: 8,
  TEACHER: 2,
  STUDENT: 1,
  PARENT: 4,
  COUNSELOR: 16,
  NURSE: 32,
  FINANCE_STAFF: 64,
  LIBRARIAN: 128,
  TRANSPORT_COORDINATOR: 256,
  ADMISSIONS_STAFF: 512,
  CAFETERIA_STAFF: 1024,
};

export const roleLabelKey = (code) => `roles.${code}`;

export const roleName = (flags, t) => {
  const roleEntries = Object.entries(ROLE_FLAGS)
    .filter(([, flag]) => (Number(flags || 0) & flag) !== 0)
    .map(([code]) => t(roleLabelKey(code)));
  return roleEntries.join(', ') || t('admin.users.noRole');
};

export const primaryRoleBadgeClass = (flags) => {
  const value = Number(flags || 0);
  if (value & ROLE_FLAGS.ADMIN) return 'badge-danger';
  if (value & ROLE_FLAGS.TEACHER) return 'badge-info';
  if (value & ROLE_FLAGS.STUDENT) return 'badge-success';
  if (value & ROLE_FLAGS.PARENT) return 'badge-purple';
  if (value & (ROLE_FLAGS.NURSE | ROLE_FLAGS.COUNSELOR)) return 'badge-warning';
  return 'badge-info';
};
