package com.tlias.weather.notification;

import com.tlias.weather.alert.AlertEvent;
import com.tlias.weather.security.User;
import com.tlias.weather.subscription.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class NotificationService {
    private final UserSubscriptionRepository subscriptions;
    private final AppNotificationRepository notifications;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public NotificationService(UserSubscriptionRepository subscriptions, AppNotificationRepository notifications) {
        this(subscriptions, notifications, Clock.systemUTC());
    }

    NotificationService(UserSubscriptionRepository subscriptions, AppNotificationRepository notifications, Clock clock) {
        this.subscriptions = subscriptions;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Transactional
    public void notifySubscribers(AlertEvent event) {
        subscriptions.findByCityIdAndEnabledTrue(event.getCity().getId()).stream()
                .filter(subscription -> severityRank(event.getRule().getSeverity()) >= severityRank(subscription.getMinimumSeverity()))
                .map(subscription -> subscription.getUser())
                .filter(user -> !notifications.existsByUserIdAndAlertEventId(user.getId(), event.getId()))
                .forEach(user -> notifications.save(new AppNotification(user, event, clock.instant())));
    }

    private int severityRank(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
