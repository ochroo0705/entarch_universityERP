import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getMyTeachingAssignments } from '../../api/endpoints';

export default function TeacherClasses() {
  const { t } = useTranslation();
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyTeachingAssignments()
      .then((res) => setAssignments(res.data || []))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const classes = useMemo(() => {
    const classMap = new Map();
    assignments.forEach((assignment) => {
      const classInfo = assignment.classInfo;
      if (!classInfo) return;

      if (!classMap.has(classInfo.id)) {
        classMap.set(classInfo.id, {
          classInfo,
          subjects: [],
        });
      }

      if (assignment.subject) {
        classMap.get(classInfo.id).subjects.push({
          name: assignment.subject.name,
          code: assignment.subject.subjectCode,
          taId: assignment.id,
          hoursPerWeek: assignment.subject.hoursPerWeek,
        });
      }
    });
    return [...classMap.values()];
  }, [assignments]);

  if (loading) return <div className="loading"><div className="spinner" />{t('common.loadingClasses')}</div>;

  return (
    <div>
      <div className="page-header">
        <h1>{t('teacher.classes.title')}</h1>
      </div>

      {classes.length === 0 ? (
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>
            {t('teacher.classes.noClasses')}
          </div>
        </div>
      ) : (
        <div className="teacher-card-grid">
          {classes.map(({ classInfo, subjects }) => (
            <Link
              key={classInfo.id}
              to={`/teacher/classes/${classInfo.id}`}
              className="card teacher-card-link"
            >
              <div className="card-body teacher-card-content">
                <div className="teacher-card-header">
                  <div>
                    <h3 style={{ fontSize: '1.15rem', marginBottom: '0.2rem' }}>{classInfo.className}</h3>
                    <div className="teacher-card-meta">
                      {t('admin.classes.gradeN', { n: classInfo.grade })} - {t('admin.createClass.section')} {classInfo.section}
                    </div>
                  </div>
                  {classInfo.roomNumber ? (
                    <span className="badge badge-info">{t('teacher.classDetail.room')} {classInfo.roomNumber}</span>
                  ) : null}
                </div>

                <div className="teacher-card-copy">
                  <span style={{ fontWeight: 600 }}>{t('teacher.classes.subjects')}: </span>
                  {subjects.map((subject) => subject.name).join(', ') || t('common.none')}
                </div>

                <div className="teacher-card-meta">
                  {subjects.reduce((total, subject) => total + (subject.hoursPerWeek || 0), 0)} {t('teacher.classes.hrsWeek')}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
