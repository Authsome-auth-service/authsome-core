package dev.kuku.authsome.services.tenant.api.dto;

/**
 * Record representing a fetched tenant from the data store.
 * <p>
 * Contains basic tenant information including identity and audit timestamps.
 *
 * @param id        the unique identifier of the tenant
 * @param username  the username of the tenant
 * @param createdAt the timestamp when the tenant was created (in milliseconds since epoch)
 * @param updatedAt the timestamp when the tenant was last updated (in milliseconds since epoch)
 */
public record FetchedTenant(String id, String username, long createdAt, long updatedAt) {
}