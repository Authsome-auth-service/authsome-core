package dev.kuku.authsome.services.tenant.api;

import dev.kuku.authsome.services.tenant.api.model.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.model.IdentityType;

/**
 * Service interface for tenant management operations.
 * <p>
 * Provides methods to query and manage tenant accounts in the system.
 */
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
}