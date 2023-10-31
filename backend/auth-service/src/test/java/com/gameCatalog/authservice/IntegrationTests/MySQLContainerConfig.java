package com.gameCatalog.authservice.IntegrationTests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;


public class MySQLContainerConfig {

    @Container
    public static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:latest")
            .withDatabaseName("test_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @BeforeAll
    public void startContainer() {
        mySQLContainer.start();
    }
}