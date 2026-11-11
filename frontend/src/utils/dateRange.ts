export function historyRange(hours: number, now = new Date()): { from: string; to: string } {
  const safeHours = Math.min(168, Math.max(1, hours))
  return {
    from: new Date(now.getTime() - safeHours * 3_600_000).toISOString(),
    to: now.toISOString(),
  }
}
