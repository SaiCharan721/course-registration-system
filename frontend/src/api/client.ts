import axios, { AxiosError } from 'axios';
import type { AuthResponse, Course, CourseInput, Enrollment, Page, RosterEntry } from './types';

const TOKEN_KEY = 'crs.auth';

export const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const stored = loadSession();
  if (stored) {
    config.headers.Authorization = `Bearer ${stored.token}`;
  }
  return config;
});

export function saveSession(session: AuthResponse | null) {
  if (session) localStorage.setItem(TOKEN_KEY, JSON.stringify(session));
  else localStorage.removeItem(TOKEN_KEY);
}

export function loadSession(): AuthResponse | null {
  try {
    const raw = localStorage.getItem(TOKEN_KEY);
    return raw ? (JSON.parse(raw) as AuthResponse) : null;
  } catch {
    return null;
  }
}

/** Pulls a readable message out of the backend's problem+json error. */
export function errorMessage(err: unknown): string {
  if (err instanceof AxiosError) {
    const data = err.response?.data as { detail?: string; errors?: Record<string, string> } | undefined;
    if (data?.errors) {
      return Object.entries(data.errors).map(([field, msg]) => `${field} ${msg}`).join(', ');
    }
    if (data?.detail) return data.detail;
    if (err.response?.status === 401) return 'Please sign in again.';
  }
  return 'Something went wrong. Please try again.';
}

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }).then((r) => r.data),
  register: (fullName: string, email: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { fullName, email, password }).then((r) => r.data),
};

export const courseApi = {
  list: (q: string, page = 0) =>
    api.get<Page<Course>>('/courses', { params: { q: q || undefined, page, size: 9 } }).then((r) => r.data),
  create: (input: CourseInput) => api.post<Course>('/admin/courses', input).then((r) => r.data),
  update: (id: number, input: CourseInput) => api.put<Course>(`/admin/courses/${id}`, input).then((r) => r.data),
  remove: (id: number) => api.delete(`/admin/courses/${id}`),
  roster: (id: number) => api.get<RosterEntry[]>(`/admin/courses/${id}/enrollments`).then((r) => r.data),
};

export const enrollmentApi = {
  enroll: (courseId: number) => api.post<Enrollment>('/enrollments', { courseId }).then((r) => r.data),
  mine: () => api.get<Enrollment[]>('/enrollments/me').then((r) => r.data),
  drop: (id: number) => api.delete(`/enrollments/${id}`),
};
