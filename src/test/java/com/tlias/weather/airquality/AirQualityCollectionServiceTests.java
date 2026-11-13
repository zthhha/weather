package com.tlias.weather.airquality;

import com.tlias.weather.alert.AlertEvaluationService;
import com.tlias.weather.city.City;
import com.tlias.weather.city.CityLocationResolver;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.city.ResolvedCity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirQualityCollectionServiceTests {
    private final CityRepository cities = mock(CityRepository.class);
    private final CityLocationResolver cityLocationResolver = mock(CityLocationResolver.class);
    private final AirQualityProvider provider = mock(AirQualityProvider.class);
    private final AirQualityObservationRepository observations = mock(AirQualityObservationRepository.class);
    private final CollectionJobLogRepository logs = mock(CollectionJobLogRepository.class);
    private final AlertEvaluationService alertEvaluationService = mock(AlertEvaluationService.class);
    private final Instant now = Instant.parse("2026-11-11T00:00:00Z");
    private final AirQualityCollectionService service = new AirQualityCollectionService(cities, cityLocationResolver, provider, observations, logs,
            alertEvaluationService, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void recordsPartialSuccessAndContinuesAfterCityFailure() {
        City successfulCity = city("Beijing");
        City failingCity = city("Shanghai");
        when(cities.findAll()).thenReturn(List.of(successfulCity, failingCity));
        when(logs.save(any(CollectionJobLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AirQualityReading reading = reading();
        when(provider.fetchCurrent(successfulCity)).thenReturn(reading);
        when(provider.fetchCurrent(failingCity)).thenThrow(new AirQualityProviderException("temporary outage"));
        when(observations.findByCityAndObservedAtAndSource(successfulCity, reading.observedAt(), reading.source()))
                .thenReturn(Optional.empty());
        when(observations.save(any(AirQualityObservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CollectionJobLog result = service.collectAllEnabledCities();

        assertThat(result.getStatus()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(result.getSuccessfulCities()).isEqualTo(1);
        assertThat(result.getFailedCities()).isEqualTo(1);
        verify(observations).save(any(AirQualityObservation.class));
        verify(alertEvaluationService).evaluateNewObservation(any(AirQualityObservation.class));
        verifyNoInteractions(cityLocationResolver);
    }

    @Test
    void resolvesAndPersistsCoordinatesBeforeCollectingHistoricalCity() {
        City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
        ResolvedCity resolved = new ResolvedCity("101010100", "Asia/Shanghai", new BigDecimal("39.90"), new BigDecimal("116.40"));
        AirQualityReading reading = reading();
        when(cities.findAll()).thenReturn(List.of(city));
        when(logs.save(any(CollectionJobLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cityLocationResolver.resolve("Beijing")).thenReturn(resolved);
        when(provider.fetchCurrent(city)).thenReturn(reading);
        when(observations.findByCityAndObservedAtAndSource(city, reading.observedAt(), reading.source())).thenReturn(Optional.empty());
        when(observations.save(any(AirQualityObservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CollectionJobLog result = service.collectAllEnabledCities();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(city.getLatitude()).isEqualByComparingTo("39.90");
        assertThat(city.getLongitude()).isEqualByComparingTo("116.40");
        verify(cities).save(city);
        verify(provider).fetchCurrent(city);
    }

    @Test
    void doesNotPersistExistingObservationAgain() {
        City city = city("Beijing");
        AirQualityReading reading = reading();
        when(cities.findAll()).thenReturn(List.of(city));
        when(logs.save(any(CollectionJobLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(provider.fetchCurrent(city)).thenReturn(reading);
        when(observations.findByCityAndObservedAtAndSource(city, reading.observedAt(), reading.source()))
                .thenReturn(Optional.of(mock(AirQualityObservation.class)));

        CollectionJobLog result = service.collectAllEnabledCities();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        verify(observations, never()).save(any());
        verifyNoInteractions(alertEvaluationService);
    }

    private City city(String name) {
        return new City(name, "101010100", "Asia/Shanghai", true, new BigDecimal("39.90"), new BigDecimal("116.40"));
    }

    private AirQualityReading reading() {
        return new AirQualityReading(now, "QWEATHER", 88,
                new BigDecimal("35"), new BigDecimal("50"), new BigDecimal("4"), new BigDecimal("20"),
                new BigDecimal("0.8"), new BigDecimal("90"));
    }
}
