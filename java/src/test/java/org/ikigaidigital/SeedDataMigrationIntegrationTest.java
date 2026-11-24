package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,filesystem:db/seed")
@Import(TestcontainersConfiguration.class)
class SeedDataMigrationIntegrationTest {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void localSeedDataAppliesOnTopOfTheSchema() {
        assertThat(count("time_deposits")).isEqualTo(6);
        assertThat(count("withdrawals")).isEqualTo(3);
    }

    private long count(String table) {
        return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single();
    }
}
