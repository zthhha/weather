package com.tlias.weather.security.api;

import java.util.List;

public record LoginResponse(String accessToken, String tokenType, List<String> roles) {
}
