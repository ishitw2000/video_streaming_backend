package com.ishitwa.video_streaming.service;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_NewUser() {
        User user = new User();
        user.setUsername("newuser");
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.registerUser(user);
        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void testGetUserByUsername_Found() {
        User user = new User();
        user.setUsername("existinguser");
        when(userRepository.findByUsername("existinguser")).thenReturn(user);
        User result = userService.getUserByUsername("existinguser");
        assertEquals(user, result);
        verify(userRepository).findByUsername("existinguser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(null);
        User result = userService.getUserByUsername("nouser");
        assertNull(result);
        verify(userRepository).findByUsername("nouser");
    }
}

