package com.tlias.weather.airquality;

import com.tlias.weather.city.City;
import com.tlias.weather.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class QWeatherAirQualityProvider implements AirQualityProvider {
    private static final String SOURCE = "QWEATHER";
    private final RestClient client;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public QWeatherAirQualityProvider(RestClient.Builder builder, AppProperties properties, ObjectMapper objectMapper) {
        this.client = builder.baseUrl(properties.qweather().baseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AirQualityReading fetchCurrent(City city) {
        if (properties.qweather().apiKey() == null || properties.qweather().apiKey().isBlank()) {
            throw new AirQualityProviderException("QWeather API key is not configured");
        }
        try {
            String body = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/v7/air/now")
                            .queryParam("location", city.getLocationId())
                            .queryParam("key", properties.qweather().apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AirQualityProviderException("QWeather returned HTTP " + response.getStatusCode().value());
                    })
                    .body(String.class);
            return parse(body);
        } catch (RestClientResponseException exception) {
            throw new AirQualityProviderException("QWeather request failed with HTTP " + exception.getStatusCode().value(), exception);
        } catch (AirQualityProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AirQualityProviderException("Unable to retrieve QWeather air quality data", exception);
        }
    }

    private AirQualityReading parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!"200".equals(root.path("code").asText())) {
                throw new AirQualityProviderException("QWeather returned business code " + root.path("code").asText());
            }
            JsonNode now = root.path("now");
            if (now.isMissingNode()) {
                throw new AirQualityProviderException("QWeather response did not contain current air quality");
            }
            return new AirQualityReading(
                    Instant.now(), SOURCE, integer(now, "aqi"), decimal(now, "pm2p5"), decimal(now, "pm10"),
                    decimal(now, "so2"), decimal(now, "no2"), decimal(now, "co"), decimal(now, "o3"));
        } catch (AirQualityProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AirQualityProviderException("Invalid QWeather response", exception);
        }
    }

    private int integer(JsonNode node, String field) {
        try { return Integer.parseInt(required(node, field)); }
        catch (NumberFormatException e) { throw new AirQualityProviderException("Invalid QWeather field: " + field, e); }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        try { return new BigDecimal(required(node, field)); }
        catch (NumberFormatException e) { throw new AirQualityProviderException("Invalid QWeather field: " + field, e); }
    }

    private String required(JsonNode node, String field) {
        String value = node.path(field).asText();
        if (value.isBlank()) throw new AirQualityProviderException("Missing QWeather field: " + field);
        return value;
    }
}
