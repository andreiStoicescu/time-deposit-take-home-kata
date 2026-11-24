package org.ikigaidigital.application.port;

import org.ikigaidigital.Withdrawal;

import java.util.List;

public interface WithdrawalRepository {

    List<Withdrawal> findAll();
}
