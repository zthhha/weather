package com.tlias.weather.city;

import java.math.BigDecimal;

public record ResolvedCity(String locationId, String timezone, BigDecimal latitude, BigDecimal longitude) {
}
