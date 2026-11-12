package com.tlias.weather.city.api;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityLocationResolver;
import com.tlias.weather.city.CityRepository;
import com.tlias.weather.city.ResolvedCity;
import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.security.User;
import com.tlias.weather.security.UserRepository;
import com.tlias.weather.subscription.UserSubscription;
import com.tlias.weather.subscription.UserSubscriptionRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/city-search")
public class CitySearchController {
    private final CityRepository cities;
    private final CityLocationResolver resolver;
    private final UserRepository users;
    private final UserSubscriptionRepository subscriptions;

    public CitySearchController(CityRepository cities, CityLocationResolver resolver, UserRepository users, UserSubscriptionRepository subscriptions) {
        this.cities = cities;
        this.resolver = resolver;
        this.users = users;
        this.subscriptions = subscriptions;
    }

    @GetMapping
    public ApiResponse<List<CityResponse>> search(@RequestParam @NotBlank String keyword) {
        List<City> local = cities.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
        if (!local.isEmpty()) return ApiResponse.of(local.stream().map(CityResponse::from).toList());
        ResolvedCity resolved = resolver.resolve(keyword.trim());
        City city = cities.findByName(keyword.trim())
                .orElseGet(() -> cities.save(new City(keyword.trim(), resolved.locationId(), resolved.timezone(), false, resolved.latitude(), resolved.longitude())));
        return ApiResponse.of(List.of(CityResponse.from(city)));
    }

    @GetMapping("/subscribe")
    public ApiResponse<CityResponse> searchAndSubscribe(@RequestParam @NotBlank String keyword, Authentication authentication) {
        City city = search(keyword).data().stream().findFirst()
                .flatMap(response -> cities.findById(response.id()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
        User user = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        UserSubscription subscription = subscriptions.findByUserIdAndCityId(user.getId(), city.getId())
                .orElseGet(() -> new UserSubscription(user, city, "MEDIUM", true));
        subscription.update("MEDIUM", true);
        subscriptions.save(subscription);
        return ApiResponse.of(CityResponse.from(city));
    }
}
