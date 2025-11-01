package dev.kuku.authsome.services.jwt.impl;

import dev.kuku.authsome.services.jwt.api.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public String generateToken(String subject, Map<String, String> claims, String issuer, int expiry, TimeUnit expiryUnit) {
        try {
            long expiryMillis = expiryUnit.toMillis(expiry);
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiryMillis);

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            return Jwts.builder()
                    .subject(subject)
                    .claims(claims)
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating JWT token for subject: {}", subject, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}