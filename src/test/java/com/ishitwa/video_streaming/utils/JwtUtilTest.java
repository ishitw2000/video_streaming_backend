package com.ishitwa.video_streaming.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("secretKeysecretKeysecretKeysecretKey");
    }

    @Test
    void testGenerateTokenAndValidate() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtUtil.validateJwtToken(token));
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String extracted = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extracted);
    }

    @Test
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.value";

        assertFalse(jwtUtil.validateJwtToken(invalidToken));
    }
}

