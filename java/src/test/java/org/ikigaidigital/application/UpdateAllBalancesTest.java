package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateAllBalancesTest {

    @Test
    void storesOneMonthOfInterestForEveryDeposit() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(
                new TimeDeposit(1, "basic", 1200.00, 31),
                new TimeDeposit(2, "premium", 1200.00, 46),
                new TimeDeposit(3, "gold", 1200.00, 100));

        new UpdateAllBalances(repository).updateAll();

        assertThat(repository.balanceOf(1)).isEqualTo(1201.00);
        assertThat(repository.balanceOf(2)).isEqualTo(1205.00);
        assertThat(repository.balanceOf(3)).isEqualTo(1200.00);
    }

    @Test
    void eachCallAddsAnotherMonthOfInterest() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(new TimeDeposit(1, "basic", 1200.00, 31));
        UpdateAllBalances updateAllBalances = new UpdateAllBalances(repository);

        updateAllBalances.updateAll();
        updateAllBalances.updateAll();

        assertThat(repository.balanceOf(1)).isEqualTo(1202.00);
    }
}
