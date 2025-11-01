package dev.kuku.authsome.services.tenant.api;

import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenantIdentity;
import dev.kuku.authsome.services.tenant.api.dto.IdentityType;
import dev.kuku.authsome.services.tenant.api.dto.TenantAndRefreshToken;

import java.util.Map;

/**
 * Service interface for tenant management operations.
 * <p>
 * Provides methods to query and manage tenant accounts in the system.
 */
@SuppressWarnings("UnusedReturnValue")
public interface TenantService {

    /**
     * Retrieves a tenant by their identity (email or phone number).
     *
     * @param identityType the type of identity to search by
     * @param identity     the identity value to search for
     * @return the tenant if found, null otherwise
     */
    FetchedTenant getTenantByIdentity(IdentityType identityType, String identity);

    /**
     * Retrieves a tenant by their username.
     *
     * @param username the username to search for
     * @return the tenant if found, null otherwise
     */
    FetchedTenant getTenantByUsername(String username);

    /**
     * Creates a new tenant with the specified username and password.
     *
     * @param username    the username for the new tenant
     * @param rawPassword the raw password for the new tenant
     * @return the created tenant
     */
    FetchedTenant createTenant(String username, String rawPassword);

    /**
     * Adds an identity (such as email or username) for the specified tenant.
     *
     * @param tenantId     the unique identifier of the tenant
     * @param identityType the type of identity to add
     * @param identity     the identity value to add
     * @return the created tenant identity record
     */
    FetchedTenantIdentity addIdentityForTenant(String tenantId, IdentityType identityType, String identity);

    boolean validateTenantCredentials(String tenantId, String rawPassword);

    /**
     * Create and persist a new session for the specified tenant. Should keep track of session expiration internally.
     *
     * @param tenantId id of the tenant
     * @return id of the created session
     */
    String createTenantRefreshToken(String tenantId, Map<String, Object> metadata);

    /**
     * Get tenant and refresh token info by refresh token. Rotate token if required.
     *
     * @param refreshToken current refresh token
     * @return tenant and refresh token info
     */
    TenantAndRefreshToken refreshToken(String refreshToken);

    /**
     * Revoke the specified refresh token, making it invalid for future use.
     *
     * @param refreshToken refresh token to invalidate
     */
    void revokeTenantRefreshToken(String refreshToken);

    /**
     * Generate an API key for the specified tenant. This will represent the tenant in API calls.
     *
     * @param tenantId id of the tenant
     * @return api key string
     */
    String generateAPIKeyForTenant(String tenantId);

    /**
     * Get tenant by API key.
     *
     * @param apiKey api key string
     * @return the tenant if found, null otherwise
     */
    FetchedTenant getTenantByApiKey(String apiKey);
}