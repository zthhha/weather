package com.tlias.weather.city.api;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityLocationResolver;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.city.ResolvedCity;
import com.tlias.weather.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {
    private final CityRepository cities;
    private final CityLocationResolver cityLocationResolver;

    public CityController(CityRepository cities, CityLocationResolver cityLocationResolver) {
        this.cities = cities;
        this.cityLocationResolver = cityLocationResolver;
    }

    @GetMapping
    public ApiResponse<List<CityResponse>> list() {
        return ApiResponse.of(cities.findAll().stream().map(CityResponse::from).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CityResponse> create(@Valid @RequestBody CityRequest request) {
        ResolvedCity resolved = cityLocationResolver.resolve(request.name());
        City city = cities.save(new City(request.name(), resolved.locationId(), resolved.timezone(), request.enabled(), resolved.latitude(), resolved.longitude()));
        return ApiResponse.of(CityResponse.from(city));
    }

    @PutMapping("/{id}")
    public ApiResponse<CityResponse> update(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        City city = cities.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
        ResolvedCity resolved = cityLocationResolver.resolve(request.name());
        city.update(request.name(), resolved.locationId(), resolved.timezone(), request.enabled(), resolved.latitude(), resolved.longitude());
        return ApiResponse.of(CityResponse.from(cities.save(city)));
    }
}
