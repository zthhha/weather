package com.tlias.weather.city;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlias.weather.airquality.AirQualityProviderException;
import com.tlias.weather.config.AppProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class QWeatherCityLocationResolver implements CityLocationResolver {
    private final RestClient client;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public QWeatherCityLocationResolver(RestClient.Builder builder, AppProperties properties, ObjectMapper objectMapper) {
        this.client = builder.baseUrl(properties.qweather().baseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResolvedCity resolve(String cityName) {
        if (properties.qweather().apiKey() == null || properties.qweather().apiKey().isBlank()) {
            throw new AirQualityProviderException("QWeather API key is not configured");
        }
        try {
            String body = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/geo/v2/city/lookup")
                            .queryParam("location", cityName)
                            .queryParam("key", properties.qweather().apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AirQualityProviderException("QWeather city lookup returned HTTP " + response.getStatusCode().value());
                    })
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            if (!"200".equals(root.path("code").asText())) {
                throw new AirQualityProviderException("QWeather city lookup returned business code " + root.path("code").asText());
            }
            JsonNode location = root.path("location");
            if (!location.isArray() || location.isEmpty()) {
                throw new AirQualityProviderException("City not found in QWeather: " + cityName);
            }
            JsonNode first = location.get(0);
            String id = required(first, "id");
            String timezone = first.path("tz").asText("Asia/Shanghai");
            return new ResolvedCity(id, timezone);
        } catch (AirQualityProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AirQualityProviderException("Unable to resolve city through QWeather", exception);
        }
    }

    private String required(JsonNode node, String field) {
        String value = node.path(field).asText();
        if (value.isBlank()) {
            throw new AirQualityProviderException("QWeather city lookup missing field: " + field);
        }
        return value;
    }
}
