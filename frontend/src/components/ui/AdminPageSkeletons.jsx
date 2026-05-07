function SkeletonBar({ className = '', style }) {
  return <div className={`users-skeleton ${className}`.trim()} style={style} aria-hidden="true" />;
}

export function AdminPageHeaderSkeleton({ hasAction = true, kicker = true, summary = true }) {
  return (
    <div className="page-header admin-page-skeleton-header" aria-hidden="true">
      <div className="admin-page-skeleton-stack">
        {kicker ? <SkeletonBar className="users-skeleton-id admin-page-skeleton-kicker" /> : null}
        <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-heading" />
        {summary ? <SkeletonBar className="users-skeleton-email admin-page-skeleton-summary" /> : null}
      </div>
      {hasAction ? <SkeletonBar className="users-skeleton-button admin-page-skeleton-header-action" /> : null}
    </div>
  );
}

export function AdminFilterRowSkeleton({ fields = 3, includeCount = true }) {
  return (
    <div className="list-filter-row admin-page-skeleton-filter-row" aria-hidden="true">
      {Array.from({ length: fields }, (_, index) => (
        <SkeletonBar key={`filter-${index}`} className="admin-page-skeleton-input" />
      ))}
      {includeCount ? <SkeletonBar className="users-skeleton-text admin-page-skeleton-count" /> : null}
    </div>
  );
}

export function AdminToolbarSkeleton({ fields = 4, includeCount = true }) {
  return (
    <div className="filter-toolbar users-filter-toolbar admin-page-skeleton-filter-row" aria-hidden="true">
      {Array.from({ length: fields }, (_, index) => (
        <SkeletonBar key={`toolbar-${index}`} className="admin-page-skeleton-input" />
      ))}
      {includeCount ? <SkeletonBar className="users-skeleton-text admin-page-skeleton-count" /> : null}
    </div>
  );
}

export function AdminStatsSkeleton({ count = 4 }) {
  return (
    <div className="stats-grid" aria-hidden="true">
      {Array.from({ length: count }, (_, index) => (
        <article key={`stat-${index}`} className="stat-card">
          <SkeletonBar className="users-skeleton-pill admin-page-skeleton-stat-icon" />
          <div className="stat-info admin-page-skeleton-stack">
            <SkeletonBar className="users-skeleton-id admin-page-skeleton-stat-value" />
            <SkeletonBar className="users-skeleton-text admin-page-skeleton-stat-label" />
          </div>
        </article>
      ))}
    </div>
  );
}

export function AdminAnnouncementListSkeleton({ count = 4 }) {
  return (
    <div className="stack-list" aria-hidden="true">
      {Array.from({ length: count }, (_, index) => (
        <article key={`announcement-list-${index}`} className={`announcement-item${index === 0 ? ' is-featured' : ''} is-static`}>
          <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-announcement-title" />
          <SkeletonBar className="users-skeleton-email admin-page-skeleton-announcement-copy" />
          <SkeletonBar className="users-skeleton-email admin-page-skeleton-announcement-copy short" />
        </article>
      ))}
    </div>
  );
}

export function AdminDashboardSkeleton() {
  return (
    <div className="content-stack" aria-hidden="true">
      <AdminPageHeaderSkeleton hasAction={false} />
      <AdminStatsSkeleton count={4} />
      <div className="panel-grid-two">
        <section className="card section-card">
          <div className="card-body admin-page-skeleton-stack">
            <div className="section-header">
              <div className="admin-page-skeleton-stack">
                <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
                <SkeletonBar className="users-skeleton-email admin-page-skeleton-summary" />
              </div>
            </div>
            <div className="quick-action-grid">
              {Array.from({ length: 4 }, (_, index) => (
                <article key={`action-${index}`} className="quick-action-tile admin-page-skeleton-tile">
                  <SkeletonBar className="users-skeleton-pill admin-page-skeleton-tile-icon" />
                  <SkeletonBar className="users-skeleton-text admin-page-skeleton-tile-copy" />
                </article>
              ))}
            </div>
            <SkeletonBar className="users-skeleton-button admin-page-skeleton-primary-action" />
          </div>
        </section>

        <section className="card section-card">
          <div className="card-body admin-page-skeleton-stack">
            <div className="section-header">
              <div className="admin-page-skeleton-stack">
                <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
              </div>
              <SkeletonBar className="users-skeleton-button admin-page-skeleton-inline-action" />
            </div>
            <AdminAnnouncementListSkeleton />
          </div>
        </section>
      </div>
    </div>
  );
}

