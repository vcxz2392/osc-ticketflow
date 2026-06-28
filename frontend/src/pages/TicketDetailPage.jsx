import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ticketsApi, usersApi } from '../api/tickets';
import { StatusBadge, PriorityBadge } from '../components/Badge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { STATUS_TRANSITIONS, STATUS_LABELS, formatDateTime } from '../constants';

export default function TicketDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const { showToast } = useToast();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [busy, setBusy] = useState(false);

  const [admins, setAdmins] = useState([]);
  const [selectedAssignee, setSelectedAssignee] = useState('');
  const [comment, setComment] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setTicket(await ticketsApi.get(id));
    } catch (err) {
      setError(err.displayMessage || '티켓을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!isAdmin) return;
    usersApi
      .admins()
      .then(setAdmins)
      .catch(() => setAdmins([]));
  }, [isAdmin]);

  const runAction = async (fn, successMessage) => {
    setActionError('');
    setBusy(true);
    try {
      const updated = await fn();
      if (updated) setTicket(updated);
      if (successMessage) showToast(successMessage);
    } catch (err) {
      setActionError(err.displayMessage || '작업에 실패했습니다.');
    } finally {
      setBusy(false);
    }
  };

  const handleStatusChange = (status) =>
    runAction(() => ticketsApi.updateStatus(id, status), '상태가 변경되었습니다');

  const handleAssign = () => {
    if (!selectedAssignee) return;
    runAction(
      () => ticketsApi.assign(id, Number(selectedAssignee)),
      '담당자가 배정되었습니다'
    );
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!comment.trim()) return;
    await runAction(async () => {
      const updated = await ticketsApi.addComment(id, comment.trim());
      setComment('');
      return updated;
    }, '댓글이 등록되었습니다');
  };

  const handleDelete = async () => {
    if (!window.confirm('이 티켓을 삭제하시겠습니까?')) return;
    setActionError('');
    setBusy(true);
    try {
      await ticketsApi.remove(id);
      navigate('/');
    } catch (err) {
      setActionError(err.displayMessage || '삭제에 실패했습니다.');
      setBusy(false);
    }
  };

  if (loading) return <div className="page"><div className="empty">불러오는 중...</div></div>;
  if (error)
    return (
      <div className="page">
        <Link to="/" className="back-link">← 목록으로</Link>
        <div className="alert alert-error">{error}</div>
      </div>
    );
  if (!ticket) return null;

  const transitions = STATUS_TRANSITIONS[ticket.status] || [];

  return (
    <div className="page">
      <Link to="/" className="back-link">← 목록으로</Link>

      <div className="card detail-head">
        <div className="detail-title-row">
          <h1>{ticket.title}</h1>
          <div className="badges">
            <StatusBadge status={ticket.status} />
            <PriorityBadge priority={ticket.priority} />
          </div>
        </div>
        <p className="detail-desc">{ticket.description || '설명이 없습니다.'}</p>
        <dl className="detail-meta">
          <div>
            <dt>요청자</dt>
            <dd>{ticket.requester?.name || '-'}</dd>
          </div>
          <div>
            <dt>담당자</dt>
            <dd>{ticket.assignee?.name || <span className="muted">미배정</span>}</dd>
          </div>
          <div>
            <dt>생성일</dt>
            <dd>{formatDateTime(ticket.createdAt)}</dd>
          </div>
          <div>
            <dt>수정일</dt>
            <dd>{formatDateTime(ticket.updatedAt)}</dd>
          </div>
        </dl>
      </div>

      {actionError && <div className="alert alert-error">{actionError}</div>}

      {isAdmin && (
        <div className="card">
          <h2>상태 변경</h2>
          {transitions.length === 0 ? (
            <p className="muted">더 이상 변경할 수 있는 상태가 없습니다.</p>
          ) : (
            <div className="btn-group">
              {transitions.map((s) => (
                <button
                  key={s}
                  className="btn btn-outline"
                  disabled={busy}
                  onClick={() => handleStatusChange(s)}
                >
                  {STATUS_LABELS[s]} ({s})
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {isAdmin && (
        <div className="card">
          <h2>담당자 배정</h2>
          <div className="assign-row">
            <select
              value={selectedAssignee}
              onChange={(e) => setSelectedAssignee(e.target.value)}
            >
              <option value="">담당자 선택</option>
              {admins.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.name} ({a.username})
                </option>
              ))}
            </select>
            <button
              className="btn btn-primary"
              disabled={busy || !selectedAssignee}
              onClick={handleAssign}
            >
              배정
            </button>
          </div>
        </div>
      )}

      <div className="card">
        <h2>댓글</h2>
        <ul className="comment-list">
          {(ticket.comments || []).length === 0 && (
            <li className="muted">댓글이 없습니다.</li>
          )}
          {(ticket.comments || []).map((c) => (
            <li key={c.id} className="comment">
              <div className="comment-head">
                <span className="comment-author">{c.authorName}</span>
                <span className="comment-time muted">
                  {formatDateTime(c.createdAt)}
                </span>
              </div>
              <div className="comment-body">{c.message}</div>
            </li>
          ))}
        </ul>
        <form className="comment-form" onSubmit={handleAddComment}>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={3}
            placeholder="댓글을 입력하세요"
          />
          <button className="btn btn-primary" disabled={busy || !comment.trim()}>
            댓글 등록
          </button>
        </form>
      </div>

      {isAdmin && (
        <div className="card danger-zone">
          <h2>위험 구역</h2>
          <button className="btn btn-danger" disabled={busy} onClick={handleDelete}>
            티켓 삭제
          </button>
        </div>
      )}
    </div>
  );
}
