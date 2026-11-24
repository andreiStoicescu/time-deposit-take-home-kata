package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListTimeDepositsTest {

    @Test
    void groupsWithdrawalsByDepositAndDefaultsToEmptyList() {
        Withdrawal first = new Withdrawal(10, 1, new BigDecimal("100.00"), LocalDate.of(2026, 1, 15));
        Withdrawal second = new Withdrawal(11, 1, new BigDecimal("50.00"), LocalDate.of(2026, 2, 15));
        ListTimeDeposits listTimeDeposits = new ListTimeDeposits(
                () -> List.of(new TimeDeposit(1, "basic", 1000.00, 31), new TimeDeposit(2, "premium", 5000.00, 46)),
                () -> List.of(first, second));

        assertThat(listTimeDeposits.listAll()).containsExactly(
                new TimeDepositView(1, "basic", 1000.00, 31, List.of(first, second)),
                new TimeDepositView(2, "premium", 5000.00, 46, List.of()));
    }
}
