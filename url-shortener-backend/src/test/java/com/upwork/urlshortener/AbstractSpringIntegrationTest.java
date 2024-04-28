package com.upwork.urlshortener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractSpringIntegrationTest {

    @Container
    @ServiceConnection
    private static final MongoDBContainer container = new MongoDBContainer("mongo:7.0.6")
            .withExposedPorts(27017);

    static {
        // Testcontainers will expose port 27017 as a random port (to avoid port conflicts on test machines)
        // This is necessary because the random exposed port for MongoDB can only be known after the container has been started
        container.start();
        System.setProperty("spring.data.mongodb.uri",
                "mongodb://" + container.getHost() + ":" + container.getFirstMappedPort());
    }
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Test
    void connectionEstablished() {
        assertThat(container.isCreated()).isTrue();
        assertThat(container.isRunning()).isTrue();
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