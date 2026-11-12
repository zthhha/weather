package com.tlias.weather.notification.api;

import com.tlias.weather.notification.AppNotification;

import java.time.Instant;

public record NotificationResponse(Long id, Long alertEventId, String ruleName, String cityName, String severity,
                                   Instant lastTriggeredAt, Instant readAt, Instant createdAt) {
    public static NotificationResponse from(AppNotification notification) {
        var event = notification.getAlertEvent();
        return new NotificationResponse(notification.getId(), event.getId(), event.getRule().getName(), event.getCity().getName(),
                event.getRule().getSeverity(), event.getLastTriggeredAt(), notification.getReadAt(), notification.getCreatedAt());
    }
}
