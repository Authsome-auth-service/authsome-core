package dev.kuku.authsome.coordinator;

import dev.kuku.authsome.model.CreateProjectRequest;
import dev.kuku.authsome.services.project.api.ProjectService;
import dev.kuku.authsome.services.project.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCoordinator {
    final ProjectService projectService;

    public FetchedTenantProject createProject(String tenantId, CreateProjectRequest createProjectData) {
        log.debug("Create project request: {} {}", tenantId, createProjectData);
        //1. Create the project.
        var createdProject = projectService.createProjectForTenant(tenantId, createProjectData.projectName, createProjectData.rules);
        log.debug("created project = {}", createdProject);
        return createdProject;
    }

    public FetchedProjectUser createProjectUser(String tenantId, String projectId, String username, String password) {
        log.debug("Create project user request: {} {}, {}, {}**..", tenantId, projectId, username, password.substring(0, 3));
        //1. Create the project user
        FetchedProjectUser createdUser = projectService.createProjectUser(tenantId, projectId, username, password);
        log.debug("created user is {}", createdUser);
        return createdUser;
    }

    public FetchedProjectUserIdentity addIdentityForUser(String tenantId, String projectId, String userId, String identity, IdentityType identityType, IdentityOptions options) {
        /*
        Add the identity if configuration set by the user allows it. both at project level and overridden by role configuration
         */
        log.debug("add identity for user {}, {}, {}, {}, {}", tenantId, projectId, userId, identity, identityType);
        var addedIdentity = projectService.addIdentityForUser(tenantId, projectId, userId, identity, identityType, options);
        log.debug("added identity is {}", addedIdentity);
        return addedIdentity;
    }

    public String startAddIdentityForUserProcess(String tenantId, String projectId, String userId, String identity, IdentityType identityType) {
        log.debug("startAddIdentityForUserProcess({}, {}, {}, {}, {})", tenantId, projectId, userId, identity, identityType);
        String token = projectService.startAddIdentityForUserProcess(tenantId, projectId, userId, identity, identityType);
        log.debug("token is {}", token);
        return token;
    }

    public FetchedProjectUserIdentity verifyIdentityForUser(String tenantId, String token, String otp) {
        log.debug("verifyIdentityForUser({}, {}, {})", tenantId, token, otp);
        FetchedProjectUserIdentity addedIdentity = projectService.verifyIdentityForUser(tenantId, token, otp);
        log.debug("added identity is {}", addedIdentity);
        return addedIdentity;

    }
}
