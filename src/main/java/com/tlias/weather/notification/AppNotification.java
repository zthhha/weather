package com.tlias.weather.notification;

import com.tlias.weather.alert.AlertEvent;
import com.tlias.weather.security.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "app_notifications")
public class AppNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "alert_event_id") private AlertEvent alertEvent;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected AppNotification() { }
    public AppNotification(User user, AlertEvent alertEvent, Instant createdAt) { this.user = user; this.alertEvent = alertEvent; this.createdAt = createdAt; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public AlertEvent getAlertEvent() { return alertEvent; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void markRead(Instant at) { if (readAt == null) readAt = at; }
}
