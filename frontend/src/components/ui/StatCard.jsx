export default function StatCard({ icon, tone = 'teachers', value, label, hint, interactive = false, onClick }) {
  const Tag = interactive ? 'button' : 'div';

  return (
    <Tag
      type={interactive ? 'button' : undefined}
      className={`stat-card${interactive ? ' stat-card-button' : ''}`}
      onClick={interactive ? onClick : undefined}
    >
      <div className={`stat-icon ${tone}`} aria-hidden="true">{icon}</div>
      <div className="stat-info">
        <h3>{value}</h3>
        <p>{label}</p>
        {hint ? <span className="stat-hint">{hint}</span> : null}
      </div>
    </Tag>
  );
}
