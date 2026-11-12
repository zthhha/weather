package com.tlias.weather.common;

import com.tlias.weather.airquality.AirQualityProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AirQualityProviderException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    ApiResponse<ErrorResponse> handleAirQualityProvider(AirQualityProviderException exception) {
        return ApiResponse.of(new ErrorResponse("UPSTREAM_SERVICE_ERROR", exception.getMessage()));
    }

    public record ErrorResponse(String code, String message) {
    }
}
