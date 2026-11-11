package com.tlias.weather.alert;

import com.tlias.weather.airquality.AirQualityObservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AlertEvaluationService {
    private final AlertRuleRepository rules;
    private final AlertEventRepository events;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public AlertEvaluationService(AlertRuleRepository rules, AlertEventRepository events) {
        this(rules, events, Clock.systemUTC());
    }

    AlertEvaluationService(AlertRuleRepository rules, AlertEventRepository events, Clock clock) {
        this.rules = rules;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public void evaluateNewObservation(AirQualityObservation observation) {
        for (AlertRule rule : rules.findEnabledApplicableToCity(observation.getCity().getId())) {
            String activeKey = AlertEvent.activeKey(rule.getId(), observation.getCity().getId());
            var existing = events.findByActiveKey(activeKey);
            if (rule.matches(observation)) {
                AlertEvent event = existing.orElseGet(() -> AlertEvent.activate(rule, observation, clock.instant()));
                if (event.getId() != null) {
                    event.refresh(observation, clock.instant());
                }
                events.save(event);
            } else {
                existing.ifPresent(event -> {
                    event.resolve(observation, clock.instant());
                    events.save(event);
                });
            }
        }
    }
}
