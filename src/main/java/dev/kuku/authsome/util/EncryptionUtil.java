package dev.kuku.authsome.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Simple utility for encrypting and decrypting sensitive data.
 * <p>
 * Uses AES encryption with a configured secret key.
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

    public EncryptionUtil(@Value("${authsome.encryption.key:MySecretKey123456MySecretKey12}") String key) {
        // Ensure key is exactly 16, 24, or 32 bytes for AES
        byte[] keyBytes = key.getBytes();
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 16, 24, or 32 characters");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        log.info("EncryptionUtil initialized");
    }

    /**
     * Encrypts the given plaintext.
     *
     * @param plaintext the text to encrypt
     * @return the encrypted text as Base64 string
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts the given encrypted text.
     *
     * @param encryptedText the encrypted text as Base64 string
     * @return the decrypted plaintext
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}