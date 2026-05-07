import { Link } from 'react-router-dom';

export default function QuickActionTile({ to, icon, label, tone = 'var(--primary)' }) {
  return (
    <Link to={to} className="quick-action-tile" style={{ '--action-accent': tone }}>
      <span className="quick-action-icon" aria-hidden="true">{icon}</span>
      <span>{label}</span>
    </Link>
  );
}
