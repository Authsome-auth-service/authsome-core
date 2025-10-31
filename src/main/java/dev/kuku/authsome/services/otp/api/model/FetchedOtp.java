package dev.kuku.authsome.services.otp.api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * Model representing a fetched OTP from the data store.
 * <p>
 * Contains the OTP details including the code, context, expiry time,
 * and any associated metadata.
 */
@AllArgsConstructor
@ToString
@NoArgsConstructor
public final class FetchedOtp {
    /** The unique identifier of the OTP */
    public String id;

    /** The actual OTP code that was generated */
    public String code;

    /** The context identifier for this OTP (e.g., "TENANT_SIGNUP") */
    public String context;

    /** The timestamp when this OTP expires (in milliseconds since epoch) */
    public long expiresAt;

    /** Additional metadata associated with this OTP */
    public Map<String, Object> metadata;
}