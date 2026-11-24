package org.ikigaidigital.adapter.rest;

import org.ikigaidigital.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class TimeDepositApiIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void deleteRows() {
        jdbc.sql("DELETE FROM withdrawals").update();
        jdbc.sql("DELETE FROM time_deposits").update();
    }

    @Test
    void listsEveryTimeDepositWithItsWithdrawals() {
        jdbc.sql("""
                INSERT INTO time_deposits (id, plan_type, days, balance)
                VALUES (1, 'basic', 31, 1000.50), (2, 'premium', 46, 5000.75)""").update();
        jdbc.sql("""
                INSERT INTO withdrawals (id, time_deposit_id, amount, date)
                VALUES (1, 1, 100.25, DATE '2026-01-15')""").update();

        client.get().uri("/time-deposits")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        [
                          {"id": 1, "planType": "basic", "balance": 1000.50, "days": 31,
                           "withdrawals": [{"id": 1, "amount": 100.25, "date": "2026-01-15"}]},
                          {"id": 2, "planType": "premium", "balance": 5000.75, "days": 46, "withdrawals": []}
                        ]""", JsonCompareMode.STRICT);
    }

    @Test
    void swaggerUiServesTheApiContract() {
        client.get().uri("/swagger-ui.html").exchange().expectStatus().isOk();
        client.get().uri("/webjars/swagger-ui/swagger-ui-bundle.js").exchange().expectStatus().isOk();
        client.get().uri("/openapi.yaml").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(spec -> assertThat(spec).contains("/time-deposits:"));
    }
}
