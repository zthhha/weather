package com.tlias.weather.alert;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AlertEvaluationService {
    private final AlertRuleRepository rules;
    private final AlertEventRepository events;
    private final NotificationService notifications;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public AlertEvaluationService(AlertRuleRepository rules, AlertEventRepository events, NotificationService notifications) {
        this(rules, events, notifications, Clock.systemUTC());
    }

    AlertEvaluationService(AlertRuleRepository rules, AlertEventRepository events, NotificationService notifications, Clock clock) {
        this.rules = rules;
        this.events = events;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Transactional
    public void evaluateNewObservation(AirQualityObservation observation) {
        for (AlertRule rule : rules.findEnabledApplicableToCity(observation.getCity().getId())) {
            String activeKey = AlertEvent.activeKey(rule.getId(), observation.getCity().getId());
            var existing = events.findByActiveKey(activeKey);
            if (rule.matches(observation)) {
                if (existing.isPresent()) {
                    AlertEvent event = existing.get();
                    event.refresh(observation, clock.instant());
                    events.save(event);
                } else {
                    AlertEvent event = events.save(AlertEvent.activate(rule, observation, clock.instant()));
                    notifications.notifySubscribers(event);
                }
            } else {
                existing.ifPresent(event -> {
                    event.resolve(observation, clock.instant());
                    events.save(event);
                });
            }
        }
    }
}
