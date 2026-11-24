package org.ikigaidigital.application;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.TimeDepositRepository;
import org.ikigaidigital.application.port.WithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListTimeDeposits {

    private final TimeDepositRepository timeDeposits;
    private final WithdrawalRepository withdrawals;

    public ListTimeDeposits(TimeDepositRepository timeDeposits, WithdrawalRepository withdrawals) {
        this.timeDeposits = timeDeposits;
        this.withdrawals = withdrawals;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<TimeDepositView> listAll() {
        Map<Integer, List<Withdrawal>> withdrawalsByDeposit = withdrawals.findAll().stream()
                .collect(Collectors.groupingBy(Withdrawal::timeDepositId));
        return timeDeposits.findAll().stream()
                .map(deposit -> new TimeDepositView(
                        deposit.getId(),
                        deposit.getPlanType(),
                        deposit.getBalance(),
                        deposit.getDays(),
                        withdrawalsByDeposit.getOrDefault(deposit.getId(), List.of())))
                .toList();
    }
}
