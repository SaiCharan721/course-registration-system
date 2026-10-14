import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { courseApi, enrollmentApi, errorMessage } from '../api/client';
import type { Course, Page } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import CourseCard from '../components/CourseCard';

export default function CoursesPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<Course> | null>(null);
  const [enrolledIds, setEnrolledIds] = useState<Set<number>>(new Set());
  const [busyId, setBusyId] = useState<number | null>(null);
  const [message, setMessage] = useState<{ type: 'ok' | 'error'; text: string } | null>(null);

  const load = useCallback(async () => {
    try {
      setData(await courseApi.list(search, page));
      if (user?.role === 'STUDENT') {
        const mine = await enrollmentApi.mine();
        setEnrolledIds(new Set(mine.map((e) => e.courseId)));
      }
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    }
  }, [search, page, user]);

  useEffect(() => {
    load();
  }, [load]);

  async function enroll(course: Course) {
    if (!user) {
      navigate('/login');
      return;
    }
    setBusyId(course.id);
    setMessage(null);
    try {
      await enrollmentApi.enroll(course.id);
      setMessage({ type: 'ok', text: `You're enrolled in ${course.code}.` });
      await load();
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    } finally {
      setBusyId(null);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h1>Course catalog</h1>
        <form
          className="search"
          onSubmit={(e) => {
            e.preventDefault();
            setPage(0);
            setSearch(query);
          }}
        >
          <input
            placeholder="Search by title, code or instructor"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            aria-label="Search courses"
          />
          <button className="btn" type="submit">Search</button>
        </form>
      </div>

      {message && <div className={`alert ${message.type}`}>{message.text}</div>}

      {data && data.content.length === 0 && <p className="muted">No courses match your search.</p>}

      <div className="grid">
        {data?.content.map((course) => (
          <CourseCard
            key={course.id}
            course={course}
            enrolled={enrolledIds.has(course.id)}
            busy={busyId === course.id}
            canEnroll={user?.role !== 'ADMIN'}
            onEnroll={enroll}
          />
        ))}
      </div>

      {data && data.totalPages > 1 && (
        <div className="pager">
          <button className="btn ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span className="muted">Page {page + 1} of {data.totalPages}</span>
          <button className="btn ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
            Next
          </button>
        </div>
      )}
    </section>
  );
}
