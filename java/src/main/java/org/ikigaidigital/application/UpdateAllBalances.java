package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.TimeDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateAllBalances {

    private final TimeDepositRepository timeDeposits;
    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    public UpdateAllBalances(TimeDepositRepository timeDeposits) {
        this.timeDeposits = timeDeposits;
    }

    // Each call adds one month of interest. The rows stay locked until commit, so concurrent calls
    // apply one after the other instead of overwriting each other's result.
    @Transactional
    public void updateAll() {
        List<TimeDeposit> deposits = timeDeposits.findAllForUpdate();
        calculator.updateBalance(deposits);
        timeDeposits.updateBalances(deposits);
    }
}
