import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="navbar">
      <Link to="/" className="brand">CourseReg</Link>
      <nav>
        <NavLink to="/" end>Catalog</NavLink>
        {user && <NavLink to="/my-courses">My courses</NavLink>}
        {user?.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
      </nav>
      <div className="session">
        {user ? (
          <>
            <span className="muted">{user.fullName}</span>
            <button className="btn ghost" onClick={() => { logout(); navigate('/login'); }}>Sign out</button>
          </>
        ) : (
          <Link to="/login" className="btn">Sign in</Link>
        )}
      </div>
    </header>
  );
}
