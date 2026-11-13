package com.tlias.weather.notification;

import com.tlias.weather.alert.AlertEvent;
import com.tlias.weather.alert.AlertEventRepository;
import com.tlias.weather.alert.AlertEventStatus;
import com.tlias.weather.city.City;
import com.tlias.weather.security.User;
import com.tlias.weather.subscription.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class NotificationService {
    private final UserSubscriptionRepository subscriptions;
    private final AppNotificationRepository notifications;
    private final AlertEventRepository alertEvents;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public NotificationService(UserSubscriptionRepository subscriptions, AppNotificationRepository notifications,
                               AlertEventRepository alertEvents) {
        this(subscriptions, notifications, alertEvents, Clock.systemUTC());
    }

    NotificationService(UserSubscriptionRepository subscriptions, AppNotificationRepository notifications,
                        AlertEventRepository alertEvents, Clock clock) {
        this.subscriptions = subscriptions;
        this.notifications = notifications;
        this.alertEvents = alertEvents;
        this.clock = clock;
    }

    @Transactional
    public void notifySubscribers(AlertEvent event) {
        subscriptions.findByCityIdAndEnabledTrue(event.getCity().getId()).stream()
                .map(subscription -> subscription.getUser())
                .forEach(user -> notifyUser(user, event));
    }

    @Transactional
    public void notifyUserOfActiveEvents(User user, City city) {
        alertEvents.findByCityIdAndStatus(city.getId(), AlertEventStatus.ACTIVE)
                .forEach(event -> notifyUser(user, event));
    }

    @Transactional
    public void reconcileActiveNotifications(User user) {
        subscriptions.findByUserOrderByIdDesc(user).stream()
                .filter(subscription -> subscription.isEnabled())
                .forEach(subscription -> notifyUserOfActiveEvents(user, subscription.getCity()));
    }

    private void notifyUser(User user, AlertEvent event) {
        if (!notifications.existsByUserIdAndAlertEventId(user.getId(), event.getId())) {
            notifications.save(new AppNotification(user, event, clock.instant()));
        }
    }
}
