export interface ApiEnvelope<T> {
  data: T
  timestamp: string
}

export interface City {
  id: number
  name: string
  locationId: string
  timezone: string
  enabled: boolean
}

export interface CityRequest {
  name: string
  enabled: boolean
}

export interface Observation {
  id: number
  cityId: number
  cityName: string
  observedAt: string
  collectedAt: string
  source: string
  aqi: number
  aqiLevel: string
  aqiColor: string
  healthAdvice: string
  pm25: number | null
  pm10: number | null
  so2: number | null
  no2: number | null
  co: number | null
  o3: number | null
}

export interface Forecast {
  forecastAt: string
  generatedAt: string
  aqi: number
  aqiLevel: string
  aqiColor: string
  healthAdvice: string
  modelVersion: string
  qualityStatus: string
}

export interface CollectionJobLog {
  id: number
  startedAt: string
  finishedAt: string | null
  status: 'SUCCESS' | 'FAILED' | 'PARTIAL_SUCCESS' | 'RUNNING'
  totalCities: number
  successfulCities: number
  failedCities: number
  errorSummary: string | null
}

export type AlertMetric = 'AQI'
export type AlertOperator = 'GT' | 'GTE' | 'LT' | 'LTE'
export type AlertEventStatus = 'ACTIVE' | 'RESOLVED'

export interface AlertRule {
  id: number
  name: string
  cityId: number | null
  cityName: string | null
  global: boolean
  metric: AlertMetric
  operator: AlertOperator
  threshold: number
  severity: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface AlertRuleRequest {
  name: string
  cityId: number | null
  metric: AlertMetric
  operator: AlertOperator
  threshold: number
  severity: string
  enabled: boolean
}

export interface AlertEvent {
  id: number
  ruleId: number
  ruleName: string
  cityId: number
  cityName: string
  metric: AlertMetric
  operator: AlertOperator
  threshold: number
  severity: string
  status: AlertEventStatus
  triggeredValue: number
  lastEvaluatedValue: number
  firstTriggeredAt: string
  lastTriggeredAt: string
  resolvedAt: string | null
}
