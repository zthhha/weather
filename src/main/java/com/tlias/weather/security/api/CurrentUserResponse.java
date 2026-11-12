package com.tlias.weather.security.api;

import java.util.List;

public record CurrentUserResponse(Long id, String username, List<String> roles) {
}
