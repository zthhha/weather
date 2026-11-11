package com.tlias.weather.city.api;

import com.tlias.weather.city.City;

public record CityResponse(Long id, String name, String locationId, String timezone, boolean enabled) {
    public static CityResponse from(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getLocationId(), city.getTimezone(), city.isEnabled());
    }
}
