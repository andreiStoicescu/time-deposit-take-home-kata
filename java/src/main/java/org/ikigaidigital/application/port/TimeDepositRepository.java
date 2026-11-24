package org.ikigaidigital.application.port;

import org.ikigaidigital.TimeDeposit;

import java.util.List;

public interface TimeDepositRepository {

    List<TimeDeposit> findAll();

    List<TimeDeposit> findAllForUpdate();

    void updateBalances(List<TimeDeposit> deposits);
}
