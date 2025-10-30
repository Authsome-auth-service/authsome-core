package dev.kuku.authsome.controller;

import dev.kuku.authsome.model.ResponseModel;
import dev.kuku.authsome.model.SignupTenantRequest;
import dev.kuku.authsome.orchestrator.TenantCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Authsome service operations.
 * <p>
 * Handles tenant management and authentication-related endpoints.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/authsome-service")
@Slf4j
public class AuthsomeServiceController {
    final TenantCoordinator tenantCoordinator;

    /**
     * Initiates the tenant signup process.
     * <p>
     * Sends a verification code to the provided identity (email/phone) and generates
     * a signup token that must be included in subsequent verification requests.
     *
     * @param body the signup request containing identity type, identity value, username, and password
     * @return a response containing the signup token to be used for verification
     */
    public ResponseModel<String> signup(SignupTenantRequest body) {
        log.trace("signup : {}", body);
        String token = tenantCoordinator.startTenantSignupProcess(body.identityType, body.identity, body.username, body.password);
        log.debug("generated signup token : ...{}...", token.substring(3, 6));
        return ResponseModel.of(token);
    }
}