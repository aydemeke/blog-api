package com.example.blog.service;

import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.RegisterRequest;
import com.example.blog.dto.response.AuthResponse;
import com.example.blog.model.Role;
import com.example.blog.model.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_happyPath() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secret123")
                .build();
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-abc");

        // Act
        AuthResponse result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("jwt-token-abc", result.getToken());
        verify(userRepository).existsByUsername("alice");
        verify(userRepository).existsByEmail("alice@example.com");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secret123")
                .build();
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertTrue(ex.getMessage().contains("alice"));
        verify(userRepository).existsByUsername("alice");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_happyPath() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("alice")
                .password("secret123")
                .build();
        User storedUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("hashed_secret")
                .role(Role.USER)
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(storedUser));
        when(jwtService.generateToken(storedUser)).thenReturn("jwt-token-xyz");

        // Act
        AuthResponse result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("jwt-token-xyz", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("alice");
        verify(jwtService).generateToken(storedUser);
    }

    @Test
    void login_wrongPassword_throwsException() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("alice")
                .password("wrong-password")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}
