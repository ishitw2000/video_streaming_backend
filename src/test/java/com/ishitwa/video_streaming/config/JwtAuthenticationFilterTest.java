package com.ishitwa.video_streaming.config;

import com.ishitwa.video_streaming.service.CustomUserDetails;
import com.ishitwa.video_streaming.service.CustomUserDetailsService;
import com.ishitwa.video_streaming.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void testDoFilterInternal_AuthorizationHeaderWithoutBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Token abc");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void testDoFilterInternal_BearerTokenValidAndNoCurrentAuthentication() throws ServletException, IOException {
        CustomUserDetails details = new CustomUserDetails(
                "alice",
                "encoded",
                true,
                List.of(new SimpleGrantedAuthority("USER")),
                "alice@example.com");
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.getUsernameFromToken("token123")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);
        when(jwtUtil.validateJwtToken("token123")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_BearerTokenInvalid() throws ServletException, IOException {
        CustomUserDetails details = new CustomUserDetails(
                "alice",
                "encoded",
                true,
                List.of(new SimpleGrantedAuthority("USER")),
                "alice@example.com");
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.getUsernameFromToken("token123")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);
        when(jwtUtil.validateJwtToken("token123")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AuthenticationAlreadyPresent() throws ServletException, IOException {
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.getUsernameFromToken("token123")).thenReturn("alice");

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateJwtToken(anyString());
        assertEquals("existing", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }
}

