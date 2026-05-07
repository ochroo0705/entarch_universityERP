import { useEffect, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { getTranslationsBatch } from '../api/endpoints';

export default function useEntityTranslations(entityType, entities) {
  const { i18n } = useTranslation();
  const locale = i18n.language?.startsWith('mn') ? 'mn' : 'en';
  const [translationsMap, setTranslationsMap] = useState({});

  const entityIds = useMemo(
    () => (entities || []).map((e) => e.id).filter(Boolean).join(','),
    [entities]
  );

  useEffect(() => {
    if (!entityIds) {
      setTranslationsMap({});
      return;
    }
    const ids = entityIds.split(',').map(Number);

    getTranslationsBatch(entityType, ids)
      .then((res) => {
        const map = {};
        for (const [eid, translations] of Object.entries(res.data)) {
          map[eid] = {};
          for (const t of translations) {
            if (!map[eid][t.fieldName]) map[eid][t.fieldName] = {};
            map[eid][t.fieldName][t.locale] = t.value;
          }
        }
        setTranslationsMap(map);
      })
      .catch(() => setTranslationsMap({}));
  }, [entityType, entityIds, locale]);

  const getField = useCallback((entity, fieldName, fallback) => {
    if (!entity?.id) return fallback;
    const fields = translationsMap[entity.id];
    if (!fields || !fields[fieldName]) return fallback;
    return fields[fieldName][locale] || fields[fieldName]['mn'] || fallback;
  }, [translationsMap, locale]);

  return { translationsMap, getField };
}
