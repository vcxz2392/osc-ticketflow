import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          TicketFlow
        </Link>
        <nav className="nav-links">
          <Link to="/">티켓</Link>
        </nav>
        <div className="nav-user">
          {user && (
            <>
              <span className="nav-company">{user.companyName}</span>
              <span className="nav-name">{user.name}</span>
              <span className={`badge role-${user.role}`}>{user.role}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>
                로그아웃
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
