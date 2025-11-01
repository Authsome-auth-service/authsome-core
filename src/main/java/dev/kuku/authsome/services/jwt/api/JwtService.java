package dev.kuku.authsome.services.jwt.api;

import dev.kuku.authsome.services.jwt.api.dto.ParsedToken;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface JwtService {
    String generateToken(String subject, Map<String, String> claims, String issuer, int expiry, TimeUnit expiryUnit);

    ParsedToken parseToken(String accessToken);
}
