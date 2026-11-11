import { describe, expect, it } from 'vitest'
import { historyRange } from '@/utils/dateRange'

describe('historyRange', () => {
  it('creates ISO range ending at now', () => {
    const now = new Date('2026-11-11T12:00:00Z')
    expect(historyRange(24, now)).toEqual({ from: '2026-11-10T12:00:00.000Z', to: '2026-11-11T12:00:00.000Z' })
  })

  it('caps requests at backend maximum of seven days', () => {
    const now = new Date('2026-11-11T12:00:00Z')
    expect(historyRange(500, now).from).toBe('2026-11-04T12:00:00.000Z')
  })
})
