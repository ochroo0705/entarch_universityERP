import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getAllUsers, getRoleOptions, updateUserRoles } from '../../api/endpoints';
import SectionCard from '../../components/ui/SectionCard';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';
import { roleLabelKey, roleName, primaryRoleBadgeClass } from '../../utils/roles';

const buildName = (user) => [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || `#${user.id}`;

export default function StaffPermissions() {
  const { t } = useTranslation();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState('');
  const [error, setError] = useState('');

  const staffRoleCodes = useMemo(() => new Set(['ADMIN', 'TEACHER', 'FINANCE_STAFF', 'ADMISSIONS_STAFF']), []);
  const staffRoles = useMemo(() => roles.filter((role) => staffRoleCodes.has(role.code)), [roles, staffRoleCodes]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [roleRes, userRes] = await Promise.all([
        getRoleOptions(),
        getAllUsers({ page: 1, pageSize: 100, sortBy: 'name', sortOrder: 'asc' }),
      ]);
      const nextRoles = roleRes.data || [];
      const nextUsers = Array.isArray(userRes.data?.items) ? userRes.data.items : [];
      setRoles(nextRoles);
      setUsers(nextUsers);
      setDrafts(Object.fromEntries(nextUsers.map((user) => [user.id, Number(user.roleFlags || 0)])));
    } catch (err) {
      console.error('Failed to load staff permissions', err);
      setError(err.response?.data?.message || t('admin.staffPermissions.loadErrorDescription'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const toggleRole = (userId, flag) => {
    setDrafts((current) => ({
      ...current,
      [userId]: (Number(current[userId] || 0) & flag)
        ? Number(current[userId] || 0) & ~flag
        : Number(current[userId] || 0) | flag,
    }));
  };

  const saveRoles = async (userId) => {
    setSaving(String(userId));
    try {
      await updateUserRoles(userId, { roleFlags: drafts[userId] });
      await load();
    } finally {
      setSaving('');
    }
  };

  if (loading) return <LoadingState label={t('admin.staffPermissions.loading')} />;
  if (error) return <ErrorState title={t('admin.staffPermissions.loadErrorTitle')} description={error} retryLabel={t('admin.users.retry')} onRetry={load} />;

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-kicker">{t('admin.staffPermissions.kicker')}</div>
          <h1>{t('admin.staffPermissions.title')}</h1>
          <p className="page-summary">{t('admin.staffPermissions.summary')}</p>
        </div>
      </div>

      <SectionCard title={t('admin.staffPermissions.staffRolesTitle')} subtitle={t('admin.staffPermissions.staffRolesSubtitle')}>
        {users.length ? (
          <div className="staff-permissions-grid">
            {users.map((user) => {
              const draftFlags = Number(drafts[user.id] || 0);
              const unchanged = draftFlags === Number(user.roleFlags || 0);
              return (
                <article className="data-card staff-permission-card" key={user.id}>
                  <div className="data-card-header">
                    <div>
                      <div className="data-card-title">{buildName(user)}</div>
                      <div className="muted-copy">@{user.username}</div>
                    </div>
                    <span className={`badge ${primaryRoleBadgeClass(draftFlags)}`}>{roleName(draftFlags, t)}</span>
                  </div>

                  <div className="staff-role-list">
                    {staffRoles.map((role) => (
                      <label className="staff-role-option" key={role.flag}>
                        <input
                          type="checkbox"
                          checked={(draftFlags & role.flag) !== 0}
                          onChange={() => toggleRole(user.id, role.flag)}
                        />
                        <span>
                          <strong>{t(roleLabelKey(role.code))}</strong>
                          <small>{t(`rolesDescription.${role.code}`)}</small>
                        </span>
                      </label>
                    ))}
                  </div>

                  <button type="button" className="btn btn-primary btn-block" disabled={unchanged || saving === String(user.id) || draftFlags <= 0} onClick={() => saveRoles(user.id)}>
                    {saving === String(user.id) ? t('common.saving') : t('admin.staffPermissions.saveRoles')}
                  </button>
                </article>
              );
            })}
          </div>
        ) : (
          <EmptyState title={t('admin.staffPermissions.empty')} description={t('admin.staffPermissions.emptyDescription')} />
        )}
      </SectionCard>
    </div>
  );
}
