package com.sfexplorer.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;


public final class PasswordHasher {
    private static final SecureRandom RNG = new SecureRandom();
    private static final String PEPPER = "SF_EXPLORER_PEPPER_v1_CHANGE_ME"; // project "pepper"
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordHasher() {}

    public static record HashResult(String saltB64, String hashB64, int iterations) {}

    public static HashResult hash(char[] password) {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);

        byte[] hash = pbkdf2(passwordWithPepper(password), salt, ITERATIONS, KEY_LENGTH_BITS);
        return new HashResult(b64(salt), b64(hash), ITERATIONS);
    }

    public static boolean verify(char[] password, String saltB64, String expectedHashB64, int iterations) {
        byte[] salt = Base64.getDecoder().decode(saltB64);
        byte[] expected = Base64.getDecoder().decode(expectedHashB64);
        byte[] actual = pbkdf2(passwordWithPepper(password), salt, iterations, expected.length * 8);

        return MessageDigest.isEqual(expected, actual);
    }

    private static char[] passwordWithPepper(char[] password) {
        String combined = new String(password) + ":" + PEPPER;
        return combined.toCharArray();
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
