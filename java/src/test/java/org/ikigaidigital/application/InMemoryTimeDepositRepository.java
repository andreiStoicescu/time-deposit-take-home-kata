package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.TimeDepositRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InMemoryTimeDepositRepository implements TimeDepositRepository {

    private final Map<Integer, Double> balancesById = new HashMap<>();
    private final List<TimeDeposit> deposits;

    InMemoryTimeDepositRepository(TimeDeposit... deposits) {
        this.deposits = List.of(deposits);
        this.deposits.forEach(deposit -> balancesById.put(deposit.getId(), deposit.getBalance()));
    }

    @Override
    public List<TimeDeposit> findAll() {
        return deposits.stream()
                .map(deposit -> new TimeDeposit(deposit.getId(), deposit.getPlanType(), balancesById.get(deposit.getId()), deposit.getDays()))
                .toList();
    }

    @Override
    public List<TimeDeposit> findAllForUpdate() {
        return findAll();
    }

    @Override
    public void updateBalances(List<TimeDeposit> updated) {
        updated.forEach(deposit -> balancesById.put(deposit.getId(), deposit.getBalance()));
    }

    double balanceOf(int id) {
        return balancesById.get(id);
    }
}
