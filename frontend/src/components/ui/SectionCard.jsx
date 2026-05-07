export default function SectionCard({ title, subtitle, action, children, className = '' }) {
  return (
    <section className={`card section-card ${className}`.trim()}>
      <div className="card-body">
        {(title || action || subtitle) ? (
          <div className="section-header">
            <div>
              {title ? <h2 className="section-title">{title}</h2> : null}
              {subtitle ? <p className="section-subtitle">{subtitle}</p> : null}
            </div>
            {action ? <div className="section-action">{action}</div> : null}
          </div>
        ) : null}
        {children}
      </div>
    </section>
  );
}
