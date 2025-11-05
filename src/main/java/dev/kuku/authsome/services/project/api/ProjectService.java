package dev.kuku.authsome.services.project.api;

import dev.kuku.authsome.services.project.api.model.*;

public interface ProjectService {
    /**
     * Create a new project for a tenant
     *
     * @param tenantId    id of the tenant
     * @param projectName name of the project
     * @param projectRule rule of the project
     * @return created project
     */
    FetchedTenantProject createProjectForTenant(String tenantId, String projectName, ProjectRule projectRule);

    /**
     * Create a new user for the given project id
     *
     * @param tenantId  id of the tenant
     * @param projectId id of the project
     * @param username  unique username
     * @param password  optional password for "sign-in"
     * @return created user
     */
    FetchedProjectUser createProjectUser(String tenantId, String projectId, String username, String password);

    /**
     * Add verified id for the user
     *
     * @param tenantId     id of the tenant doing the add operation
     * @param projectId    id of the project
     * @param userId       userid of the user the identity is being added to
     * @param identity     identity value
     * @param identityType identity type
     * @param options      options for the identity. Should verify against configs set for the project or user
     * @return created identity
     */
    FetchedProjectUserIdentity addIdentityForUser(String tenantId, String projectId, String userId, String identity, IdentityType identityType, IdentityOptions options);

    /**
     * Start the process of verifying and adding identity for user by sending a verification code to the identity source
     *
     * @param tenantId     the tenant doing the operation
     * @param projectId    id of the project
     * @param userId       id of the user
     * @param identity     identity attempting to be added
     * @param identityType identity type
     * @return token that needs to be send during verification along with the verification code.
     */
    String startAddIdentityForUserProcess(String tenantId, String projectId, String userId, String identity, IdentityType identityType);

    /**
     * Complete the verification of adding identity for the user
     *
     * @param tenantId id of the tenant doing the operation
     * @param token    token passed during start of the verification
     * @param otp      otp that was sent to the identity
     * @return created identity record
     */
    FetchedProjectUserIdentity verifyIdentityForUser(String tenantId, String token, String otp);
}
