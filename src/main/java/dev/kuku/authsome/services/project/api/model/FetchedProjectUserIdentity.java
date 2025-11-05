package dev.kuku.authsome.services.project.api.model;

public record FetchedProjectUserIdentity(String id, String projectId, String userId, String identity,
                                         IdentityType identityType) {
}
