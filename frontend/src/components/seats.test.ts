import { describe, expect, it } from 'vitest';
import { seatLabel, seatLevel } from './seats';

describe('seatLevel', () => {
  it('marks a course with no seats as full', () => {
    expect(seatLevel(0, 30)).toBe('full');
  });

  it('marks a nearly full course as low', () => {
    expect(seatLevel(3, 40)).toBe('low');
    expect(seatLevel(5, 25)).toBe('low');
  });

  it('marks a course with plenty of seats as open', () => {
    expect(seatLevel(20, 40)).toBe('open');
  });
});

describe('seatLabel', () => {
  it('uses singular and plural forms', () => {
    expect(seatLabel(1)).toBe('1 seat left');
    expect(seatLabel(12)).toBe('12 seats left');
    expect(seatLabel(0)).toBe('Full');
  });
});
