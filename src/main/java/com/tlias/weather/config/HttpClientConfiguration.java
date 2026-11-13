package com.tlias.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlias.weather.qweather.QWeatherTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfiguration {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    ClientHttpRequestInterceptor qWeatherAuthorizationInterceptor(QWeatherTokenProvider tokenProvider) {
        return (request, body, execution) -> {
            request.getHeaders().setBearerAuth(tokenProvider.token());
            return execution.execute(request, body);
        };
    }
}
