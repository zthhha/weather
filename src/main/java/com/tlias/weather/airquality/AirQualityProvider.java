package com.tlias.weather.airquality;

import com.tlias.weather.city.City;

public interface AirQualityProvider {
    AirQualityReading fetchCurrent(City city);
}
