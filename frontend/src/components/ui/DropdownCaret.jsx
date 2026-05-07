export default function DropdownCaret({ isOpen = false, className = '' }) {
  const classes = ['dropdown-caret', isOpen ? 'is-open' : '', className].filter(Boolean).join(' ');

  return (
    <span className={classes} aria-hidden="true">
      <svg viewBox="0 0 20 20" fill="none" focusable="false">
        <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </span>
  );
}
