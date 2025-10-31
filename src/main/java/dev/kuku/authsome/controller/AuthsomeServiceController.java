package dev.kuku.authsome.controller;

import dev.kuku.authsome.model.ResponseModel;
import dev.kuku.authsome.model.SignupTenantRequest;
import dev.kuku.authsome.model.TenantSignInRequest;
import dev.kuku.authsome.orchestrator.TenantCoordinator;
import dev.kuku.authsome.services.tenant.api.model.TokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    /**
     * Orchestrator for tenant-related business logic.
     */
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
    @PostMapping("/signup")
    public ResponseModel<String> signup(SignupTenantRequest body) {
        log.trace("signup : {}", body);
        String token = tenantCoordinator.startTenantSignupProcess(body.identityType, body.identity, body.username, body.password);
        log.debug("generated signup token : ...{}...", token.substring(3, 6));
        return ResponseModel.of(token);
    }

    /**
     * Verifies the tenant signup process using the provided OTP and signup token.
     * <p>
     * Completes the signup process if the OTP is valid and the token matches a pending signup.
     *
     * @param otp   the one-time password sent to the user
     * @param token the signup token received from the signup step (in the Signup-Token header)
     * @return a response indicating the result of the verification
     */
    @PutMapping("/signup/{otp}")
    public ResponseModel<Void> verifySignup(@PathVariable String otp, @RequestHeader("Signup-Token") String token) {
        log.trace("verifySignup : {}, {}", otp, token);
        tenantCoordinator.completeTenantSignupProcess(token, otp);
        return ResponseModel.of(null);
    }

    @PostMapping("/sign-in/password")
    public ResponseModel<TokenData> signIn(TenantSignInRequest body) {
        log.trace("signIn : {}", body);
        TokenData tokenData = tenantCoordinator.signInTenantWithPassword(body.identityType, body.identity, body.password);
        return ResponseModel.of(tokenData);
    }

    @PutMapping("/refresh-token")
    public ResponseModel<TokenData> refreshToken(String refreshToken) {
        log.trace("refreshToken : {}...", refreshToken.substring(0, 5));
        TokenData tokenData = tenantCoordinator.refreshTenantToken(refreshToken);
        return ResponseModel.of(tokenData);
    }

    @DeleteMapping("/revoke-refresh-token")
    public ResponseModel<Void> revokeRefreshToken(String refreshToken) {
        log.trace("revokeRefreshToken : {}...", refreshToken.substring(0, 5));
        tenantCoordinator.revokeTenantRefreshToken(refreshToken);
        return ResponseModel.of(null);
    }
}
