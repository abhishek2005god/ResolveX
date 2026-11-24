package com.resolvex;

import com.resolvex.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationTest {

    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000);
    }

    @Test
    @DisplayName("BCrypt password encoder should hash and verify passwords securely")
    void bcryptHashingAndVerification() {
        String rawPassword = "Password@123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$10$") || encoded.startsWith("$2b$10$"));
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("WrongPassword", encoded));
    }

    @Test
    @DisplayName("JWT utils should generate valid token and parse subject and claims")
    void jwtTokenGenerationAndValidation() {
        String token = jwtUtils.generateTokenFromUsername("test.user", 99L, "ADMIN");

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("test.user", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    @DisplayName("JWT utils should reject invalid or tampered token")
    void jwtTokenRejectionOnTampering() {
        String token = jwtUtils.generateTokenFromUsername("test.user", 99L, "ADMIN");
        String tampered = token + "xyz";

        assertFalse(jwtUtils.validateJwtToken(tampered));
    }
}
