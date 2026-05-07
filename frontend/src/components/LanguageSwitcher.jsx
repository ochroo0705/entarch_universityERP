import { useTranslation } from 'react-i18next';

export default function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const current = i18n.language?.startsWith('mn') ? 'mn' : 'en';

  const toggle = () => {
    i18n.changeLanguage(current === 'en' ? 'mn' : 'en');
  };

  return (
    <button
      onClick={toggle}
      className="lang-switcher"
      title={current === 'en' ? 'Монгол хэл рүү солих' : 'Switch to English'}
    >
      {current === 'en' ? 'MN' : 'EN'}
    </button>
  );
}
