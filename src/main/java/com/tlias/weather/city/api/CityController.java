package com.tlias.weather.city.api;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {
    private final CityRepository cities;

    public CityController(CityRepository cities) {
        this.cities = cities;
    }

    @GetMapping
    public ApiResponse<List<CityResponse>> list() {
        return ApiResponse.of(cities.findAll().stream().map(CityResponse::from).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CityResponse> create(@Valid @RequestBody CityRequest request) {
        City city = cities.save(new City(request.name(), request.locationId(), request.timezone(), request.enabled()));
        return ApiResponse.of(CityResponse.from(city));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CityResponse> update(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        City city = cities.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
        city.update(request.name(), request.locationId(), request.timezone(), request.enabled());
        return ApiResponse.of(CityResponse.from(cities.save(city)));
    }
}
