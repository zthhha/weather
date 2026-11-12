package com.tlias.weather.city.api;

import jakarta.validation.constraints.NotBlank;

public record CityRequest(@NotBlank String name, boolean enabled) {
}
