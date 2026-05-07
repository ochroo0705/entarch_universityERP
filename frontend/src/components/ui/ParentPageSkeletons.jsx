function SkeletonBar({ className = '', style }) {
  return <div className={`parent-page-skeleton ${className}`.trim()} style={style} aria-hidden="true" />;
}

export function ParentScheduleSkeleton() {
  return (
    <div className="parent-page-skeleton-stack" aria-hidden="true">
      <div className="stats-grid">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={index} className="stat-card">
            <div className="stat-info" style={{ gap: '0.5rem' }}>
              <SkeletonBar className="parent-page-skeleton-icon" />
              <SkeletonBar className="parent-page-skeleton-value" />
              <SkeletonBar className="parent-page-skeleton-label" />
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-body">
          <div className="parent-page-skeleton-row" style={{ marginBottom: '1rem' }}>
            <SkeletonBar className="parent-page-skeleton-title" />
          </div>
          <div className="parent-page-skeleton-schedule-grid">
            {Array.from({ length: 18 }, (_, index) => (
              <SkeletonBar key={index} className="parent-page-skeleton-schedule-cell" />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export function ParentHomeworkSkeleton() {
  return (
    <div className="parent-page-skeleton-stack" aria-hidden="true">
      <div className="stats-grid">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={index} className="stat-card">
            <div className="stat-info" style={{ gap: '0.45rem' }}>
              <SkeletonBar className="parent-page-skeleton-value" />
              <SkeletonBar className="parent-page-skeleton-label" />
            </div>
          </div>
        ))}
      </div>

      <div className="parent-page-skeleton-stack">
        {Array.from({ length: 3 }, (_, index) => (
          <div key={index} className="card">
            <div className="card-body">
              <div className="parent-page-skeleton-row" style={{ marginBottom: '0.8rem' }}>
                <SkeletonBar className="parent-page-skeleton-badge" />
                <SkeletonBar className="parent-page-skeleton-badge" style={{ width: '96px' }} />
              </div>
              <SkeletonBar className="parent-page-skeleton-title" style={{ marginBottom: '0.5rem' }} />
              <SkeletonBar className="parent-page-skeleton-label" style={{ width: '120px', marginBottom: '1rem' }} />
              <SkeletonBar className="parent-page-skeleton-copy" />
              <SkeletonBar className="parent-page-skeleton-copy" style={{ width: '82%' }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function ParentGradesSkeleton() {
  return (
    <div className="parent-page-skeleton-stack" aria-hidden="true">
      <div className="parent-page-skeleton-grades-top">
        <div className="parent-page-skeleton-card-stack">
          <SkeletonBar className="parent-page-skeleton-label" style={{ width: '150px' }} />
          <div className="parent-page-skeleton-mini-grid">
            {Array.from({ length: 4 }, (_, index) => (
              <div key={index} className="stat-card">
                <div className="stat-info" style={{ gap: '0.45rem' }}>
                  <SkeletonBar className="parent-page-skeleton-value" />
                  <SkeletonBar className="parent-page-skeleton-label" />
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <SkeletonBar className="parent-page-skeleton-title" style={{ marginBottom: '0.85rem' }} />
            <SkeletonBar className="parent-page-skeleton-chart" />
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-body parent-page-skeleton-row">
          {Array.from({ length: 5 }, (_, index) => (
            <SkeletonBar key={index} className="parent-page-skeleton-pill" />
          ))}
        </div>
      </div>

      <div className="parent-page-skeleton-stack">
        {Array.from({ length: 2 }, (_, index) => (
          <div key={index} className="card">
            <div className="card-body">
              <div className="parent-page-skeleton-row" style={{ justifyContent: 'space-between', marginBottom: '0.9rem' }}>
                <SkeletonBar className="parent-page-skeleton-title" style={{ width: '140px' }} />
                <SkeletonBar className="parent-page-skeleton-value" style={{ width: '56px' }} />
              </div>
              <div className="parent-page-skeleton-grade-grid">
                {Array.from({ length: 4 }, (_, gradeIndex) => (
                  <SkeletonBar key={gradeIndex} className="parent-page-skeleton-grade-card" />
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function ParentAttendanceSkeleton() {
  return (
    <div className="parent-page-skeleton-stack" aria-hidden="true">
      <div className="stats-grid">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={index} className="stat-card">
            <div className="stat-info" style={{ gap: '0.45rem' }}>
              <SkeletonBar className="parent-page-skeleton-value" />
              <SkeletonBar className="parent-page-skeleton-label" />
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-body">
          <div className="parent-page-skeleton-row parent-page-skeleton-space-between" style={{ marginBottom: '1rem' }}>
            <SkeletonBar className="parent-page-skeleton-pill" />
            <SkeletonBar className="parent-page-skeleton-title" style={{ width: '180px' }} />
            <SkeletonBar className="parent-page-skeleton-pill" />
          </div>
          <div className="parent-page-skeleton-calendar-grid">
            {Array.from({ length: 35 }, (_, index) => (
              <SkeletonBar key={index} className="parent-page-skeleton-calendar-cell" />
            ))}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-body parent-page-skeleton-row" style={{ justifyContent: 'center', flexWrap: 'wrap' }}>
          {Array.from({ length: 5 }, (_, index) => (
            <SkeletonBar key={index} className="parent-page-skeleton-pill" />
          ))}
        </div>
      </div>

      <div className="card">
        <div className="card-body">
          <SkeletonBar className="parent-page-skeleton-title" style={{ marginBottom: '1rem' }} />
          <div className="parent-page-skeleton-table">
            {Array.from({ length: 5 }, (_, index) => (
              <SkeletonBar key={index} className="parent-page-skeleton-table-row" />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
