package dev.kuku.authsome.services.tenant.api.dto;

/**
 * Record representing a tenant's identity information.
 * <p>
 * Contains the identity type and value associated with a tenant.
 *
 * @param id           the unique identifier for this identity record
 * @param tenantId     the associated tenant's unique identifier
 * @param identityType the type of identity (e.g., EMAIL, USERNAME)
 * @param identity     the identity value (e.g., email address, username)
 */
public record FetchedTenantIdentity(String id, String tenantId, IdentityType identityType, String identity) {
}
