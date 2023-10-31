package com.gameCatalog.authservice.IntegrationTests;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerIT extends MySQLContainerConfig {

    public static final MySQLContainer<?> mySQLContainer = MySQLContainerConfig.mySQLContainer;

    @Autowired
    private MockMvc mockMvc;

    private static String refreshToken; // Store the refresh token for reuse

    @Test
    @Order(1)
    public void testRegisterEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .content("{\"firstname\": \"John\", \"lastname\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"testpassword\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void testAuthenticateEndpoint() throws Exception {
        // Authenticate the user and get the access token
        MvcResult authenticateResult = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .content("{\"email\": \"john.doe@example.com\", \"password\": \"testpassword\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String rtoke = JsonPath.read(authenticateResult.getResponse().getContentAsString(), "$.refreshToken");
        // Extract the refresh token from the authentication response
        refreshToken = JsonPath.read(authenticateResult.getResponse().getContentAsString(), "$.refreshToken");
    }

    @Test
    @Order(3)
    public void testRefreshTokenEndpoint() throws Exception {
        // Use the obtained refresh token to refresh the token
        mockMvc.perform(post("/api/v1/auth/refreshToken")
                        .content("{\"token\": \"" + refreshToken + "\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }
}