export function AdminTableSkeleton({
  columns = 6,
  rows = 6,
  mobileCards = 4,
  action = true,
}) {
  return (
    <div className="card" aria-hidden="true">
      <div className="table-container desktop-table">
        <table>
          <thead>
            <tr>
              {Array.from({ length: columns }, (_, index) => (
                <th key={`head-${index}`}>
                  <SkeletonBar className="users-skeleton-text admin-page-skeleton-table-head" />
                </th>
              ))}
              {action ? (
                <th>
                  <SkeletonBar className="users-skeleton-text admin-page-skeleton-table-head" />
                </th>
              ) : null}
            </tr>
          </thead>
          <tbody>
            {Array.from({ length: rows }, (_, rowIndex) => (
              <tr key={`row-${rowIndex}`} className="users-skeleton-row">
                {Array.from({ length: columns }, (_, colIndex) => (
                  <td key={`cell-${rowIndex}-${colIndex}`}>
                    <SkeletonBar className={colIndex === 0 ? 'users-skeleton-id' : colIndex === 1 ? 'users-skeleton-name' : 'users-skeleton-text'} />
                  </td>
                ))}
                {action ? (
                  <td>
                    <SkeletonBar className="users-skeleton-button" />
                  </td>
                ) : null}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card-body mobile-card-list">
        {Array.from({ length: mobileCards }, (_, index) => (
          <article key={`mobile-card-${index}`} className="data-card users-skeleton-card">
            <div className="data-card-header">
              <div>
                <SkeletonBar className="users-skeleton-card-title" />
                <SkeletonBar className="users-skeleton-card-subtitle" />
              </div>
              <SkeletonBar className="users-skeleton-pill" />
            </div>
            <div className="data-card-meta">
              {Array.from({ length: 3 }, (_, itemIndex) => (
                <div key={`meta-${index}-${itemIndex}`} className="data-card-meta-row">
                  <SkeletonBar className="users-skeleton-meta-label" />
                  <SkeletonBar className="users-skeleton-meta-value" />
                </div>
              ))}
            </div>
            {action ? <SkeletonBar className="users-skeleton-card-button" /> : null}
          </article>
        ))}
      </div>
    </div>
  );
}

export function AdminDetailSkeleton({ includeContent = true, includeSchedule = false }) {
  return (
    <div className="content-stack" aria-hidden="true">
      <AdminPageHeaderSkeleton />
      <section className="card">
        <div className="card-body admin-page-skeleton-stack">
          <div className="admin-detail-skeleton-top">
            <SkeletonBar className="users-skeleton-pill admin-detail-skeleton-avatar" />
            <div className="admin-page-skeleton-stack">
              <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-heading" />
              <div className="admin-detail-skeleton-badges">
                <SkeletonBar className="users-skeleton-pill" />
                <SkeletonBar className="users-skeleton-pill" />
              </div>
            </div>
          </div>

          {includeContent ? (
            <>
              <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
              <div className="admin-detail-skeleton-copy">
                <SkeletonBar className="users-skeleton-email admin-page-skeleton-announcement-copy" />
                <SkeletonBar className="users-skeleton-email admin-page-skeleton-announcement-copy" />
                <SkeletonBar className="users-skeleton-email admin-page-skeleton-announcement-copy short" />
              </div>
            </>
          ) : null}

          <div className="admin-detail-skeleton-grid">
            {Array.from({ length: 6 }, (_, index) => (
              <div key={`detail-${index}`} className="admin-page-skeleton-stack">
                <SkeletonBar className="users-skeleton-meta-label" />
                <SkeletonBar className="users-skeleton-text" />
              </div>
            ))}
          </div>
        </div>
      </section>

      {includeSchedule ? (
        <section className="card">
          <div className="card-body admin-page-skeleton-stack">
            <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
            <div className="admin-detail-skeleton-schedule-grid">
              {Array.from({ length: 24 }, (_, index) => (
                <SkeletonBar key={`schedule-${index}`} className="admin-detail-skeleton-schedule-cell" />
              ))}
            </div>
          </div>
        </section>
      ) : null}
    </div>
  );
}

export function AdminScheduleSectionSkeleton() {
  return (
    <div className="admin-page-skeleton-stack" aria-hidden="true">
      <div className="admin-detail-skeleton-schedule-grid">
        {Array.from({ length: 18 }, (_, index) => (
          <SkeletonBar key={`inline-schedule-${index}`} className="admin-detail-skeleton-schedule-cell" />
        ))}
      </div>
    </div>
  );
}

export function AdminFormFieldSkeleton({ withLabel = true }) {
  return (
    <div className="form-group admin-page-skeleton-stack" aria-hidden="true">
      {withLabel ? <SkeletonBar className="users-skeleton-meta-label admin-page-skeleton-field-label" /> : null}
      <SkeletonBar className="admin-page-skeleton-input" />
    </div>
  );
}

export function AdminRiskConfigSkeleton() {
  return (
    <div className="content-stack" aria-hidden="true">
      <AdminPageHeaderSkeleton hasAction={false} />
      <AdminStatsSkeleton count={4} />
      <div className="grid-dashboard-bottom">
        {Array.from({ length: 2 }, (_, index) => (
          <section key={`risk-config-${index}`} className="card section-card">
            <div className="card-body admin-page-skeleton-stack">
              <div className="section-header">
                <div className="admin-page-skeleton-stack">
                  <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
                  <SkeletonBar className="users-skeleton-email admin-page-skeleton-summary" />
                </div>
              </div>
              <div className="risk-filters-grid">
                {Array.from({ length: index === 0 ? 5 : 5 }, (_, fieldIndex) => (
                  <AdminFormFieldSkeleton key={`risk-field-${index}-${fieldIndex}`} />
                ))}
              </div>
              {index === 0 ? <SkeletonBar className="users-skeleton-text admin-page-skeleton-summary" /> : null}
              {index === 1 ? (
                <div className="form-actions">
                  <SkeletonBar className="users-skeleton-button admin-page-skeleton-inline-action" />
                  <SkeletonBar className="users-skeleton-button admin-page-skeleton-inline-action" />
                </div>
              ) : null}
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}

export function AdminAuditLogSkeleton() {
  return (
    <div className="audit-log-page content-stack" aria-hidden="true">
      <AdminPageHeaderSkeleton hasAction={false} />
      <section className="card">
        <div className="card-body admin-page-skeleton-stack">
          <SkeletonBar className="users-skeleton-email admin-page-skeleton-banner" />
        </div>
      </section>
      <section className="card section-card">
        <div className="card-body admin-page-skeleton-stack">
          <div className="section-header">
            <div className="admin-page-skeleton-stack">
              <SkeletonBar className="users-skeleton-card-title admin-page-skeleton-section-title" />
              <SkeletonBar className="users-skeleton-email admin-page-skeleton-summary" />
            </div>
          </div>
          <AdminToolbarSkeleton fields={1} includeCount />
          <AdminTableSkeleton columns={6} rows={6} mobileCards={4} action={false} />
        </div>
      </section>
    </div>
  );
}
