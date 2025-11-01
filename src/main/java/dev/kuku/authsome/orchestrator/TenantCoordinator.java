package dev.kuku.authsome.orchestrator;

import dev.kuku.authsome.services.jwt.api.JwtService;
import dev.kuku.authsome.services.notifier.api.NotifierService;
import dev.kuku.authsome.services.otp.api.OtpService;
import dev.kuku.authsome.services.otp.api.model.FetchedOtp;
import dev.kuku.authsome.services.otp.api.model.OtpType;
import dev.kuku.authsome.services.tenant.api.TenantService;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.dto.IdentityType;
import dev.kuku.authsome.services.tenant.api.dto.TenantAndRefreshToken;
import dev.kuku.authsome.services.tenant.api.dto.TokenData;
import dev.kuku.authsome.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrator service for tenant-related operations.
 * <p>
 * Coordinates the business logic for tenant management, including signup,
 * verification, and account provisioning workflows. Acts as a facade layer
 * orchestrating multiple services to complete complex tenant operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantCoordinator {
    final TenantService tenantService;
    final OtpService otpService;
    final NotifierService notifierService;
    final JwtService jwtService;
    final EncryptionUtil encryptionUtil;

    /**
     * Initiates the tenant signup process by validating the request, generating an OTP,
     * and sending a verification code to the provided identity.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Validates that the identity type is supported</li>
     *   <li>Checks if the identity is already registered</li>
     *   <li>Verifies that the username is available</li>
     *   <li>Encrypts the password for secure temporary storage</li>
     *   <li>Generates and stores a 4-digit numeric OTP valid for 5 minutes</li>
     *   <li>Sends the OTP to the user via their chosen identity method</li>
     * </ol>
     *
     * @param identityType the type of identity being used for verification (e.g., EMAIL)
     * @param identity     the identity value (email address or phone number)
     * @param username     the desired username for the new tenant account
     * @param password     the password for the new tenant account
     * @return a signup token (OTP ID) that must be used to complete the verification process
     * @throws ResponseStatusException with BAD_REQUEST if the identity type is unsupported
     * @throws ResponseStatusException with CONFLICT if the identity or username is already in use
     */
    public String startTenantSignupProcess(IdentityType identityType, String identity, String username, String password) {
        //TODO rate limiting
        log.info("Start tenant signup process for identityType: {}, identity: {}, username: {}", identityType, identity, username);

        // Validate identity type is supported for signup
        if (Objects.requireNonNull(identityType) != IdentityType.EMAIL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported identity type for signup");
        }

        // Check if the identity is already registered
        FetchedTenant fetchedTenant = tenantService.getTenantByIdentity(identityType, identity);
        if (fetchedTenant != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identity already in use exists");
        }

        // Verify that the username is available
        fetchedTenant = tenantService.getTenantByUsername(username);
        if (fetchedTenant != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use exists");
        }

        // Encrypt the password before storing in metadata
        String encryptedPassword = encryptionUtil.encrypt(password);

        // Generate and save a 4-digit numeric OTP valid for 5 minutes
        // Store signup data in OTP metadata for verification step
        FetchedOtp fetchedOtp = otpService.generateAndSaveOtp(
                OtpType.NUMERIC,              // Type of OTP
                4,                             // Length of OTP
                -1,                            // Min number of digits (not applicable for NUMERIC type)
                -1,                            // Min number of alphabets (not applicable for NUMERIC type)
                -1,                            // Max number of digits (not applicable for NUMERIC type)
                -1,                            // Max number of alphabets (not applicable for NUMERIC type)
                300,                           // Expiry in seconds (5 minutes)
                "AUTHSOME_TENANT_SIGNUP",     // Context identifier
                Map.of(
                        "identity", identity,
                        "identityType", String.valueOf(identityType),
                        "username", username,
                        "password", encryptedPassword  // Store encrypted password
                )                              // Metadata containing signup information
        );

        // Send the OTP to the user's identity (email/phone)
        notifierService.sendNotification(
                dev.kuku.authsome.services.notifier.api.model.IdentityType.valueOf(identityType.name()),
                identity,
                "OTP to create authsome account",
                "Your OTP to create your Authsome account is: " + fetchedOtp.code
        );

        // Return the OTP ID as the signup token for verification
        return fetchedOtp.id;
    }

    /**
     * Completes the tenant signup process by verifying the OTP and creating the tenant account.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Validates that the provided OTP is not null or empty</li>
     *   <li>Retrieves the stored OTP record using the signup token</li>
     *   <li>Verifies the OTP code matches the user-provided value</li>
     *   <li>Validates the OTP context matches the signup process</li>
     *   <li>Extracts and validates signup metadata (identity, username, password)</li>
     *   <li>Decrypts the stored password</li>
     *   <li>Creates the tenant account with the provided credentials</li>
     *   <li>Associates the verified identity with the newly created tenant</li>
     * </ol>
     *
     * @param token the signup token (OTP ID) returned from {@link #startTenantSignupProcess}
     * @param otp   the 4-digit verification code sent to the user's identity
     * @throws ResponseStatusException with BAD_REQUEST if the OTP is invalid, the token is invalid,
     *                                 the OTP doesn't match, the context is incorrect, or metadata is missing
     */
    public void completeTenantSignupProcess(String token, String otp) {
        log.trace("completeTenantSignupProcess : {}, {}", token, otp);
        //validate if otp is valid
        if (otp == null || otp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP cannot be null or empty");
        }
        //1. fetch otp from otp service by id (token)
        FetchedOtp fetchedOtp = otpService.getOtpById(token);
        // check if otp exists
        if (fetchedOtp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
        //2. validate otp
        if (!fetchedOtp.code.equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid otp");
        }
        // validate context
        if (!fetchedOtp.context.equals("AUTHSOME_TENANT_SIGNUP")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid context");
        }
        // validate metadata
        if (fetchedOtp.metadata == null ||
            !fetchedOtp.metadata.containsKey("identity") ||
            !fetchedOtp.metadata.containsKey("identityType") ||
            !fetchedOtp.metadata.containsKey("username") ||
            !fetchedOtp.metadata.containsKey("password")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid metadata");
        }
        String identity = fetchedOtp.metadata.get("identity").toString();
        IdentityType identityType = IdentityType.valueOf((String) fetchedOtp.metadata.get("identityType"));
        String username = fetchedOtp.metadata.get("username").toString();
        String encryptedPassword = fetchedOtp.metadata.get("password").toString();
        String password = encryptionUtil.decrypt(encryptedPassword);
        //3. create tenant
        FetchedTenant createdUser = tenantService.createTenant(username, password);
        // Add identity for the tenant
        tenantService.addIdentityForTenant(createdUser.id(), identityType, identity);
    }

    public TokenData signInTenantWithPassword(IdentityType identityType, String identity, String password) {
        log.trace("signInTenant : {}, {}", identityType, identity);
        //Fetch tenant by identity
        FetchedTenant fetchedTenant = tenantService.getTenantByIdentity(identityType, identity);
        //Validate if the tenant exists
        if (fetchedTenant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with identity");
        }
        //Validate credentials
        boolean valid = tenantService.validateTenantCredentials(fetchedTenant.id(), password);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials");
        }
        //Generate refresh token.
        String refreshToken = tenantService.createTenantRefreshToken(fetchedTenant.id(), null);
        //Generate access token
        String accessToken = generateAccessToken(fetchedTenant.id());
        TokenData tokenData = new TokenData(accessToken, refreshToken);
        log.debug("Generated token data = {}... {}...", tokenData.accessToken().substring(5, 10), tokenData.refreshToken().substring(5, 10));
        return tokenData;
    }


    public TokenData refreshTenantToken(String refreshToken) {
        log.trace("refreshTenantToken : {}", refreshToken);
        TenantAndRefreshToken tenantAndRefreshToken = tenantService.refreshToken(refreshToken);
        if (tenantAndRefreshToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh token");
        }
        //Generate new access token
        String accessToken = generateAccessToken(tenantAndRefreshToken.tenant.id());
        TokenData tokenData = new TokenData(accessToken, tenantAndRefreshToken.refreshToken);
        log.debug("Generated new token data = {}... {}...", tokenData.accessToken().substring(5, 10), tokenData.refreshToken().substring(5, 10));
        return tokenData;
    }

    public void revokeTenantRefreshToken(String refreshToken) {
        log.trace("revokeTenantRefreshToken : {}", refreshToken);
        log.info("revokeTenantRefreshToken : {}", refreshToken);
        tenantService.revokeTenantRefreshToken(refreshToken);
    }

    private String generateAccessToken(String tenantId) {
        log.trace("generateAccessToken : {}", tenantId);
        String accessToken = jwtService.generateToken(tenantId, null, "AUTHSOME_TENANT", 3600, TimeUnit.MINUTES);
        return accessToken;
    }

    public String generateAPIKeyForTenant(String tenantId) {
        log.trace("generateAPIKeyForTenant : {}, {}", tenantId);
        String apiKey = tenantService.generateAPIKeyForTenant(tenantId);
        log.debug("generateAPIKeyForTenant : {}, {}", apiKey, tenantId);
        return apiKey;
    }
}