import { useEffect, useState } from 'react';
import { enrollmentApi, errorMessage } from '../api/client';
import type { Enrollment } from '../api/types';

export default function MyCoursesPage() {
  const [items, setItems] = useState<Enrollment[] | null>(null);
  const [error, setError] = useState('');

  async function load() {
    try {
      setItems(await enrollmentApi.mine());
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function drop(enrollment: Enrollment) {
    if (!window.confirm(`Drop ${enrollment.courseCode}?`)) return;
    try {
      await enrollmentApi.drop(enrollment.id);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  const totalCredits = items?.reduce((sum, e) => sum + e.credits, 0) ?? 0;

  return (
    <section>
      <div className="page-head">
        <h1>My courses</h1>
        <span className="pill open">{totalCredits} credits</span>
      </div>
      {error && <div className="alert error">{error}</div>}
      {items && items.length === 0 && <p className="muted">You haven't enrolled in any courses yet.</p>}
      {items && items.length > 0 && (
        <table className="table">
          <thead>
            <tr><th>Code</th><th>Course</th><th>Schedule</th><th>Credits</th><th>Enrolled</th><th /></tr>
          </thead>
          <tbody>
            {items.map((e) => (
              <tr key={e.id}>
                <td className="code">{e.courseCode}</td>
                <td>{e.courseTitle}</td>
                <td>{e.schedule}</td>
                <td>{e.credits}</td>
                <td>{new Date(e.enrolledAt).toLocaleDateString()}</td>
                <td><button className="btn ghost danger" onClick={() => drop(e)}>Drop</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
