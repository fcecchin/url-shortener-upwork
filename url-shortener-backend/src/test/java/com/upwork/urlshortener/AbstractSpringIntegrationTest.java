package com.upwork.urlshortener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public abstract class AbstractSpringIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:latest")
            .withReuse(true);
    protected MockMvc mockMvc;
    @Autowired
    protected JdbcTemplate jdbcTemplate;


    @Autowired
    private WebApplicationContext context;

    @Test
    void connectionEstablished() {
        assertThat(mySqlContainer.isCreated()).isTrue();
        assertThat(mySqlContainer.isRunning()).isTrue();
    }

    @BeforeEach
    public void resetState() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        cleanAllDatabases();
    }


    abstract protected void cleanAllDatabases();
}