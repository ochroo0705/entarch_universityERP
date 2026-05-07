export function LoadingState({ label }) {
  return (
    <div className="state-panel">
      <div className="loading">
        <div className="spinner" />
        {label}
      </div>
    </div>
  );
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="state-panel">
      <div className="empty-state">
        <div className="empty-state-eyebrow">No results</div>
        <h3>{title}</h3>
        {description ? <p>{description}</p> : null}
        {action}
      </div>
    </div>
  );
}

export function ErrorState({ title, description, retryLabel, onRetry }) {
  return (
    <div className="state-panel state-panel-error">
      <div className="empty-state">
        <div className="empty-state-eyebrow">Something went wrong</div>
        <h3>{title}</h3>
        {description ? <p>{description}</p> : null}
        {onRetry ? (
          <button type="button" className="btn btn-primary" onClick={onRetry}>
            {retryLabel}
          </button>
        ) : null}
      </div>
    </div>
  );
}
