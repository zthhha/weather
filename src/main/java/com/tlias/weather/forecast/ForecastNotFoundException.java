package com.tlias.weather.forecast;

public class ForecastNotFoundException extends RuntimeException {
    public ForecastNotFoundException(String message) {
        super(message);
    }
}
