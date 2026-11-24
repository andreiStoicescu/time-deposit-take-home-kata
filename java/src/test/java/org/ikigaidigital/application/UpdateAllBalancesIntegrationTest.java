package org.ikigaidigital.application;

import org.ikigaidigital.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UpdateAllBalancesIntegrationTest {

    @Autowired
    private UpdateAllBalances updateAllBalances;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private TransactionTemplate transaction;

    @BeforeEach
    void insertDeposit() {
        jdbc.sql("DELETE FROM withdrawals").update();
        jdbc.sql("DELETE FROM time_deposits").update();
        jdbc.sql("INSERT INTO time_deposits (id, plan_type, days, balance) VALUES (1, 'basic', 31, 1200.00)").update();
    }

    @Test
    void waitsForAConcurrentWriterAndAppliesInterestOnTopOfItsResult() throws Exception {
        CountDownLatch rowLocked = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CompletableFuture<Void> concurrentWriter = CompletableFuture.runAsync(() -> transaction.executeWithoutResult(status -> {
            jdbc.sql("UPDATE time_deposits SET balance = 1300.00 WHERE id = 1").update();
            rowLocked.countDown();
            await(releaseLock);
        }));
        assertThat(rowLocked.await(10, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Void> update;
        try {
            update = CompletableFuture.runAsync(updateAllBalances::updateAll);
            awaitASessionBlockedOnALock();
        } finally {
            releaseLock.countDown();
        }
        concurrentWriter.get(10, TimeUnit.SECONDS);
        update.get(10, TimeUnit.SECONDS);

        assertThat(jdbc.sql("SELECT balance FROM time_deposits WHERE id = 1").query(BigDecimal.class).single())
                .isEqualByComparingTo("1301.08");
    }

    private void awaitASessionBlockedOnALock() throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            long blocked = jdbc.sql("SELECT count(*) FROM pg_stat_activity WHERE wait_event_type = 'Lock'")
                    .query(Long.class).single();
            if (blocked > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("the update never waited for the locked row");
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
