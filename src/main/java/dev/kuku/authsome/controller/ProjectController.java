package dev.kuku.authsome.controller;

import dev.kuku.authsome.coordinator.ProjectCoordinator;
import dev.kuku.authsome.model.*;
import dev.kuku.authsome.services.project.api.model.FetchedProjectUser;
import dev.kuku.authsome.services.project.api.model.FetchedProjectUserIdentity;
import dev.kuku.authsome.services.project.api.model.FetchedTenantProject;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/project")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {
    final ProjectCoordinator projectCoordinator;

    @PostMapping("/")
    public ResponseModel<FetchedTenantProject> createProject(@RequestBody CreateProjectRequest body) {
        log.debug("Received request to create project {}", body);
        FetchedTenant currentUser = getCurrentUser();
        FetchedTenantProject createdProject = projectCoordinator.createProject(currentUser.id(), body);
        return ResponseModel.of(createdProject);
    }

    @PostMapping("/user")
    public ResponseModel<FetchedProjectUser> createProjectUser(@RequestBody CreateProjectUserRequest body) {
        log.debug("Received request to create project user {}", body);
        var currentUser = getCurrentUser();
        FetchedProjectUser createdUser = projectCoordinator.createProjectUser(currentUser.id(), body.projectId, body.username, body.password);
        log.debug("Created user is {}", createdUser);
        return ResponseModel.of(createdUser);
    }

    @PostMapping("/identity")
    public ResponseModel<FetchedProjectUserIdentity> addIdentityForUser(@RequestBody AddIdentityForProjectUserRequest body) {
        log.debug("Received request to add identity for user {}", body);
        FetchedTenant currentUser = getCurrentUser();
        var addedIdentity = projectCoordinator.addIdentityForUser(currentUser.id(), body.projectId, body.userId, body.identity, body.identityType, body.options);
        log.debug("added identity is {}", addedIdentity);
        return ResponseModel.of(addedIdentity);
    }

    @PostMapping("verify-identity")
    public ResponseModel<String> startAddIdentityProcessForUser(@RequestBody AddIdentityForProjectUserRequest body) {
        log.debug("Received request to start add identity for user {}", body);
        var cu = getCurrentUser();
        String token = projectCoordinator.startAddIdentityForUserProcess(cu.id(), body.projectId, body.userId, body.identity, body.identityType);
        return ResponseModel.of(token);
    }

    @PutMapping("verify-identity")
    public ResponseModel<FetchedProjectUserIdentity> verifyIdentityForUser(@RequestBody VerifyIdentityForUserRequest body, @RequestHeader("Verification-Token") String token) {
        log.debug("Received request to verify identity for user {}", body);
        var cu = getCurrentUser();
        FetchedProjectUserIdentity addedIdentity = projectCoordinator.verifyIdentityForUser(cu.id(), token, body.otp);
        log.debug("added identity is {}", addedIdentity);
        return ResponseModel.of(addedIdentity);
    }

    private FetchedTenant getCurrentUser() {
        FetchedTenant currentUser = (FetchedTenant) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Current user is {}", currentUser);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return currentUser;
    }
}
