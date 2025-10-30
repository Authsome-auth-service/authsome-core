package dev.kuku.authsome.orchestrator;

import dev.kuku.authsome.services.notifier.api.NotifierService;
import dev.kuku.authsome.services.otp.api.OtpService;
import dev.kuku.authsome.services.otp.api.model.FetchedOtp;
import dev.kuku.authsome.services.otp.api.model.OtpType;
import dev.kuku.authsome.services.tenant.api.TenantService;
import dev.kuku.authsome.services.tenant.api.model.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.model.IdentityType;
import dev.kuku.authsome.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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
        switch (identityType) {
            case EMAIL:
                break;
            default:
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
}