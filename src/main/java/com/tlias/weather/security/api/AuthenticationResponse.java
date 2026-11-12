package com.tlias.weather.security.api;

import java.util.List;

public record AuthenticationResponse(String accessToken, String tokenType, List<String> roles) {
}
