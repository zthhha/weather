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

    protected City() {
    }

    public City(String name, String locationId, String timezone, boolean enabled) {
        this.name = name;
        this.locationId = locationId;
        this.timezone = timezone;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocationId() { return locationId; }
    public String getTimezone() { return timezone; }
    public boolean isEnabled() { return enabled; }

    public void update(String name, String locationId, String timezone, boolean enabled) {
        this.name = name;
        this.locationId = locationId;
        this.timezone = timezone;
        this.enabled = enabled;
    }
}
