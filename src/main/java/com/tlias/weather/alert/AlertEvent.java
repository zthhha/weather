package com.tlias.weather.alert;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.city.City;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alert_events")
public class AlertEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "rule_id") private AlertRule rule;
    @ManyToOne(optional = false) @JoinColumn(name = "city_id") private City city;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AlertEventStatus status;
    @Column(name = "triggered_value", nullable = false) private BigDecimal triggeredValue;
    @Column(name = "first_triggered_at", nullable = false) private Instant firstTriggeredAt;
    @Column(name = "last_triggered_at", nullable = false) private Instant lastTriggeredAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "active_key") private String activeKey;
    @ManyToOne @JoinColumn(name = "observation_id") private AirQualityObservation observation;
    @ManyToOne @JoinColumn(name = "last_observation_id") private AirQualityObservation lastObservation;
    @Column(name = "last_evaluated_at") private Instant lastEvaluatedAt;
    @Column(name = "last_evaluated_value") private BigDecimal lastEvaluatedValue;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AlertEvent() { }

    public static AlertEvent activate(AlertRule rule, AirQualityObservation observation, Instant now) {
        AlertEvent event = new AlertEvent();
        event.rule = rule;
        event.city = observation.getCity();
        event.status = AlertEventStatus.ACTIVE;
        event.triggeredValue = BigDecimal.valueOf(observation.getAqi());
        event.firstTriggeredAt = observation.getObservedAt();
        event.lastTriggeredAt = observation.getObservedAt();
        event.activeKey = activeKey(rule.getId(), observation.getCity().getId());
        event.observation = observation;
        event.lastObservation = observation;
        event.lastEvaluatedAt = observation.getObservedAt();
        event.lastEvaluatedValue = event.triggeredValue;
        event.updatedAt = now;
        return event;
    }

    public void refresh(AirQualityObservation observation, Instant now) {
        this.lastTriggeredAt = observation.getObservedAt();
        this.lastObservation = observation;
        this.lastEvaluatedAt = observation.getObservedAt();
        this.lastEvaluatedValue = BigDecimal.valueOf(observation.getAqi());
        this.updatedAt = now;
    }

    public void resolve(AirQualityObservation observation, Instant now) {
        this.status = AlertEventStatus.RESOLVED;
        this.resolvedAt = observation.getObservedAt();
        this.activeKey = null;
        this.lastObservation = observation;
        this.lastEvaluatedAt = observation.getObservedAt();
        this.lastEvaluatedValue = BigDecimal.valueOf(observation.getAqi());
        this.updatedAt = now;
    }

    public static String activeKey(Long ruleId, Long cityId) { return ruleId + ":" + cityId; }
    public Long getId() { return id; }
    public AlertRule getRule() { return rule; }
    public City getCity() { return city; }
    public AlertEventStatus getStatus() { return status; }
    public BigDecimal getTriggeredValue() { return triggeredValue; }
    public Instant getFirstTriggeredAt() { return firstTriggeredAt; }
    public Instant getLastTriggeredAt() { return lastTriggeredAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public BigDecimal getLastEvaluatedValue() { return lastEvaluatedValue; }
}
