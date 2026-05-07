import { useEffect } from 'react';

export default function FloatingNotification({
  message,
  tone = 'success',
  duration = 3000,
  onClose,
}) {
  useEffect(() => {
    if (!message || !onClose) return undefined;

    const timeoutId = window.setTimeout(() => {
      onClose();
    }, duration);

    return () => window.clearTimeout(timeoutId);
  }, [duration, message, onClose]);

  if (!message) return null;

  return (
    <div className="floating-notification-wrap" aria-live="polite" aria-atomic="true">
      <div className={`floating-notification is-${tone}`} role="status">
        <span className="floating-notification-message">{message}</span>
        <button
          type="button"
          className="floating-notification-close"
          onClick={onClose}
          aria-label="Dismiss notification"
        >
          x
        </button>
      </div>
    </div>
  );
}
