package dev.kuku.authsome.model;

import dev.kuku.authsome.services.tenant.api.model.IdentityType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request model for tenant signup.
 * <p>
 * Contains the necessary information to initiate a new tenant registration,
 * including identity verification details and account credentials.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignupTenantRequest {
    /**
     * The type of identity being used for verification (e.g., EMAIL, PHONE)
     */
    public IdentityType identityType;

    /**
     * The identity value (email address or phone number) for verification
     */
    public String identity;

    /**
     * The desired username for the tenant account
     */
    public String username;

    /**
     * The password for the tenant account
     */
    public String password;
}