package com.tlias.weather.subscription.api;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.notification.NotificationService;
import com.tlias.weather.security.User;
import com.tlias.weather.security.UserRepository;
import com.tlias.weather.subscription.UserSubscription;
import com.tlias.weather.subscription.UserSubscriptionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/subscriptions")
public class SubscriptionController {
    private final UserRepository users;
    private final CityRepository cities;
    private final UserSubscriptionRepository subscriptions;
    private final NotificationService notificationService;

    public SubscriptionController(UserRepository users, CityRepository cities, UserSubscriptionRepository subscriptions,
                                  NotificationService notificationService) {
        this.users = users;
        this.cities = cities;
        this.subscriptions = subscriptions;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<SubscriptionResponse>> list(Authentication authentication) {
        return ApiResponse.of(subscriptions.findByUserOrderByIdDesc(currentUser(authentication)).stream()
                .map(SubscriptionResponse::from).toList());
    }

    @PutMapping("/{cityId}")
    public ApiResponse<SubscriptionResponse> put(@PathVariable Long cityId, @Valid @RequestBody SubscriptionRequest request,
                                                  Authentication authentication) {
        User user = currentUser(authentication);
        City city = cities.findById(cityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
        if (request.enabled() && !city.isEnabled()) {
            city.enable();
            cities.save(city);
        }
        UserSubscription subscription = subscriptions.findByUserIdAndCityId(user.getId(), cityId)
                .orElseGet(() -> new UserSubscription(user, city, request.minimumSeverity(), request.enabled()));
        subscription.update(request.minimumSeverity(), request.enabled());
        UserSubscription saved = subscriptions.save(subscription);
        if (saved.isEnabled()) notificationService.notifyUserOfActiveEvents(user, city);
        return ApiResponse.of(SubscriptionResponse.from(saved));
    }

    @DeleteMapping("/{cityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long cityId, Authentication authentication) {
        UserSubscription subscription = subscriptions.findByUserIdAndCityId(currentUser(authentication).getId(), cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        subscriptions.delete(subscription);
    }

    private User currentUser(Authentication authentication) {
        return users.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
