package com.tlias.weather.subscription.api;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionRequest(@NotBlank String minimumSeverity, boolean enabled) {
}
