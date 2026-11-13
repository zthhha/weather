package com.tlias.weather.qweather;

import com.tlias.weather.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class QWeatherTokenProviderTests {
    private static final Instant NOW = Instant.parse("2026-11-13T12:00:00Z");

    @Test
    void createsEdDsaTokenWithRequiredQWeatherClaims() throws Exception {
        KeyPair keyPair = ed25519KeyPair();
        QWeatherTokenProvider provider = provider(keyPair, Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofHours(24), Duration.ofHours(1));

        String token = provider.token();
        var jws = Jwts.parser().verifyWith(keyPair.getPublic()).build().parseSignedClaims(token);
        Header header = jws.getHeader();
        Claims claims = jws.getPayload();

        assertThat(header.getAlgorithm()).isEqualTo("EdDSA");
        assertThat(header.get("kid")).isEqualTo("test-key-id");
        assertThat(claims.getIssuer()).isEqualTo("Q123456789");
        assertThat(claims.getSubject()).isEqualTo("TESTPROJECT");
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW.minusSeconds(30));
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plus(Duration.ofHours(24)).minusSeconds(30));
    }

    @Test
    void reusesCachedTokenWhileItRemainsOutsideRefreshThreshold() throws Exception {
        KeyPair keyPair = ed25519KeyPair();
        QWeatherTokenProvider provider = provider(keyPair, Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofHours(24), Duration.ofHours(1));

        assertThat(provider.token()).isEqualTo(provider.token());
        assertThat(provider.authorizationHeader()).startsWith("Bearer ");
    }

    private QWeatherTokenProvider provider(KeyPair keyPair, Clock clock, Duration validity, Duration threshold) {
        return new QWeatherTokenProvider(new AppProperties.QWeather(
                "https://example.test", "https://example.test", "test-key-id", "Q123456789", "TESTPROJECT",
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()), validity, threshold,
                Duration.ofSeconds(5), Duration.ofSeconds(10)), clock);
    }

    private KeyPair ed25519KeyPair() throws Exception {
        return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    }
}
