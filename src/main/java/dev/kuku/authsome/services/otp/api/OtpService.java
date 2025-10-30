package dev.kuku.authsome.services.otp.api;

import dev.kuku.authsome.services.otp.api.model.FetchedOtp;
import dev.kuku.authsome.services.otp.api.model.OtpType;

import java.util.Map;

/**
 * Service interface for OTP (One-Time Password) generation and management.
 * <p>
 * Provides functionality to generate, store, and validate OTPs for various
 * authentication and verification workflows.
 */
public interface OtpService {

    /**
     * Generates a new OTP and saves it with associated metadata.
     *
     * @param otpType            the type of OTP to generate (NUMERIC, ALPHANUMERIC, etc.)
     * @param otpLength          the length of the OTP to generate
     * @param minNumber          minimum number of numeric characters required (-1 to ignore)
     * @param minAlphabet        minimum number of alphabetic characters required (-1 to ignore)
     * @param maxNumber          maximum number of numeric characters allowed (-1 to ignore)
     * @param maxAlphabet        maximum number of alphabetic characters allowed (-1 to ignore)
     * @param expiresAfterSecond expiry time in seconds from generation
     * @param context            context identifier for the OTP (e.g., "TENANT_SIGNUP", "PASSWORD_RESET")
     * @param metadata           additional metadata to store with the OTP
     * @return the generated OTP with its ID and expiry information
     */
    FetchedOtp generateAndSaveOtp(OtpType otpType, int otpLength, int minNumber, int minAlphabet,
                                  int maxNumber, int maxAlphabet, int expiresAfterSecond,
                                  String context, Map<String, String> metadata);
    //TODO tips for otp service id + context can be unique, partition can be made on context
}