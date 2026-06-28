import { STATUS_LABELS, PRIORITY_LABELS } from '../constants';

export function StatusBadge({ status }) {
  return (
    <span className={`badge status-${status}`}>
      {STATUS_LABELS[status] || status}
    </span>
  );
}

export function PriorityBadge({ priority }) {
  return (
    <span className={`badge priority-${priority}`}>
      {PRIORITY_LABELS[priority] || priority}
    </span>
  );
}
