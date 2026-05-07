import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import LanguageSwitcher from './LanguageSwitcher';
import edusysLogo from '../res/edusys_logo.png';

const ROLE_CONFIG = {
  admin: {
    portalLabelKey: 'sidebar.adminPortal',
    roleLabelKey: 'roles.administrator',
    nav: [
      { sectionKey: 'sidebar.overview', links: [{ to: '/admin', labelKey: 'sidebar.dashboard', icon: '\u{1F4CA}', end: true }] },
      {
        sectionKey: 'sidebar.people',
        links: [
          { to: '/admin/users', labelKey: 'sidebar.users', icon: '\u{1F465}' },
          { to: '/admin/staff-permissions', labelKey: 'sidebar.staffPermissions', icon: '\u{1F510}' },
        ],
      },
      {
        sectionKey: 'sidebar.academics',
        links: [
          { to: '/admin/classes', labelKey: 'sidebar.classes', icon: '\u{1F3EB}' },
          { to: '/admin/subjects', labelKey: 'sidebar.subjects', icon: '\u{1F4DA}' },
          { to: '/admin/teaching-assignments', labelKey: 'sidebar.teachingAssignments', icon: '\u{1F4CB}' },
          { to: '/admin/enrollments', labelKey: 'sidebar.enrollments', icon: '\u{1F4DD}' },
        ],
      },
      {
        sectionKey: 'sidebar.operations',
        links: [
          { to: '/admin/schedules', labelKey: 'sidebar.schedules', icon: '\u{1F5D3}\uFE0F' },
          { to: '/admin/exam-schedules', labelKey: 'sidebar.examSchedules', icon: '\u{1F4C5}' },
          { to: '/admin/finance-cafeteria', labelKey: 'sidebar.financeCafeteria', icon: '\u{1F9FE}' },
          { to: '/admin/announcements', labelKey: 'sidebar.announcements', icon: '\u{1F4E2}' },
        ],
      },
      {
        sectionKey: 'sidebar.enterpriseArchitecture',
        links: [
          { to: '/admin/university-erp', labelKey: 'sidebar.universityErpBlueprint', icon: '\u{1F5FA}\uFE0F' },
          { to: '/admin/university-erp/admissions', labelKey: 'sidebar.erpAdmissions', icon: '\u{1F4E5}' },
          { to: '/admin/university-erp/course-selection', labelKey: 'sidebar.erpCourseSelection', icon: '\u{1F4DA}' },
          { to: '/admin/university-erp/student-services', labelKey: 'sidebar.erpStudentServices', icon: '\u{1F4AC}' },
          { to: '/admin/university-erp/reporting', labelKey: 'sidebar.erpReporting', icon: '\u{1F4C8}' },
        ],
      },
      {
        sectionKey: 'sidebar.aiTools',
        links: [
          { to: '/admin/ai/risk-dashboard', labelKey: 'sidebar.aiRiskDashboard', icon: '\u26A0\uFE0F' },
          { to: '/admin/ai/risk-config', labelKey: 'sidebar.aiRiskConfig', icon: '\u2699\uFE0F' },
          { to: '/admin/ai/audit', labelKey: 'sidebar.aiAudit', icon: '\u{1F50D}' },
        ],
      },
    ],
  },
  teacher: {
    portalLabelKey: 'sidebar.teacherPortal',
    roleLabelKey: 'roles.teacher',
    nav: [
      { sectionKey: 'sidebar.overview', links: [{ to: '/teacher', labelKey: 'sidebar.dashboard', icon: '\u{1F4CA}', end: true }] },
      {
        sectionKey: 'sidebar.teaching',
        links: [
          { to: '/teacher/classes', labelKey: 'sidebar.myClasses', icon: '\u{1F3EB}' },
          { to: '/teacher/schedule', labelKey: 'sidebar.mySchedule', icon: '\u{1F5D3}\uFE0F' },
          { to: '/teacher/exams', labelKey: 'sidebar.exams', icon: '\u{1F4C5}' },
        ],
      },
      { sectionKey: 'sidebar.communication', links: [{ to: '/teacher/announcements', labelKey: 'sidebar.announcements', icon: '\u{1F4E2}' }] },
      {
        sectionKey: 'sidebar.aiTools',
        links: [
          { to: '/teacher/ai/risk-dashboard', labelKey: 'sidebar.aiRiskDashboard', icon: '\u26A0\uFE0F' },
        ],
      },
    ],
  },
  staff: {
    portalLabelKey: 'sidebar.staffPortal',
    roleLabelKey: 'roles.operationsStaff',
    nav: [
      { sectionKey: 'sidebar.operations', links: [{ to: '/staff/finance-cafeteria', labelKey: 'sidebar.financeCafeteria', icon: '\u{1F9FE}' }] },
    ],
  },
  student: {
    portalLabelKey: 'sidebar.studentPortal',
    roleLabelKey: 'roles.student',
    nav: [
      { sectionKey: 'sidebar.overview', links: [{ to: '/student', labelKey: 'sidebar.dashboard', icon: '\u{1F3E0}', end: true }] },
      {
        sectionKey: 'sidebar.academics',
        links: [
          { to: '/student/schedule', labelKey: 'sidebar.mySchedule', icon: '\u{1F5D3}\uFE0F' },
          { to: '/student/subjects', labelKey: 'sidebar.subjects', icon: '\u{1F4DA}' },
          { to: '/student/exams', labelKey: 'sidebar.exams', icon: '\u{1F4C5}' },
          { to: '/student/homework', labelKey: 'sidebar.homework', icon: '\u{1F4DD}' },
          { to: '/student/grades', labelKey: 'sidebar.myGrades', icon: '\u{1F4CA}' },
          { to: '/student/attendance', labelKey: 'sidebar.attendance', icon: '\u2705' },
          { to: '/student/finance-cafeteria', labelKey: 'sidebar.financeCafeteria', icon: '\u{1F9FE}' },
        ],
      },
      { sectionKey: 'sidebar.communication', links: [{ to: '/student/announcements', labelKey: 'sidebar.announcements', icon: '\u{1F4E2}' }] },
    ],
  },
  parent: {
    portalLabelKey: 'sidebar.parentPortal',
    roleLabelKey: 'roles.parent',
    nav: [
      { sectionKey: 'sidebar.overview', links: [{ to: '/parent', labelKey: 'sidebar.dashboard', icon: '\u{1F3E0}', end: true }] },
      {
        sectionKey: 'sidebar.myChildren',
        links: [
          { to: '/parent/schedule', labelKey: 'sidebar.schedule', icon: '\u{1F5D3}\uFE0F' },
          { to: '/parent/exams', labelKey: 'sidebar.exams', icon: '\u{1F4C5}' },
          { to: '/parent/homework', labelKey: 'sidebar.homework', icon: '\u{1F4DD}' },
          { to: '/parent/grades', labelKey: 'sidebar.grades', icon: '\u{1F4CA}' },
          { to: '/parent/attendance', labelKey: 'sidebar.attendance', icon: '\u2705' },
          { to: '/parent/finance-cafeteria', labelKey: 'sidebar.financeCafeteria', icon: '\u{1F9FE}' },
        ],
      },
      { sectionKey: 'sidebar.communication', links: [{ to: '/parent/announcements', labelKey: 'sidebar.announcements', icon: '\u{1F4E2}' }] },
    ],
  },
};

