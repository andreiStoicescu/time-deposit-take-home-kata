package org.ikigaidigital.adapter.persistence;

import org.ikigaidigital.TestcontainersConfiguration;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcTimeDepositRepository.class, JdbcWithdrawalRepository.class})
class JdbcRepositoriesIntegrationTest {

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private JdbcTimeDepositRepository timeDeposits;

    @Autowired
    private JdbcWithdrawalRepository withdrawals;

    @BeforeEach
    void insertRowsOutOfOrder() {
        jdbc.sql("""
                INSERT INTO time_deposits (id, plan_type, days, balance)
                VALUES (2, 'gold', 400, 1234567.89), (1, 'basic', 31, 1000.00)""").update();
        jdbc.sql("""
                INSERT INTO withdrawals (id, time_deposit_id, amount, date)
                VALUES (1, 1, 50.25, DATE '2026-03-01'),
                       (2, 2, 100.00, DATE '2026-03-01'),
                       (3, 1, 20.00, DATE '2026-01-15')""").update();
    }

    @Test
    void findsAllTimeDepositsOrderedByIdWithBalanceAsDouble() {
        assertThat(timeDeposits.findAll())
                .extracting(TimeDeposit::getId, TimeDeposit::getPlanType, TimeDeposit::getBalance, TimeDeposit::getDays)
                .containsExactly(
                        tuple(1, "basic", 1000.00, 31),
                        tuple(2, "gold", 1234567.89, 400));
    }

    @Test
    void findsAllForUpdateWithTheSameRowsAsFindAll() {
        assertThat(timeDeposits.findAllForUpdate())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(timeDeposits.findAll());
    }

    @Test
    void updatesBalancesRoundedHalfUpToCents() {
        timeDeposits.updateBalances(List.of(
                new TimeDeposit(1, "basic", 1000.9000000000001, 31),
                new TimeDeposit(2, "gold", 0.125, 400)));

        assertThat(jdbc.sql("SELECT balance FROM time_deposits ORDER BY id").query(BigDecimal.class).list())
                .containsExactly(new BigDecimal("1000.90"), new BigDecimal("0.13"));
    }

    @Test
    void findsAllWithdrawalsOrderedByDateThenId() {
        assertThat(withdrawals.findAll()).containsExactly(
                new Withdrawal(3, 1, new BigDecimal("20.00"), LocalDate.of(2026, 1, 15)),
                new Withdrawal(1, 1, new BigDecimal("50.25"), LocalDate.of(2026, 3, 1)),
                new Withdrawal(2, 2, new BigDecimal("100.00"), LocalDate.of(2026, 3, 1)));
    }
}
