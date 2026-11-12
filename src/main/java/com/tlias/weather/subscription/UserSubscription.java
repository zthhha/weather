package com.tlias.weather.subscription;

import com.tlias.weather.city.City;
import com.tlias.weather.security.User;
import jakarta.persistence.*;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "city_id") private City city;
    @Column(name = "minimum_severity", nullable = false) private String minimumSeverity;
    @Column(nullable = false) private boolean enabled;
    protected UserSubscription() { }
    public UserSubscription(User user, City city, String minimumSeverity, boolean enabled) {
        this.user = user; this.city = city; this.minimumSeverity = minimumSeverity; this.enabled = enabled;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public City getCity() { return city; }
    public String getMinimumSeverity() { return minimumSeverity; }
    public boolean isEnabled() { return enabled; }
    public void update(String minimumSeverity, boolean enabled) { this.minimumSeverity = minimumSeverity; this.enabled = enabled; }
}
