package com.tlias.weather.airquality;

import com.tlias.weather.city.City;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "air_quality_observations", uniqueConstraints = @UniqueConstraint(name = "uk_observation_city_time_source", columnNames = {"city_id", "observed_at", "source"}))
public class AirQualityObservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "city_id")
    private City city;
    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;
    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;
    @Column(nullable = false)
    private String source;
    @Column(nullable = false)
    private int aqi;
    @Column(name = "pm2_5") private BigDecimal pm25;
    private BigDecimal pm10;
    private BigDecimal so2;
    private BigDecimal no2;
    private BigDecimal co;
    private BigDecimal o3;

    protected AirQualityObservation() { }

    public AirQualityObservation(City city, AirQualityReading reading, Instant collectedAt) {
        this.city = city;
        this.observedAt = reading.observedAt();
        this.collectedAt = collectedAt;
        this.source = reading.source();
        this.aqi = reading.aqi();
        this.pm25 = reading.pm25();
        this.pm10 = reading.pm10();
        this.so2 = reading.so2();
        this.no2 = reading.no2();
        this.co = reading.co();
        this.o3 = reading.o3();
    }

    public Long getId() { return id; }
    public City getCity() { return city; }
    public Instant getObservedAt() { return observedAt; }
    public Instant getCollectedAt() { return collectedAt; }
    public String getSource() { return source; }
    public int getAqi() { return aqi; }
    public BigDecimal getPm25() { return pm25; }
    public BigDecimal getPm10() { return pm10; }
    public BigDecimal getSo2() { return so2; }
    public BigDecimal getNo2() { return no2; }
    public BigDecimal getCo() { return co; }
    public BigDecimal getO3() { return o3; }
}
