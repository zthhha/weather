export const severityLabels: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '严重',
}

export function severityLabel(severity: string) {
  return severityLabels[severity] ?? severity
}
