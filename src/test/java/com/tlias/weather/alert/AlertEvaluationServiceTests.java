package com.tlias.weather.alert;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AirQualityReading;
import com.tlias.weather.city.City;
import com.tlias.weather.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertEvaluationServiceTests {
    private final AlertRuleRepository rules = mock(AlertRuleRepository.class);
    private final AlertEventRepository events = mock(AlertEventRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final Instant now = Instant.parse("2026-11-11T00:00:00Z");
    private final AlertEvaluationService service = new AlertEvaluationService(rules, events, notifications, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void createsActiveEventWhenRuleFirstMatches() {
        City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
        AlertRule rule = rule(city, AlertOperator.GTE, 100, true);
        AirQualityObservation observation = observation(city, 100, now);
        when(rules.findEnabledApplicableToCity(city.getId())).thenReturn(List.of(rule));
        when(events.findByActiveKey(any())).thenReturn(Optional.empty());

        service.evaluateNewObservation(observation);

        verify(events).save(any(AlertEvent.class));
    }

    @Test
    void updatesExistingEventWhenRuleContinuesToMatch() {
        City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
        AlertRule rule = rule(city, AlertOperator.GTE, 100, true);
        AirQualityObservation observation = observation(city, 120, now.plusSeconds(3600));
        AlertEvent existing = mock(AlertEvent.class);
        when(existing.getId()).thenReturn(1L);
        when(rules.findEnabledApplicableToCity(city.getId())).thenReturn(List.of(rule));
        when(events.findByActiveKey(any())).thenReturn(Optional.of(existing));

        service.evaluateNewObservation(observation);

        verify(existing).refresh(eq(observation), eq(now));
        verify(events).save(existing);
    }

    @Test
    void resolvesExistingEventWhenRuleStopsMatching() {
        City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
        AlertRule rule = rule(city, AlertOperator.GTE, 100, true);
        AirQualityObservation observation = observation(city, 99, now.plusSeconds(3600));
        AlertEvent existing = mock(AlertEvent.class);
        when(rules.findEnabledApplicableToCity(city.getId())).thenReturn(List.of(rule));
        when(events.findByActiveKey(any())).thenReturn(Optional.of(existing));

        service.evaluateNewObservation(observation);

        verify(existing).resolve(eq(observation), eq(now));
        verify(events).save(existing);
    }

    @Test
    void respectsStrictOperatorBoundaries() {
        City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
        AlertRule strict = rule(city, AlertOperator.GT, 100, true);
        when(rules.findEnabledApplicableToCity(city.getId())).thenReturn(List.of(strict));
        when(events.findByActiveKey(any())).thenReturn(Optional.empty());

        service.evaluateNewObservation(observation(city, 100, now));

        verify(events, never()).save(any());
    }

    private AlertRule rule(City city, AlertOperator operator, int threshold, boolean enabled) {
        return new AlertRule("AQI warning", city, AlertMetric.AQI, operator, BigDecimal.valueOf(threshold), "HIGH", enabled, now);
    }

    private AirQualityObservation observation(City city, int aqi, Instant observedAt) {
        return new AirQualityObservation(city, new AirQualityReading(observedAt, "TEST", aqi,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), observedAt);
    }
}
