package com.tlias.weather.forecast;

import com.tlias.weather.city.City;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "forecasts", uniqueConstraints = @UniqueConstraint(name = "uk_forecast_city_time_version", columnNames = {"city_id", "forecast_at", "model_version"}))
public class AqiForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(nullable = false)
    private int aqi;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "quality_status", nullable = false)
    private String qualityStatus;

    protected AqiForecast() {
    }

    public AqiForecast(City city, Instant forecastAt, Instant generatedAt, int aqi, String modelVersion, String qualityStatus) {
        this.city = city;
        this.forecastAt = forecastAt;
        this.generatedAt = generatedAt;
        this.aqi = aqi;
        this.modelVersion = modelVersion;
        this.qualityStatus = qualityStatus;
    }

    public Long getId() { return id; }
    public City getCity() { return city; }
    public Instant getForecastAt() { return forecastAt; }
    public Instant getGeneratedAt() { return generatedAt; }
    public int getAqi() { return aqi; }
    public String getModelVersion() { return modelVersion; }
    public String getQualityStatus() { return qualityStatus; }
}
