package com.ishitwa.video_streaming.service;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void testRegisterUser_NewUser() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("raw-password");
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUser(user);

        assertEquals("encoded-password", result.getPassword());
        assertEquals(user, result);
        verify(passwordEncoder).encode("raw-password");
        verify(userRepository).save(user);
    }

    @Test
    void testRegisterUser_NullPassword() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword(null);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUser(user);

        assertNull(result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
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

    @Test
    void testDeleteUser() {
        userService.deleteUser("user-to-delete");
        verify(userRepository).deleteByUsername("user-to-delete");
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUsername("updated");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void testLoadUserByUsername_Found() {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("encoded");
        when(userRepository.findByUsername("existinguser")).thenReturn(user);

        UserDetails details = userService.loadUserByUsername("existinguser");

        assertEquals("existinguser", details.getUsername());
        assertEquals("encoded", details.getPassword());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing"));

        assertEquals("User not found", ex.getMessage());
    }
}

