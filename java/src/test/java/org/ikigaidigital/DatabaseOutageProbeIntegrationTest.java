package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.hikari.connection-timeout=1000")
@AutoConfigureRestTestClient
@Testcontainers
@DirtiesContext
class DatabaseOutageProbeIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(TestcontainersConfiguration.POSTGRES_IMAGE);

    @Autowired
    private RestTestClient client;

    @Test
    void databaseOutageTakesAppOutOfRotationWithoutFailingLiveness() {
        assertProbe("readiness", HttpStatus.OK, "UP");

        postgres.stop();

        assertProbe("readiness", HttpStatus.SERVICE_UNAVAILABLE, "DOWN");
        assertProbe("liveness", HttpStatus.OK, "UP");
    }

    private void assertProbe(String probe, HttpStatus expectedStatus, String expectedHealth) {
        client.get().uri("/actuator/health/" + probe)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .jsonPath("$.status").isEqualTo(expectedHealth);
    }
}
