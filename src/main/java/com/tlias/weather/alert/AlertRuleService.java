package com.tlias.weather.alert;

import com.tlias.weather.alert.api.AlertRuleRequest;
import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;

@Service
public class AlertRuleService {
    private final AlertRuleRepository rules;
    private final CityRepository cities;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public AlertRuleService(AlertRuleRepository rules, CityRepository cities) {
        this(rules, cities, Clock.systemUTC());
    }

    AlertRuleService(AlertRuleRepository rules, CityRepository cities, Clock clock) {
        this.rules = rules;
        this.cities = cities;
        this.clock = clock;
    }

    @Transactional
    public AlertRule create(AlertRuleRequest request) {
        City city = resolveCity(request.cityId());
        return rules.save(new AlertRule(request.name(), city, request.metric(), request.operator(), request.threshold(),
                request.severity(), request.enabled(), clock.instant()));
    }

    private City resolveCity(Long cityId) {
        if (cityId == null) {
            return null;
        }
        return cities.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
    }
}
