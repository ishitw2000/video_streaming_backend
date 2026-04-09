package com.ishitwa.video_streaming.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void testUserDetailsAccessorsAndFlags() {
        CustomUserDetails details = new CustomUserDetails(
                "bob",
                "encoded-pass",
                true,
                List.of(new SimpleGrantedAuthority("USER")),
                "bob@example.com");

        assertEquals("bob", details.getUsername());
        assertEquals("encoded-pass", details.getPassword());
        assertEquals("bob@example.com", details.getEmail());
        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
    }
}

