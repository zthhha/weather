package com.tlias.weather.airquality;

import com.tlias.weather.alert.AlertEvaluationService;
import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AirQualityCollectionService {
    private final CityRepository cities;
    private final AirQualityProvider provider;
    private final AirQualityObservationRepository observations;
    private final CollectionJobLogRepository jobLogs;
    private final AlertEvaluationService alertEvaluationService;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public AirQualityCollectionService(CityRepository cities, AirQualityProvider provider,
                                       AirQualityObservationRepository observations, CollectionJobLogRepository jobLogs,
                                       AlertEvaluationService alertEvaluationService) {
        this(cities, provider, observations, jobLogs, alertEvaluationService, Clock.systemUTC());
    }

    AirQualityCollectionService(CityRepository cities, AirQualityProvider provider,
                                AirQualityObservationRepository observations, CollectionJobLogRepository jobLogs,
                                AlertEvaluationService alertEvaluationService, Clock clock) {
        this.cities = cities;
        this.provider = provider;
        this.observations = observations;
        this.jobLogs = jobLogs;
        this.alertEvaluationService = alertEvaluationService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.collection.fixed-delay}")
    public void scheduledCollection() {
        collectAllEnabledCities();
    }

    @Transactional
    public CollectionJobLog collectAllEnabledCities() {
        List<City> enabledCities = cities.findAll().stream().filter(City::isEnabled).toList();
        Instant startedAt = clock.instant();
        CollectionJobLog jobLog = jobLogs.save(new CollectionJobLog(startedAt, enabledCities.size()));
        int successes = 0;
        int failures = 0;
        StringBuilder errors = new StringBuilder();
        for (City city : enabledCities) {
            try {
                AirQualityReading reading = provider.fetchCurrent(city);
                persistIfNew(city, reading, clock.instant());
                successes++;
            } catch (RuntimeException exception) {
                failures++;
                if (!errors.isEmpty()) errors.append("; ");
                errors.append(city.getName()).append(": ").append(exception.getMessage());
            }
        }
        jobLog.finish(successes, failures, errors.isEmpty() ? null : errors.substring(0, Math.min(errors.length(), 1000)), clock.instant());
        return jobLogs.save(jobLog);
    }

    private void persistIfNew(City city, AirQualityReading reading, Instant collectedAt) {
        observations.findByCityAndObservedAtAndSource(city, reading.observedAt(), reading.source())
                .orElseGet(() -> {
                    AirQualityObservation observation = observations.save(new AirQualityObservation(city, reading, collectedAt));
                    alertEvaluationService.evaluateNewObservation(observation);
                    return observation;
                });
    }
}
