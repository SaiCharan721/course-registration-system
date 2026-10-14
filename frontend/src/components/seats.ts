export type SeatLevel = 'open' | 'low' | 'full';

/** Classifies remaining seats so the catalog can color-code availability. */
export function seatLevel(available: number, capacity: number): SeatLevel {
  if (available <= 0) return 'full';
  if (available / capacity <= 0.2 || available <= 3) return 'low';
  return 'open';
}

export function seatLabel(available: number): string {
  if (available <= 0) return 'Full';
  return available === 1 ? '1 seat left' : `${available} seats left`;
}
