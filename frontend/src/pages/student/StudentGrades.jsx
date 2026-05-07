import { useEffect, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { getStudentGrades, getGradeTrends } from '../../api/endpoints';

const QUARTER_LABELS = { 1: 'Q1', 2: 'Q2', 3: 'Q3', 4: 'Q4' };

const GRADE_COLORS = {
  QUARTER: '#4f46e5',
  MIDTERM: '#0891b2',
  FINAL: '#dc2626',
  YEARLY: '#059669',
};

function getGradeColor(value) {
  if (value >= 90) return 'var(--success)';
  if (value >= 75) return 'var(--primary)';
  if (value >= 60) return 'var(--warning)';
  return 'var(--danger)';
}

export default function StudentGrades() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [allGrades, setAllGrades] = useState([]);
  const [trends, setTrends] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedQuarter, setSelectedQuarter] = useState(null);

  useEffect(() => {
    Promise.all([
      getStudentGrades(user.userId).catch(() => ({ data: [] })),
      getGradeTrends(user.userId).catch(() => ({ data: null })),
    ])
      .then(([gradeRes, trendRes]) => {
        setAllGrades(gradeRes.data || []);
        setTrends(trendRes.data || null);
      })
      .finally(() => setLoading(false));
  }, [user.userId]);

  const grades = selectedQuarter ? allGrades.filter((grade) => grade.quarter === selectedQuarter) : allGrades;

  const bySubject = useMemo(() => {
    const map = {};
    grades.forEach((grade) => {
      const key = grade.subjectName || 'Unknown';
      if (!map[key]) map[key] = [];
      map[key].push(grade);
    });
    return map;
  }, [grades]);

  const allValues = grades.filter((grade) => grade.gradeValue != null).map((grade) => grade.gradeValue);
  const avgGrade = allValues.length > 0 ? (allValues.reduce((a, b) => a + b, 0) / allValues.length).toFixed(1) : null;
  const highestGrade = allValues.length > 0 ? Math.max(...allValues) : null;
  const lowestGrade = allValues.length > 0 ? Math.min(...allValues) : null;

  if (loading) return <div className="loading"><div className="spinner" />{t('common.loadingGrades')}</div>;

  return (
    <div>
      <div className="page-header">
        <h1>{t('student.grades.title')}</h1>
      </div>

      <div className="parent-hero-grid">
        <div className="parent-stack">
          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
            {selectedQuarter ? t('student.grades.quarterStats', { n: selectedQuarter }) : t('student.grades.allQuarterStats')}
          </span>
          <div className="parent-mini-stats-grid">
            <div className="stat-card">
              <div className="stat-icon teachers">{'\u{1F4CA}'}</div>
              <div className="stat-info">
                <h3 style={{ color: avgGrade ? getGradeColor(parseFloat(avgGrade)) : undefined }}>
                  {avgGrade || '-'}
                </h3>
                <p>Average</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon students">{'\u{1F3C6}'}</div>
              <div className="stat-info">
                <h3 style={{ color: highestGrade ? getGradeColor(highestGrade) : undefined }}>
                  {highestGrade ?? '-'}
                </h3>
                <p>{t('student.grades.highest')}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon assignments">{'\u{1F4C9}'}</div>
              <div className="stat-info">
                <h3 style={{ color: lowestGrade ? getGradeColor(lowestGrade) : undefined }}>
                  {lowestGrade ?? '-'}
                </h3>
                <p>{t('student.grades.lowest')}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon classes">{'\u{1F4DA}'}</div>
              <div className="stat-info">
                <h3>{Object.keys(bySubject).length}</h3>
                <p>{t('student.grades.subjects')}</p>
              </div>
            </div>
          </div>
        </div>

        {trends && trends.quarterComparison && trends.quarterComparison.length > 0 ? (() => {
          const data = trends.quarterComparison;
          const width = 400;
          const height = 140;
          const padL = 35;
          const padR = 15;
          const padT = 22;
          const padB = 28;
          const chartW = width - padL - padR;
          const chartH = height - padT - padB;
          const values = data.map((item) => item.gpa || 0);
          const minV = Math.min(...values, 0);
          const maxV = Math.max(...values, 100);
          const range = maxV - minV || 1;
          const points = data.map((item, index) => {
            const x = padL + (data.length === 1 ? chartW / 2 : (index / (data.length - 1)) * chartW);
            const y = padT + chartH - ((item.gpa - minV) / range) * chartH;
            return { x, y, val: item.gpa, quarter: item.quarter };
          });
          const lineStr = points.map((point, index) => `${index === 0 ? 'M' : 'L'}${point.x},${point.y}`).join(' ');
          const areaStr = `${lineStr} L${points[points.length - 1].x},${padT + chartH} L${points[0].x},${padT + chartH} Z`;
          const gridLines = [0, 25, 50, 75, 100].filter((value) => value >= minV && value <= maxV);

          return (
            <div className="card parent-chart-card" style={{ marginBottom: 0 }}>
              <div className="card-body" style={{ padding: '0.75rem 1rem' }}>
                <h3 style={{ fontSize: '0.9rem', marginBottom: '0.4rem', fontFamily: 'var(--font-heading)', fontWeight: 700 }}>
                  Performance Trend
                </h3>
                <div className="parent-chart-frame">
                  <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', height: 'auto', overflow: 'visible' }}>
                    <defs>
                      <linearGradient id="areaGradStudent" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="var(--primary)" stopOpacity="0.25" />
                        <stop offset="100%" stopColor="var(--primary)" stopOpacity="0.02" />
                      </linearGradient>
                    </defs>
                    {gridLines.map((value) => {
                      const y = padT + chartH - ((value - minV) / range) * chartH;
                      return (
                        <g key={value}>
                          <line x1={padL} y1={y} x2={width - padR} y2={y} stroke="var(--border)" strokeWidth="1" strokeDasharray="4 3" />
                          <text x={padL - 6} y={y + 3.5} textAnchor="end" fontSize="10" fill="var(--text-muted)" fontFamily="var(--font-body)">{value}</text>
                        </g>
                      );
                    })}
                    <path d={areaStr} fill="url(#areaGradStudent)">
                      <animate attributeName="opacity" from="0" to="1" dur="0.6s" fill="freeze" />
                    </path>
                    <path d={lineStr} fill="none" stroke="var(--primary)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <animate attributeName="stroke-dashoffset" from="600" to="0" dur="0.8s" fill="freeze" />
                      <animate attributeName="stroke-dasharray" from="600" to="600" dur="0s" fill="freeze" />
                    </path>
                    {points.map((point, index) => (
                      <g key={index}>
                        <circle cx={point.x} cy={point.y} r="5" fill="var(--primary)" stroke="white" strokeWidth="2.5">
                          <animate attributeName="r" from="0" to="5" dur="0.3s" begin={`${0.3 + index * 0.1}s`} fill="freeze" />
                        </circle>
                        <text x={point.x} y={point.y - 12} textAnchor="middle" fontSize="11" fontWeight="700" fill="var(--primary)" fontFamily="var(--font-heading)">
                          {point.val ? point.val.toFixed(0) : '-'}
                        </text>
                        <text x={point.x} y={padT + chartH + 18} textAnchor="middle" fontSize="11" fontWeight="600" fill="var(--text-muted)" fontFamily="var(--font-heading)">
                          {QUARTER_LABELS[point.quarter] || `Q${point.quarter}`}
                        </text>
                      </g>
                    ))}
                  </svg>
                </div>
                {trends.trend && (
                  <div style={{ marginTop: '0.4rem', fontSize: '0.8rem', display: 'flex', gap: '0.4rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '1.1rem' }}>
                      {trends.trend === 'IMPROVING' ? '\u{1F4C8}' : trends.trend === 'DECLINING' ? '\u{1F4C9}' : '\u27A1\uFE0F'}
                    </span>
                    <span style={{ fontWeight: 600, color: trends.trend === 'IMPROVING' ? 'var(--success)' : trends.trend === 'DECLINING' ? 'var(--danger)' : 'var(--text-muted)' }}>
                      {trends.trend}
                    </span>
                    {trends.improvement != null && (
                      <span style={{ color: 'var(--text-muted)' }}>
                        ({trends.improvement > 0 ? '+' : ''}{trends.improvement.toFixed(1)} points)
                      </span>
                    )}
                  </div>
                )}
              </div>
            </div>
          );
        })() : (
          <div className="card parent-chart-card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
              No trend data available yet.
            </div>
          </div>
        )}
      </div>

      <div className="card" style={{ marginBottom: '1rem' }}>
        <div className="card-body parent-filter-row" style={{ padding: '0.75rem 1rem' }}>
          <span style={{ fontWeight: 600, fontSize: '0.85rem', marginRight: '0.5rem' }}>{t('student.grades.quarterLabel')}</span>
          <button
            className={`btn btn-sm ${selectedQuarter === null ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setSelectedQuarter(null)}
          >{t('student.grades.all')}</button>
          {[1, 2, 3, 4].map((quarter) => (
            <button
              key={quarter}
              className={`btn btn-sm ${selectedQuarter === quarter ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setSelectedQuarter(quarter)}
            >{QUARTER_LABELS[quarter]}</button>
          ))}
        </div>
      </div>

      {Object.keys(bySubject).length === 0 ? (
        <div className="card">
          <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '3rem' }}>
            {t('student.grades.noGrades')}
          </div>
        </div>
      ) : (
        <div className="parent-stack">
          {Object.entries(bySubject).map(([subject, subjectGrades], subjectIndex) => {
            const subjectAverages = subjectGrades.filter((grade) => grade.gradeValue).map((grade) => grade.gradeValue);
            const avg = subjectAverages.length > 0 ? (subjectAverages.reduce((a, b) => a + b, 0) / subjectAverages.length).toFixed(0) : null;

            return (
              <div key={subject} className="card" style={{ animation: `fadeInUp 0.35s cubic-bezier(0.16,1,0.3,1) ${subjectIndex * 0.05}s both` }}>
                <div className="card-body">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem', gap: '0.75rem', flexWrap: 'wrap' }}>
                    <h3 style={{ fontSize: '1rem', fontFamily: 'var(--font-heading)', fontWeight: 700, wordBreak: 'break-word' }}>{subject}</h3>
                    {avg ? (
                      <span style={{ fontWeight: 700, fontSize: '1.1rem', color: getGradeColor(parseInt(avg, 10)) }}>
                        {avg}
                      </span>
                    ) : null}
                  </div>
                  <div className="parent-subject-grade-grid">
                    {[...subjectGrades].sort((a, b) => (a.quarter || 0) - (b.quarter || 0)).map((grade) => (
                      <div
                        key={grade.id}
                        style={{
                          padding: '0.6rem 0.75rem', borderRadius: '6px', border: '1px solid var(--border)',
                          background: 'rgba(246,244,240,0.5)',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <span style={{
                            fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase',
                            color: GRADE_COLORS[grade.gradeType] || 'var(--text-muted)',
                          }}>
                            {grade.gradeType}
                          </span>
                          <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
                            {QUARTER_LABELS[grade.quarter] || `Q${grade.quarter}`}
                          </span>
                        </div>
                        <div style={{ fontSize: '1.25rem', fontWeight: 700, color: getGradeColor(grade.gradeValue || 0) }}>
                          {grade.gradeValue ?? '-'}
                        </div>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          {grade.recordedAt ? new Date(grade.recordedAt).toLocaleDateString() : ''}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
