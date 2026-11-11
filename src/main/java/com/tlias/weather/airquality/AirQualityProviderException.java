package com.tlias.weather.airquality;

public class AirQualityProviderException extends RuntimeException {
    public AirQualityProviderException(String message) { super(message); }
    public AirQualityProviderException(String message, Throwable cause) { super(message, cause); }
}
