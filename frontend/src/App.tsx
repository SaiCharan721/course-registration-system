import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactElement } from 'react';
import Navbar from './components/Navbar';
import { useAuth } from './auth/AuthContext';
import type { Role } from './api/types';
import CoursesPage from './pages/CoursesPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import MyCoursesPage from './pages/MyCoursesPage';
import AdminPage from './pages/AdminPage';

function RequireRole({ role, children }: { role?: Role; children: ReactElement }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<CoursesPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/my-courses" element={<RequireRole><MyCoursesPage /></RequireRole>} />
          <Route path="/admin" element={<RequireRole role="ADMIN"><AdminPage /></RequireRole>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
