package dev.kuku.authsome.services.jwt.api.dto;

import java.util.Map;

public record ParsedToken(String subject, String issuer, long issuedAt, boolean expired, Map<String, String> claims) {
}
