package com.ishitwa.video_streaming.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // If JwtUtil requires secret/key setup, set it here
    }

    @Test
    void testGenerateTokenAndValidate() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void testValidateToken_Invalid() {
        String username = "testuser";
        String invalidToken = "invalid.token.value";
        assertFalse(jwtUtil.validateToken(invalidToken, username));
    }
}

