package com.tlias.weather.forecast.api;

import com.tlias.weather.city.CityRepository;
import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.forecast.AqiForecastRepository;
import com.tlias.weather.forecast.ForecastService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forecasts")
public class ForecastController {
    private final ForecastService forecastService;
    private final AqiForecastRepository forecasts;
    private final CityRepository cities;

    public ForecastController(ForecastService forecastService, AqiForecastRepository forecasts, CityRepository cities) {
        this.forecastService = forecastService;
        this.forecasts = forecasts;
        this.cities = cities;
    }

    @PostMapping("/cities/{cityId}/regenerate")
    public ApiResponse<ForecastService.ForecastGenerationResult> regenerate(@PathVariable Long cityId) {
        return ApiResponse.of(forecastService.regenerate(cityId));
    }

    @GetMapping("/cities/{cityId}")
    public ApiResponse<List<AqiForecastResponse>> list(@PathVariable Long cityId) {
        if (!cities.existsById(cityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found");
        }
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        List<AqiForecastResponse> result = upcomingForecasts(cityId, now);
        if (result.size() < 24) {
            forecastService.regenerate(cityId);
            result = upcomingForecasts(cityId, now);
        }
        return ApiResponse.of(result);
    }

    private List<AqiForecastResponse> upcomingForecasts(Long cityId, Instant now) {
        return forecasts.findByCityIdAndForecastAtBetweenOrderByForecastAtAsc(cityId, now, now.plus(24, ChronoUnit.HOURS))
                .stream().map(AqiForecastResponse::from).toList();
    }
}
