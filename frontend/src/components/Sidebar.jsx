import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState, useEffect, useMemo, useCallback, memo } from 'react';
import { useTranslation } from 'react-i18next';
import LanguageSwitcher from './LanguageSwitcher';

export default memo(function Sidebar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navItems = useMemo(() => [
    {
      section: t('sidebar.overview'),
      links: [
        { to: '/admin', label: t('sidebar.dashboard'), icon: '📊', end: true },
      ],
    },
    {
      section: t('sidebar.people'),
      links: [
        { to: '/admin/users', label: t('sidebar.users'), icon: '👥' },
        { to: '/admin/parent-students', label: t('sidebar.parentStudentLinks'), icon: '👨‍👧' },
      ],
    },
    {
      section: t('sidebar.academics'),
      links: [
        { to: '/admin/classes', label: t('sidebar.classes'), icon: '🏫' },
        { to: '/admin/subjects', label: t('sidebar.subjects'), icon: '📚' },
        { to: '/admin/teaching-assignments', label: t('sidebar.teachingAssignments'), icon: '📋' },
        { to: '/admin/enrollments', label: t('sidebar.enrollments'), icon: '📝' },
      ],
    },
    {
      section: t('sidebar.operations'),
      links: [
        { to: '/admin/schedules', label: t('sidebar.schedules'), icon: '🗓️' },
        { to: '/admin/announcements', label: t('sidebar.announcements'), icon: '📢' },
      ],
    },
  ], [t]);

  useEffect(() => { setMobileOpen(false); }, [location.pathname]);

  const handleLogout = useCallback(() => {
    logout();
    navigate('/login');
  }, [logout, navigate]);

  const initials = user
    ? `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase()
    : 'A';

  return (
    <>
      <button className="mobile-menu-btn" onClick={() => setMobileOpen(!mobileOpen)} aria-label="Toggle menu">
        {mobileOpen ? '✕' : '☰'}
      </button>
      {mobileOpen && <div className="sidebar-overlay active" onClick={() => setMobileOpen(false)} />}
      <aside className={`sidebar${mobileOpen ? ' open' : ''}`}>
      <div className="sidebar-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <div style={{
            width: 32, height: 32, borderRadius: 8,
            background: 'linear-gradient(135deg, #1A6B5C, #5DE5B5)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '0.9rem', flexShrink: 0,
          }}>🎓</div>
          <div>
            <h2>EduSys</h2>
            <span>{t('sidebar.adminPortal')}</span>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((section) => (
          <div key={section.section} className="sidebar-section">
            <div className="sidebar-section-title">{section.section}</div>
            {section.links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                className={({ isActive }) =>
                  `sidebar-link${isActive ? ' active' : ''}`
                }
              >
                <span className="icon">{link.icon}</span>
                {link.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <div className="sidebar-avatar">{initials}</div>
          <div className="sidebar-user-info">
            <div className="sidebar-user-name">
              {user?.firstName} {user?.lastName}
            </div>
            <div className="sidebar-user-role">{t('roles.administrator')}</div>
          </div>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <button className="btn btn-secondary btn-sm btn-block" onClick={handleLogout}
            style={{ background: 'rgba(255,255,255,0.06)', color: '#8B9BB4', border: '1px solid rgba(255,255,255,0.08)', flex: 1 }}>
            {t('common.signOut')}
          </button>
          <LanguageSwitcher />
        </div>
      </div>
      </aside>
    </>
  );
});
