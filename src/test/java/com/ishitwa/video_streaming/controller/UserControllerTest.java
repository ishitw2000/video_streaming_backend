package com.ishitwa.video_streaming.controller;

import com.ishitwa.video_streaming.dao.UserLogin;
import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.service.UserServiceImpl;
import com.ishitwa.video_streaming.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        User user = new User();
        when(userService.registerUser(user)).thenReturn(user);
        User result = userController.registerUser(user);
        assertEquals(user, result);
        verify(userService).registerUser(user);
    }

    @Test
    void testLoginUser_Success() {
        UserLogin login = new UserLogin();
        login.setUsername("testuser");
        login.setPassword("password");
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");
        ResponseEntity<?> response = userController.loginUser(login);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(((Map<?,?>)response.getBody()).containsKey("token"));
    }

    @Test
    void testLoginUser_Failure() {
        UserLogin login = new UserLogin();
        login.setUsername("testuser");
        login.setPassword("wrongpassword");
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        ResponseEntity<?> response = userController.loginUser(login);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Login Failed", response.getBody());
    }

    @Test
    void testLoginUser_UserNotFound() {
        UserLogin login = new UserLogin();
        login.setUsername("nouser");
        login.setPassword("password");
        when(userService.getUserByUsername("nouser")).thenReturn(null);
        ResponseEntity<?> response = userController.loginUser(login);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Login Failed", response.getBody());
    }
}

