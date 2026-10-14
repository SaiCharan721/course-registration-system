export type Role = 'STUDENT' | 'ADMIN';

export interface AuthResponse {
  token: string;
  fullName: string;
  email: string;
  role: Role;
}

export interface Course {
  id: number;
  code: string;
  title: string;
  description: string;
  instructor: string;
  credits: number;
  capacity: number;
  seatsTaken: number;
  seatsAvailable: number;
  schedule: string;
}

export type CourseInput = Omit<Course, 'id' | 'seatsTaken' | 'seatsAvailable'>;

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Enrollment {
  id: number;
  courseId: number;
  courseCode: string;
  courseTitle: string;
  credits: number;
  schedule: string;
  status: string;
  enrolledAt: string;
}

export interface RosterEntry {
  enrollmentId: number;
  studentName: string;
  studentEmail: string;
  enrolledAt: string;
}
