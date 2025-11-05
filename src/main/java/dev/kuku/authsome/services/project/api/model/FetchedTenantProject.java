package dev.kuku.authsome.services.project.api.model;

public record FetchedTenantProject(String id, String projectName, long createdAt, String tenantId) {
}
