import { useEffect, useState, type FormEvent } from 'react';
import { courseApi, errorMessage } from '../api/client';
import type { Course, CourseInput, RosterEntry } from '../api/types';

const EMPTY: CourseInput = {
  code: '', title: '', description: '', instructor: '', credits: 3, capacity: 30, schedule: '',
};

export default function AdminPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [form, setForm] = useState<CourseInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [roster, setRoster] = useState<{ course: Course; entries: RosterEntry[] } | null>(null);
  const [message, setMessage] = useState<{ type: 'ok' | 'error'; text: string } | null>(null);

  async function load() {
    const page = await courseApi.list('', 0);
    setCourses(page.content);
  }

  useEffect(() => {
    load().catch((err) => setMessage({ type: 'error', text: errorMessage(err) }));
  }, []);

  async function submit(e: FormEvent) {
    e.preventDefault();
    try {
      if (editingId) await courseApi.update(editingId, form);
      else await courseApi.create(form);
      setMessage({ type: 'ok', text: editingId ? 'Course updated.' : 'Course created.' });
      setForm(EMPTY);
      setEditingId(null);
      await load();
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    }
  }

  function edit(c: Course) {
    setEditingId(c.id);
    setForm({ code: c.code, title: c.title, description: c.description, instructor: c.instructor,
      credits: c.credits, capacity: c.capacity, schedule: c.schedule });
  }

  async function remove(c: Course) {
    if (!window.confirm(`Delete ${c.code}?`)) return;
    try {
      await courseApi.remove(c.id);
      await load();
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    }
  }

  async function showRoster(c: Course) {
    try {
      setRoster({ course: c, entries: await courseApi.roster(c.id) });
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    }
  }

  const field = (key: keyof CourseInput, label: string, type = 'text') => (
    <label>{label}
      <input
        type={type}
        value={form[key]}
        disabled={key === 'code' && editingId !== null}
        onChange={(e) => setForm({ ...form, [key]: type === 'number' ? Number(e.target.value) : e.target.value })}
        required={key !== 'description' && key !== 'schedule'}
      />
    </label>
  );

  return (
    <section>
      <div className="page-head"><h1>Admin dashboard</h1></div>
      {message && <div className={`alert ${message.type}`}>{message.text}</div>}

      <div className="admin-layout">
        <form className="panel" onSubmit={submit}>
          <h2>{editingId ? 'Edit course' : 'New course'}</h2>
          {field('code', 'Code (e.g. CS-501)')}
          {field('title', 'Title')}
          {field('instructor', 'Instructor')}
          {field('schedule', 'Schedule')}
          <div className="row">
            {field('credits', 'Credits', 'number')}
            {field('capacity', 'Capacity', 'number')}
          </div>
          {field('description', 'Description')}
          <div className="row">
            <button className="btn">{editingId ? 'Save changes' : 'Create course'}</button>
            {editingId && (
              <button type="button" className="btn ghost" onClick={() => { setEditingId(null); setForm(EMPTY); }}>
                Cancel
              </button>
            )}
          </div>
        </form>

        <div className="panel">
          <h2>Courses</h2>
          <table className="table">
            <thead><tr><th>Code</th><th>Title</th><th>Seats</th><th /></tr></thead>
            <tbody>
              {courses.map((c) => (
                <tr key={c.id}>
                  <td className="code">{c.code}</td>
                  <td>{c.title}</td>
                  <td>{c.seatsTaken}/{c.capacity}</td>
                  <td className="actions">
                    <button className="btn ghost" onClick={() => showRoster(c)}>Roster</button>
                    <button className="btn ghost" onClick={() => edit(c)}>Edit</button>
                    <button className="btn ghost danger" onClick={() => remove(c)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {roster && (
            <div className="roster">
              <h3>{roster.course.code} roster ({roster.entries.length})</h3>
              {roster.entries.length === 0 ? <p className="muted">No students enrolled yet.</p> : (
                <ul>{roster.entries.map((r) => <li key={r.enrollmentId}>{r.studentName} · {r.studentEmail}</li>)}</ul>
              )}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
