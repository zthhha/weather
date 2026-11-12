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
        if (properties.qweather().jwtToken() == null || properties.qweather().jwtToken().isBlank()) {
            throw new AirQualityProviderException("QWeather JWT token is not configured");
        }
        if (city.getLatitude() == null || city.getLongitude() == null) {
            throw new AirQualityProviderException("City coordinates are not configured: " + city.getName());
        }
        try {
            String body = client.get()
                    .uri("/airquality/v1/current/{latitude}/{longitude}", city.getLatitude(), city.getLongitude())
                    .header("Authorization", "Bearer " + properties.qweather().jwtToken())
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
            JsonNode indexes = root.path("indexes");
            if (!indexes.isArray() || indexes.isEmpty()) {
                throw new AirQualityProviderException("QWeather response did not contain air quality indexes");
            }
            JsonNode index = indexes.get(0);
            JsonNode pollutants = root.path("pollutants");
            return new AirQualityReading(
                    Instant.now(), SOURCE, index.path("aqi").asInt(), pollutant(pollutants, "pm2p5"), pollutant(pollutants, "pm10"),
                    pollutant(pollutants, "so2"), pollutant(pollutants, "no2"), pollutant(pollutants, "co"), pollutant(pollutants, "o3"));
        } catch (AirQualityProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AirQualityProviderException("Invalid QWeather response", exception);
        }
    }

    private BigDecimal pollutant(JsonNode pollutants, String code) {
        if (!pollutants.isArray()) return null;
        for (JsonNode pollutant : pollutants) {
            if (code.equals(pollutant.path("code").asText())) {
                String value = pollutant.path("concentration").path("value").asText();
                if (!value.isBlank()) return new BigDecimal(value);
            }
        }
        return null;
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
