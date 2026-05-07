import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { login as loginApi } from '../api/endpoints';
import edusysLogo from '../res/edusys_logo.png';

const normalizeRole = (role) => String(role || '').replace(/^ROLE_/, '');
const hasRole = (roles, role) => roles.map(normalizeRole).includes(role);

export default function Login() {
  const { t, i18n } = useTranslation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const currentLang = i18n.language?.startsWith('mn') ? 'mn' : 'en';
  const toggleLang = () => i18n.changeLanguage(currentLang === 'en' ? 'mn' : 'en');
  const uiCopy = useMemo(() => (
    currentLang === 'mn'
      ? {
          switchTitle: 'Mongol hel ruu solih',
          demoTitle: 'Turshiltiin nevtreh medeellel',
          demoBody: 'Admin orchnoor shalgah bol admin / admin123 ashiglana uu.',
          show: 'Harah',
          hide: 'Nuuh',
        }
      : {
          switchTitle: 'Switch to Mongolian',
          demoTitle: 'Demo credentials',
          demoBody: 'Use admin / admin123 to quickly review the admin experience.',
          show: 'Show',
          hide: 'Hide',
        }
  ), [currentLang]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await loginApi(username, password);
      const data = res.data;
      const roles = Array.isArray(data.roles) && data.roles.length > 0 ? data.roles : [data.role];

      login(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
          firstName: data.firstName,
          lastName: data.lastName,
          role: data.role,
          roles,
        },
        data.token
      );

      if (hasRole(roles, 'ADMIN')) {
        navigate('/admin');
      } else if (hasRole(roles, 'TEACHER')) {
        navigate('/teacher');
      } else if (hasRole(roles, 'STUDENT')) {
        navigate('/student');
      } else if (hasRole(roles, 'FINANCE_STAFF')) {
        navigate('/staff/finance-cafeteria');
      } else {
        setError(t('login.roleNotSupported', { role: data.role }));
      }
    } catch (err) {
      setError(err.response?.data || t('login.loginFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <button onClick={toggleLang} className="login-lang-switcher" title={uiCopy.switchTitle}>
        {currentLang === 'en' ? 'MN' : 'EN'}
      </button>
      <div className="login-card">
        <div style={{ marginBottom: '0.5rem', textAlign: 'center' }}>
          <img src={edusysLogo} alt="EduSys logo" className="login-brand-logo" />
        </div>
        <h1>{t('login.appName')}</h1>
        <p className="subtitle">{t('login.subtitle')}</p>

        {error ? <div className="alert alert-error">{error}</div> : null}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="username">{t('login.username')}</label>
            <input
              id="username"
              type="text"
              className="form-control"
              placeholder={t('login.enterUsername')}
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
              autoComplete="username"
            />
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label htmlFor="password">{t('login.password')}</label>
            <div className="password-field">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="form-control"
                placeholder={t('login.enterPassword')}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                autoComplete="current-password"
              />
              <button type="button" className="password-toggle" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? uiCopy.hide : uiCopy.show}>
                {showPassword ? uiCopy.hide : uiCopy.show}
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading} style={{ padding: '0.75rem 1.25rem', fontSize: '0.9rem' }}>
            {loading ? t('login.signingIn') : t('login.signIn')}
          </button>
        </form>

        <div className="login-helper">
          <strong>{uiCopy.demoTitle}</strong>
          <span>{uiCopy.demoBody}</span>
        </div>
      </div>
    </div>
  );
}
