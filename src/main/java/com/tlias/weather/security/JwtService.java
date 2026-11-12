package com.tlias.weather.security;

import com.tlias.weather.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey key;
    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Role::getCode).sorted().toList();
        return Jwts.builder().subject(user.getUsername()).claim("roles", roles)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(properties.jwt().expiration())))
                .signWith(key).compact();
    }

    public String usernameFrom(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
