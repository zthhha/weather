package com.tlias.weather.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull QWeather qweather,
        @Valid @NotNull Collection collection
) {
    public record Jwt(@NotBlank String secret, @NotNull Duration expiration) {
    }

    public record QWeather(
            @NotBlank String baseUrl,
            @NotBlank String geoBaseUrl,
            String jwtToken,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout
    ) {
    }

    public record Collection(@NotNull Boolean enabled, @NotNull Duration fixedDelay) {
    }
}
