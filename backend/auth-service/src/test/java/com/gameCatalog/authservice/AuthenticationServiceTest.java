package com.gameCatalog.authservice;

import com.gameCatalog.authservice.Auth.AuthenticationRequest;
import com.gameCatalog.authservice.Auth.AuthenticationResponse;
import com.gameCatalog.authservice.Auth.RegisterRequest;
import com.gameCatalog.authservice.Exception.AuthenticationException;
import com.gameCatalog.authservice.JWT.JwtService;
import com.gameCatalog.authservice.Model.RefreshToken;
import com.gameCatalog.authservice.Model.Role;
import com.gameCatalog.authservice.Model.User;
import com.gameCatalog.authservice.Repository.UserRepository;
import com.gameCatalog.authservice.Service.AuthenticationService;
import com.gameCatalog.authservice.Service.RefreshTokenService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test register with valid user")
    public void testRegister() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("John", "Doe", "john.doe@example.com", "password");

        // Mock the dependencies
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        User user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("mockedJwtToken");
        RefreshToken refreshToken = RefreshToken.builder()
                .token("mockedRefreshToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();
        when(refreshTokenService.createRefreshToken(user.getEmail())).thenReturn(refreshToken);

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertEquals("mockedJwtToken", response.getToken());
        assertEquals("mockedRefreshToken", response.getRefreshToken());
    }

    @Test
    @DisplayName("Test authenticate with valid credentials")
    public void testAuthenticate() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("john.doe@example.com", "password");

        // Mock the authenticationManager behavior
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mockito.mock(Authentication.class));

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mockedJwtToken");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mockedRefreshToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();

        when(refreshTokenService.findbyUser(user)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Assert
        assertEquals("mockedJwtToken", response.getToken());
        assertEquals("mockedRefreshToken", response.getRefreshToken());
    }


    @Test
    @DisplayName("Test authenticate with invalid credentials")
    public void testAuthenticate_InvalidCredentials() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("nonexistent@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException(HttpStatus.UNAUTHORIZED,"Invalid username/password"));

        // Act and Assert


        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(authenticationRequest),
                "AuthenticationException should be thrown for invalid credentials");
    }

    @Test
    @DisplayName("Test Register - Valid - Backend Validation")
    public void testValidRegisterRequest() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("validPass")
                .build();

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertEquals(0, violations.size(), "Expect no validation violations for a valid RegisterRequest");
    }

    @Test
    @DisplayName("Test Register - Invalid - Backend Validation")
    public void testInvalidRegisterRequest() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("") // Invalid: empty
                .lastname("Doe")
                .email("invalid-email.com") // Invalid: not well formatted
                .password("short") // Invalid: less than 6 characters
                .build();

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertEquals(3, violations.size(), "Expect 3 validation violations for an invalid RegisterRequest");
    }

    @Test
    @DisplayName("Test Authenticate - Valid - Backend Validation")
    public void testValidAuthenticateRequest() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        // Arrange
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .password("validPass")
                .build();

        // Act
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(authenticationRequest);

        // Assert
        assertEquals(0, violations.size(), "Expect no validation violations for a valid AuthenticationRequest");
    }

    @Test
    @DisplayName("Test Authenticate - Invalid - Backend Validation")
    public void testInvalidAuthenticateRequest() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        // Arrange
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("example.com") //Invalid: not well formatted
                .password("")  //Invalid: empty
                .build();

        // Act
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(authenticationRequest);

        // Assert
        assertEquals(2, violations.size(), "Expect no validation violations for a valid AuthenticationRequest");
    }
}
