package com.tlias.weather.city;

import jakarta.persistence.*;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "location_id", nullable = false, unique = true)
    private String locationId;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private boolean enabled;

    @Column(precision = 6, scale = 2)
    private java.math.BigDecimal latitude;

    @Column(precision = 6, scale = 2)
    private java.math.BigDecimal longitude;

    protected City() {
    }

    public City(String name, String locationId, String timezone, boolean enabled) {
        this(name, locationId, timezone, enabled, null, null);
    }

    public City(String name, String locationId, String timezone, boolean enabled, java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        this.name = name;
        this.locationId = locationId;
        this.timezone = timezone;
        this.enabled = enabled;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocationId() { return locationId; }
    public String getTimezone() { return timezone; }
    public boolean isEnabled() { return enabled; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }

    public void enable() {
        this.enabled = true;
    }

    public void update(String name, String locationId, String timezone, boolean enabled) {
        this.name = name;
        this.locationId = locationId;
        this.timezone = timezone;
        this.enabled = enabled;
    }

    public void update(String name, String locationId, String timezone, boolean enabled, java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        update(name, locationId, timezone, enabled);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
