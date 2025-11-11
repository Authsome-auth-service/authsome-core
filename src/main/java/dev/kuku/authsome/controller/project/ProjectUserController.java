package dev.kuku.authsome.controller.project;

import dev.kuku.authsome.coordinator.ProjectCoordinator;
import dev.kuku.authsome.model.*;
import dev.kuku.authsome.services.project.api.model.FetchedProjectUser;
import dev.kuku.authsome.services.project.api.model.FetchedProjectUserIdentity;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("LoggingSimilarMessage")
@RestController
@RequestMapping("/api/v1/project/user")
@Slf4j
public class ProjectUserController extends ProjectController {
    public ProjectUserController(ProjectCoordinator projectCoordinator) {
        super(projectCoordinator);
    }

    @PostMapping("/")
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

    @PostMapping("/verify-identity")
    public ResponseModel<String> startAddIdentityProcessForUser(@RequestBody AddIdentityForProjectUserRequest body) {
        log.debug("Received request to start add identity for user {}", body);
        var cu = getCurrentUser();
        String token = projectCoordinator.startAddIdentityForUserProcess(cu.id(), body.projectId, body.userId, body.identity, body.identityType);
        return ResponseModel.of(token);
    }

    @PutMapping("/verify-identity")
    public ResponseModel<FetchedProjectUserIdentity> verifyIdentityForUser(@RequestBody VerifyIdentityForUserRequest body, @RequestHeader("Verification-Token") String token) {
        log.debug("Received request to verify identity for user {}", body);
        var cu = getCurrentUser();
        FetchedProjectUserIdentity addedIdentity = projectCoordinator.verifyIdentityForUser(cu.id(), token, body.otp);
        log.debug("added identity is {}", addedIdentity);
        return ResponseModel.of(addedIdentity);
    }

    @PostMapping("/sign-in")
    public ResponseModel<String> signInUserWithCredential(@RequestBody SignInUserWithCredentialRequest body) {
        log.debug("Received request to sign in user with credential {}", body);
        return ResponseModel.of("FAKE TOKEN");
    }

    @PostMapping("/sign-in-otp")
    public ResponseModel<String> signInWithOtp(@RequestBody SignInWithOtpRequest body) {
        log.debug("Received request to sign in with otp {}", body);
    }

    @PostMapping("/sign-in-otp-verify")
    public ResponseModel<String> signInWithOtpVerify(@RequestHeader("Verification-Token") String token, @RequestBody SignInWithOtpVerifyRequest body) {
        log.debug("Received request to sign in with otp verify {}", body);
    }
}