function AppSidebar({ role }) {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const asideRef = useRef(null);
  const config = ROLE_CONFIG[role] || ROLE_CONFIG.student;

  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!mobileOpen) return undefined;

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') setMobileOpen(false);
    };

    document.addEventListener('keydown', handleKeyDown);
    asideRef.current?.querySelector('.sidebar-link')?.focus();
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [mobileOpen]);

  useEffect(() => {
    if (!mobileOpen || typeof window === 'undefined' || window.innerWidth > 768) {
      return undefined;
    }

    const { body, documentElement } = document;
    const scrollY = window.scrollY;
    const previousBodyOverflow = body.style.overflow;
    const previousBodyPosition = body.style.position;
    const previousBodyTop = body.style.top;
    const previousBodyWidth = body.style.width;
    const previousHtmlOverflow = documentElement.style.overflow;

    // Lock the page behind the mobile drawer so touch scrolling does not move the layout.
    body.style.overflow = 'hidden';
    body.style.position = 'fixed';
    body.style.top = `-${scrollY}px`;
    body.style.width = '100%';
    documentElement.style.overflow = 'hidden';

    return () => {
      body.style.overflow = previousBodyOverflow;
      body.style.position = previousBodyPosition;
      body.style.top = previousBodyTop;
      body.style.width = previousBodyWidth;
      documentElement.style.overflow = previousHtmlOverflow;
      window.scrollTo(0, scrollY);
    };
  }, [mobileOpen]);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 768) {
        setMobileOpen(false);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleLogout = useCallback(() => {
    logout();
    navigate('/login');
  }, [logout, navigate]);

  const initials = useMemo(() => {
    if (!user) return role?.[0]?.toUpperCase() || 'U';
    return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
  }, [role, user]);

  return (
    <>
      <button className="mobile-menu-btn" onClick={() => setMobileOpen((open) => !open)} aria-label="Toggle menu" aria-expanded={mobileOpen} aria-controls="app-sidebar">
        {mobileOpen ? '\u2715' : '\u2630'}
      </button>
      {mobileOpen ? <button className="sidebar-overlay active" onClick={() => setMobileOpen(false)} aria-label="Close menu" /> : null}
      <aside id="app-sidebar" ref={asideRef} className={`sidebar${mobileOpen ? ' open' : ''}`} aria-hidden={!mobileOpen && typeof window !== 'undefined' && window.innerWidth <= 768}>
        <div className="sidebar-header">
          <div className="sidebar-brand">
            <div className="sidebar-brand-icon">
              <img src={edusysLogo} alt="EduSys logo" className="sidebar-brand-logo" />
            </div>
            <div>
              <h2>EduSys</h2>
              <span>{t(config.portalLabelKey)}</span>
            </div>
          </div>
        </div>

        <nav className="sidebar-nav">
          {config.nav.map((section) => (
            <div key={section.sectionKey} className="sidebar-section">
              <div className="sidebar-section-title">{t(section.sectionKey)}</div>
              {section.links.map((link) => (
                <NavLink key={link.to} to={link.to} end={link.end} className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}>
                  <span className="icon" aria-hidden="true">{link.icon}</span>
                  <span>{t(link.labelKey)}</span>
                </NavLink>
              ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-avatar">{initials}</div>
            <div className="sidebar-user-info">
              <div className="sidebar-user-name">{user?.firstName} {user?.lastName}</div>
              <div className="sidebar-user-role">{t(config.roleLabelKey)}</div>
            </div>
          </div>
          <div className="sidebar-footer-actions">
            <button className="btn btn-secondary btn-sm btn-block sidebar-logout-btn" onClick={handleLogout}>
              {t('common.signOut')}
            </button>
            <LanguageSwitcher />
          </div>
        </div>
      </aside>
    </>
  );
}

export default memo(AppSidebar);
