import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, signup } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const [mode, setMode] = useState('login'); // 'login' | 'signup'
  const [form, setForm] = useState({
    companyName: '',
    username: '',
    password: '',
    name: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      if (mode === 'login') {
        await login(form.username, form.password);
      } else {
        await signup({
          companyName: form.companyName,
          username: form.username,
          password: form.password,
          name: form.name,
        });
      }
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.displayMessage || '인증에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <h1 className="auth-title">TicketFlow</h1>
        <p className="auth-subtitle">
          {mode === 'login' ? '로그인' : '회사 등록 및 관리자 계정 생성'}
        </p>

        <form onSubmit={handleSubmit} className="auth-form">
          {mode === 'signup' && (
            <label className="field">
              <span>회사명</span>
              <input
                value={form.companyName}
                onChange={update('companyName')}
                required
                placeholder="회사명"
              />
            </label>
          )}
          <label className="field">
            <span>아이디</span>
            <input
              value={form.username}
              onChange={update('username')}
              required
              placeholder="username"
              autoComplete="username"
            />
          </label>
          <label className="field">
            <span>비밀번호</span>
            <input
              type="password"
              value={form.password}
              onChange={update('password')}
              required
              placeholder="password"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </label>
          {mode === 'signup' && (
            <label className="field">
              <span>이름</span>
              <input
                value={form.name}
                onChange={update('name')}
                required
                placeholder="이름"
              />
            </label>
          )}

          {error && <div className="alert alert-error">{error}</div>}

          <button className="btn btn-primary btn-block" disabled={submitting}>
            {submitting
              ? '처리 중...'
              : mode === 'login'
              ? '로그인'
              : '가입하기'}
          </button>
        </form>

        <div className="auth-toggle">
          {mode === 'login' ? (
            <button className="link-btn" onClick={() => setMode('signup')}>
              회사가 없으신가요? 회원가입
            </button>
          ) : (
            <button className="link-btn" onClick={() => setMode('login')}>
              이미 계정이 있으신가요? 로그인
            </button>
          )}
        </div>

        {mode === 'login' && (
          <div className="demo-hint">
            <div className="demo-hint-title">데모 계정</div>
            <div>
              <code>admin1 / admin123</code> (ADMIN)
            </div>
            <div>
              <code>user1 / user123</code> (USER)
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
