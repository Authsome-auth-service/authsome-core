package dev.kuku.authsome.services.tenant.api;

import dev.kuku.authsome.services.tenant.api.model.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.model.FetchedTenantIdentity;
import dev.kuku.authsome.services.tenant.api.model.IdentityType;

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
}