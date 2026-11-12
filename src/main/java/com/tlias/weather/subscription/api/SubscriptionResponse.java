package com.tlias.weather.subscription.api;

import com.tlias.weather.subscription.UserSubscription;

public record SubscriptionResponse(Long id, Long cityId, String cityName, String minimumSeverity, boolean enabled) {
    public static SubscriptionResponse from(UserSubscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getCity().getId(), subscription.getCity().getName(),
                subscription.getMinimumSeverity(), subscription.isEnabled());
    }
}
