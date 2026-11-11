package com.tlias.weather.airquality.api;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AirQualityObservationRepository;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/air-quality")
public class AirQualityController {
    private static final Duration MAX_HISTORY_RANGE = Duration.ofDays(7);

    private final AirQualityObservationRepository observations;
    private final CityRepository cities;

    public AirQualityController(AirQualityObservationRepository observations, CityRepository cities) {
        this.observations = observations;
        this.cities = cities;
    }

    @GetMapping("/cities/{cityId}/latest")
    public ApiResponse<AirQualityObservationResponse> latest(@PathVariable Long cityId) {
        requireCity(cityId);
        AirQualityObservation observation = observations.findFirstByCityIdOrderByObservedAtDesc(cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No observation for this city"));
        return ApiResponse.of(AirQualityObservationResponse.from(observation));
    }

    @GetMapping("/cities/{cityId}/history")
    public ApiResponse<List<AirQualityObservationResponse>> history(
            @PathVariable Long cityId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        requireCity(cityId);
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to must not be before from");
        }
        if (Duration.between(from, to).compareTo(MAX_HISTORY_RANGE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "history range must not exceed 7 days");
        }
        List<AirQualityObservationResponse> results = observations
                .findByCityIdAndObservedAtBetweenOrderByObservedAtAsc(cityId, from, to)
                .stream().map(AirQualityObservationResponse::from).toList();
        return ApiResponse.of(results);
    }

    private void requireCity(Long cityId) {
        if (!cities.existsById(cityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found");
        }
    }
}
