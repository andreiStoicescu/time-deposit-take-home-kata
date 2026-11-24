package org.ikigaidigital.adapter.rest;

import org.ikigaidigital.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class TimeDepositApiIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

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
    void updatesEveryBalanceWithOneMonthOfInterest() {
        jdbc.sql("""
                INSERT INTO time_deposits (id, plan_type, days, balance)
                VALUES (1, 'basic', 31, 1234.56), (2, 'student', 366, 1200.00), (3, 'premium', 46, 1200.00)""").update();

        client.post().uri("/time-deposits/balance-updates").exchange().expectStatus().isNoContent();

        client.get().uri("/time-deposits")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].id").isEqualTo(List.of(1, 2, 3))
                .jsonPath("$[*].balance").isEqualTo(List.of(1235.59, 1200.00, 1205.00));
    }

    @Test
    void exposesExactlyTheTwoBusinessEndpoints() {
        assertThat(handlerMapping.getHandlerMethods().entrySet())
                .filteredOn(entry -> entry.getValue().getBeanType().getPackageName().startsWith("org.ikigaidigital"))
                .extracting(entry -> entry.getKey().getMethodsCondition() + " " + entry.getKey().getPathPatternsCondition())
                .containsExactlyInAnyOrder("[GET] [/time-deposits]", "[POST] [/time-deposits/balance-updates]");
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
