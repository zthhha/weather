package com.tlias.weather.qweather;

import com.tlias.weather.config.AppProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class QWeatherTokenProvider {
    private static final String ALGORITHM = "EdDSA";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final java.time.Duration CLOCK_SKEW = java.time.Duration.ofSeconds(30);
    private static final java.time.Duration MAX_TOKEN_VALIDITY = java.time.Duration.ofHours(24);

    private final AppProperties.QWeather properties;
    private final Clock clock;
    private volatile PrivateKey privateKey;
    private volatile CachedToken cachedToken;

    @org.springframework.beans.factory.annotation.Autowired
    public QWeatherTokenProvider(AppProperties properties) {
        this(properties.qweather(), Clock.systemUTC());
    }

    QWeatherTokenProvider(AppProperties.QWeather properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String token() {
        Instant now = clock.instant();
        CachedToken current = cachedToken;
        if (current == null || current.shouldRefreshAtOrBefore(now, properties.refreshThreshold())) {
            synchronized (this) {
                current = cachedToken;
                if (current == null || current.shouldRefreshAtOrBefore(now, properties.refreshThreshold())) {
                    current = createToken(now);
                    cachedToken = current;
                }
            }
        }
        return current.value();
    }

    public String authorizationHeader() {
        return BEARER_PREFIX + token();
    }

    private CachedToken createToken(Instant now) {
        validateConfiguration();
        PrivateKey signingKey = privateKey;
        if (signingKey == null) {
            synchronized (this) {
                if (privateKey == null) privateKey = decodePrivateKey(properties.privateKey());
                signingKey = privateKey;
            }
        }
        Instant issuedAt = now.minus(CLOCK_SKEW);
        Instant expiresAt = issuedAt.plus(properties.tokenValidity());
        String token = Jwts.builder()
                .header().keyId(properties.keyId()).and()
                .issuer(properties.issuer())
                .subject(properties.subject())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.EdDSA)
                .compact();
        return new CachedToken(token, expiresAt);
    }

    private PrivateKey decodePrivateKey(String encodedKey) {
        try {
            byte[] der = Base64.getDecoder().decode(encodedKey);
            return java.security.KeyFactory.getInstance("Ed25519")
                    .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(der));
        } catch (Exception exception) {
            throw new IllegalStateException("QWeather private key must be a Base64-encoded PKCS#8 Ed25519 private key", exception);
        }
    }

    private void validateConfiguration() {
        requireText(properties.keyId(), "key-id");
        requireText(properties.issuer(), "issuer");
        requireText(properties.subject(), "subject");
        requireText(properties.privateKey(), "private-key");
        if (properties.tokenValidity().isNegative() || properties.tokenValidity().isZero()
                || properties.tokenValidity().compareTo(MAX_TOKEN_VALIDITY) > 0) {
            throw new IllegalStateException("QWeather token-validity must be greater than zero and no longer than PT24H");
        }
        if (properties.refreshThreshold().isNegative() || properties.refreshThreshold().compareTo(properties.tokenValidity()) >= 0) {
            throw new IllegalStateException("QWeather refresh-threshold must be non-negative and shorter than token-validity");
        }
    }

    private void requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("QWeather " + propertyName + " must be configured");
        }
    }

    private record CachedToken(String value, Instant expiresAt) {
        private boolean shouldRefreshAtOrBefore(Instant now, java.time.Duration refreshThreshold) {
            return !expiresAt.isAfter(now.plus(refreshThreshold));
        }
    }
}
