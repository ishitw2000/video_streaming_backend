package com.ishitwa.video_streaming.service;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_Found() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setEnabled(true);
        user.setEmail("alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(user);

        CustomUserDetails details = customUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertTrue(details.isEnabled());
        assertEquals("alice@example.com", details.getEmail());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing"));

        assertTrue(ex.getMessage().contains("User not found with username: missing"));
    }

    @Test
    void testAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetailsService.authorities();

        assertEquals(1, authorities.size());
        assertEquals("USER", authorities.iterator().next().getAuthority());
    }
}

