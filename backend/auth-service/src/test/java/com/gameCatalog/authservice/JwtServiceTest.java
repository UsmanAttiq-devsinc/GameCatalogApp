package com.gameCatalog.authservice;

import com.gameCatalog.authservice.JWT.JwtService;
import com.gameCatalog.authservice.Model.Role;
import com.gameCatalog.authservice.Model.User;
import com.gameCatalog.authservice.Repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566b5970"; // A valid secret key

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService();
        jwtService.setSecretKey(secretKey);
        jwtService.setJwtExpiration(3600000); // 1 hour

        // Mock user retrieval
        User testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Test - Extract Username")
    public void testExtractUsername() {
        // Arrange
        User user = userRepository.findById(1).get();
        String token = jwtService.generateToken(new HashMap<>(), user);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals(user.getEmail(), username, "Extracted username should match UserDetails email");
    }

    @Test
    @DisplayName("Test - isTokenValid - Valid Token")
    public void testIsTokenValid_ValidToken() {
        // Arrange
        User user = userRepository.findById(1).get();
        String token = jwtService.generateToken(new HashMap<>(), user);

        // Act
        boolean isValid = jwtService.isTokenValid(token, user);

        // Assert
        assertTrue(isValid, "Token should be valid");
    }

    @Test
    @DisplayName("Test - isTokenValid - Expired Token")
    public void testIsTokenValid_ExpiredToken() {
        // Arrange
        User user = userRepository.findById(1).get();
        jwtService.setJwtExpiration(1); // 1 millisecond expiration for testing
        String token = jwtService.generateToken(new HashMap<>(), user);

        // Act and Assert
        Exception exception = assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
        assertNotNull(exception, "Expired token should throw an exception");
    }

    @Test
    @DisplayName("Test - ValidateToken - Valid Token")
    public void testValidateToken_ValidToken() {
        // Arrange
        User user = userRepository.findById(1).get();
        String token = jwtService.generateToken(new HashMap<>(), user);

        // Act and Assert
        assertDoesNotThrow(() -> jwtService.validateToken(token), "Valid token should not throw an exception");
    }

    @Test
    @DisplayName("Test - ValidateToken - Invalid Token")
    public void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";

        // Act and Assert
        assertThrows(Exception.class, () -> jwtService.validateToken(invalidToken), "Invalid token should throw an exception");
    }

    @Test
    @DisplayName("Test - Generate Token")
    public void testGenerateToken() {
        // Arrange
        User user = userRepository.findById(1).get();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("claim1", "value1");

        // Act
        String token = jwtService.generateToken(extraClaims, user);

        // Assert
        assertNotNull(token, "Generated token should not be null");
    }

    @Test
    @DisplayName("Test - Extract Claim")
    public void testExtractClaim() {
        // Arrange
        User user = userRepository.findById(1).get();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("claim1", "value1");
        String token = jwtService.generateToken(extraClaims, user);

        // Act
        String claim1 = jwtService.extractClaim(token, claims -> claims.get("claim1", String.class));

        // Assert
        assertEquals("value1", claim1, "Extracted claim should match the expected value");
    }
}