package com.tlias.weather.alert;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.city.City;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alert_rules")
public class AlertRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne @JoinColumn(name = "city_id") private City city;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AlertMetric metric;
    @Enumerated(EnumType.STRING) @Column(name = "operator", nullable = false) private AlertOperator operator;
    @Column(nullable = false) private BigDecimal threshold;
    @Column(nullable = false) private String severity;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AlertRule() { }

    public AlertRule(String name, City city, AlertMetric metric, AlertOperator operator, BigDecimal threshold, String severity, boolean enabled, Instant now) {
        this.name = name;
        this.city = city;
        this.metric = metric;
        this.operator = operator;
        this.threshold = threshold;
        this.severity = severity;
        this.enabled = enabled;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, City city, AlertMetric metric, AlertOperator operator, BigDecimal threshold,
                       String severity, boolean enabled, Instant now) {
        this.name = name;
        this.city = city;
        this.metric = metric;
        this.operator = operator;
        this.threshold = threshold;
        this.severity = severity;
        this.enabled = enabled;
        this.updatedAt = now;
    }

    public boolean matches(AirQualityObservation observation) {
        if (metric != AlertMetric.AQI) {
            return false;
        }
        return operator.matches(BigDecimal.valueOf(observation.getAqi()), threshold);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public City getCity() { return city; }
    public AlertMetric getMetric() { return metric; }
    public AlertOperator getOperator() { return operator; }
    public BigDecimal getThreshold() { return threshold; }
    public String getSeverity() { return severity; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
