import type { Course } from '../api/types';
import { seatLabel, seatLevel } from './seats';

interface Props {
  course: Course;
  enrolled: boolean;
  busy: boolean;
  canEnroll: boolean;
  onEnroll: (course: Course) => void;
}

export default function CourseCard({ course, enrolled, busy, canEnroll, onEnroll }: Props) {
  const level = seatLevel(course.seatsAvailable, course.capacity);
  return (
    <article className="card">
      <div className="card-head">
        <span className="code">{course.code}</span>
        <span className={`pill ${level}`}>{seatLabel(course.seatsAvailable)}</span>
      </div>
      <h3>{course.title}</h3>
      <p className="muted">{course.description}</p>
      <dl>
        <dt>Instructor</dt><dd>{course.instructor}</dd>
        <dt>Schedule</dt><dd>{course.schedule || 'TBA'}</dd>
        <dt>Credits</dt><dd>{course.credits}</dd>
      </dl>
      {canEnroll && (
        <button
          className="btn"
          disabled={busy || enrolled || level === 'full'}
          onClick={() => onEnroll(course)}
        >
          {enrolled ? 'Enrolled' : level === 'full' ? 'Course full' : busy ? 'Enrolling…' : 'Enroll'}
        </button>
      )}
    </article>
  );
}
