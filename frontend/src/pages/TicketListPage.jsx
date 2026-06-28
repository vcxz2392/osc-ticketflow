import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketsApi } from '../api/tickets';
import { useToast } from '../context/ToastContext';
import { StatusBadge, PriorityBadge } from '../components/Badge';
import {
  STATUS_ORDER,
  STATUS_LABELS,
  PRIORITY_ORDER,
  PRIORITY_LABELS,
  formatDateTime,
} from '../constants';

const EMPTY_STATS = { open: 0, inProgress: 0, resolved: 0, closed: 0 };

export default function TicketListPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [tickets, setTickets] = useState([]);
  const [stats, setStats] = useState(EMPTY_STATS);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM' });
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState('');

  const loadStats = useCallback(async () => {
    try {
      setStats(await ticketsApi.stats());
    } catch {
      /* stats are non-critical */
    }
  }, []);

  const loadTickets = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = statusFilter ? { status: statusFilter } : {};
      setTickets(await ticketsApi.list(params));
    } catch (err) {
      setError(err.displayMessage || '티켓을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    loadTickets();
  }, [loadTickets]);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    setCreating(true);
    try {
      const created = await ticketsApi.create(form);
      setForm({ title: '', description: '', priority: 'MEDIUM' });
      setShowForm(false);
      showToast('티켓이 등록되었습니다');
      await Promise.all([loadTickets(), loadStats()]);
      if (created?.id) navigate(`/tickets/${created.id}`);
    } catch (err) {
      setFormError(err.displayMessage || '티켓 생성에 실패했습니다.');
    } finally {
      setCreating(false);
    }
  };

  const statCards = [
    { key: 'open', label: STATUS_LABELS.OPEN, cls: 'status-OPEN' },
    { key: 'inProgress', label: STATUS_LABELS.IN_PROGRESS, cls: 'status-IN_PROGRESS' },
    { key: 'resolved', label: STATUS_LABELS.RESOLVED, cls: 'status-RESOLVED' },
    { key: 'closed', label: STATUS_LABELS.CLOSED, cls: 'status-CLOSED' },
  ];

  return (
    <div className="page">
      <div className="page-header">
        <h1>티켓</h1>
        <button className="btn btn-primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? '닫기' : '새 티켓'}
        </button>
      </div>

      <div className="stats-grid">
        {statCards.map((c) => (
          <div key={c.key} className="stat-card">
            <div className={`stat-dot ${c.cls}`} />
            <div className="stat-meta">
              <div className="stat-value">{stats[c.key] ?? 0}</div>
              <div className="stat-label">{c.label}</div>
            </div>
          </div>
        ))}
      </div>

      {showForm && (
        <form className="card create-form" onSubmit={handleCreate}>
          <h2>새 티켓 등록</h2>
          <label className="field">
            <span>제목</span>
            <input
              value={form.title}
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
              required
              placeholder="제목"
            />
          </label>
          <label className="field">
            <span>설명</span>
            <textarea
              value={form.description}
              onChange={(e) =>
                setForm((f) => ({ ...f, description: e.target.value }))
              }
              rows={4}
              placeholder="상세 내용을 입력하세요"
            />
          </label>
          <label className="field">
            <span>우선순위</span>
            <select
              value={form.priority}
              onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value }))}
            >
              {PRIORITY_ORDER.map((p) => (
                <option key={p} value={p}>
                  {PRIORITY_LABELS[p]} ({p})
                </option>
              ))}
            </select>
          </label>
          {formError && <div className="alert alert-error">{formError}</div>}
          <div className="form-actions">
            <button className="btn btn-primary" disabled={creating}>
              {creating ? '등록 중...' : '등록'}
            </button>
          </div>
        </form>
      )}

      <div className="toolbar">
        <label className="field inline">
          <span>상태 필터</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">전체</option>
            {STATUS_ORDER.map((s) => (
              <option key={s} value={s}>
                {STATUS_LABELS[s]} ({s})
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="empty">불러오는 중...</div>
      ) : tickets.length === 0 ? (
        <div className="empty">표시할 티켓이 없습니다.</div>
      ) : (
        <div className="card table-wrap">
          <table className="ticket-table">
            <thead>
              <tr>
                <th>제목</th>
                <th>상태</th>
                <th>우선순위</th>
                <th>요청자</th>
                <th>담당자</th>
                <th>생성일</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((t) => (
                <tr
                  key={t.id}
                  className="ticket-row"
                  onClick={() => navigate(`/tickets/${t.id}`)}
                >
                  <td className="cell-title">{t.title}</td>
                  <td>
                    <StatusBadge status={t.status} />
                  </td>
                  <td>
                    <PriorityBadge priority={t.priority} />
                  </td>
                  <td>{t.requesterName}</td>
                  <td>
                    {t.assigneeName || <span className="muted">미배정</span>}
                  </td>
                  <td className="muted">{formatDateTime(t.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
