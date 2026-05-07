import { useEffect, useId, useState, createContext, useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { getMyChildren } from '../api/endpoints';
import SelectMenu from './ui/SelectMenu';

const ChildContext = createContext(null);
const SELECTED_CHILD_STORAGE_KEY = 'webadv:selected-child-id';

function getChildDisplayName(child) {
  if (!child) return '';
  return `${child.firstName} ${child.lastName}`.trim();
}

function getChildInitials(child) {
  if (!child) return '';

  return [child.firstName, child.lastName]
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
    .slice(0, 2);
}

export function useChild() {
  return useContext(ChildContext);
}

export function ChildProvider({ children: reactChildren }) {
  const [children, setChildren] = useState([]);
  const [selectedChild, setSelectedChild] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyChildren()
      .then((res) => {
        const kids = res.data || [];
        setChildren(kids);
        if (kids.length > 0) {
          const storedChildId = window.localStorage.getItem(SELECTED_CHILD_STORAGE_KEY);
          const nextSelectedChild = kids.find((child) => String(child.id) === storedChildId) || kids[0];
          setSelectedChild(nextSelectedChild);
        }
      })
      .catch(() => setChildren([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (selectedChild?.id) {
      window.localStorage.setItem(SELECTED_CHILD_STORAGE_KEY, String(selectedChild.id));
    }
  }, [selectedChild]);

  return (
    <ChildContext.Provider value={{ children, selectedChild, setSelectedChild, loading }}>
      {reactChildren}
    </ChildContext.Provider>
  );
}

export default function ChildSelector() {
  const { t } = useTranslation();
  const selectId = useId();
  const { children, selectedChild, setSelectedChild, loading } = useChild();
  const childOptions = children.map((child) => ({ value: String(child.id), label: getChildDisplayName(child) }));

  if (loading) {
    return (
      <div className="child-switcher child-switcher-loading" aria-hidden="true">
        <div className="child-switcher-current">
          <div className="child-switcher-avatar child-switcher-skeleton" />
          <div className="child-switcher-copy">
            <span className="child-switcher-copy-label child-switcher-skeleton" />
            <span className="child-switcher-copy-name child-switcher-skeleton" />
          </div>
        </div>
        <div className="child-switcher-options child-switcher-options-loading">
          <span className="child-switcher-option child-switcher-skeleton" />
          <span className="child-switcher-option child-switcher-skeleton" />
        </div>
      </div>
    );
  }

  if (children.length <= 1) return null;

  return (
    <section className="child-switcher" aria-label={t('childSelector.viewing')}>
      <div className="child-switcher-header">
        <div className="child-switcher-current">
          <div className="child-switcher-avatar" aria-hidden="true">
            {getChildInitials(selectedChild)}
          </div>
          <div className="child-switcher-copy">
            <strong>{t('childSelector.viewing')}</strong>
            <span>{getChildDisplayName(selectedChild)}</span>
            <p>{t('childSelector.choices', { count: children.length })}</p>
          </div>
        </div>

        <div className="child-switcher-select-wrap">
          <label className="child-switcher-select-label" htmlFor={selectId}>
            {t('childSelector.viewing')}
          </label>
          <div id={selectId} className="child-switcher-select">
            <SelectMenu
              options={childOptions}
              value={selectedChild?.id ? String(selectedChild.id) : ''}
              onChange={(value) => {
                const nextChild = children.find((child) => String(child.id) === String(value));
                if (nextChild) setSelectedChild(nextChild);
              }}
              placeholder={t('childSelector.viewing')}
            />
          </div>
        </div>
      </div>

      <div className="child-switcher-options" role="radiogroup" aria-label={t('childSelector.viewing')}>
        {children.map((child) => {
          const isSelected = selectedChild?.id === child.id;

          return (
            <button
              key={child.id}
              type="button"
              role="radio"
              aria-checked={isSelected}
              onClick={() => setSelectedChild(child)}
              className={`child-switcher-option${isSelected ? ' is-selected' : ''}`}
            >
              <span className="child-switcher-option-avatar" aria-hidden="true">
                {getChildInitials(child)}
              </span>
              <span className="child-switcher-option-copy">
                <span className="child-switcher-option-name">{getChildDisplayName(child)}</span>
                <span className="child-switcher-option-meta">
                  {isSelected ? t('childSelector.current') : t('childSelector.switch')}
                </span>
              </span>
            </button>
          );
        })}
      </div>
    </section>
  );
}
